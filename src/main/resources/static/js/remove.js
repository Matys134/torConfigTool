$(document).ready(function () {
    const removeButton = $(".remove-button");

    removeButton.click(function () {
        const button = $(this);
        const data = {
            relayNickname: button.attr('data-config-nickname'),
            relayType: button.attr('data-config-type')
        };

        if (confirm("Are you sure you want to remove the Torrc file and DataDirectory for this relay?")) {
            $.ajax({
                type: "POST",
                url: "/relay-operations/remove",
                data: data,
                success: function (response) {
                    if (response.success) {
                        // Optionally update the view or perform other actions on success
                        alert("Torrc file and DataDirectory removed successfully!");
                    } else {
                        alert("Failed to remove Torrc file and DataDirectory.");
                    }
                }
            });
        }
    });

});
