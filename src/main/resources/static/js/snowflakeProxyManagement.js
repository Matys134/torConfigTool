/**
 * This file contains JavaScript code for managing a Snowflake proxy.
 * It includes functions to start, stop, remove, and run the Snowflake proxy.
 * Each function is triggered by a button click event.
 */

// Wait for the document to be ready
$(document).ready(function() {
    // When the 'start-snowflake-proxy-button' is clicked
    $('#start-snowflake-proxy-button').click(function() {
        // Send a POST request to the '/relay-operations-api/start-snowflake-proxy' endpoint
        $.ajax({
            type: "POST",
            url: "/relay-operations-api/start-snowflake-proxy",
            success: function(response) {
                // On success, alert the response
                alert(response);
            }
        });
    });

    // When the 'stop-snowflake-proxy-button' is clicked
    $('#stop-snowflake-proxy-button').click(function() {
        // Send a POST request to the '/relay-operations-api/stop-snowflake-proxy' endpoint
        $.ajax({
            type: "POST",
            url: "/relay-operations-api/stop-snowflake-proxy",
            success: function(response) {
                // On success, alert the response
                alert(response);
            }
        });
    });
});

// Wait for the document to be ready
$(document).ready(function() {
    // When the 'remove-snowflake-proxy-button' is clicked
    $('#remove-snowflake-proxy-button').click(function() {
        // Send a POST request to the '/relay-operations-api/remove-snowflake-proxy' endpoint
        $.ajax({
            type: "POST",
            url: "/relay-operations-api/remove-snowflake-proxy",
            success: function(response) {
                // On success, alert the response
                alert(response);
            }
        });
    });
});

// When the 'runSnowflakeProxyButton' is clicked
document.getElementById("runSnowflakeProxyButton").addEventListener("click", function () {
    // Send a POST request to the '/bridge-api/setup-snowflake-proxy' endpoint
    $.ajax({
        url: '/bridge-api/setup-snowflake-proxy',
        type: 'POST',
        success: function (response) {
            // On success, alert that the Snowflake proxy started successfully
            alert("Snowflake proxy started successfully");
        },
        error: function (error) {
            // On error, alert the error message
            alert("Error starting snowflake proxy: " + error.responseText);
        }
    });
});