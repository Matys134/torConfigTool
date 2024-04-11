/**
 * This script is responsible for updating the hostname display on the webpage.
 * It fetches the current hostnames from the '/onion-api/current-hostnames' endpoint
 * and updates the corresponding HTML elements with the fetched hostnames.
 * The hostname update is performed every 10 seconds.
 */

document.addEventListener('DOMContentLoaded', (event) => {
    /**
     * Fetches the current hostnames from the '/onion-api/current-hostnames' endpoint
     * and updates the corresponding HTML elements with the fetched hostnames.
     */
    function updateHostname() {
        fetch('/onion-api/current-hostnames')
            .then(response => response.json())
            .then(hostnames => {
                // Iterate over each hostname
                for (const [port, hostname] of Object.entries(hostnames)) {
                    // Get the HTML element corresponding to the current port
                    const element = document.getElementById('hostname-display-' + port);
                    // If the element exists, update its text content with the current hostname
                    if (element) {
                        element.textContent = hostname.toString();
                    }
                }
            })
            .catch(error => console.error('Error fetching hostname:', error)); // Log any errors that occur during the fetch operation
    }

    setInterval(updateHostname, 10000); // Set an interval to update the hostnames every 10 seconds
    updateHostname(); // Perform an initial hostname update
});