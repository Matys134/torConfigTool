/**
 * This script is responsible for handling the file upload and deletion forms,
 * as well as refreshing Nginx.
 */
$(document).ready(function () {
    $('#upload-form').on('submit', function () {
        const files = $('#file-input').get(0).files;
        if (files.length === 0) {
            alert('Please select a file to upload');
            return false;
        }
    });
    $('#delete-form').on('submit', function () {
        return confirm('Are you sure you want to delete these files?');
    });
});

$('#refresh-nginx').on('click', function () {
    $.ajax({
        url: '/nginx-api/refresh-nginx',
        type: 'POST',
        success: function() {
            alert('Nginx refreshed successfully');
        },
        error: function() {
            alert('Failed to refresh Nginx');
        }
    });
});