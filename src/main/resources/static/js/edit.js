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

    // Function to show the modal with the data for editing the relay configuration file based on the relay type (guard or bridge). We can do it by hiding empty fields and only showing the ones that are needed.
    function showModalWith(data, relayType) {
        // Set the modal title
        configSelectors.modal.find(".modal-title").text(`Edit ${relayType} configuration`);

        // Set the nickname
        configSelectors.nickname.val(data.nickname);

        // Set the ORPort
        configSelectors.orPort.val(data.orPort);

        // Set the ServerTransportListenAddr
        if (relayType === 'bridge') {
            configSelectors.serverTransport.val(data.serverTransport);
            configSelectors.serverTransport.parent().show();
            isBridgeEdit = true;
        } else {
            configSelectors.serverTransport.parent().hide();
            isBridgeEdit = false;
        }

        // Set the Contact
        configSelectors.contact.val(data.contact);

        // Set the Control Port
        configSelectors.controlPort.val(data.controlPort);

        // Show the modal
        configSelectors.modal.show();
    }

    function hideModal() {
        configSelectors.modal.hide();
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
        console.log('Relay type:', relayType); // Add this line

        const data = {
            nickname: $(this).data('config-nickname'),
            orPort: $(this).data('config-orport'),
            contact: $(this).data('config-contact'),
            controlPort: $(this).data('config-controlport'),
            serverTransport: relayType === 'bridge' ? $(this).data('config-servertransport') : ""
        };

        showModalWith(data, relayType);
    });

    buttons.save.click(function () {
        const data = {
            nickname: configSelectors.nickname.val(),
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