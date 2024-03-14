$(document).ready(function() {
    // Function to check the limit state and bridge, guard, onion configuration
    function checkStateAndUpdate() {
        $.get("/setup-api/limit-state", function(data) {
            if (data.limitOn) {
                const runningTypeRequest = $.get("/bridge-api/bridges/configured-type");
                const bridgeConfiguredRequest = $.get("/bridge-api/bridge-configured");
                const guardConfiguredRequest = $.get("/guard-api/guard-configured");
                const onionConfiguredRequest = $.get("/onion-api/onion-configured");

                $.when(runningTypeRequest, bridgeConfiguredRequest, guardConfiguredRequest, onionConfiguredRequest).done(function(runningTypeData, bridgeData, guardData, onionData) {
                    // Add a new AJAX call to get the bridge count
                    $.get("/bridge-api/bridges/limit-reached", { bridgeType: 'snowflake' }, function(snowflakeCountData) {
                        // Move the code that updates the page into the callback function of the AJAX call
                        if (runningTypeData[0] && !jQuery.isEmptyObject(runningTypeData[0])) {
                            const runningBridgeType = Object.values(runningTypeData[0])[0];
                            console.log(runningBridgeType);
                            if (runningBridgeType === 'obfs4' || runningBridgeType === 'snowflake') {
                                // Disable the bridge type field
                                $('#bridgeType').prop('disabled', true);
                            } else {
                                // Enable the bridge type field and disable all fields in the bridge form
                                $('#bridgeType').prop('disabled', false);
                                $('#bridgeForm :input').prop('disabled', true);
                            }
                        }

                        if (bridgeData[0].bridgeConfigured || snowflakeCountData.bridgeCount > 0) {
                            // Disable the form fields in the guard and onion tabs
                            $('#guardForm :input').prop('disabled', true);
                            $('#onionForm :input').prop('disabled', true);
                        } else {
                            // Enable the form fields in the guard and onion tabs
                            $('#guardForm :input').prop('disabled', false);
                            $('#onionForm :input').prop('disabled', false);
                        }

                        if (guardData[0].guardConfigured) {
                            $('#onionForm :input').prop('disabled', true);
                            $('#bridgeForm :input').prop('disabled', true);
                        }

                        if (onionData[0].onionConfigured) {
                            $('#guardForm :input').prop('disabled', true);
                            $('#bridgeForm :input').prop('disabled', true);
                        }

                        if (bridgeData[0].bridgeConfigured && snowflakeCountData.bridgeCount >= 1) {
                            // Disable the form fields in the bridge tab
                            $('#bridgeForm :input').prop('disabled', true);
                        }

                        // Check if a guard is configured and the guard limit has been reached
                        if (guardData[0].guardConfigured && data.guardCount >= 8) {
                            // Disable the form fields in the guard tab
                            $('#guardForm :input').prop('disabled', true);
                        }
                    });
                });
            } else {
                // Enable the form fields in the bridge, guard and onion tabs
                $('#bridgeForm :input').prop('disabled', false);
                $('#guardForm :input').prop('disabled', false);
                $('#onionForm :input').prop('disabled', false);
            }
        });
    }

    // Call the function when the page is loaded
    checkStateAndUpdate();

    // Call the function when the button is clicked
    $("#toggleLimitButton").click(function() {
        $.post("/setup-api/toggle-limit", function() {
            $.get("/setup-api/limit-state", function(data) {
                const toggleLimitButton = $("#toggleLimitButton");
                if (data.limitOn) {
                    toggleLimitButton.text("Turn Limit Off");
                } else {
                    toggleLimitButton.text("Turn Limit On");
                }
                checkStateAndUpdate();
            });
        });
    });
});

const bridgeTypes = ['obfs4', 'webtunnel', 'snowflake'];

bridgeTypes.forEach(function(bridgeType) {
    $.get("/bridge-api/bridges/limit-reached", { bridgeType: bridgeType }, function(data) {
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

$.get("/guard-api/guards/limit-reached", function(data) {
    if (data.guardLimitReached) {
        // Disable the form fields and buttons
        const guardForm = document.getElementById('guardForm');
        guardForm.disabled = true;
        const formElements = guardForm.elements;
        for (let i = 0; i < formElements.length; i++) {
            formElements[i].disabled = true;
        }

        // Create a new div element for the message
        const messageDiv = document.createElement("div");
        messageDiv.className = "alert alert-warning";
        messageDiv.role = "alert";
        messageDiv.innerHTML = "Maximum number of guard relays has been reached. You can edit an existing relay or remove some to create a new one.";

        // Append the message div to the container
        document.querySelector('.container').appendChild(messageDiv);
    }
});

$.get("/bridge-api/bridges/limit-reached", { bridgeType: 'obfs4' }, function(data) {
    const bridgeCountElement = $('#bridgeCount');
    bridgeCountElement.text(data.bridgeCount + "/2");
    if (data.bridgeCount > 2) {
        bridgeCountElement.removeClass('limit-reached');
        bridgeCountElement.addClass('over-limit');
    } else if (data.bridgeCount >= 2) {
        bridgeCountElement.removeClass('over-limit');
        bridgeCountElement.addClass('limit-reached');
    } else {
        bridgeCountElement.removeClass('limit-reached over-limit');
    }
});

$.get("/guard-api/guards/limit-reached", function(data) {
    const guardCountElement = $('#guardCount');
    guardCountElement.text(data.guardCount + "/8");
    if (data.guardCount > 8) {
        guardCountElement.removeClass('limit-reached');
        guardCountElement.addClass('over-limit');
    } else if (data.guardCount >= 8) {
        guardCountElement.removeClass('over-limit');
        guardCountElement.addClass('limit-reached');
    } else {
        guardCountElement.removeClass('limit-reached over-limit');
    }
});

$.get("/bridge-api/bridges/limit-reached", { bridgeType: 'webtunnel' }, function(data) {
    const webtunnelCountElement = $('#webtunnelCount');
    webtunnelCountElement.text(data.bridgeCount + "/1");
    if (data.bridgeCount > 1) {
        webtunnelCountElement.removeClass('limit-reached');
        webtunnelCountElement.addClass('over-limit');
    } else if (data.bridgeCount >= 1) {
        webtunnelCountElement.removeClass('over-limit');
        webtunnelCountElement.addClass('limit-reached');
    } else {
        webtunnelCountElement.removeClass('limit-reached over-limit');
    }
});

$.get("/bridge-api/bridges/limit-reached", { bridgeType: 'snowflake' }, function(data) {
    const snowflakeCountElement = $('#snowflakeCount');
    snowflakeCountElement.text(data.bridgeCount + "/1");
    if (data.bridgeCount > 1) {
        snowflakeCountElement.removeClass('limit-reached');
        snowflakeCountElement.addClass('over-limit');
    } else if (data.bridgeCount >= 1) {
        snowflakeCountElement.removeClass('over-limit');
        snowflakeCountElement.addClass('limit-reached');
    } else {
        snowflakeCountElement.removeClass('limit-reached over-limit');
    }
});

$.get("/bridge-api/bridges/configured-type", function(runningBridgeTypes) {
    if (runningBridgeTypes && !jQuery.isEmptyObject(runningBridgeTypes)) {
        // Extract the first bridge type from the response
        const runningBridgeType = Object.values(runningBridgeTypes)[0];
        console.log(runningBridgeType);

        // Disable the bridge type selection
        document.getElementById('bridgeType').disabled = true;

        // Create a new div element for the message
        const messageDiv = document.createElement("div");
        messageDiv.className = "alert alert-warning";
        messageDiv.role = "alert";
        messageDiv.innerHTML = "A bridge of type " + runningBridgeType + " is already running. You can stop the running bridge to create a new one.";

        // Append the message div to the container
        document.querySelector('.container').appendChild(messageDiv);
    }
});

document.getElementById("toggleLimitButton").addEventListener("click", function () {
    const limitStateCheckbox = document.getElementById("limitState");
    limitStateCheckbox.checked = !limitStateCheckbox.checked;
});
