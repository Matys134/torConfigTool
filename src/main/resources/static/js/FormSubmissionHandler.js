$(document).ready(function() {
    $('#bridgeForm').on('submit', function() {
        const bridgeTypeField = $('#bridgeType');
        if (bridgeTypeField.prop('disabled')) {
            const bridgeType = bridgeTypeField.val();
            if (!$("input[name='bridgeType']").length) {
                $(this).append('<input type="hidden" name="bridgeType" value="' + bridgeType + '">');
            }
        }
    });
});

$(document).ready(function() {
    $('#bridgeForm').on('submit', function(e) {
        const orPort = $('#bridgePort').val();
        const serverTransportListenAddr = $('#bridgeTransportListenAddr').val();

        if (orPort == 9001 || serverTransportListenAddr == 9001) {
            e.preventDefault(); // prevent form from being submitted
            alert('Port 9001 is associated with Tor and may be blocked by censorship authorities. Please choose a different port.');
        }
    });
});

$(document).ready(function() {
    $('#guardForm').on('submit', function(e) {
        const bandwidth = parseInt($('#relayBandwidth').val());
        if (bandwidth !== 0 && bandwidth < 75) {
            alert("Bandwidth must be 0 or 75 and larger");
            e.preventDefault(); // prevent form from being submitted
        }
    });
});

$(document).ready(function() {
    $('#bridgeForm').on('submit', function(e) {
        const bandwidth = parseInt($('#bridgeBandwidth').val());
        if (bandwidth !== 0 && bandwidth < 75) {
            alert("Bandwidth must be 0 or 75 and larger");
            e.preventDefault(); // prevent form from being submitted
        }
    });
});

// Get the input fields
var relayPort = document.getElementById('relayPort');
var controlPort = document.getElementById('controlPort');
var bridgeControlPort = document.getElementById('bridgeControlPort');
var bridgePort = document.getElementById('bridgePort');
var bridgeTransportListenAddr = document.getElementById('bridgeTransportListenAddr');
var onionServicePort = document.getElementById('onionServicePort');

// Add event listeners to the input fields
relayPort.addEventListener('change', checkPortUniqueness);
controlPort.addEventListener('change', checkPortUniqueness);
bridgeControlPort.addEventListener('change', checkPortUniqueness);
bridgePort.addEventListener('change', checkPortUniqueness);
bridgeTransportListenAddr.addEventListener('change', checkPortUniqueness);
onionServicePort.addEventListener('change', checkPortUniqueness);

// Function to check if the ports are unique
function checkPortUniqueness() {
    var ports = [relayPort.value, controlPort.value, bridgeControlPort.value, bridgePort.value, bridgeTransportListenAddr.value, onionServicePort.value];

    // Filter out empty values
    ports = ports.filter(function(port) {
        return port !== '';
    });

    var uniquePorts = [...new Set(ports)];

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