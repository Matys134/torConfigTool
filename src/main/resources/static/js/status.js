$(document).ready(function() {
    // Function to check and update relay status
    function updateRelayStatus(nickname) {
        $.get("/relay/status?relayNickname=" + nickname, function(data) {
            var statusElement = $("#status-" + nickname);
            if (data === "online") {
                statusElement.text("Online");
                statusElement.css("color", "green");
            } else {
                statusElement.text("Offline");
                statusElement.css("color", "red");
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

    // Periodically update relay status every 10 seconds (adjust the interval as needed)
    setInterval(updateAllRelayStatus, 1000);
});
