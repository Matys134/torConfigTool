$(document).ready(function () {

    var isBridgeEdit = false; // Add this variable to track which config is being edited
    var serverTransportProtocolAndAddress;

    const configSelectors = {
        modal: $("#edit-modal"),
        nickname: $("#edit-nickname"),
        orPort: $("#edit-orport"),
        serverTransport: $("#edit-server-transport"),
        contact: $("#edit-contact"),
        controlPort: $("#edit-controlport"),
        webtunnel: $("#edit-webtunnel") // Add this line
    };

    const buttons = {
        edit: $(".edit-button, .edit-bridge-button"), // Combined edit buttons for guard and bridge
        save: $("#save-button"),
        cancel: $("#cancel-button"),
    };

    // Function to show the modal with the data for editing
    function showModalWith(data, relayType, bridgeType) {
        console.log('Data:', data); // Log the data object

        // Split the serverTransport into protocol and port
        var serverTransportParts = data.serverTransport ? data.serverTransport.split(':') : [];
        serverTransportProtocolAndAddress = serverTransportParts.slice(0, -1).join(':');
        var serverTransportPort = serverTransportParts[serverTransportParts.length - 1];

        // Set the values of the input fields
        configSelectors.nickname.text(data.nickname); // Use .text() for nickname
        configSelectors.orPort.val(data.orPort);
        configSelectors.serverTransport.val(serverTransportPort);
        configSelectors.contact.val(data.contact);
        configSelectors.controlPort.val(data.controlPort);
        configSelectors.webtunnel.val(data.webtunnel); // Add this line

        // Hide all fields initially
        $('#edit-form label, #edit-form input').hide();

        // Show or hide the fields based on the relay type and bridge type
        $('#edit-form [data-config-type]').each(function() {
            var configTypes = $(this).attr('data-config-type').split(' ');
            var bridgeTypes = $(this).attr('data-bridge-type') ? $(this).attr('data-bridge-type').split(' ') : [];
            if (configTypes.includes(relayType) && (relayType !== 'bridge' || bridgeTypes.includes(bridgeType))) {
                $(this).show();
                $(this).next('input').show();
                // Populate the input fields with the current values
                var inputId = $(this).next('input').attr('id');
                if (inputId in data) {
                    var value = data[inputId.replace('edit-', '')];
                    console.log('Setting value for input field with id ' + inputId + ': ' + value);
                    $(this).next('input').val(value);
                }
            }
        });

        // Show the modal
        $('#edit-modal').modal('show');
    }


    function hideModal() {
        $('#edit-modal').modal('hide');
    }

    function updateView(data) {
        const configElement = $(`[data-config-nickname="${data.nickname}"]`);

        configElement.find(".config-orport").text(`ORPort: ${data.orPort}`);
        configElement.find(".config-server-transport").text(`ServerTransportListenAddr: ${data.serverTransport}`);
        configElement.find(".config-contact").text(`Contact: ${data.contact}`);
        configElement.find(".config-controlport").text(`Control Port: ${data.controlPort}`);
        configElement.find(".config-webtunnel").text(`WebTunnel: ${data.webtunnel}`); // Add this line
    }

    function sendUpdateRequest(url, data) {
        // Check if serverTransport is defined before splitting it
        if (data.serverTransport) {
            var serverTransportParts = data.serverTransport.split(':');
            var serverTransportPort = serverTransportParts[serverTransportParts.length - 1];

            // Combine the protocol and address with the new port to form the updated serverTransport
            data.serverTransport = serverTransportPort;
        }

        $.ajax({
            type: "POST",
            url: url,
            contentType: "application/json",
            data: JSON.stringify(data),
            success: function (response) {
                if (response.success) {
                    // Update the view with the new configuration, if needed
                    updateView(data);
                } else {
                    alert("Failed to update configuration.");
                }
            }
        });
    }

    buttons.edit.click(function () {
        const relayType = $(this).attr('data-config-type'); // Get the relay type from the data attribute
        const nickname = $(this).data('config-nickname'); // Get the nickname from the data attribute

        // Set isBridgeEdit based on the relay type
        isBridgeEdit = relayType === 'bridge';

        const data = {
            nickname: nickname,
            orPort: $(this).data('config-orport'),
            contact: $(this).data('config-contact'),
            controlPort: $(this).data('config-controlport'),
            serverTransport: relayType === 'bridge' ? $(this).data('config-servertransport') : "",
            webtunnel: relayType === 'bridge' ? $(this).data('config-webtunnel') : "" // Add this line
        };

        // Send a GET request to the /bridge/running-type endpoint
        $.get("http://192.168.2.130:8081/bridge/running-type", function(runningBridgeTypes) {
            // Get the bridge type for the current nickname
            const bridgeType = runningBridgeTypes[nickname];

            console.log('Relay type:', relayType);
            console.log('Bridge type:', bridgeType);

            showModalWith(data, relayType, bridgeType);
        });
    });

    buttons.save.click(function () {
        const data = {
            nickname: configSelectors.nickname.text(),
            contact: configSelectors.contact.val(),
            controlPort: parseInt(configSelectors.controlPort.val()),
            webtunnel: configSelectors.webtunnel.val(),
        };

        $.get("http://192.168.2.130:8081/bridge/running-type", function(runningBridgeTypes) {
            data.bridgeType = runningBridgeTypes[data.nickname];

            hideModal();

            // If the bridge type is not webtunnel, set the orPort and serverTransport values
            if (data.bridgeType !== 'webtunnel') {
                data.orPort = parseInt(configSelectors.orPort.val());
                data.serverTransport = configSelectors.serverTransport.val();
            }

            // If only the contact field is being edited, skip the port availability check
            if (isBridgeEdit && data.bridgeType === 'webtunnel') {
                let url = '/update-bridge-config';
                sendUpdateRequest(url, data);
            } else {
                // Check for the uniqueness of ports
                if (!arePortsUnique(data.orPort, data.controlPort)) {
                    alert("The ports specified must be unique. Please check your entries.");
                    return;
                }

                // Now send a GET request to your new API for checking the port availability
                $.get("/update-guard-config/check-port-availability",
                    {
                        nickname: data.nickname,
                        orPort: data.orPort,
                        controlPort: data.controlPort,
                        webtunnel: data.webtunnel
                    },
                    function (response) {
                        if (response['available']) {
                            let url = isBridgeEdit ? '/update-bridge-config' : '/update-guard-config';
                            sendUpdateRequest(url, data);
                        } else {
                            alert("One or more ports are already in use. Please choose different ports.");
                        }
                    });
            }
        });
    });

    // Method to check uniqueness of ports
    function arePortsUnique(relayPort, controlPort) {
        return !(relayPort === controlPort);
    }

    buttons.cancel.click(hideModal);
});