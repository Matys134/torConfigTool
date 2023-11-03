$(document).ready(function () {
    // Add a click event handler for the "Start" buttons
    $(".start-button").click(function () {
        var nickname = $(this).data("config-nickname");
        var relayType = $(this).data("config-type");

        // Send an AJAX request to start the relay
        $.ajax({
            url: '/relay-operations/start',
            type: 'POST',
            data: {
                relayNickname: nickname,
                relayType: relayType
            },
            success: function (data) {
                // Handle the server response here (if needed)
                console.log('Relay started:', data);
            },
            error: function (error) {
                // Handle any errors here
                console.error('Error starting relay:', error);
            }
        });
    });
});
