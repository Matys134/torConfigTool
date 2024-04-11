/**
 * Function to toggle the display of different fields based on the selected bridge type.
 * It retrieves the selected bridge type and the different field elements.
 * It hides all fields initially and shows the submit button.
 * It removes the 'required' attribute from the 'bridgePort' and 'bridgeTransportListenAddr' fields initially.
 * Then, based on the selected bridge type, it shows specific fields and adds the 'required' attribute to certain fields.
 */
function toggleBridgeFields() {
    const bridgeType = document.getElementById("bridgeType").value;
    const commonFields = document.getElementById("commonFields");
    const bridgeFields = document.getElementById("bridgeFields");
    const webtunnelFields = document.getElementById("webtunnelFields");
    const snowflakeFields = document.getElementById("snowflakeFields");
    const submitButton = document.querySelector("#bridgeForm button[type='submit']");

    // Retrieve the form elements
    const bridgePort = document.getElementById("bridgePort");
    const bridgeTransportListenAddr = document.getElementById("bridgeTransportListenAddr");

    // Hide all fields initially
    commonFields.style.display = "none";
    bridgeFields.style.display = "none";
    webtunnelFields.style.display = "none";
    snowflakeFields.style.display = "none";

    // Show the submit button initially
    submitButton.style.display = "block";

    // Remove 'required' attributes initially
    bridgePort.removeAttribute("required");
    bridgeTransportListenAddr.removeAttribute("required");

    // Show specific fields and add 'required' attributes based on the selected bridge type
    if (bridgeType === "obfs4" || bridgeType === "webtunnel") {
        commonFields.style.display = "block";
    }

    if (bridgeType === "obfs4") {
        bridgeFields.style.display = "block";
        bridgePort.setAttribute("required", "");
        bridgeTransportListenAddr.setAttribute("required", "");
    } else if (bridgeType === "webtunnel") {
        webtunnelFields.style.display = "block";
    } else if (bridgeType === "snowflake") {
        snowflakeFields.style.display = "block";
        submitButton.style.display = "none";
    }
}

// Call the function to initialize fields when the page loads
window.onload = toggleBridgeFields;