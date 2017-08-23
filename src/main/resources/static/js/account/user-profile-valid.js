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
        currentPassword: {
            required: "Please enter the current password.",
            nowhitespace: "Space is not allowed.",
            minlength: "Requires minimum 4 chars.",
            maxlength: "Number of chars exceed."
        },
        newPassword: {
            required: "Please enter a new password.",
            nowhitespace: "Space is not allowed.",
            minlength: "Requires minimum 4 chars.",
            maxlength: "Number of chars exceed."
        },
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