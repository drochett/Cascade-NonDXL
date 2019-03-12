//This is not used
unp.config = {};
unp.config.loggedInMessage = "Login successful, redirecting...";
unp.config.loginFailMessage = "Username / password incorrect, please try again";
unp.config.loginFailReason2Message = "Username / password incorrect, please try again.";
unp.config.loginFailReason3Message = "Session has expired. You must log in again.";
unp.config.loginFailReason4Message = "Session out of sync, server clocks may be out of sync.";
unp.config.loginFailReason5Message = "Your account has been locked out.";
unp.config.urlAfterLogin = "/";
unp.config.urlLoginNSF = "names.nsf";

var unp = {
		_firstLoad : true,
		_oldiscrollbottom : ""
		}

unp.login = function() {
	alert("test");
var config = unp.config;
var un = $("#username").val();
var p = $("#password").val();
if (un == "") {
alert('Username is mandatory');
return false;
} else if (p == "") {
alert('Password is mandatory');
return false;
};
config.urlAfterLogin = decodeURIComponent($.urlParam('redirectto'));
var loginurl = config.urlLoginNSF.split('.nsf')[0] + '.nsf?Login';
$.ajax( {
type : 'POST',
url : loginurl,
data : {
"username": un,
"password": p
},
cache : false,
encoding : "UTF-8",
beforeSend : function() {
}
}).done( function(response) {
var responseLogin = response;
responseLogin.toLowerCase();
if (getCookie('DomAuthSessId') != null || getCookie('LtpaToken') != null) {
$("#divLoginMessage").show();
dojo.byId("divLoginMessage").innerHTML = config.loggedInMessage;
dojo.byId("divLoginMessage").className = "alert alert-success";
if (config.urlAfterLogin == "" || config.urlAfterLogin == "/") {
window.location.reload()
} else {
window.location.href = config.urlAfterLogin;
};
} else {
$("#divLoginMessage").show();
dojo.byId("divLoginMessage").className = "alert alert-danger";
if (responseLogin.toLowerCase().indexOf('input name' + '=\"reasontype\"') == -1) {
dojo.byId("divLoginMessage").innerHTML = config.loginFailMessage;
} else {
var reasonFail = responseLogin.toLowerCase().substring(
responseLogin.toLowerCase().indexOf('input name' +
'=\"reasontype\"'), (responseLogin.toLowerCase().indexOf(
'input name' + '=\"reasontype\"') + 80));
var reasonNr = reasonFail.substring(reasonFail.indexOf('value') + 7, (reasonFail.indexOf('value') + 8));
if (reasonNr == "2") {
dojo.byId("divLoginMessage").innerHTML = config.loginFailReason2Message;
}
if (reasonNr == "3") {
dojo.byId("divLoginMessage").innerHTML = config.loginFailReason3Message;
}
if (reasonNr == "4") {
dojo.byId("divLoginMessage").innerHTML = config.loginFailReason4Message;
}
if (reasonNr == "5") {
dojo.byId("divLoginMessage").innerHTML = config.loginFailReason5Message;
}
}
}
});
};
