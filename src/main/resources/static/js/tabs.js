// JavaScript function to show/hide bandwidth field based on checkbox
document.getElementById("includeBandwidth").addEventListener("change", function () {
    var bandwidthField = document.getElementById("bandwidthField");
    bandwidthField.style.display = this.checked ? "block" : "none";
});

// JavaScript function to show/hide bridge bandwidth field based on checkbox
document.getElementById("includeBridgeBandwidth").addEventListener("change", function () {
    var bridgeBandwidthField = document.getElementById("bridgeBandwidthField");
    bridgeBandwidthField.style.display = this.checked ? "block" : "none";
});

// JavaScript function to switch between tabs
function openTab(evt, tabName) {
    var i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    document.getElementById(tabName).style.display = "block";
    evt.currentTarget.className += " active";
}

$(document).ready(function() {
    // Fetch the limit state and the count of guard relays
    $.get("/guard/limit-reached", function(data) {
        // Access the guardLimitReached property of the returned data
        if (data.guardLimitReached) {
            // Disable the bridge and onion tabs
            document.getElementById('bridge-tab').disabled = true;
            document.getElementById('onion-tab').disabled = true;
        }
    });
});
