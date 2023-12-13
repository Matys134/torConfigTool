$(document).ready(function() {
    // Function to check and update relay status
    function updateRelayStatus(nickname, relayType) {
        $.get("/relay-operations/status?relayNickname=" + nickname + "&relayType=" + relayType, function(data) {
            console.log(data); // Log the server response

            var statusElement = $("#status-" + nickname);
            var statusMessageElement = $("#status-message-" + nickname);

            if (data === "online") {
                statusElement.text("Online");
                statusElement.css("color", "green");
            } else if (data === "offline") {
                statusElement.text("Offline");
                statusElement.css("color", "red");
            } else {
                statusElement.text("Unknown"); // Handle any other status
                statusElement.css("color", "blue"); // Adjust color as needed
            }

            // Check the status message and update accordingly
            if (statusMessageElement.text() === "Shutting down" && data !== "online") {
                statusMessageElement.text("Idle");
            }
        });
    }

    // Function to update relay status for guard relays
    function updateGuardRelayStatus() {
        $(".edit-button").each(function() {
            var nickname = $(this).data("config-nickname");
            var relayType = $(this).data("config-type"); // Fetch the relayType parameter
            if (relayType === "guard") {
                updateRelayStatus(nickname, relayType);
            }
        });
    }

    // Function to update relay status for bridge relays
    function updateBridgeRelayStatus() {
        $(".edit-bridge-button").each(function() {
            var nickname = $(this).data("config-nickname");
            var relayType = $(this).data("config-type"); // Fetch the relayType parameter
            if (relayType === "bridge") {
                updateRelayStatus(nickname, relayType);
            }
        });
    }

    // Function to update relay status for Onion relays
    function updateOnionRelayStatus() {
        $(".edit-onion-button").each(function() {
            var port = $(this).data("config-port");
            var relayType = $(this).data("config-type"); // Fetch the relayType parameter
            if (relayType === "onion") {
                updateRelayStatus(port, relayType);
            }
        });
    }


    // Update relay status on page load for both guard and bridge relays
    updateGuardRelayStatus();
    updateBridgeRelayStatus();
    updateOnionRelayStatus();

    // Periodically update relay status every 10 seconds (adjust the interval as needed)
    setInterval(function() {
        updateGuardRelayStatus();
        updateBridgeRelayStatus();
        updateOnionRelayStatus();
    }, 10000);
});
