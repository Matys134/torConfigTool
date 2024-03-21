$(document).ready(function () {
    $(".stop-button").click(function () {
        const nickname = $(this).data("config-nickname");
        const relayType = $(this).data("config-type");

        // Select the start, stop and edit buttons
        const startButton = $(".start-button[data-config-nickname='" + nickname + "']");
        const stopButton = $(".stop-button[data-config-nickname='" + nickname + "']");
        const editButton = $(".edit-button[data-config-nickname='" + nickname + "']");
        const removeButton = $(".remove-button[data-config-nickname='" + nickname + "']");

        // Disable the buttons
        startButton.prop('disabled', true);
        stopButton.prop('disabled', true);
        editButton.prop('disabled', true);

        // Show the appropriate spinner
        $("#spinner-" + relayType).show();

        // Send an AJAX request to stop the relay
        $.ajax({
            url: '/relay-operations-api/stop',
            type: 'POST',
            data: {
                relayNickname: nickname,
                relayType: relayType
            },
            success: function (data) {
                console.log('Relay stopped:', data);
                // Call the updateRelayStatus function
                updateRelayStatus(nickname, relayType);
            },
            error: function (error) {
                // Hide the spinner
                $("#spinner").hide();
                console.error('Error stopping relay:', error);
            },
            complete: function () {
                // Enable the buttons
                startButton.prop('disabled', false);
                stopButton.prop('disabled', false);
                editButton.prop('disabled', false);
                removeButton.prop('disabled', true);
            }
        });
    });
});