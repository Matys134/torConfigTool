$(document).ready(function () {
    // Function to show the edit modal
    function showEditModal(nickname, orport, contact) {
        $("#edit-nickname").val(nickname);
        $("#edit-orport").val(orport);
        $("#edit-contact").val(contact);
        $("#edit-controlport").val(controlport);
        $("#edit-socksport").val(socksport);
        $("#edit-modal").show();
    }

    // Function to hide the edit modal
    function hideEditModal() {
        $("#edit-modal").hide();
    }

    // Handle the "Edit" button clicks
    $(".edit-button").click(function () {
        const nickname = $(this).data("config-nickname");
        const orport = $(this).data("config-orport");
        const contact = $(this).data("config-contact");
        const controlport = $(this).data("config-controlport");
        const socksport = $(this).data("config-socksport");

        showEditModal(nickname, orport, contact);
    });

    // Handle the "Save" button click
    $("#save-button").click(function () {
        const editedNickname = $("#edit-nickname").val();
        const editedOrPort = $("#edit-orport").val();
        const editedContact = $("#edit-contact").val();
        const editedControlPort = $("#edit-controlport").val();
        const editedSocksPort = $("#edit-socksport").val();

        // Send an AJAX request to the server to update the configuration
        $.ajax({
            type: "POST",
            url: "/update-guard-config",
            data: {
                nickname: editedNickname,
                orPort: editedOrPort,
                contact: editedContact,
                controlPort: editedControlPort,
                socksPort: editedSocksPort
            },
            success: function (data) {
                // Check if the configuration was successfully updated
                if (data.success) {
                    // Update the view with the new configuration
                    // This is where you can update the view with the updated configuration
                } else {
                    alert("Failed to update configuration.");
                }
            }
        });

        hideEditModal();
    });

    // Handle the "Cancel" button click
    $("#cancel-button").click(function () {
        hideEditModal();
    });
});
