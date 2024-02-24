function toggleBridgeFields() {
    const bridgeType = document.getElementById("bridgeType").value;
    const commonFields = document.getElementById("commonFields");
    const bridgeFields = document.getElementById("bridgeFields");
    const webtunnelFields = document.getElementById("webtunnelFields");
    const snowflakeFields = document.getElementById("snowflakeFields");

    // Retrieve the form elements
    const bridgePort = document.getElementById("bridgePort");
    const bridgeTransportListenAddr = document.getElementById("bridgeTransportListenAddr");

    // Hide all fields initially
    commonFields.style.display = "none";
    bridgeFields.style.display = "none";
    webtunnelFields.style.display = "none";
    snowflakeFields.style.display = "none";

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
    }
}

// Call the function to initialize fields when the page loads
window.onload = toggleBridgeFields;