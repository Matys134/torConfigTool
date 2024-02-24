const bridgeTypes = ['obfs4', 'webtunnel', 'snowflake'];

bridgeTypes.forEach(function(bridgeType) {
    $.get("/bridge/limit-reached", { bridgeType: bridgeType }, function(data) {
        if (data.bridgeLimitReached) {
            // Disable the bridge form fields and buttons
            const bridgeForm = document.getElementById('bridgeForm');
            bridgeForm.disabled = true;
            const formElements = bridgeForm.elements;
            for (let i = 0; i < formElements.length; i++) {
                formElements[i].disabled = true;
            }

            // Create a new div element for the message
            const messageDiv = document.createElement("div");
            messageDiv.className = "alert alert-warning";
            messageDiv.role = "alert";
            messageDiv.innerHTML = "Maximum number of " + bridgeType + " bridges has been reached. You can edit an existing bridge or remove some to create a new one.";

            // Append the message div to the container
            document.querySelector('.container').appendChild(messageDiv);
        }
    });
});