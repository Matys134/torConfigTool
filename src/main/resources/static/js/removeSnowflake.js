$(document).ready(function() {
    $('#remove-snowflake-proxy-button').click(function() {
        $.ajax({
            type: "POST",
            url: "/relay-operations/remove-snowflake-proxy",
            success: function(response) {
                alert(response);
            }
        });
    });
});