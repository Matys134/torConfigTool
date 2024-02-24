// JavaScript function to show/hide bandwidth field based on checkbox
document.getElementById("includeBandwidth").addEventListener("change", function () {
    const bandwidthField = document.getElementById("bandwidthField");
    bandwidthField.style.display = this.checked ? "block" : "none";
});

// JavaScript function to show/hide bridge bandwidth field based on checkbox
document.getElementById("includeBridgeBandwidth").addEventListener("change", function () {
    const bridgeBandwidthField = document.getElementById("bridgeBandwidthField");
    bridgeBandwidthField.style.display = this.checked ? "block" : "none";
});

// JavaScript function to switch between tabs
