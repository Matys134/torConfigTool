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

document.getElementById("runSnowflakeProxyButton").addEventListener("click", function () {
    $.ajax({
        url: '/bridge-api/setup-snowflake-proxy',
        type: 'POST',
        success: function (response) {
            alert("Snowflake proxy started successfully");
        },
        error: function (error) {
            alert("Error starting snowflake proxy: " + error.responseText);
        }
    });
});