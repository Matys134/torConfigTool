/**
 * This file contains JavaScript code for managing relay operations.
 * It includes a function to stop the relay.
 * The function is triggered by a button click event.
 */

// Wait for the document to be ready
$(document).ready(function () {
    // When the 'stop-button' is clicked
    $(".stop-button").click(function () {
        // Get the nickname and type of the relay from the button's data attributes
        const nickname = $(this).data("config-nickname");
        const relayType = $(this).data("config-type");

        // Select the start, stop, edit and remove buttons for the current relay
        const startButton = $(".start-button[data-config-nickname='" + nickname + "']");
        const stopButton = $(".stop-button[data-config-nickname='" + nickname + "']");
        const editButton = $(".edit-button[data-config-nickname='" + nickname + "']");
        const removeButton = $(".remove-button[data-config-nickname='" + nickname + "']");
        const spinner = stopButton.find(".spinner");

        // Disable the buttons and show the spinner while the relay is stopping
        startButton.prop('disabled', true);
        stopButton.prop('disabled', true);
        editButton.prop('disabled', true);
        removeButton.prop('disabled', true);
        spinner.show();

        // Show the appropriate spinner
        $("#spinner-" + nickname).show();

        // Send an AJAX request to stop the relay
        $.ajax({
            url: '/relay-operations-api/stop',
            type: 'POST',
            data: {
                relayNickname: nickname,
                relayType: relayType
            },
            success: function (data) {
                // Log the successful stop of the relay
                console.log('Relay stopped:', data);
                // Call the updateRelayStatus function to update the status of the relay
                updateRelayStatus(nickname, relayType);
            },
            error: function (error) {
                // Log any error that occurred while stopping the relay
                console.error('Error stopping relay:', error);
            },
            complete: function () {
                // Enable the buttons and hide the spinner after the relay has stopped
                startButton.prop('disabled', false);
                stopButton.prop('disabled', false);
                editButton.prop('disabled', false);
                spinner.hide();
            }
        });
    });
});