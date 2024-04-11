/**
 * Sends a POST request to the '/relay-operations-api/toggle-upnp' endpoint to enable or disable UPnP.
 * @param {boolean} enable - A boolean indicating whether to enable or disable UPnP.
 */
function toggleUPnP(enable) {
    $.ajax({
        url: '/relay-operations-api/toggle-upnp',
        type: 'POST',
        data: { enable: enable },
        success: function(response) {
            alert(response.message);
        },
        error: function(error) {
            console.error('Error:', error);
        }
    });
}

// Wait for the document to be fully loaded
$(document).ready(function() {
    $('#upnp-toggle-button').click(function() {
        const button = $(this);
        const upnpEnabled = button.data('upnp-enabled');
        if (upnpEnabled) {
            toggleUPnP(false);
            button.text('Enable UPnP for Guard Relays');
        } else {
            toggleUPnP(true);
            button.text('Disable UPnP for Guard Relays');
        }
        button.data('upnp-enabled', !upnpEnabled);
    });
});