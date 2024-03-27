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