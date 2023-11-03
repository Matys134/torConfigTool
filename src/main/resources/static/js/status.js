$(document).ready(function() {
    // Function to check and update relay status
    function updateRelayStatus(nickname, relayType) {
        $.get("/relay-operations/status?relayNickname=" + nickname + "&relayType=" + relayType, function(data) {
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

    // Function to update relay status for all relays
    function updateAllRelayStatus() {
        $(".edit-button").each(function() {
            var nickname = $(this).data("config-nickname");
            var relayType = $(this).data("config-type"); // Fetch the relayType parameter
            updateRelayStatus(nickname, relayType);
        });
    }

    // Update relay status on page load
    updateAllRelayStatus();

    // Periodically update relay status every 10 seconds (adjust the interval as needed)
    setInterval(updateAllRelayStatus, 1000);
});
