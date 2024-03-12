document.getElementById("runSnowflakeProxyButton").addEventListener("click", function () {
    $.ajax({
        url: '/bridge-api/run-snowflake-proxy',
        type: 'POST',
        success: function (response) {
            alert("Snowflake proxy started successfully");
        },
        error: function (error) {
            alert("Error starting snowflake proxy: " + error.responseText);
        }
    });
});