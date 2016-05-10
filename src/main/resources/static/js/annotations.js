var apiURL = "http://localhost:8080/api";
var tokenURL = apiURL+"/oauth/token";

function getAnnotationsToken() {
    var client = "curl";
    var clientPassword = "";
    var username = document.getElementById("login").username.value;
    var password = document.getElementById("login").password.value;

    console.log("fetching annotations token for " + "username: " + username);

    var params = "grant_type=password&username="+username+"&password="+password;

    $.ajax({
        url: tokenURL,
        type: 'post',
        data: {
            grant_type: 'password',
            username: username,
            password: password
        },
        headers: {
            'Authorization': 'Basic ' + btoa(client+":"+clientPassword),
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        dataType: 'json',
        success: function(data) {
            console.log(data)
            console.log(data['access_token']);
            console.log("expires in: " + data['expires_in']);
            console.log("expires at: " + new Date(((new Date()).getTime() + data['expires_in']*1000)));
        }
    });
}