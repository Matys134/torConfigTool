$.get("/bridge/running-type", function(runningBridgeTypes) {
    if (runningBridgeTypes && !jQuery.isEmptyObject(runningBridgeTypes)) {
        // Extract the first bridge type from the response
        var runningBridgeType = Object.values(runningBridgeTypes)[0];
        console.log(runningBridgeType);

        // Disable the bridge type selection
        document.getElementById('bridgeType').disabled = true;

        // Create a new div element for the message
        var messageDiv = document.createElement("div");
        messageDiv.className = "alert alert-warning";
        messageDiv.role = "alert";
        messageDiv.innerHTML = "A bridge of type " + runningBridgeType + " is already running. You can stop the running bridge to create a new one.";

        // Append the message div to the container
        document.querySelector('.container').appendChild(messageDiv);
    }
});