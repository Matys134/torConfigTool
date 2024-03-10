document.getElementById('shutdownButton').addEventListener('click', function(event) {
    event.preventDefault();
    const csrfToken = document.querySelector('meta[name="csrf-token"]').getAttribute('content');
    fetch('/shutdown', {
        method: 'POST',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    });
});