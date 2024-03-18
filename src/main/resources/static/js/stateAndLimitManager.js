$(document).ready(function() {
    function disableFormFields(formId) {
        const form = document.getElementById(formId);
        form.disabled = true;
        const formElements = form.elements;
        for (let i = 0; i < formElements.length; i++) {
            formElements[i].disabled = true;
        }
    }

    function checkLimitAndDisableForm(apiUrl, formId, message) {
        $.get(apiUrl, function(data) {
            if (data.guardLimitReached || data.bridgeLimitReached) {
                // Disable the form fields and buttons
                var form = document.getElementById(formId);
                form.disabled = true;
                var formElements = form.elements;
                for (var i = 0; i < formElements.length; i++) {
                    formElements[i].disabled = true;
                }

                // Create a new div element for the message
                var messageDiv = document.createElement("div");
                messageDiv.className = "alert alert-warning";
                messageDiv.role = "alert";
                messageDiv.innerHTML = message;

                // Append the message div to the container
                document.querySelector('.container').appendChild(messageDiv);
            }
        });
    }

    var bridgeTypes = ['obfs4', 'webtunnel', 'snowflake'];

    bridgeTypes.forEach(function(bridgeType) {
        checkLimitAndDisableForm("/bridge-api/bridges/limit-reached", 'bridgeForm', "Maximum number of " + bridgeType + " bridges has been reached. You can edit an existing bridge or remove some to create a new one.");
    });

    checkLimitAndDisableForm("/guard-api/guards/limit-reached", 'guardForm', "Maximum number of guard relays has been reached. You can edit an existing relay or remove some to create a new one.");


    function checkStateAndUpdate() {
        $.get("/setup-api/limit-state", function(limitOn) {
            if (limitOn) {
                const runningTypeRequest = $.get("/bridge-api/bridges/configured-type");
                const bridgeConfiguredRequest = $.get("/bridge-api/bridge-configured");
                const guardConfiguredRequest = $.get("/guard-api/guard-configured");
                const onionConfiguredRequest = $.get("/onion-api/onion-configured");

                $.when(runningTypeRequest, bridgeConfiguredRequest, guardConfiguredRequest, onionConfiguredRequest)
                    .done(function(runningTypeData, bridgeData, guardData, onionData) {
                    if (runningTypeData[0] && !jQuery.isEmptyObject(runningTypeData[0])) {
                        const runningBridgeType = Object.values(runningTypeData[0])[0];
                        if (runningBridgeType === 'obfs4' || runningBridgeType === 'snowflake') {
                            $('#bridgeType').prop('disabled', true);
                        } else {
                            $('#bridgeType').prop('disabled', false);
                            disableFormFields('bridgeForm');
                        }
                    }

                    const bridgeTypes = ['obfs4', 'webtunnel', 'snowflake'];
                    bridgeTypes.forEach(function(bridgeType) {
                        handleLimitReached("/bridge-api/bridges/limit-reached", bridgeType, 'bridgeForm');
                    });

                    if (bridgeData[0].bridgeConfigured) {
                        disableFormFields('guardForm');
                        disableFormFields('onionForm');
                        showWarningMessage('Bridge');
                    } else {
                        $('#guardForm :input').prop('disabled', false);
                        $('#onionForm :input').prop('disabled', false);
                    }

                    if (guardData[0].guardConfigured) {
                        disableFormFields('onionForm');
                        disableFormFields('bridgeForm');
                        showWarningMessage('Guard');
                    }

                    if (onionData[0].onionConfigured) {
                        disableFormFields('guardForm');
                        disableFormFields('bridgeForm');
                        showWarningMessage('Onion');
                    }

                });
            } else {
                $('#bridgeForm :input').prop('disabled', false);
                $('#guardForm :input').prop('disabled', false);
                $('#onionForm :input').prop('disabled', false);
            }
        });
    }

    function showWarningMessage(serviceName) {
        // Check if a warning message for this service already exists
        const existingWarning = document.querySelector('.alert-warning[data-service="' + serviceName + '"]');
        if (!existingWarning) {
            // Create and append new warning message
            const messageDiv = document.createElement("div");
            messageDiv.className = "alert alert-warning";
            messageDiv.dataset.service = serviceName; // Add a data attribute to identify the service
            messageDiv.role = "alert";
            messageDiv.innerHTML = serviceName + " service has been configured. Other services are now blocked due to the limit.";
            document.querySelector('.container').appendChild(messageDiv);
        }
    }

    function updateButtonText() {
        $.get("/setup-api/limit-state", function(limitOn) {
            const toggleLimitButton = $("#toggleLimitButton");
            if (limitOn) {
                toggleLimitButton.text("Turn Limit Off");
            } else {
                toggleLimitButton.text("Turn Limit On");
            }
        });
    }

    checkStateAndUpdate();
    updateButtonText();

    $("#toggleLimitButton").off('click').on('click', function() {
        $.post("/setup-api/toggle-limit", function() {
            updateButtonText();
            checkStateAndUpdate();
        });
    });
});