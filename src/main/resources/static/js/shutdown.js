document.getElementById('shutdownButton').addEventListener('click', function(event) {
    event.preventDefault();
    fetch('/shutdown', {
        method: 'POST'
    });
});