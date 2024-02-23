$.get("/guard/limit-reached", function(data) {
    if (data.guardLimitReached) {
        // Disable the form fields and buttons
        var guardForm = document.getElementById('guardForm');
        guardForm.disabled = true;
        var formElements = guardForm.elements;
        for (var i = 0; i < formElements.length; i++) {
            formElements[i].disabled = true;
        }

        // Create a new div element for the message
        var messageDiv = document.createElement("div");
        messageDiv.className = "alert alert-warning";
        messageDiv.role = "alert";
        messageDiv.innerHTML = "Maximum number of guard relays has been reached. You can edit an existing relay or remove some to create a new one.";

        // Append the message div to the container
        document.querySelector('.container').appendChild(messageDiv);
    }
});