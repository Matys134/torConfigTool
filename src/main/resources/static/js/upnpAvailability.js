$(document).ready(function() {
    $.get("/relay-operations-api/upnp-availability", function(upnpAvailable) {
        if (upnpAvailable) {
            $('#upnp-toggle-button').prop('disabled', false);
        } else {
            $('#upnp-toggle-button').prop('disabled', true);
        }
    });
});