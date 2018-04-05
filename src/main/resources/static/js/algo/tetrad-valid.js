$(document).ready(function () {
    $("#tetradForm").validate({
        rules: {
            datasetId: {
                required: true
            }, algorithm: {
                required: true
            }, score: {
                required: true
            }, test: {
                required: true
            }
        }, messages: {
            datasetId: {
                required: "Please select a dataset."
            }, algorithm: {
                required: "Please select an algorithm."
            }, score: {
                required: "Please select a score."
            }, test: {
                required: "Please select a test of independence."
            }
        }
    })
});