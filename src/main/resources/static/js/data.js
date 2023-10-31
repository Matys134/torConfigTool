$(document).ready(function () {
    // Define the base API endpoint
    var baseApiUrl = 'http://192.168.2.117:8081/api/relay-data';

    // Function to create and update a chart for a given relay
    function createRelayChart(port) {
        // Create a container for the relay chart
        var chartContainer = $('<div class="relay-chart"></div>');
        var chartCanvas = $('<canvas width="400" height="200"></canvas>').appendTo(chartContainer);
        var relayName = 'Relay on Port ' + port;

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

                    var latestData = data[data.length - 1];
                    // Update the chart's data and labels
                    relayChart.data.labels = Array.from({ length: data.length }, (_, i) => i + 1);
                    relayChart.data.datasets[0].data = uploadData;
                    relayChart.data.datasets[1].data = downloadData;
                    // Update the chart
                    relayChart.update();
                }
            });
        }

        // Update the data and chart for the relay initially
        updateRelayTrafficDataAndChart();

        // Set an interval to update the data and chart periodically (e.g., every 10 seconds)
        setInterval(updateRelayTrafficDataAndChart, 1000); // 10 seconds
    }

    // Fetch the list of control ports dynamically
    $.get('http://192.168.2.117:8081/api/control-ports', function (controlPorts) {
        // Create charts for each relay based on the retrieved control ports
        controlPorts.forEach(function (port) {
            createRelayChart(port);
        });
    });
});
