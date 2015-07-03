var createTimer = function (totalTime, onTrigger, onCompleted) {
    var Timer;
    var incrementTime = 70;
    var currentTime = totalTime;
    var before, now, elapsedTime;
    var updateTimer = function () {
        now = new Date();

        if (currentTime == 0) {
            Timer.stop();
            onCompleted();
            return;
        }
        elapsedTime = (now.getTime() - before.getTime());
        currentTime -= elapsedTime / 10;

        if (currentTime < 0) currentTime = 0;

        onTrigger(currentTime);

        before = new Date();
    };

    var Object = {};
    Object.start = function () {
        Timer = $.timer(updateTimer, incrementTime, true);
        before = new Date();
    };
    Object.toggle = function () {
        Timer.toggle();
        before = new Date();
    };
    Object.stop = function () {
        Timer.stop();
    };
    Object.reset = function () {
        currentTime = totalTime;
    };
    Object.setCurrentTime = function (time) {
        currentTime = time;
        onTrigger(currentTime);
    };
    return Object;
};

// Common functions
function pad(number, length) {
    var str = '' + number;
    while (str.length < length) {
        str = '0' + str;
    }
    return str;
}
function formatTime(time) {
    var min = parseInt(time / 6000),
        sec = parseInt(time / 100) - (min * 60),
        hundredths = pad(time - (sec * 100) - (min * 6000), 2);
    return (min > 0 ? pad(min, 2) : "00") + ":" + pad(sec, 2);
}