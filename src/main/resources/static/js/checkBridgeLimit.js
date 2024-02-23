var bridgeTypes = ['obfs4', 'webtunnel', 'snowflake'];

bridgeTypes.forEach(function(bridgeType) {
    $.get("/bridge/limit-reached", { bridgeType: bridgeType }, function(data) {
        if (data.bridgeLimitReached) {
            // Disable the bridge form fields and buttons
            var bridgeForm = document.getElementById('bridgeForm');
            bridgeForm.disabled = true;
            var formElements = bridgeForm.elements;
            for (var i = 0; i < formElements.length; i++) {
                formElements[i].disabled = true;
            }

            // Create a new div element for the message
            var messageDiv = document.createElement("div");
            messageDiv.className = "alert alert-warning";
            messageDiv.role = "alert";
            messageDiv.innerHTML = "Maximum number of " + bridgeType + " bridges has been reached. You can edit an existing bridge or remove some to create a new one.";

            // Append the message div to the container
            document.querySelector('.container').appendChild(messageDiv);
        }
    });
});