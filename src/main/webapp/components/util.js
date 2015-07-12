function waitUntil(test, max, pause, success) {
    var error = function () {
        window.alert("Timeout waiting. Sorry.");
    };
    waitForTest(test, 0, max, pause, success, error);
}

function waitForTest(test, attempt, max, pause, success, error) {
    if (test()) {
        success()
    } else if (attempt <= max) {
        setTimeout(function () {
            waitForTest(test, attempt++, max, pause, success, error)
        }, pause);
    } else {
        error()
    }
}

function getQueryArgument(parameter) {
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (pair[0] == parameter) {
            return decodeURIComponent(pair[1]);
        }
    }
    return (false);
}
