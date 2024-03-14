$(document).ready(function() {
    function disableFormFields(formId) {
        const form = document.getElementById(formId);
        form.disabled = true;
        const formElements = form.elements;
        for (let i = 0; i < formElements.length; i++) {
            formElements[i].disabled = true;
        }
    }

    function updateCountAndApplyClasses(countElementId, count, limit) {
        const countElement = $(countElementId);
        console.log(`countElement: ${countElement}`);
        countElement.text(count + "/" + limit);
        if (count > limit) {
            countElement.removeClass('limit-reached');
            countElement.addClass('over-limit');
        } else if (count == limit) {
            countElement.removeClass('over-limit');
            countElement.addClass('limit-reached');
        } else {
            countElement.removeClass('limit-reached over-limit');
        }
    }

    function handleLimitReached(apiEndpoint, bridgeType, formId) {
        $.get(apiEndpoint, { bridgeType: bridgeType }, function(data) {
            if (data.bridgeLimitReached) {
                disableFormFields(formId);

                const messageDiv = document.createElement("div");
                messageDiv.className = "alert alert-warning";
                messageDiv.role = "alert";
                messageDiv.innerHTML = "Maximum number of " + bridgeType + " bridges has been reached. You can edit an existing bridge or remove some to create a new one.";

                document.querySelector('.container').appendChild(messageDiv);
            }

            updateCountAndApplyClasses('#' + bridgeType + 'Count', data.bridgeCount, bridgeType === 'webtunnel' || bridgeType === 'snowflake' ? 1 : 2);
        });
    }

    const bridgeTypes = ['obfs4', 'webtunnel', 'snowflake'];
    bridgeTypes.forEach(function(bridgeType) {
        handleLimitReached("/bridge-api/bridges/limit-reached", bridgeType, 'bridgeForm');
    });

    function checkStateAndUpdate() {
        $.get("/setup-api/limit-state", function(limitOn) {
            if (limitOn) {
                const runningTypeRequest = $.get("/bridge-api/bridges/configured-type");
                const bridgeConfiguredRequest = $.get("/bridge-api/bridge-configured");
                const guardConfiguredRequest = $.get("/guard-api/guard-configured");
                const onionConfiguredRequest = $.get("/onion-api/onion-configured");

                $.when(runningTypeRequest, bridgeConfiguredRequest, guardConfiguredRequest, onionConfiguredRequest).done(function(runningTypeData, bridgeData, guardData, onionData) {
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
                    } else {
                        $('#guardForm :input').prop('disabled', false);
                        $('#onionForm :input').prop('disabled', false);
                    }

                    if (guardData[0].guardConfigured) {
                        disableFormFields('onionForm');
                        disableFormFields('bridgeForm');
                    }

                    if (onionData[0].onionConfigured) {
                        disableFormFields('guardForm');
                        disableFormFields('bridgeForm');
                    }
                });
            } else {
                $('#bridgeForm :input').prop('disabled', false);
                $('#guardForm :input').prop('disabled', false);
                $('#onionForm :input').prop('disabled', false);
            }
        });
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