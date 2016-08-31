$(document).ready(function () {
    $('[data-toggle=tooltip]').tooltip();

    $('#file_table').DataTable({
        'order': [[3, 'desc']],
        responsive: true,
        'lengthMenu': [[10, 25, 50, 100, -1], [10, 25, 50, 100, 'All']],
        'columnDefs': [
            {'orderable': false, 'bSearchable': false, 'className': 'dt-body-center', 'targets': 0},
            {'orderable': false, 'bSearchable': false, 'targets': 2},
            {'orderable': false, 'bSearchable': false, 'targets': 4}
        ]
    });
});
$('#select_all').change(function () {
    $('input[type="checkbox"]').not('[disabled]').prop('checked', $(this).prop('checked'));
});
$('#delete_btn').click(function () {
    if ($('input[type="checkbox"]:checkbox:checked').not('#select_all').length === 0) {
        $('#errorSelection').modal('show');
    } else {
        $('#confirm_delete').modal('show');
    }
});
$('#compare_btn').click(function () {
    if ($('input[type="checkbox"]:checkbox:checked').not('#select_all').length === 0) {
        $('#errorSelection').modal('show');
    } else {
        if ($('input[type="checkbox"]:checkbox:checked[value*=error]').length === 0) {
            var url = $('#resultAction').attr('action') + '/comparison/compare';
            $('#resultAction').attr('action', url);
            $('form').submit();
        } else {
            $('#selectErrorFile').modal('show');
        }
    }
});
$('#ok_delete').click(function () {
    var url = $('#resultAction').attr('action') + '/delete';
    $('#resultAction').attr('action', url);
    $('form').submit();
});
