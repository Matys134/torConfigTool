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
                        // Remove the corresponding HTML element
                        button.closest('.list-group-item').remove();

                        // Make an AJAX call to revert the Nginx configuration
                        $.ajax({
                            type: "POST",
                            url: "/bridge/revert-nginx-config",
                            success: function (response) {
                                if (response.status === 200) {
                                    alert("Nginx configuration reverted successfully!");
                                } else {
                                    alert("Failed to revert Nginx configuration.");
                                }
                            }
                        });
                    } else {
                        alert("Failed to remove Torrc file and DataDirectory.");
                    }
                }
            });
        }
    });
});