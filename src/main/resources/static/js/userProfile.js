$(document).on('click', '#collapse_link', function (e) {
    collapseAction(this);
});
$(document).ready(function () {
    $("#view_info_panel").show();
    $("#edit_info_panel").hide();

    $("#edit_info").click(function () {
        $("#view_info_panel").hide();
        $("#edit_info_panel").show();
    });
    $("#cancel_info").click(function () {
        $("#view_info_panel").show();
        $("#edit_info_panel").hide();
    });
});
$('#passwordChange').validate({
    rules: {
        currentPassword: {
            minlength: 5,
            maxlength: 25,
            nowhitespace: true,
            required: true
        },
        newPassword: {
            minlength: 5,
            maxlength: 25,
            nowhitespace: true,
            required: true
        },
        newConfirmPassword: {
            equalTo: "#newPassword"
        }
    },
    messages: {
        currentPassword: "Please enter your password",
        newPassword: "Please enter a new, valid password.",
        newConfirmPassword: "Password does not match."
    },
    highlight: function (element) {
        $(element).closest('.form-group').addClass('has-error');
    },
    unhighlight: function (element) {
        $(element).closest('.form-group').removeClass('has-error');
    },
    errorElement: 'span',
    errorClass: 'help-block'
});
