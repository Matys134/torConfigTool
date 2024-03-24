$(document).ready(function () {
    $(".start-button").click(function () {
        const nickname = $(this).data("config-nickname");
        const relayType = $(this).data("config-type");

        // Select the start, stop, edit buttons and spinner
        const startButton = $(".start-button[data-config-nickname='" + nickname + "']");
        const stopButton = $(".stop-button[data-config-nickname='" + nickname + "']");
        const editButton = $(".edit-button[data-config-nickname='" + nickname + "']");
        const removeButton = $(".remove-button[data-config-nickname='" + nickname + "']");
        const spinner = startButton.find(".spinner");

        // Disable the buttons and show the spinner
        startButton.prop('disabled', true);
        stopButton.prop('disabled', true);
        editButton.prop('disabled', true);
        removeButton.prop('disabled', true);
        spinner.show();

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
                console.error('Error starting relay:', error);
            },
            complete: function () {
                // Enable the buttons and hide the spinner
                startButton.prop('disabled', false);
                stopButton.prop('disabled', false);
                editButton.prop('disabled', false);
                spinner.hide();
            }
        });
    });
});