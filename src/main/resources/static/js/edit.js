$(document).ready(function () {

    var isBridgeEdit = false; // Add this variable to track which config is being edited

    const configSelectors = {
        modal: $("#edit-modal"),
        nickname: $("#edit-nickname"),
        orPort: $("#edit-orport"),
        serverTransport: $("#edit-server-transport"),
        contact: $("#edit-contact"),
        controlPort: $("#edit-controlport"),
    };

    const buttons = {
        edit: $(".edit-button, .edit-bridge-button"), // Combined edit buttons for guard and bridge
        save: $("#save-button"),
        cancel: $("#cancel-button"),
    };

    // Function to show the modal with the data for editing
    function showModalWith(data, relayType) {
        console.log(data);
        // Set the values of the input fields
        configSelectors.nickname.text(data.nickname);
        configSelectors.orPort.val(data.orPort);
        configSelectors.serverTransport.val(data.serverTransport);
        configSelectors.contact.val(data.contact);
        configSelectors.controlPort.val(data.controlPort);

        // Show or hide the input fields based on whether the corresponding data attribute has a value
        configSelectors.nickname.closest('label').toggle(!!data.nickname);
        configSelectors.orPort.closest('label').toggle(!!data.orPort);
        configSelectors.contact.closest('label').toggle(!!data.contact);
        configSelectors.controlPort.closest('label').toggle(!!data.controlPort);

        // Show or hide the serverTransport field based on the relay type
        if (relayType === 'bridge') {
            configSelectors.serverTransport.closest('label').show();
        } else {
            configSelectors.serverTransport.closest('label').hide();
        }

        // Set the data-config-type attribute of each field to the relay type
        $('#edit-form [data-config-type]').each(function() {
            $(this).toggle($(this).attr('data-config-type').split(' ').includes(relayType));
        });

        // Check the bridge type and show only relevant fields
        if (data.bridgeType === 'obfs4') {
            configSelectors.orPort.closest('label').show();
            configSelectors.serverTransport.closest('label').show();
            configSelectors.contact.closest('label').show();
            configSelectors.controlPort.closest('label').show();
        } else if (data.bridgeType === 'webtunnel') {
            configSelectors.orPort.closest('label').hide();
            configSelectors.serverTransport.closest('label').hide();
            configSelectors.contact.closest('label').show();
            configSelectors.controlPort.closest('label').hide();
        } else if (data.bridgeType === 'snowflake') {
            // Snowflake can't be edited
            configSelectors.orPort.closest('label').hide();
            configSelectors.serverTransport.closest('label').hide();
            configSelectors.contact.closest('label').hide();
            configSelectors.controlPort.closest('label').hide();
        }

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
    }

    function sendUpdateRequest(url, data) {
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
        const bridgeType = $(this).data('config-bridge-type'); // Get the bridge type from the data attribute

        console.log('Relay type:', relayType);
        console.log('Bridge type:', bridgeType); // Add this line

        const data = {
            nickname: $(this).data('config-nickname'),
            orPort: $(this).data('config-orport'),
            contact: $(this).data('config-contact'),
            controlPort: $(this).data('config-controlport'),
            serverTransport: relayType === 'bridge' ? $(this).data('config-servertransport') : "",
            bridgeType: bridgeType // Use the retrieved bridge type
        };

        showModalWith(data, relayType);
    });

    buttons.save.click(function () {
        const data = {
            nickname: configSelectors.nickname.text(), // Use .text() instead of .val() as nickname is now a <p> element
            orPort: parseInt(configSelectors.orPort.val()),
            serverTransport: configSelectors.serverTransport.val(),
            contact: configSelectors.contact.val(),
            controlPort: parseInt(configSelectors.controlPort.val()),
        };

        // Check for the uniqueness of ports
        if (!arePortsUnique(data.orPort, data.controlPort)) {
            alert("The ports specified must be unique. Please check your entries.");
            return;
        }

        hideModal();

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
                    hideModal();
                } else {
                    alert("One or more ports are already in use. Please choose different ports.");
                }
            });
    });

    // Method to check uniqueness of ports
    function arePortsUnique(relayPort, controlPort) {
        return !(relayPort === controlPort);
    }

    buttons.cancel.click(hideModal);
});