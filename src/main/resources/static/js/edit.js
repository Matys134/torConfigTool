$(document).ready(function () {
    const editModal = $("#edit-modal");
    const editNickname = $("#edit-nickname");
    const editOrPort = $("#edit-orport");
    const editContact = $("#edit-contact");
    const editControlPort = $("#edit-controlport");
    const editSocksPort = $("#edit-socksport");

    const editButton = $(".edit-button");
    const saveButton = $("#save-button");
    const cancelButton = $("#cancel-button");

    function showEditModal(nickname, orport, contact) {
        editNickname.val(nickname);
        editOrPort.val(orport);
        editContact.val(contact);
        editModal.show();
    }

    function hideEditModal() {
        editModal.hide();
    }

    editButton.click(function () {
        const button = $(this);
        const data = button.data();

        showEditModal(data.configNickname, data.configOrport, data.configContact);
    });

    saveButton.click(function () {
        const data = {
            nickname: editNickname.val(),
            orPort: editOrPort.val(),
            contact: editContact.val(),
            controlPort: editControlPort.val(),
            socksPort: editSocksPort.val(),
        };

        $.ajax({
            type: "POST",
            url: "/update-guard-config",
            contentType: "application/json",
            data: JSON.stringify(data),
            success: function (data) {
                if (data.success) {
                    // Update the view with the new configuration
                } else {
                    alert("Failed to update configuration.");
                }
            }
        });

        hideEditModal();
    });

    cancelButton.click(function () {
        hideEditModal();
    });
});
