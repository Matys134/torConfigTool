$(document).ready(function () {
    // Add a click event handler for the "Stop" buttons
    $(".stop-button").click(function () {
        // Show the spinner
        $("#spinner").show();

        const nickname = $(this).data("config-nickname");
        const relayType = $(this).data("config-type");

        // Send an AJAX request to stop the relay
        $.ajax({
            url: '/relay-operations-api/stop',
            type: 'POST',
            data: {
                relayNickname: nickname,
                relayType: relayType
            },
            success: function (data) {
                // Hide the spinner
                $("#spinner").hide();
                console.log('Relay stopped:', data);
            },
            error: function (error) {
                // Hide the spinner
                $("#spinner").hide();
                console.error('Error stopping relay:', error);
            }
        });
    });
});