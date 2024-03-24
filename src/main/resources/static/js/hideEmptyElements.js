$(document).ready(function() {
    if ($('#guard-configs .list-group-item').length === 0) {
        $('h2:contains("Guard Relay Configurations")').hide();
    }
    if ($('#bridge-configs .list-group-item').length === 0) {
        $('h2:contains("Bridge Relay Configurations")').hide();
    }
    if ($('#onion-configs .list-group-item').length === 0) {
        $('h2:contains("Onion Relay Configurations")').hide();
    }
});