/**
 * Total number of relays.
 * @type {number}
 */
let totalRelays = 0;

/**
 * Updates the count of bridges or guards.
 * @param {string} endpoint - The endpoint to get the count from.
 * @param {string} elementId - The ID of the HTML element to update with the count.
 * @param {string} [bridgeType=null] - The type of the bridge (optional).
 */
function updateCount(endpoint, elementId, bridgeType = null) {
    let data = bridgeType ? { bridgeType: bridgeType } : {};
    $.get(endpoint, data, function(response) {
        const element = $('#' + elementId);
        let count = 0;
        if (typeof response.bridgeCount === 'number') {
            count = response.bridgeCount;
        } else if (typeof response.guardCount === 'number') {
            count = response.guardCount;
        }
        element.text(count);
        element.addClass('blue-text');
        totalRelays += count;
        updateTotalRelays();
    });
}

/**
 * Checks if a bridge or guard is configured.
 * @param {string} endpoint - The endpoint to check the configuration from.
 * @param {string} warningElementId - The ID of the HTML element to show the warning.
 * @param {string} formElementId - The ID of the HTML form to disable.
 */
function checkConfigured(endpoint, warningElementId, formElementId) {
    $.get(endpoint, function(data) {
        if (data.bridgeConfigured || data.guardConfigured) {
            $(`#${warningElementId}`).show();
            $(`#${formElementId} :input`).prop('disabled', true);
        }
    });
}

/**
 * Handles the click event of the understand button.
 * @param {string} buttonElementId - The ID of the button element.
 * @param {string} warningElementId - The ID of the warning element.
 * @param {string} formElementId - The ID of the form element.
 */
function handleUnderstandButtonClick(buttonElementId, warningElementId, formElementId) {
    $(`#${buttonElementId}`).click(function() {
        $(`#${warningElementId}`).hide();
        if (!$('#relayLimitWarning').is(':visible')) {
            $(`#${formElementId} :input`).prop('disabled', false);
        }
    });
}

// Update counts for different types of bridges and guards.
updateCount("/guard-api/guards/guard-count", "guardCount");
updateCount("/bridge-api/bridges/bridge-count", "bridgeCount", 'obfs4');
updateCount("/bridge-api/bridges/bridge-count", "webtunnelCount", 'webtunnel');
updateCount("/bridge-api/bridges/bridge-count", "snowflakeCount", 'snowflake');

// Check if bridges and guards are configured.
checkConfigured("/bridge-api/bridge-configured", "guardWarning", "guardForm");
handleUnderstandButtonClick("understandButton", "guardWarning", "guardForm");
checkConfigured("/guard-api/guard-configured", "bridgeWarning", "bridgeForm");
handleUnderstandButtonClick("understandBridgeButton", "bridgeWarning", "bridgeForm");

/**
 * Total number of onion services.
 * @type {number}
 */
let onionServiceCount = 0;

// Check if onion service is configured.
$.get("/onion-api/onion-configured", function(data) {
    const onionServiceCountElement = $('#onionServiceCount');
    onionServiceCountElement.text(onionServiceCount);
    onionServiceCountElement.addClass('blue-text');
});

/**
 * Updates the total number of relays.
 */
function updateTotalRelays() {
    const totalRelaysElement = $('#totalRelays');
    totalRelaysElement.text(totalRelays + "/8");
    if (totalRelays >= 8) {
        totalRelaysElement.addClass('red-text');
        $('#relayLimitWarning').show();
        $('#guardForm :input').prop('disabled', true);
        $('#bridgeForm :input').prop('disabled', true);
    } else {
        totalRelaysElement.addClass('green-text');
    }
}