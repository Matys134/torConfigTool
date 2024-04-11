/**
 * This file contains JavaScript code for managing relay operations.
 * It includes functions to update the status of different types of relays.
 * Each function is triggered by a button click event or a periodic interval.
 */

$(document).ready(function () {
    /**
     * Function to check and update relay status
     * @param {string} nickname - The nickname of the relay
     * @param {string} relayType - The type of the relay
     */
    function updateRelayStatus(nickname, relayType) {
        // Send a GET request to the relay operations API to get the status of the relay
        $.get("/relay-operations-api/status?relayNickname=" + nickname + "&relayType=" + relayType, function (data) {

            // Select the status elements for the relay
            const statusElement = $("#status-" + nickname);
            const statusMessageElement = $("#status-message-" + nickname);
            const spinner = $(".start-button[data-config-nickname='" + nickname + "']").find(".spinner");

            // Select the start, stop and edit buttons for the relay
            const startButton = $(".start-button[data-config-nickname='" + nickname + "']");
            const stopButton = $(".stop-button[data-config-nickname='" + nickname + "']");
            const editButton = $(".edit-button[data-config-nickname='" + nickname + "']");

            // Update the status and buttons based on the status of the relay
            if (data === "online") {
                // Update the status to online and color to green
                statusElement.text("Online");
                statusElement.css("color", "green");

                // Disable the start and edit buttons and enable the stop button
                startButton.prop('disabled', true);
                stopButton.prop('disabled', false);
                editButton.prop('disabled', true);

                // Hide the spinner
                spinner.hide();
            } else if (data === "offline") {
                // Update the status to offline and color to red
                statusElement.text("Offline");
                statusElement.css("color", "red");

                // Enable the start and edit buttons and disable the stop button
                startButton.prop('disabled', false);
                stopButton.prop('disabled', true);
                editButton.prop('disabled', false);

                // Hide the spinner
                spinner.hide();
            } else {
                // Handle any other status
                statusElement.text("Unknown");
                statusElement.css("color", "blue");

                // Enable all buttons
                startButton.prop('disabled', false);
                stopButton.prop('disabled', false);
                editButton.prop('disabled', false);
            }

            // Check the status message and update accordingly
            if (statusMessageElement.text() === "Shutting down" && data !== "online") {
                statusMessageElement.text("Idle");
            }

            // Hide the spinner if the relay is online or offline
            if (data === "online" || data === "offline") {
                $("#spinner-" + relayType).hide();
            }
        });
    }

    /**
     * Function to update relay status for guard relays
     */
    function updateGuardRelayStatus() {
        // For each start button, check if the relay type is guard and update the status
        $(".start-button").each(function () {
            const nickname = $(this).data("config-nickname");
            const relayType = $(this).data("config-type");
            if (relayType === "guard") {
                updateRelayStatus(nickname, relayType);
            }
        });
    }

    /**
     * Function to update relay status for bridge relays
     */
    function updateBridgeRelayStatus() {
        // For each start button, check if the relay type is bridge and update the status
        $(".start-button").each(function () {
            const nickname = $(this).data("config-nickname");
            const relayType = $(this).data("config-type");
            if (relayType === "bridge") {
                updateRelayStatus(nickname, relayType);
            }
        });
    }

    /**
     * Function to update relay status for Onion relays
     */
    function updateOnionRelayStatus() {
        // For each start button, check if the relay type is onion and update the status
        $(".start-button").each(function () {
            const nickname = $(this).data("config-nickname");
            const relayType = $(this).data("config-type");
            if (relayType === "onion") {
                updateRelayStatus(nickname, relayType);
            }
        });
    }

    // Update relay status on page load for all types of relays
    updateGuardRelayStatus();
    updateBridgeRelayStatus();
    updateOnionRelayStatus();

    // Periodically update relay status every 5 seconds
    setInterval(function () {
        updateGuardRelayStatus();
        updateBridgeRelayStatus();
        updateOnionRelayStatus();
    }, 5000);
});