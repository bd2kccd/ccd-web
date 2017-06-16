$('#passwordChange').validate({
    rules: {
        currentPassword: {
            minlength: 4,
            maxlength: 10,
            nowhitespace: true,
            required: true
        },
        newPassword: {
            minlength: 4,
            maxlength: 10,
            nowhitespace: true,
            required: true
        },
        newConfirmPassword: {
            equalTo: "#newPassword"
        }
    },
    messages: {
        currentPassword: "Please enter your current password.",
        newPassword: "Please enter a new password (4-10 chars).",
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