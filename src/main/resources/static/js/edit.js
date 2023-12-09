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
        isBridgeEdit = $(this).hasClass('edit-bridge-button'); // Set this flag when the edit button is clicked

        const data = {
            nickname: $(this).data('config-nickname'),
            orPort: $(this).data('config-orport'),
            contact: $(this).data('config-contact'),
            controlPort: $(this).data('config-controlport'),
            serverTransport: isBridgeEdit ? $(this).data('config-servertransport') : ""
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
                    let url = isBridgeEdit ? '/update-bridge-config' : '/update-guard-config';
                    sendUpdateRequest(url, data);
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