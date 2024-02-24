$(document).ready(function() {
    $.get("/bridge/running-type", function(runningBridgeTypes) {
        for (var bridgeNickname in runningBridgeTypes) {
            if (runningBridgeTypes.hasOwnProperty(bridgeNickname)) {
                var bridgeType = runningBridgeTypes[bridgeNickname];
                var bridgeTypeElement = $("#bridge-type-" + bridgeNickname);
                bridgeTypeElement.text(bridgeType);
            }
        }
    });
});