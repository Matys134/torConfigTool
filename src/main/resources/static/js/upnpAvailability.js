/**
 * This script is responsible for checking the availability of UPnP (Universal Plug and Play)
 * on the user's network and enabling or disabling the UPnP toggle button accordingly.
 */
$(document).ready(function() {
    $.get("/relay-operations-api/upnp-availability", function(upnpAvailable) {
        if (upnpAvailable) {
            $('#upnp-toggle-button').prop('disabled', false);
        } else {
            $('#upnp-toggle-button').prop('disabled', true);
        }
    });
});