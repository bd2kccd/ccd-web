$(document).ready(function () {
    $('#accept').prop('disabled', !$('#agree').prop('checked'));

    $('#agree').click(function () {
        $('#accept').prop('disabled', !$(this).prop('checked'));
    })
});
