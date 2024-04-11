/**
 * This file contains JavaScript code for managing the display of bandwidth fields.
 * It includes event listeners for changes in checkboxes to toggle the display of bandwidth fields.
 */

// Event listener for the 'includeBandwidth' checkbox
document.getElementById("includeBandwidth").addEventListener("change", function () {
    // Get the 'bandwidthField' element
    const bandwidthField = document.getElementById("bandwidthField");
    // If the checkbox is checked, display the 'bandwidthField', otherwise hide it
    bandwidthField.style.display = this.checked ? "block" : "none";
});

// Event listener for the 'includeBridgeBandwidth' checkbox
document.getElementById("includeBridgeBandwidth").addEventListener("change", function () {
    // Get the 'bridgeBandwidthField' element
    const bridgeBandwidthField = document.getElementById("bridgeBandwidthField");
    // If the checkbox is checked, display the 'bridgeBandwidthField', otherwise hide it
    bridgeBandwidthField.style.display = this.checked ? "block" : "none";
});