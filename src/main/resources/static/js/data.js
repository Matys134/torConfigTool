$(document).ready(function () {
    // Define the base API endpoint
    var baseApiUrl = 'http://' + location.hostname + ':8081/api/relay-data';

    // Function to create and update a chart for a given relay
    function createRelayChart(port) {
        // Create a container for the relay chart and hide it initially
        var chartContainer = $('<div class="relay-chart" id="relayChart' + port + '"></div>').hide();
        var chartCanvas = $('<canvas width="400" height="200"></canvas>').appendTo(chartContainer);
        var relayName = 'Relay on Port ' + port;

        // Create a container for the upload and download rates
        var ratesContainer = $('<div class="relay-rates" id="relayRates' + port + '"></div>').appendTo(chartContainer);

        // Create a container for the events
        var eventsContainer = $('<div class="relay-events" id="relayEvents' + port + '"></div>').appendTo(chartContainer);

        // Create a container for the flags
        var flagsContainer = $('<div class="relay-flags" id="relayFlags' + port + '"></div>').appendTo(chartContainer);

        var eventContainer = $('<div class="relay-event" id="eventData' + port + '"></div>');
        eventContainer.appendTo($('#eventData'));


        // Append the chart container to the relayCharts div
        chartContainer.appendTo($('#relayCharts'));

        // Get references to the chart canvas and its context
        var ctx = chartCanvas[0].getContext('2d');

        // Create an initial empty chart
        var relayChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [
                    {
                        label: 'Upload',
                        borderColor: 'rgb(54, 162, 235)',
                        pointBackgroundColor: 'rgb(54, 162, 235)',
                        pointRadius: 5,
                        pointHoverRadius: 7,
                        lineTension: 0.1, // This will make the line smoother
                        data: [],
                        fill: false,
                    },
                    {
                        label: 'Download',
                        borderColor: 'rgb(255, 99, 132)',
                        pointBackgroundColor: 'rgb(255, 99, 132)',
                        pointRadius: 5,
                        pointHoverRadius: 7,
                        lineTension: 0.1, // This will make the line smoother
                        data: [],
                        fill: false,
                    },
                ],
            },
            options: {
                animation: false,
                title: {
                    display: true,
                    text: relayName,
                },
                tooltips: {
                    mode: 'index',
                    intersect: false,
                },
                hover: {
                    mode: 'nearest',
                    intersect: true
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Time'
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Bytes/s'
                        }
                    }]
                }
            }
        });

        // Function to update the traffic data and chart for the relay
        // Function to update the traffic data and chart for the relay
        function updateRelayTrafficDataAndChart() {
            var apiUrl = baseApiUrl + '/' + port;
            $.get(apiUrl, function (data) {
                if (data && data.length > 0) {
                    // Filter out null values from the data
                    data = data.filter(function (relayData) {
                        return relayData !== null;
                    });

                    var uploadData = data.map(function (relayData) {
                        return relayData.upload;
                    });
                    var downloadData = data.map(function (relayData) {
                        return relayData.download;
                    });

                    // Fetch the flags data
                    var flagsData = data.map(function (relayData) {
                        return relayData.flags;
                    });

                    // Update the flagsData div with the fetched flags data
                    var flagsContainer = $('#relayFlags' + port);
                    flagsContainer.html('Flags: ' + flagsData[flagsData.length - 1]);

                    // Determine the maximum value among the upload and download data
                    var maxDataValue = Math.max(Math.max(...uploadData), Math.max(...downloadData));

                    // Determine the scale and unit based on the maximum value
                    var scale, unit;
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

                    // Get the current time and format it as a string
                    var currentTime = new Date();
                    var timeLabel = currentTime.getHours() + ':' + currentTime.getMinutes() + ':' + currentTime.getSeconds();

                    // Update the chart's data and labels
                    relayChart.data.labels.push(timeLabel);
                    relayChart.data.datasets[0].data.push(uploadData[uploadData.length - 1]);
                    relayChart.data.datasets[1].data.push(downloadData[downloadData.length - 1]);

                    // If the number of data points exceeds a certain limit (e.g., 50), remove the oldest data point
                    if (relayChart.data.labels.length > 50) {
                        relayChart.data.labels.shift();
                        relayChart.data.datasets[0].data.shift();
                        relayChart.data.datasets[1].data.shift();
                    }

                    // Fetch the uptime
                    var uptime = data.map(function (relayData) {
                        return relayData.uptime;
                    });

                    var torVersion = data.map(function (relayData) {
                        return relayData.version;
                    });

                    // Update the relay-rates div with the latest upload and download rates and uptime
                    var ratesContainer = $('#relayRates' + port);
                    ratesContainer.html(`
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Relay Statistics</h5>
            <p class="card-text">Upload: ${uploadData[uploadData.length - 1]} ${unit}</p>
            <p class="card-text">Download: ${downloadData[downloadData.length - 1]} ${unit}</p>
            <p class="card-text">Uptime: ${uptime[uptime.length - 1]} seconds</p>
            <p class="card-text">Tor Version: ${torVersion[torVersion.length - 1]}</p>
        </div>
    </div>
`);

// Update the flagsData div with the fetched flags data
                    var flagsContainer = $('#relayFlags' + port);
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

        // Function to update the event data for the relay
        function updateRelayEventData() {
            var apiUrl = baseApiUrl + '/' + port + '/events';
            $.get(apiUrl, function (data) {
                // Update the eventData div with the event data
                eventContainer.html('');
                data.forEach(function (event, index) {
                    if (event !== null) { // Check if the event is not null
                        var eventElement = document.createElement('p');
                        eventElement.innerText = 'Event ' + (index + 1) + ': ' + event;
                        eventContainer.append(eventElement);
                    }
                });
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
        // Fetch the list of control ports dynamically
        // Hide all relay charts initially
        $('.relay-chart').hide();

        // Fetch the list of control ports dynamically
        $.get('http://' + location.hostname + ':8081/api/control-ports', function (controlPorts) {
            // Create charts for each relay based on the retrieved control ports
            controlPorts.forEach(function (port) {
                createRelayChart(port);

                // Add an item to the dropdown menu for this relay
                var menuItem = $('<a class="dropdown-item" href="#">Relay on Port ' + port + '</a>');
                menuItem.appendTo($('#relayDropdownMenu'));

                // Add a click event handler to the menu item
                menuItem.click(function () {
                    // Hide all relay charts
                    $('.relay-chart').hide();

                    // Show the selected relay's chart
                    $('#relayChart' + port).show();
                });
            });
        }).catch(function (jqXHR, textStatus, errorThrown) {  // Use catch instead of fail
            console.error('Error fetching control ports:', textStatus, errorThrown);  // Log any errors
        });
    });
});