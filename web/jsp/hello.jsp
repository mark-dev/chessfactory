<html>
<head lang="en">
    <link href="../static/js/deps/bootstrap-3.3.4-dist/css/bootstrap.min.css" rel="stylesheet"/>
    <title>ChessFactory</title>
    <meta charset="utf-8">

    <link rel="stylesheet" href="../static/css/chesslobby_bs.css"/>
    <link href="../static/css/jquery-ui.css" rel="stylesheet">
    <link href="../static/css/jquery-ui.theme.css" rel="stylesheet">
    <link rel="icon" href="../static/img/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="../static/img/favicon.ico" type="image/xicon">

    <link rel="stylesheet" href="../static/js/deps/chessground/base.css">
    <link rel="stylesheet" href="../static/js/deps/chessground/theme.css">
    <link rel="stylesheet" href="../static/js/deps/chessground/desktop.css">

    <meta charset="UTF-8">
    <title></title>
    <style>
        html, body {
            height: 100%;
        }

        .height100 {
            height: 100%;
        }

        .height80 {
            height: 80%;
        }

        .height20 {
            height: 20%;
        }

        .height5 {
            height: 5%;
        }

        .height95 {
            height: 95%;
        }

        .height90 {
            height: 90%;
        }

        .height10 {
            height: 10%;
        }

        .height70 {
            height: 70%;
        }

        .height60 {
            height: 60%;
        }

        .height50 {
            height: 50%;
        }
    </style>
</head>
<body>

<div class="container-fluid height10">
    <div class="row height100 header_logotype">
        <span class="col-xs-hidden col-md-11 height100">
            <a href="#" class="logo height100"></a>
        </span>
        <span class="col-md-1 height100">
             <a href='#' class="actionsli" id="createGame">Play
                 <i class="fa fa-sign-in"></i>
             </a>
        </span>
    </div>
</div>


<div class="container-fluid height90">

        <span class="col-md-3 col-xs-3">
            <div class="active-games height80" style="border: 2px solid black">
                <span>Active Games</span>
                <ul id='active_game'></ul>
            </div>
            <div class="active-users height20" style="border: 2px solid red">
                <span>Active Users</span>
                <ul id='active_user'></ul>
            </div>
        </span>

    <span class="col-md-6 col-xs-6">
           <div class="row actions_panel">
               <span class="col-md-12 height100" style="text-align: center">
                    <a href="#" id="undoMove" class='action_positive'>
                        <i class="fa  fa-rotate-right"></i>&nbsp;&nbsp;Undo
                    </a>
                    <a href="#" id="takeClockTime" class='action_positive'>
                        <i class="fa  fa-clock-o"></i>&nbsp;&nbsp;+30Sec!
                    </a>
                   <a href="#" id="offerDraw" class='action_neutral'>
                       <i class="fa  fa-dashboard"></i>&nbsp;&nbsp;Draw
                   </a>
                    <a href="#" id="giveUp" class='giveup'>
                        <i class="fa  fa-flag"></i>&nbsp;&nbsp;Surrender
                    </a>
               </span>
           </div>

           <div class="clock_panel container_fluid">
               <span class="col-md-4 height100 whiteTimers">
                   <i class="fa fa-clock-o"></i>
                   <span id="whiteTimer">00:00</span>
               </span>
               <span id="evtSpan" class="col-md-4 height100"></span>
               <span class="col-md-4 height100 blackTimers">
                   <i class="fa fa-clock-o"></i>
                   <span id="blackTimer">00:00</span>
               </span>
           </div>
            <div class="height60">

                <div id="board" class="blue_modern yazart" style="width:100%; height:100%"></div>


            </div>



            <div style="height: 20%;border: 2px solid #c8e5bc">
                    <div class="col-md-12">
                        <div class="row status stst_wrng">
                            <span class="col-md-12">FEN</span>
                        </div>
                        <div class="row status">
                            <span class="col-md-12" id="fen"></span>
                        </div>
                    </div>
                    <div class="col-md-12">
                        <div class="row status stst_info">
                            <span class="col-md-12">STATUS</span>
                        </div>
                        <div class="row status">
                            <span class="col-md-12" id="status"></span>
                        </div>
                    </div>
            </div>
    </span>

    <span class="col-md-3 col-xs-3">
            <div class="height50">
                <span class="state_name stst_info col-md-2">PGN</span>

                <div class="row status height90">
                    <span class="col-md-12 pgnSpan height100" id="pgn"></span>
                </div>
            </div>
            <div class="row">
                <div class="scrolable_chat col-md-12 height50" id="chatDiv">
                    <p id="chatArea"/>
                </div>

                <input id="gameChat height100" type="text" style="width: 100%"/>
            </div>
            
    </span>
</div>


<script src="../static/js/libs/sockjs-0.3.4.js"></script>
<script src="../static/js/libs/stomp.js"></script>
<script src="../static/js/libs/chess.js"></script>
<script src="../static/js/libs/json3.min.js"></script>
<script src="../static/js/libs/jquery-1.10.1.min.js"></script>
<script src="../static/js/timerutils.js"></script>

<script src="../static/js/gamesui.js"></script>
<script src="../static/js/libs/jquery.timer.js"></script>
<script src="../static/js/libs/jquery-ui.js"></script>
<script src="../static/js/deps/chessground/chessground.min.js"></script>
<script src="../static/js/libs/Chart.min.js"></script>
<script src="../static/js/deps/bootstrap-3.3.4-dist/js/bootstrap.js"></script>
<script>
    var board = $("#board").parent();
    var scaleBoard = function () {
        board.height(board.width())
    };
    scaleBoard();
    $(window).resize(scaleBoard);
</script>
<script src="../static/js/cinnamon.js"></script>
</body>
</html>