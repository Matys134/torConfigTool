$(document).ready(function () {
    const configSelectors = {
        modal: $("#edit-modal"),
        nickname: $("#edit-nickname"),
        orPort: $("#edit-orport"),
        serverTransport: $("#edit-server-transport"),
        contact: $("#edit-contact"),
        controlPort: $("#edit-controlport"),
        socksPort: $("#edit-socksport"),
    };

    const buttons = {
        edit: $(".edit-button, .edit-bridge-button"), // Combined edit buttons for guard and bridge
        save: $("#save-button"),
        cancel: $("#cancel-button"),
    };

    function showModalWith(values) {
        for (let key in values) {
            configSelectors[key].val(values[key]);
        }

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
        configElement.find(".config-socksport").text(`Socks Port: ${data.socksPort}`);
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
        const button = $(this);
        const data = {
            nickname: button.attr('data-config-nickname'),
            orPort: button.attr('data-config-orport'),
            contact: button.attr('data-config-contact'),
            socksPort: button.attr('data-config-socksport'),
            controlPort: button.attr('data-config-controlport'),
            serverTransport: button.hasClass('edit-bridge-button') ? button.attr('data-config-servertransport') : ""
        };

        showModalWith(data);
    });

    buttons.save.click(function () {
        const data = {
            nickname: configSelectors.nickname.val(),
            orPort: parseInt(configSelectors.orPort.val()),
            serverTransport: configSelectors.serverTransport.val(),
            contact: configSelectors.contact.val(),
            controlPort: parseInt(configSelectors.controlPort.val()),
            socksPort: parseInt(configSelectors.socksPort.val()),
        };

        // Check for the uniqueness of ports
        if (!arePortsUnique(data.orPort, data.controlPort, data.socksPort)) {
            alert("The ports specified must be unique. Please check your entries.");
            return;
        }

        // Now send a GET request to your new API for checking the port availability
        $.get("/update-guard-config/check-port-availability",
            { orPort: data.orPort, controlPort: data.controlPort, socksPort: data.socksPort },
            function(response) {
                if (response['available']) {
                    sendUpdateRequest("/update-guard-config", data);
                    sendUpdateRequest("/update-bridge-config", data);
                    hideModal();
                } else {
                    alert("One or more ports are already in use. Please choose different ports.");
                }
            });
    });

    // Method to check uniqueness of ports
    function arePortsUnique(relayPort, controlPort, socksPort){
        return !(relayPort === controlPort || relayPort === socksPort || controlPort === socksPort);
    }

    buttons.cancel.click(hideModal);
});