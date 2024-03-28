$(document).ready(function () {

    // Variable to track which config is being edited
    let isBridgeEdit = false;

    // Selectors for the various elements in the modal
    const configSelectors = {
        modal: $("#edit-modal"),
        nickname: $("#edit-nickname"),
        orPort: $("#edit-orport"),
        serverTransport: $("#edit-server-transport"),
        contact: $("#edit-contact"),
        controlPort: $("#edit-controlport"),
        webtunnelUrl: $("#edit-webtunnelurl"),
        path: $("#edit-path"),
        bandwidthRate: $("#edit-bandwidthrate"),
        webtunnelPort: $("#edit-webtunnelport"),
    };

    // Selectors for the buttons
    const buttons = {
        edit: $(".edit-button, .edit-bridge-button"), // Combined edit buttons for guard and bridge
        save: $("#save-button"),
        cancel: $("#cancel-button"),
    };

    // Function to show the modal with the data for editing
    function showModalWith(data, relayType, bridgeType) {

        // Set the values of the input fields
        configSelectors.nickname.text(data.nickname);
        configSelectors.orPort.val(data.orPort);
        configSelectors.serverTransport.val(data.serverTransport);
        configSelectors.contact.val(data.contact);
        configSelectors.controlPort.val(data.controlPort);
        configSelectors.webtunnelUrl.val(data.webtunnelUrl);
        configSelectors.path.val(data.path);
        configSelectors.bandwidthRate.val(data.bandwidthRate.split(' ')[0]);
        configSelectors.webtunnelPort.val(data.webtunnelPort);


        // Hide all fields initially
        $('#edit-form label, #edit-form input').hide();

        // Show or hide the fields based on the relay type and bridge type
        $('#edit-form [data-config-type]').each(function() {
            const configTypes = $(this).attr('data-config-type').split(' ');
            const bridgeTypes = $(this).attr('data-bridge-type') ? $(this).attr('data-bridge-type').split(' ') : [];
            const isEditable = $(this).attr('data-editable') !== 'false'; // Check if the field is editable
            if (isEditable && configTypes.includes(relayType) && (relayType !== 'bridge' || bridgeTypes.includes(bridgeType))) {
                $(this).show();
                $(this).next('input').show();
                // Populate the input fields with the current values
                const inputId = $(this).next('input').attr('id');
                if (inputId in data) {
                    const value = data[inputId.replace('edit-', '')];
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
        const configElement = $(`.list-group-item:has([data-config-nickname="${data.nickname}"])`);
        const editButton = configElement.find(`.edit-button[data-config-nickname="${data.nickname}"], .edit-bridge-button[data-config-nickname="${data.nickname}"]`);

        configElement.find("h5:contains('Nickname')").text(`Nickname: ${data.nickname}`);
        configElement.find("p:contains('ORPort')").text(`ORPort: ${data.orPort}`);
        configElement.find("p:contains('ServerTransportListenAddr')").text(`ServerTransportListenAddr: ${data.serverTransport}`);
        configElement.find("p:contains('Contact')").text(`Contact: ${data.contact}`);
        configElement.find("p:contains('Control Port')").text(`Control Port: ${data.controlPort}`);
        configElement.find("p:contains('Webtunnel URL')").text(`Webtunnel URL: ${data.webtunnelUrl}`);
        configElement.find("p:contains('Path')").text(`Path: ${data.path}`);
        configElement.find("p:contains('Bandwidth Limit')").text(`Bandwidth Limit: ${data.bandwidthRate}`);
        configElement.find("p:contains('WebTunnel Port')").text(`WebTunnel Port: ${data.webtunnelPort}`);

        editButton.data('config-orport', data.orPort);
        editButton.data('config-servertransport', data.serverTransport);
        editButton.data('config-contact', data.contact);
        editButton.data('config-controlport', data.controlPort);
        editButton.data('config-webtunnelurl', data.webtunnelUrl);
        editButton.data('config-path', data.path);
        editButton.data('config-bandwidthrate', data.bandwidthRate);
        editButton.data('config-webtunnelport', data.webtunnelPort);
    }

    function sendUpdateRequest(url, data) {
        // Check if serverTransport is defined before splitting it
        if (data.serverTransport) {
            const serverTransportParts = data.serverTransport.split(':');
            // Combine the protocol and address with the new port to form the updated serverTransport
            data.serverTransport = serverTransportParts[serverTransportParts.length - 1];
        }

        $.ajax({
            type: "POST",
            url: url,
            contentType: "application/json",
            data: JSON.stringify(data),
            success: function (response) {
                if (response.status === "success") {
                    // Update the view with the new configuration
                    updateView(data);
                } else {
                    alert("Failed to update configuration.");
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error("AJAX request failed: ", textStatus, ", ", errorThrown);
                console.error("Response text: ", jqXHR.responseText);
                console.error("Status code: ", jqXHR.status);
            }
        });
    }

    buttons.edit.click(function () {
        const relayType = $(this).attr('data-config-type'); // Get the relay type from the data attribute
        const nickname = $(this).data('config-nickname'); // Get the nickname from the data attribute

        // Set isBridgeEdit based on the relay type
        isBridgeEdit = relayType === 'bridge';

        // Get the "WebTunnel Link" string
        const webTunnelLink = $(this).parent().siblings("p:contains('WebTunnel Link')").text();

        // Extract the webtunnelPort from the "WebTunnel Link" string
        const webTunnelLinkParts = webTunnelLink.split(' ');
        const webTunnelPart = webTunnelLinkParts.find(part => part.includes('webtunnel'));
        const webTunnelPort = webTunnelPart.split(':')[1];

        const data = {
            nickname: nickname,
            orPort: $(this).data('config-orport'),
            contact: $(this).data('config-contact'),
            controlPort: $(this).data('config-controlport'),
            serverTransport: relayType === 'bridge' ? $(this).data('config-servertransport') : "",
            webtunnelUrl: relayType === 'bridge' ? $(this).data('config-webtunnelurl') : "",
            path: relayType === 'bridge' ? $(this).data('config-path') : "",
            bandwidthRate: $(this).data('config-bandwidthrate'),
            webtunnelPort: webTunnelPort,
        };

        $.get("/server-ip", function(serverIp) {
        $.get("https://" + serverIp + ":8443/bridge-api/bridges/configured-type", function(runningBridgeTypes) {
            // Get the bridge type for the current nickname
            const bridgeType = runningBridgeTypes[nickname];

            showModalWith(data, relayType, bridgeType);
        });
        });
    });

    buttons.save.click(function () {
        const bandwidth = parseInt(configSelectors.bandwidthRate.val());
        if (bandwidth !== 0 && bandwidth < 75) {
            alert("Bandwidth must be 0 or 75 and larger");
            return; // stop the function if the validation fails
        }

        const data = {
            nickname: configSelectors.nickname.text(),
            contact: configSelectors.contact.val(),
            controlPort: parseInt(configSelectors.controlPort.val()),
            webtunnelUrl: configSelectors.webtunnelUrl.val(),
            path: configSelectors.path.val(),
            bandwidthRate: configSelectors.bandwidthRate.val(),
            webtunnelPort: parseInt(configSelectors.webtunnelPort.val()),
        };

        $.get("/server-ip", function(serverIp) {
        $.get("https://" + serverIp + ":8443/bridge-api/bridges/configured-type", function(runningBridgeTypes) {
            data.bridgeType = runningBridgeTypes[data.nickname];
            data.serverTransport = configSelectors.serverTransport.val();

            hideModal();

            // If the bridge type is not webtunnel, set the orPort and serverTransport values
            if (data.bridgeType !== 'webtunnel') {
                data.orPort = parseInt(configSelectors.orPort.val());
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
    });

    // Method to check uniqueness of ports
    function arePortsUnique(relayPort, controlPort) {
        return !(relayPort === controlPort);
    }

    buttons.cancel.click(hideModal);
});