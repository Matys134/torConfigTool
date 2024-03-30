$(document).ready(function () {
    // Define the base API endpoint
    const baseApiUrl = 'https://' + location.hostname + ':8443/relay-data/relays';

    $('.relay-event').hide();

    // Function to create and update a chart for a given relay
    function createRelayChart(port) {
        // Create a container for the relay chart and hide it initially
        const chartContainer = $('<div class="relay-chart" id="relayChart' + port + '"></div>').hide();
        const chartCanvas = $('<canvas width="400" height="200"></canvas>').appendTo(chartContainer);
        const relayName = 'Relay on Port ' + port;

        // Create a container for the upload and download rates
        $('<div class="relay-rates" id="relayRates' + port + '"></div>').appendTo(chartContainer);
        // Create a container for the events
        $('<div class="relay-events" id="relayEvents' + port + '"></div>').appendTo(chartContainer);
        // Create a container for the flags
        $('<div class="relay-flags" id="relayFlags' + port + '"></div>').appendTo(chartContainer);
        const eventContainer = $('<div class="relay-event" id="eventData' + port + '"></div>');
        eventContainer.appendTo($('#eventData'));


        // Append the chart container to the relayCharts div
        chartContainer.appendTo($('#relayCharts'));

        // Get references to the chart canvas and its context
        const ctx = chartCanvas[0].getContext('2d');

        const relayChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: Array(20).fill(''), // Initialize with 50 empty labels
                datasets: [
                    {
                        label: 'Upload',
                        backgroundColor: '#00ff00',
                        data: Array(20).fill(0), // Initialize with 50 zeros
                    },
                    {
                        label: 'Download',
                        backgroundColor: '#ff0000',
                        data: Array(20).fill(0), // Initialize with 50 zeros
                    },
                ],
            },
            options: {
                animation: false,
                title: {
                    display: true,
                    text: relayName,
                    fontColor: '#00ff00',
                },
                legend: {
                    labels: {
                        fontColor: '#00ff00',
                    }
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: false,
                        },
                        ticks: {
                            display: false, // Hide the x-axis labels
                            fontColor: '#00ff00',
                        },
                        gridLines: {
                            color: '#333333'
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Bytes/s',
                            fontColor: '#00ff00',
                        },
                        ticks: {
                            fontColor: '#00ff00',
                        },
                        gridLines: {
                            color: '#333333'
                        }
                    }]
                }
            }
        });

        // Function to update the traffic data and chart for the relay
        function updateRelayTrafficDataAndChart() {
            const apiUrl = baseApiUrl + '/' + port;
            $.get(apiUrl, function (data) {
                if (data && data.length > 0) {
                    // Filter out null values from the data
                    data = data.filter(function (relayData) {
                        return relayData !== null;
                    });

                    let uploadData = data.map(function (relayData) {
                        return relayData.upload;
                    });
                    let downloadData = data.map(function (relayData) {
                        return relayData.download;
                    });

                    const flagsData = data.map(function (relayData) {
                        return relayData.flags;
                    });

                    // Update the flagsData div with the fetched flags data
                    const flagsContainer = $('#relayFlags' + port);
                    flagsContainer.html('Flags: ' + flagsData[flagsData.length - 1]);

                    // Determine the maximum value among the upload and download data
                    const maxDataValue = Math.max(Math.max(...uploadData), Math.max(...downloadData));

                    // Determine the scale and unit based on the maximum value
                    let scale, unit;
                    if (maxDataValue >= 1e6) { // More than a million bytes (1 MB)
                        scale = 1e6;
                        unit = 'MB/s';
                    } else if (maxDataValue >= 1e3) { // More than a thousand bytes (1 KB)
                        scale = 1e3;
                        unit = 'KB/s';
                    } else { // Less than a thousand bytes
                        scale = 1;
                        unit = 'Bytes/s';
                    }

                    // Scale the upload and download data
                    uploadData = uploadData.map(function (value) {
                        return value / scale;
                    });
                    downloadData = downloadData.map(function (value) {
                        return value / scale;
                    });

                    // Update the y-axis label
                    if (relayChart && relayChart.options && relayChart.options.scales && relayChart.options.scales.yAxes && relayChart.options.scales.yAxes[0]) {
                        relayChart.options.scales.yAxes[0].scaleLabel.labelString = unit;
                    }

                    // Update the chart's data and labels
                    relayChart.data.labels.push(''); // Add a label for each data point
                    relayChart.data.datasets[0].data.push(uploadData[uploadData.length - 1]);
                    relayChart.data.datasets[1].data.push(downloadData[downloadData.length - 1]);

                    // If the number of data points exceeds a certain limit (e.g., 50), remove the oldest data point
                    if (relayChart.data.labels.length > 20) {
                        relayChart.data.labels.shift();
                        relayChart.data.datasets[0].data.shift();
                        relayChart.data.datasets[1].data.shift();
                    }

                    // Fetch the uptime
                    const uptime = data.map(function (relayData) {
                        return formatUptime(relayData.uptime);
                    });

                    const torVersion = data.map(function (relayData) {
                        return relayData.version;
                    });

                    // Update the relay-rates div with the latest upload and download rates and uptime
                    const ratesContainer = $('#relayRates' + port);
                    ratesContainer.html(`
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Relay Statistics</h5>
            <p class="card-text">Upload: ${uploadData[uploadData.length - 1].toFixed(2)} ${unit}</p>
            <p class="card-text">Download: ${downloadData[downloadData.length - 1].toFixed(2)} ${unit}</p>
            <p class="card-text">Uptime: ${uptime[uptime.length - 1]}</p>
            <p class="card-text">Tor Version: ${torVersion[torVersion.length - 1]}</p>
        </div>
    </div>
`);

// Update the flagsData div with the fetched flags data
                    flagsContainer.html(`
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Relay Flags</h5>
            <p class="card-text">${flagsData[flagsData.length - 1]}</p>
        </div>
    </div>
`);
                    relayChart.update();
                }
            });
        }

        function updateRelayEventData(port, eventContainer) {
            if (!$('#relayChart' + port).is(':visible')) {
                // If the chart is not visible, return immediately without fetching and displaying the events
                return;
            }
            const apiUrl = baseApiUrl + '/' + port + '/events';
            $.get(apiUrl, function (data) {
                // Clear the event container
                eventContainer.empty();

                // Reverse the order of the events
                data = data.reverse();

                // Add new events
                for (let i = 0; i < data.length; i++) {
                    const event = data[i];
                    if (event !== null) { // Check if the event is not null
                        // Parse the timestamp from the event data
                        const timestampParts = event.split(' ')[0].split('T'); // Split the timestamp into date and time parts
                        const datePart = timestampParts[0];
                        const timeParts = timestampParts[1].split('.'); // Split the time part into time and nanoseconds
                        const timePart = timeParts[0];

                        // Create a new Date object with the date and time parts
                        const date = new Date(datePart + 'T' + timePart);

                        // Manually format the date and time
                        const formattedDate = date.getDate() + '-' + (date.getMonth() + 1) + '-' + date.getFullYear();
                        const formattedTime = date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds();

                        const eventElement = document.createElement('p');
                        eventElement.innerText = formattedDate + ' ' + formattedTime + ': ' + event.split(' ').slice(1).join(' ');
                        eventContainer.append(eventElement);
                    }
                }
            });
        }


        // Update the data and chart for the relay initially
        updateRelayTrafficDataAndChart();
        updateRelayEventData(port, eventContainer);

        // Set an interval to update the data and chart periodically (e.g., every 1 seconds)
        setInterval(updateRelayTrafficDataAndChart, 1000); // 1 seconds
        setInterval(function () {
            updateRelayEventData(port, eventContainer);
        }, 1000); // 1 seconds
    }

    $(document).ready(function () {
        // Fetch the list of relay info dynamically
        $.get('https://' + location.hostname + ':8443/relay-data/relay-info', function (relayInfoArray) {
            // Create charts for each relay based on the retrieved relay info
            relayInfoArray.forEach(function (relayInfo) {
                const port = relayInfo.controlPort;
                const nickname = relayInfo.nickname;
                const type = relayInfo.type;

                createRelayChart(port, nickname);

                // Add an item to the dropdown menu for this relay
                const menuItem = $('<a class="dropdown-item" href="#">' + nickname + ' (' + type + ')' + '</a>');
                menuItem.appendTo($('#relayDropdownMenu'));

                // Add a click event handler to the menu item
                menuItem.click(function () {
                    // Hide all relay charts
                    $('.relay-chart').hide();

                    // Show the selected relay's chart
                    $('#relayChart' + port).show();

                    // Hide all event containers
                    $('.relay-event').hide();

                    // Show the selected relay's event container
                    $('#eventData' + port).show();
                });
            });
        }).catch(function (jqXHR, textStatus, errorThrown) {
            console.error('Error fetching relay info:', textStatus, errorThrown);  // Log any errors
        });
    });
});

function formatUptime(uptime) {
    let hours = Math.floor(uptime / 3600);
    let minutes = Math.floor((uptime % 3600) / 60);
    let seconds = uptime % 60;

    // Pad the minutes and seconds with leading zeros, if required
    hours = (hours < 10) ? "0" + hours : hours;
    minutes = (minutes < 10) ? "0" + minutes : minutes;
    seconds = (seconds < 10) ? "0" + seconds : seconds;

    return hours + ":" + minutes + ":" + seconds;
}