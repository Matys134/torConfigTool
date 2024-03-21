$(document).ready(function () {
    // Add a click event handler for the "Start" buttons
    $(".start-button").click(function () {
        // Show the spinner
        $("#spinner").show();

        const nickname = $(this).data("config-nickname");
        const relayType = $(this).data("config-type");

        // Send an AJAX request to start the relay
        $.ajax({
            url: '/relay-operations-api/start',
            type: 'POST',
            data: {
                relayNickname: nickname,
                relayType: relayType
            },
            success: function (data) {
                // Hide the spinner
                $("#spinner").hide();
                console.log('Relay started:', data);
            },
            error: function (error) {
                // Hide the spinner
                $("#spinner").hide();
                console.error('Error starting relay:', error);
            }
        });
    });
});
