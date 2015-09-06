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