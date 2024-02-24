$(document).ready(function() {
    // Function to check the limit state and bridge, guard, onion configuration
    function checkStateAndUpdate() {
        $.get("/guard/limit-state-and-guard-count", function(data) {
            if (data.limitOn) {
                const runningTypeRequest = $.get("/bridge/running-type");
                const bridgeConfiguredRequest = $.get("/guard/bridge-configured");
                const guardConfiguredRequest = $.get("/guard/guard-configured");
                const onionConfiguredRequest = $.get("/onion-service/onion-configured");

                $.when(runningTypeRequest, bridgeConfiguredRequest, guardConfiguredRequest, onionConfiguredRequest).done(function(runningTypeData, bridgeData, guardData, onionData) {
                    // Add a new AJAX call to get the bridge count
                    $.get("/bridge/limit-reached", { bridgeType: 'snowflake' }, function(snowflakeCountData) {
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
        $.post("/bridge/toggle-limit", function() {
            $.get("/bridge/limit-state", function(data) {
                const toggleLimitButton = $("#toggleLimitButton");
                const warningTexts = $(".alert-warning"); // select all warning texts

                if (data) {
                    // Update the button text
                    toggleLimitButton.text("Turn Limit Off");
                    // Show the warning texts
                    warningTexts.show();

                    // Check if a snowflake bridge is running
                    $.get("/bridge/running-type", function(runningBridgeTypes) {
                        if (runningBridgeTypes && runningBridgeTypes.snowflake_proxy === "snowflake") {
                            // Disable the form fields in the bridge tab
                            $('#bridgeForm :input').prop('disabled', true);
                        }
                    });
                } else {
                    // Update the button text
                    toggleLimitButton.text("Turn Limit On");
                    // Hide the warning texts
                    warningTexts.hide();
                    // Enable the form fields in the bridge tab
                    $('#bridgeForm :input').prop('disabled', false);
                }

                // Call the function to update the state of the tabs
                checkStateAndUpdate();
            });
        });
    });
});