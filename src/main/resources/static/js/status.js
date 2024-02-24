$(document).ready(function () {
    // Function to check and update relay status
    function updateRelayStatus(nickname, relayType) {
        $.get("/relay-operations/status?relayNickname=" + nickname + "&relayType=" + relayType, function (data) {
            console.log(data); // Log the server response

            const statusElement = $("#status-" + nickname);
            const statusMessageElement = $("#status-message-" + nickname);

            // Select the start, stop and edit buttons
            const startButton = $(".start-button[data-config-nickname='" + nickname + "']");
            const stopButton = $(".stop-button[data-config-nickname='" + nickname + "']");
            const editButton = $(".edit-button[data-config-nickname='" + nickname + "']");

            if (data === "online") {
                statusElement.text("Online");
                statusElement.css("color", "green");

                // Disable the start and edit buttons and enable the stop button
                startButton.prop('disabled', true);
                stopButton.prop('disabled', false);
                editButton.prop('disabled', true);
            } else if (data === "offline") {
                statusElement.text("Offline");
                statusElement.css("color", "red");

                // Enable the start and edit buttons and disable the stop button
                startButton.prop('disabled', false);
                stopButton.prop('disabled', true);
                editButton.prop('disabled', false);
            } else {
                statusElement.text("Unknown"); // Handle any other status
                statusElement.css("color", "blue"); // Adjust color as needed

                // Enable all buttons
                startButton.prop('disabled', false);
                stopButton.prop('disabled', false);
                editButton.prop('disabled', false);
            }

            // Check the status message and update accordingly
            if (statusMessageElement.text() === "Shutting down" && data !== "online") {
                statusMessageElement.text("Idle");
            }
        });
    }

    // Function to update relay status for guard relays
    function updateGuardRelayStatus() {
        $(".start-button").each(function () {
            const nickname = $(this).data("config-nickname");
            const relayType = $(this).data("config-type"); // Fetch the relayType parameter
            if (relayType === "guard") {
                updateRelayStatus(nickname, relayType);
            }
        });
    }

    // Function to update relay status for bridge relays
    function updateBridgeRelayStatus() {
        $(".start-button").each(function () {
            const nickname = $(this).data("config-nickname");
            const relayType = $(this).data("config-type"); // Fetch the relayType parameter
            if (relayType === "bridge") {
                updateRelayStatus(nickname, relayType);
            }
        });
    }

    // Function to update relay status for Onion relays
    function updateOnionRelayStatus() {
        $(".start-button").each(function () {
            const nickname = $(this).data("config-nickname");
            const relayType = $(this).data("config-type"); // Fetch the relayType parameter
            if (relayType === "onion") {
                updateRelayStatus(nickname, relayType);
            }
        });
    }


    // Update relay status on page load for both guard and bridge relays
    updateGuardRelayStatus();
    updateBridgeRelayStatus();
    updateOnionRelayStatus();

    // Periodically update relay status every 10 seconds (adjust the interval as needed)
    setInterval(function () {
        updateGuardRelayStatus();
        updateBridgeRelayStatus();
        updateOnionRelayStatus();
    }, 10000);
});
