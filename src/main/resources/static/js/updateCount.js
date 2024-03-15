let maxBridgeCount = {};

$.get("/bridge-api/bridges/max-count", function(data) {
    maxBridgeCount = data;
});

let maxGuardCount;

$.get("/guard-api/guards/max-count", function(data) {
    maxGuardCount = data;
});

$.get("/guard-api/guards/limit-reached", function(data) {
    const guardCountElement = $('#guardCount');
    guardCountElement.text(data.guardCount + "/" + maxGuardCount);
    if (data.guardCount >= maxGuardCount) {
        guardCountElement.addClass('red-text');
    } else {
        guardCountElement.addClass('blue-text');
    }
});

$.get("/bridge-api/bridges/limit-reached", { bridgeType: 'obfs4' }, function(data) {
    const bridgeCountElement = $('#bridgeCount');
    bridgeCountElement.text(data.bridgeCount + "/" + maxBridgeCount['obfs4']);
    if (data.bridgeCount >= maxBridgeCount['obfs4']) {
        bridgeCountElement.addClass('red-text');
    } else {
        bridgeCountElement.addClass('blue-text');
    }
});

$.get("/bridge-api/bridges/limit-reached", { bridgeType: 'webtunnel' }, function(data) {
    const webtunnelCountElement = $('#webtunnelCount');
    webtunnelCountElement.text(data.bridgeCount + "/" + maxBridgeCount['webtunnel']);
    if (data.bridgeCount >= maxBridgeCount['webtunnel']) {
        webtunnelCountElement.addClass('red-text');
    } else {
        webtunnelCountElement.addClass('blue-text');
    }
});

$.get("/bridge-api/bridges/limit-reached", { bridgeType: 'snowflake' }, function(data) {
    const snowflakeCountElement = $('#snowflakeCount');
    snowflakeCountElement.text(data.bridgeCount + "/" + maxBridgeCount['snowflake']);
    if (data.bridgeCount >= maxBridgeCount['snowflake']) {
        snowflakeCountElement.addClass('red-text');
    } else {
        snowflakeCountElement.addClass('blue-text');
    }
});