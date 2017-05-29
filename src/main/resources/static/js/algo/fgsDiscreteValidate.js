$(document).ready(function () {
    $("#algoForm").validate({
        rules: {
            dataset: {
                required: true
            },
            structurePrior: {
                required: true,
                number: true,
                min: 1.0
            },
            samplePrior: {
                required: true,
                number: true,
                min: 1.0
            },
            maxDegree: {
                required: true,
                number: true,
                min: -1
            },
            jvmMaxMem: {
                required: true,
                number: true,
                min: 0,
                max: 128
            }
        },
        messages: {
            dataset: {
                required: "Please select a dataset."
            },
            structurePrior: {
                required: "Please set the structure prior.",
                min: "Must be at least 1.0."
            },
            samplePrior: {
                required: "Please set the sample prior.",
                min: "Must be at least 1.0."
            },
            maxDegree: {
                required: "Please select the search max degree.",
                min: "Must be at least -1."
            },
            jvmMaxMem: {
                required: "Must be a number between 0 and 128.",
                min: "Must be at least 0.",
                max: "Must be at most 128."
            }
        },
        highlight: function (element) {
            $(element).closest('.form-group').addClass('has-error');
        },
        unhighlight: function (element) {
            $(element).closest('.form-group').removeClass('has-error');
        },
        errorElement: 'span',
        errorClass: 'help-block',
        errorPlacement: function (error, element) {
            if (element.parent('.input-group').length) {
                error.insertAfter(element.parent());
            } else {
                error.insertAfter(element);
            }
        }
    });
});