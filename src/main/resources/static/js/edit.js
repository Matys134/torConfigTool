$(document).ready(function () {
    var isBridge = false; // Add this variable to track which config is being edited
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
        isBridge = button.hasClass('edit-bridge-button'); // Set this flag when the edit button is clicked
        const data = {
            nickname: button.attr('data-config-nickname'),
            orPort: button.attr('data-config-orport'),
            contact: button.attr('data-config-contact'),
            controlPort: button.attr('data-config-controlport'),
            serverTransport: isBridge ? button.attr('data-config-servertransport') : ""
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
        };

        // Check for the uniqueness of ports
        if (!arePortsUnique(data.orPort, data.controlPort)) {
            alert("The ports specified must be unique. Please check your entries.");
            return;
        }

        if (isBridge) {
            sendUpdateRequest("/update-bridge-config", data);
        } else {
            sendUpdateRequest("/update-guard-config", data);
        }
        hideModal();

        // Now send a GET request to your new API for checking the port availability
        $.get("/update-guard-config/check-port-availability",
            {
                nickname:  data.nickname,
                orPort: data.orPort,
                controlPort: data.controlPort,
            },
            function(response) {
                if (response['available']) {
                    sendUpdateRequest("/update-guard-config", data); // Always update the guard config
                    if (isBridge) { // Only update the bridge config if the edit was for a bridge
                        sendUpdateRequest("/update-bridge-config", data);
                    }
                    hideModal();
                } else {
                    alert("One or more ports are already in use. Please choose different ports.");
                }
            });
    });

    // Method to check uniqueness of ports
    function arePortsUnique(relayPort, controlPort){
        return !(relayPort === controlPort);
    }

    buttons.cancel.click(hideModal);
});