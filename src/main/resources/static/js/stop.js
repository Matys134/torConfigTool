$(document).ready(function () {
    // Add a click event handler for the "Stop" buttons
    $(".stop-button").click(function () {
        var nickname = $(this).data("config-nickname");
        var relayType = $(this).data("config-type");

        // Send an AJAX request to stop the relay
        $.ajax({
            url: '/relay-operations/stop',
            type: 'POST',
            data: {
                relayNickname: nickname,
                relayType: relayType
            },
            success: function (data) {
                // Handle the server response here (if needed)
                console.log('Relay stopped:', data);
            },
            error: function (error) {
                // Handle any errors here
                console.error('Error stopping relay:', error);
            }
        });
    });
});