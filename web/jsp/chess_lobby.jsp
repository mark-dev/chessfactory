<html>
<head>
    <title>ChessFactory</title>
    <meta charset="utf-8">

    <link rel="stylesheet" href="../static/css/chesslobby.css"/>
    <link href="../static/css/jquery-ui.css" rel="stylesheet">
    <link href="../static/css/jquery-ui.theme.css" rel="stylesheet">
    <link rel="icon" href="../static/img/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="../static/img/favicon.ico" type="image/xicon">

    <link rel="stylesheet" href="../static/js/deps/chessground/base.css">
    <link rel="stylesheet" href="../static/js/deps/chessground/theme.css">
    <link rel="stylesheet" href="../static/js/deps/chessground/desktop.css">

</head>
<div class="header_logotype">
    <a href="#" class="logo"></a>
    <a href='#' class="actionsli" id="createGame">Play
        <i class="fa fa-sign-in"></i>
    </a>
</div>


<div class="main-board">
    <div class="active-games">
        <span>Active Games</span>
        <ul id='active_game'>
        </ul>
    </div>
    <div class="active-users">
        <span>Active Users</span>
        <ul id='active_user'>
        </ul>
    </div>
    <div class="board_game">
        <div class="actions_panel">
            <ul>
                <li>
                    <a href="#" id="undoMove" class='action_positive'>
                        <i class="fa  fa-rotate-right"></i>&nbsp;&nbsp;Undo
                    </a>
                </li>

                <li>
                    <a href="#" id="takeClockTime" class='action_positive'>
                        <i class="fa  fa-clock-o"></i>&nbsp;&nbsp;+30Sec!
                    </a>
                </li>

                <li>
                    <a href="#" id="offerDraw" class='action_neutral'>
                        <i class="fa  fa-dashboard"></i>&nbsp;&nbsp;Draw
                    </a>
                </li>

                <li>
                    <a href="#" id="giveUp" class='giveup'>
                        <i class="fa  fa-flag"></i>&nbsp;&nbsp;Surrender
                    </a>
                </li>
            </ul>
        </div>
        <div class="clock_panel">
            <div>
                <i class="fa fa-clock-o"></i>
                <span id="whiteTimer">00:00</span>
            </div>
            <div>
                <span id="evtSpan" style="height: 100%;width: 100%"></span>
            </div>
            <div>
                <i class="fa fa-clock-o"></i>
                <span id="blackTimer">00:00</span>
            </div>
        </div>

        <div class="view_game">
            <div id ="board" class="blue_modern yazart" style="width: 500px; height: 500px"></div>
        </div>

        <div class="game_status">

            <div class="status">
                <span class="state_name stst_wrng">FEN</span>
                <span id="fen" style="height: 45px; display: block"></span>
            </div>

        </div>

    </div>

    <div class="chat_block">
        <div class="status" style="margin-left: 15px;">
            <span class="state_name stst_ok">STATUS</span>
            <span id="status"></span>
        </div>
        <div class="status pgnBlock" style="margin-left: 15px;">
            <span class="state_name stst_info">PGN</span><span
                class ="pgnSpan" id="pgn"></span></div>
        <div class="scrolable_chat" id="chatDiv">
            <p id="chatArea"/>
        </div>
        <input id="gameChat" type="text" style="height: 45px; width: 100%; margin-left: 10px"/>
    </div>
</div>


<script src="../static/js/libs/sockjs-0.3.4.js"></script>
<script src="../static/js/libs/stomp.js"></script>
<script src="../static/js/libs/chess.js"></script>
<script src="../static/js/libs/json3.min.js"></script>
<script src="../static/js/libs/jquery-1.10.1.min.js"></script>
<script src="../static/js/timerutils.js"></script>
<script src="../static/js/cinnamon.js"></script>
<script src="../static/js/gamesui.js"></script>
<script src="../static/js/libs/jquery.timer.js"></script>
<script src="../static/js/libs/jquery-ui.js"></script>
<script src="../static/js/deps/chessground/chessground.min.js"></script>
<script src="../static/js/libs/Chart.min.js"></script>

</html>
