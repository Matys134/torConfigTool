$(document).ready(function() {
    $('#start-snowflake-proxy-button').click(function() {
        $.ajax({
            type: "POST",
            url: "/relay-operations/start-snowflake-proxy",
            success: function(response) {
                alert(response);
            }
        });
    });

    $('#stop-snowflake-proxy-button').click(function() {
        $.ajax({
            type: "POST",
            url: "/relay-operations/stop-snowflake-proxy",
            success: function(response) {
                alert(response);
            }
        });
    });
});