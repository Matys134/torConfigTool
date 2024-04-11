/**
 * This script is responsible for hiding empty configuration sections on the page.
 * It checks if the configuration sections for guard relays, bridge relays, and onion relays are empty.
 * If a section is empty, it hides the corresponding heading.
 */
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