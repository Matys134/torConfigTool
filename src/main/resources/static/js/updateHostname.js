document.addEventListener('DOMContentLoaded', (event) => {
    function updateHostname() {
        fetch('/onion-api/current-hostnames')
            .then(response => response.json())
            .then(hostnames => {
                for (const [port, hostname] of Object.entries(hostnames)) {
                    const element = document.getElementById('hostname-display-' + port);
                    if (element) {
                        element.textContent = hostname.toString();
                    }
                }
            })
            .catch(error => console.error('Error fetching hostname:', error));
    }

    setInterval(updateHostname, 10000);
    updateHostname();
});