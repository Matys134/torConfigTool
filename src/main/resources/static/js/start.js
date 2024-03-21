$(document).ready(function () {
    // Add a click event handler for the "Start" buttons
    $(".start-button").click(function () {
        const nickname = $(this).data("config-nickname");
        const relayType = $(this).data("config-type");

        // Show the appropriate spinner
        $("#spinner-" + relayType).show();

        // Send an AJAX request to start the relay
        $.ajax({
            url: '/relay-operations-api/start',
            type: 'POST',
            data: {
                relayNickname: nickname,
                relayType: relayType
            },
            success: function (data) {
                console.log('Relay started:', data);
                // Call the updateRelayStatus function
                updateRelayStatus(nickname, relayType);
            },
            error: function (error) {
                // Hide the spinner
                $("#spinner").hide();
                console.error('Error starting relay:', error);
            }
        });
    });
});