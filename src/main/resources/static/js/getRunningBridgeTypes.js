/**
 * This script is responsible for fetching the types of running bridges from the server.
 * It sends a GET request to the "/bridge-api/bridges/configured-type" endpoint and updates the text of the corresponding HTML elements with the received data.
 */
$(document).ready(function() {
    $.get("/bridge-api/bridges/configured-type", function(runningBridgeTypes) {
        for (const bridgeNickname in runningBridgeTypes) {
            if (runningBridgeTypes.hasOwnProperty(bridgeNickname)) {
                const bridgeType = runningBridgeTypes[bridgeNickname];
                const bridgeTypeElement = $("#bridge-type-" + bridgeNickname);
                bridgeTypeElement.text(bridgeType);
            }
        }
    });
});