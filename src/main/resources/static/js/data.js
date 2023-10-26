$(document).ready(function () {
    // Define the URL of your server-side endpoint
    var apiUrl = '/api/relay-data'; // Change to your actual API endpoint

    // Get references to the chart canvas and its context
    var ctx = document.getElementById('trafficChart').getContext('2d');

    // Create an initial empty chart
    var trafficChart = new Chart(ctx, {
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
        }
    });

    // Function to update the traffic data and chart
    function updateTrafficDataAndChart() {
        $.get(apiUrl, function (data) {
            // Assuming the API returns an array of RelayData objects
            if (data && data.length > 0) {
                // Extract upload and download data
                var uploadData = data.map(function (relayData) {
                    return relayData.upload;
                });
                var downloadData = data.map(function (relayData) {
                    return relayData.download;
                });

                var latestData = data[data.length - 1]; // Assuming the latest data is at the end of the array
                $('#uploadValue').text(latestData.upload);
                $('#downloadValue').text(latestData.download);

                // Update the chart's data and labels
                trafficChart.data.labels = Array.from({ length: data.length }, (_, i) => i + 1);
                trafficChart.data.datasets[0].data = uploadData;
                trafficChart.data.datasets[1].data = downloadData;

                // Update the chart
                trafficChart.update();
            }
        });
    }

    // Update the data and chart initially
    updateTrafficDataAndChart();

    // Set an interval to update the data and chart periodically (e.g., every 10 seconds)
    setInterval(updateTrafficDataAndChart, 1000); // 10 seconds
});
