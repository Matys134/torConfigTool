$(document).ready(function () {
    const editModal = $("#edit-modal");
    const editNickname = $("#edit-nickname");
    const editOrPort = $("#edit-orport");
    const editServerTransport = $("#edit-server-transport");
    const editContact = $("#edit-contact");
    const editControlPort = $("#edit-controlport");
    const editSocksPort = $("#edit-socksport");

    const editButton = $(".edit-button, .edit-bridge-button"); // Combined edit buttons for guard and bridge
    const saveButton = $("#save-button");
    const cancelButton = $("#cancel-button");

    function showEditModal(nickname, orport, serverTransport, contact, controlport, socksport) {
        editNickname.val(nickname);
        editOrPort.val(orport);
        editServerTransport.val(serverTransport);
        editContact.val(contact);
        editControlPort.val(controlport);
        editSocksPort.val(socksport);
        editModal.show();
    }

    function hideEditModal() {
        editModal.hide();
    }

    editButton.click(function () {
        const button = $(this);
        const data = button.data();

        showEditModal(data.configNickname, data.configOrport, data.configServerTransport, data.configContact, data.configControlport, data.configSocksport);
    });

    saveButton.click(function () {
        const data = {
            nickname: editNickname.val(),
            orPort: editOrPort.val(),
            serverTransport: editServerTransport.val(),
            contact: editContact.val(),
            controlPort: editControlPort.val(),
            socksPort: editSocksPort.val(),
        };

        $.ajax({
            type: "POST",
            url: "/update-guard-config",
            contentType: "application/json",
            data: JSON.stringify(data),
            success: function (response) {
                if (response.success) {
                    // Update the view with the new configuration, if needed
                    // For example, you can update the displayed values in the UI
                    const nickname = data.nickname;
                    const configElement = $(`[data-config-nickname="${nickname}"]`);
                    configElement.find(".config-orport").text(`ORPort: ${data.orPort}`);
                    configElement.find(".config-server-transport").text(`ServerTransportListenAddr: ${data.serverTransport}`);
                    configElement.find(".config-contact").text(`Contact: ${data.contact}`);
                    configElement.find(".config-controlport").text(`Control Port: ${data.controlPort}`);
                    configElement.find(".config-socksport").text(`Socks Port: ${data.socksPort}`);
                } else {
                    alert("Failed to update configuration.");
                }
            }
        });

        $.ajax({
            type: "POST",
            url: "/update-bridge-config",
            contentType: "application/json",
            data: JSON.stringify(data),
            success: function (response) {
                if (response.success) {
                    // Update the view with the new configuration, if needed
                    // For example, you can update the displayed values in the UI
                    const nickname = data.nickname;
                    const configElement = $(`[data-config-nickname="${nickname}"]`);
                    configElement.find(".config-orport").text(`ORPort: ${data.orPort}`);
                    configElement.find(".config-server-transport").text(`ServerTransportListenAddr: ${data.serverTransport}`);
                    configElement.find(".config-contact").text(`Contact: ${data.contact}`);
                    configElement.find(".config-controlport").text(`Control Port: ${data.controlPort}`);
                    configElement.find(".config-socksport").text(`Socks Port: ${data.socksPort}`);
                } else {
                    alert("Failed to update configuration.");
                }
            }
        })

        hideEditModal();
    });

    cancelButton.click(function () {
        hideEditModal();
    });
});
