$(document).ready(function() {
    // Function to check and update relay status
    function updateRelayStatus(nickname) {
        $.get("/relay/status?relayNickname=" + nickname, function(data) {
            var statusElement = $("#status-" + nickname);
            var statusMessageElement = $("#status-message-" + nickname);

            if (data === "online") {
                statusElement.text("Online");
                statusElement.css("color", "green");
            } else {
                statusElement.text("Offline");
                statusElement.css("color", "red");
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
            updateRelayStatus(nickname);
        });
    }

    // Update relay status on page load
    updateAllRelayStatus();

    // Function to handle the "Stop" button click event
    $(".stop-button").click(function() {
        var nickname = $(this).data("config-nickname");
        var statusMessageElement = $("#status-message-" + nickname);

        // Change the status message to "Shutting down"
        statusMessageElement.text("Shutting down");

        // Send a POST request to stop the relay using the corresponding nickname
        $.post("/relay/stop", { relayNickname: nickname }, function(data) {
            // Handle the response, e.g., show success or error messages
        });
    });

    // Periodically update relay status every 10 seconds (adjust the interval as needed)
    setInterval(updateAllRelayStatus, 10000);
});
