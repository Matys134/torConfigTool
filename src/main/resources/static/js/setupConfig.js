$(document).ready(function() {
    $.get("/config", function(data) {
        var lines = data.split('\n');
        var allBridgeTypesZero = true;
        lines.forEach(function(line) {
            var parts = line.split(' ');
            var configName = parts[0].toLowerCase();
            var value = parts[1];
            if (value === '0') {
                if (configName === 'onion') {
                    $('#onion-tab').hide();
                } else {
                    // Remove the bridge type from the dropdown
                    $("#bridgeType option[value='" + configName + "']").remove();
                }
            } else if (configName !== 'onion') {
                allBridgeTypesZero = false;
            }
        });
        if (allBridgeTypesZero) {
            $('#bridge-tab').hide();
        }
        // Call the function to toggle bridge fields based on the selected bridge type
        toggleBridgeFields();
    });
});