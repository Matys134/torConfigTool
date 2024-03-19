let maxBridgeCount = {};
let totalRelays = 0;

$.get("/bridge-api/bridges/max-count", function(data) {
    maxBridgeCount = data;
});

let maxGuardCount;

$.get("/guard-api/guards/max-count", function(data) {
    maxGuardCount = data;
});

$.get("/guard-api/guards/limit-reached", function(data) {
    const guardCountElement = $('#guardCount');
    guardCountElement.text(data.guardCount);
    guardCountElement.addClass('blue-text');
    totalRelays += data.guardCount;
});

$.get("/bridge-api/bridges/limit-reached", { bridgeType: 'obfs4' }, function(data) {
    const bridgeCountElement = $('#bridgeCount');
    bridgeCountElement.text(data.bridgeCount);
    bridgeCountElement.addClass('blue-text');
    totalRelays += data.bridgeCount;
});

$.get("/bridge-api/bridges/limit-reached", { bridgeType: 'webtunnel' }, function(data) {
    const webtunnelCountElement = $('#webtunnelCount');
    webtunnelCountElement.text(data.bridgeCount);
    webtunnelCountElement.addClass('blue-text');
    totalRelays += data.bridgeCount;

    updateTotalRelays();
});

$.get("/bridge-api/bridges/limit-reached", { bridgeType: 'snowflake' }, function(data) {
    const snowflakeCountElement = $('#snowflakeCount');
    snowflakeCountElement.text(data.bridgeCount);
    snowflakeCountElement.addClass('blue-text');
    totalRelays += data.bridgeCount;

    updateTotalRelays();
});

let onionServiceCount = 0;

$.get("/onion-api/onion-configured", function(data) {
    const onionServiceCountElement = $('#onionServiceCount');
    onionServiceCountElement.text(onionServiceCount);
    onionServiceCountElement.addClass('blue-text');
});

function updateTotalRelays() {
    // Update total relays
    const totalRelaysElement = $('#totalRelays');
    totalRelaysElement.text(totalRelays + "/8");
    if (totalRelays >= 8) {
        totalRelaysElement.addClass('red-text');

        // Display warning message
        $('#relayLimitWarning').show();

        // Disable the forms
        $('#guardForm :input').prop('disabled', true);
        $('#bridgeForm :input').prop('disabled', true);
    } else {
        totalRelaysElement.addClass('green-text');
    }
}

// Check if any bridge is configured
$.get("/bridge-api/bridge-configured", function(data) {
    if (data.bridgeConfigured) {
        // If a bridge is configured, show the warning message and disable the form
        $('#guardWarning').show();
        $('#guardForm :input').prop('disabled', true);
    }
});

// When the "I Understand" button is clicked, hide the warning message and enable the form
$('#understandButton').click(function() {
    $('#guardWarning').hide();
    // Only enable the form if the relay limit warning is not visible
    if (!$('#relayLimitWarning').is(':visible')) {
        $('#guardForm :input').prop('disabled', false);
    }
});

// Check if any guard is configured
$.get("/guard-api/guard-configured", function(data) {
    if (data.guardConfigured) {
        // If a guard is configured, show the warning message and disable the form
        $('#bridgeWarning').show();
        $('#bridgeForm :input').prop('disabled', true);
    }
});

// When the "I Understand" button is clicked, hide the warning message and enable the form
$('#understandBridgeButton').click(function() {
    $('#bridgeWarning').hide();
    // Only enable the form if the relay limit warning is not visible
    if (!$('#relayLimitWarning').is(':visible')) {
        $('#bridgeForm :input').prop('disabled', false);
    }
});