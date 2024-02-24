$.get("/bridge/limit-reached", { bridgeType: 'obfs4' }, function(data) {
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

$.get("/guard/limit-reached", function(data) {
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

$.get("/bridge/limit-reached", { bridgeType: 'webtunnel' }, function(data) {
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

$.get("/bridge/limit-reached", { bridgeType: 'snowflake' }, function(data) {
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