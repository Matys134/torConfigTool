$(document).ready(function () {
    // Define the base API endpoint
    var baseApiUrl = 'http://' + location.hostname + ':8081/api/relay-data';

    // Function to create and update a chart for a given relay
    function createRelayChart(port) {
        // Create a container for the relay chart
        var chartContainer = $('<div class="relay-chart"></div>');
        var chartCanvas = $('<canvas width="400" height="200"></canvas>').appendTo(chartContainer);
        var relayName = 'Relay on Port ' + port;

        var eventContainer = $('<div class="relay-event" id="eventData' + port + '"></div>');
        eventContainer.appendTo($('#eventData'));

        // Create a container for the relay flags
        var flagsContainer = $('<div class="relay-flags" id="flagsData' + port + '"></div>');
        flagsContainer.appendTo($('#flagsData'));


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
                        data: [],
                    },
                    {
                        label: 'Download',
                        borderColor: 'rgb(255, 99, 132)',
                        data: [],
                    },
                ],
            },
            options: {
                animation: false,
                title: {
                    display: true,
                    text: relayName,
                },
            }
        });

        // Function to update the traffic data and chart for the relay
        function updateRelayTrafficDataAndChart() {
            var apiUrl = baseApiUrl + '/' + port;
            $.get(apiUrl, function (data) {
                if (data && data.length > 0) {
                    var uploadData = data.map(function (relayData) {
                        return relayData.upload;
                    });
                    var downloadData = data.map(function (relayData) {
                        return relayData.download;
                    });

                    // Get the flags from the most recent data
                    var flagsData = data[data.length - 1].flags;

                    // Check if flagsData is undefined and if so, set it to "no flags"
                    if (typeof flagsData === 'undefined' || flagsData === null || flagsData.length === 0) {
                        flagsData = 'no flags';
                    }

                    // Update the flagsData div with the flags data
                    flagsContainer.text('Flags: ' + flagsData);

                    // Update the chart's data and labels
                    relayChart.data.labels = Array.from({length: data.length}, (_, i) => i + 1);
                    relayChart.data.datasets[0].data = uploadData;
                    relayChart.data.datasets[1].data = downloadData;
                    relayChart.options.title.text = relayName + ' Flags: ' + flagsData;

                    // Update the chart
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
        $.get('http://' + location.hostname + ':8081/api/control-ports', function (controlPorts) {
            console.log('Control ports:', controlPorts);  // Log the control ports

            // Create charts for each relay based on the retrieved control ports
            controlPorts.forEach(function (port) {
                createRelayChart(port);

                // Add an item to the dropdown menu for this relay
                var menuItem = $('<a class="dropdown-item" href="#">Relay on Port ' + port + '</a>');
                console.log('Menu item:', menuItem);  // Log the menu item

                menuItem.appendTo($('#relayDropdownMenu'));

                // Add a click event handler to the menu item
                menuItem.click(function () {
                    // Hide all relay charts
                    $('.relay-chart').hide();
                    // Show the selected relay's chart
                    $('#relayChart' + port).show();
                });
            });
        }).fail(function (jqXHR, textStatus, errorThrown) {
            console.error('Error fetching control ports:', textStatus, errorThrown);  // Log any errors
        });
    });
});