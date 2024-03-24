let totalRelays = 0;

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

function checkConfigured(endpoint, warningElementId, formElementId) {
    $.get(endpoint, function(data) {
        if (data.bridgeConfigured || data.guardConfigured) {
            $(`#${warningElementId}`).show();
            $(`#${formElementId} :input`).prop('disabled', true);
        }
    });
}

function handleUnderstandButtonClick(buttonElementId, warningElementId, formElementId) {
    $(`#${buttonElementId}`).click(function() {
        $(`#${warningElementId}`).hide();
        if (!$('#relayLimitWarning').is(':visible')) {
            $(`#${formElementId} :input`).prop('disabled', false);
        }
    });
}

updateCount("/guard-api/guards/guard-count", "guardCount");
updateCount("/bridge-api/bridges/bridge-count", "bridgeCount", 'obfs4');
updateCount("/bridge-api/bridges/bridge-count", "webtunnelCount", 'webtunnel');
updateCount("/bridge-api/bridges/bridge-count", "snowflakeCount", 'snowflake');

checkConfigured("/bridge-api/bridge-configured", "guardWarning", "guardForm");
handleUnderstandButtonClick("understandButton", "guardWarning", "guardForm");

checkConfigured("/guard-api/guard-configured", "bridgeWarning", "bridgeForm");
handleUnderstandButtonClick("understandBridgeButton", "bridgeWarning", "bridgeForm");

let onionServiceCount = 0;

$.get("/onion-api/onion-configured", function(data) {
    const onionServiceCountElement = $('#onionServiceCount');
    onionServiceCountElement.text(onionServiceCount);
    onionServiceCountElement.addClass('blue-text');
});

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