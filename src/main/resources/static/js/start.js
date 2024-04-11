/**
 * This file contains JavaScript code for managing relay operations.
 * It includes functions to start the relay.
 * Each function is triggered by a button click event.
 */

// Wait for the document to be ready
$(document).ready(function () {
    // When the 'start-button' is clicked
    $(".start-button").click(function () {
        // Get the nickname and type of the relay from the button's data attributes
        const nickname = $(this).data("config-nickname");
        const relayType = $(this).data("config-type");

        // Select the start, stop, edit buttons and spinner for the current relay
        const startButton = $(".start-button[data-config-nickname='" + nickname + "']");
        const stopButton = $(".stop-button[data-config-nickname='" + nickname + "']");
        const editButton = $(".edit-button[data-config-nickname='" + nickname + "']");
        const removeButton = $(".remove-button[data-config-nickname='" + nickname + "']");
        const spinner = startButton.find(".spinner");

        // Disable the buttons and show the spinner while the relay is starting
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
                // Log the successful start of the relay
                console.log('Relay started:', data);
                // Call the updateRelayStatus function to update the status of the relay
                updateRelayStatus(nickname, relayType);
            },
            error: function (error) {
                // Log any error that occurred while starting the relay
                console.error('Error starting relay:', error);
            },
            complete: function () {
                // Enable the buttons and hide the spinner after the relay has started
                startButton.prop('disabled', false);
                stopButton.prop('disabled', false);
                editButton.prop('disabled', false);
                spinner.hide();
            }
        });
    });
});