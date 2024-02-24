$(document).ready(function() {
    $.get("/bridge/running-type", function(runningBridgeTypes) {
        for (const bridgeNickname in runningBridgeTypes) {
            if (runningBridgeTypes.hasOwnProperty(bridgeNickname)) {
                const bridgeType = runningBridgeTypes[bridgeNickname];
                const bridgeTypeElement = $("#bridge-type-" + bridgeNickname);
                bridgeTypeElement.text(bridgeType);
            }
        }
    });
});