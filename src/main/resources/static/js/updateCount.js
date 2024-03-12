$.get("/bridge-api/bridges/limit-reached", { bridgeType: 'obfs4' }, function(data) {
    $('#bridgeCount').text(data.bridgeCount + "/2");
});

$.get("/guard-api/guards/limit-reached", function(data) {
    $('#guardCount').text(data.guardCount + "/8");
});

$.get("/bridge-api/bridges/limit-reached", { bridgeType: 'webtunnel' }, function(data) {
    $('#webtunnelCount').text(data.bridgeCount + "/1");
});

$.get("/bridge-api/bridges/limit-reached", { bridgeType: 'snowflake' }, function(data) {
    $('#snowflakeCount').text(data.bridgeCount + "/1");
});