$(".start-button").click(function () {
    const nickname = $(this).data("config-nickname");
    const relayType = $(this).data("config-type");

    // Show the spinner
    $("#spinner-" + nickname).show();

    // Send an AJAX request to start the relay
    $.ajax({
        url: '/relay-operations-api/start',
        type: 'POST',
        data: {
            relayNickname: nickname,
            relayType: relayType
        },
        success: function (data) {
            // Handle the server response here (if needed)
            console.log('Relay started:', data);

            // Hide the spinner
            $("#spinner-" + nickname).hide();
        },
        error: function (error) {
            // Handle any errors here
            console.error('Error starting relay:', error);

            // Hide the spinner
            $("#spinner-" + nickname).hide();
        }
    });
});