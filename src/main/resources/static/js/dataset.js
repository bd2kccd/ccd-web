$(document).ready(function () {
    $("[data-toggle=tooltip]").tooltip();
});

$('#confirm-delete').on('show.bs.modal', function (e) {
    $(this).find('.btn-ok').attr('href', $(e.relatedTarget).data('href'));
});

$('#fileInfo').on('show.bs.modal', function (event) {
    var link = $(event.relatedTarget);
    var filename = link.data('filename');

    var modal = $(this);
    modal.find('.modal-title').text(filename);
    modal.find('#fileInfoFrame').attr('src', 'fs/fileInfo?file=' + filename);
});