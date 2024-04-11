/**
 * This script is responsible for handling form submissions for both guard and bridge configurations.
 * It includes validation checks and alerts for certain conditions.
 */

// When the document is ready
$(document).ready(function() {
    // On bridge form submission
    $('#bridgeForm').on('submit', function() {
        // Get the bridge type field
        const bridgeTypeField = $('#bridgeType');
        // If the bridge type field is disabled
        if (bridgeTypeField.prop('disabled')) {
            // Get the bridge type value
            const bridgeType = bridgeTypeField.val();
            // If there is no input field with the name 'bridgeType'
            if (!$("input[name='bridgeType']").length) {
                // Append a hidden input field with the name 'bridgeType' and the value of the bridge type to the form
                $(this).append('<input type="hidden" name="bridgeType" value="' + bridgeType + '">');
            }
        }
    });
});

$(document).ready(function() {
    // On bridge form submission
    $('#bridgeForm').on('submit', function(e) {
        // Get the ORPort and server transport listen address values
        const orPort = $('#bridgePort').val();
        const serverTransportListenAddr = $('#bridgeTransportListenAddr').val();

        // If either the ORPort or the server transport listen address is 9001
        if (orPort === 9001 || serverTransportListenAddr === 9001) {
            // Prevent the form from being submitted
            e.preventDefault();
            // Alert the user
            alert('Port 9001 is associated with Tor and may be blocked by censorship authorities. Please choose a different port.');
        }
    });
});

$(document).ready(function() {
    // On guard form submission
    $('#guardForm').on('submit', function(e) {
        // Get the relay bandwidth value and convert it to an integer
        const bandwidth = parseInt($('#relayBandwidth').val());
        // If the bandwidth is not 0 and is less than 75
        if (bandwidth !== 0 && bandwidth < 75) {
            // Alert the user
            alert("Bandwidth must be 0 or 75 and larger");
            // Prevent the form from being submitted
            e.preventDefault();
        }
    });
});

$(document).ready(function() {
    // On bridge form submission
    $('#bridgeForm').on('submit', function(e) {
        // Get the bridge bandwidth value and convert it to an integer
        const bandwidth = parseInt($('#bridgeBandwidth').val());
        // If the bandwidth is not 0 and is less than 75
        if (bandwidth !== 0 && bandwidth < 75) {
            // Alert the user
            alert("Bandwidth must be 0 or 75 and larger");
            // Prevent the form from being submitted
            e.preventDefault();
        }
    });
});

// Get the input fields
const relayPort = document.getElementById('relayPort');
const controlPort = document.getElementById('controlPort');
const bridgeControlPort = document.getElementById('bridgeControlPort');
const bridgePort = document.getElementById('bridgePort');
const bridgeTransportListenAddr = document.getElementById('bridgeTransportListenAddr');
const onionServicePort = document.getElementById('onionServicePort');

// Add event listeners to the input fields
relayPort.addEventListener('change', checkPortUniqueness);
controlPort.addEventListener('change', checkPortUniqueness);
bridgeControlPort.addEventListener('change', checkPortUniqueness);
bridgePort.addEventListener('change', checkPortUniqueness);
bridgeTransportListenAddr.addEventListener('change', checkPortUniqueness);
onionServicePort.addEventListener('change', checkPortUniqueness);

/**
 * Function to check if the ports are unique.
 * It gets the values of the port input fields, filters out empty values, and checks if all the values are unique.
 * If the values are not unique, it alerts the user and clears the input fields.
 */
function checkPortUniqueness() {
    let ports = [relayPort.value, controlPort.value, bridgeControlPort.value, bridgePort.value, bridgeTransportListenAddr.value, onionServicePort.value];

    // Filter out empty values
    ports = ports.filter(function(port) {
        return port !== '';
    });

    const uniquePorts = [...new Set(ports)];

    if (ports.length !== uniquePorts.length) {
        alert('The ports must be unique. Please enter different values.');
        relayPort.value = '';
        controlPort.value = '';
        bridgeControlPort.value = '';
        bridgePort.value = '';
        bridgeTransportListenAddr.value = '';
        onionServicePort.value = '';
    }
}