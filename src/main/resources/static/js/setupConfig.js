/**
 * This script is responsible for setting up the configuration based on the server's response.
 * It sends a GET request to the "/config" endpoint and processes the received data.
 * If a configuration is set to '0', it hides the corresponding tab or removes the option from the dropdown menu.
 * If all bridge types are set to '0', it hides the bridge tab.
 * After processing the data, it calls the function to toggle bridge fields based on the selected bridge type.
 */
$(document).ready(function() {
    $.get("/config", function(data) {
        const lines = data.split('\n');
        let allBridgeTypesZero = true;
        lines.forEach(function(line) {
            const parts = line.split(' ');
            const configName = parts[0].toLowerCase();
            const value = parts[1];
            if (value === '0') {
                if (configName === 'onion') {
                    $('#onion-tab').hide();
                    $('#onionTab').hide(); // Hide the Onion tab content
                } else if (configName === 'relay') {
                    $('#relay-tab').hide();
                    $('#relayTab').hide(); // Hide the Relay tab content
                } else {
                    // Remove the bridge type from the dropdown
                    $("#bridgeType option[value='" + configName + "']").remove();
                }
            } else if (configName !== 'onion' && configName !== 'relay') {
                allBridgeTypesZero = false;
            }
        });
        if (allBridgeTypesZero) {
            $('#bridge-tab').hide();
            $('#bridgeTab').hide(); // Hide the Bridge tab content
        }
        // Call the function to toggle bridge fields based on the selected bridge type
        toggleBridgeFields();
    });
});