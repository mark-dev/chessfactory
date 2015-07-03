var init = function () {

    var socket;
    var stompClient;

    var COLORS = {
        WHITE: "WHITE",
        BLACK: "BLACK",
        SPECTATOR: "SPECTATOR"
    };


    var GAME_STATUS = {
        'IDLE': "idle",
        'WAIT_OPPONENT': "waitOpp",
        'GAME_PAUSED': "paused",
        'IN_PROGRESS': "in_progress",
        'GAME_TERMINATED': "terminated"
    };

    var $gui = {
        blackTimer: $('#blackTimer'),
        whiteTimer: $('#whiteTimer'),
        chatDiv: $('#chatDiv'),
        evtSpan: $("#evtSpan"),
        chat: $('#chatArea'),
        statusEl: $('#status'),
        fenEl: $('#fen'),
        pgnEl: $('#pgn'),
        online: $('#active_user'),
        games: $("#active_game"),
        board: $("#board"),
        chatInput: $('#gameChat'),
        analysis_canvas: undefined,
        gamePreview: undefined,
        analysis_progressbar: undefined,
        boardGameDiv: $(".board_game")
    };
    var $thisGame = {
        'board': undefined,
        'game': undefined,
        'id': undefined,
        'blackTimer': undefined,
        'whiteTimer': undefined,
        'gameSubscribing': undefined,
        'side': undefined,
        'currentGameStatus': GAME_STATUS.IDLE,
        'premove': undefined // undefined  | {from,to}
    };

    var onMove = function (source, target) {
        //Create NewMove object and send it
        var sendMove = function (san, algebraic) {
            var stompMsg = {
                'gameId': $thisGame.id,
                'algebraic': algebraic,
                'move': san
            };
            if ($thisGame.game.in_draw()) {
                stompMsg.isDraw = true;
            }
            stompClient.send("/app/achess/amove", {}, JSON.stringify(stompMsg));
            if (removeUndoMoveLinkIfExists()) {
                stompClient.send("/app/achess/undomove_anwer", {},
                    JSON.stringify({'isAccepted': false, 'gameId': $thisGame.id}));
            }
            if (removeDrawRequestLinkIfExists()) {
                acceptDraw($thisGame.id, false);
            }
            //Later, we got this move from server via brodcast message.
            $thisGame.game.undo();
            $thisGame.board.cancelMove();
        };


        var move = $thisGame.game.move({
            from: source,
            to: target,
            promotion: $thisGame.game.QUEEN //Try promote queen
        });
        // see if the move is legal
        if (move !== null) {

            var algebraic = move.from + move.to;
            //Check it is promotion move?
            if (move.flags.search($thisGame.game.FLAGS.PROMOTION) !== -1) {
                var dialog;

                var callBack = function (piece) {
                    console.log("Piece selected: " + piece);
                    //If we select Queen we already has valid SAN, else need got san with promoted piece
                    if (piece !== $thisGame.game.QUEEN) {
                        $thisGame.game.undo();
                        //Делаем вместе с выбранным промоушеном
                        move = $thisGame.game.move({
                            from: source,
                            to: target,
                            promotion: piece
                        });
                    }
                    sendMove(move.san, algebraic + piece);
                };
                //Call piece select dialog
                dialog = gu_invokeSelectPromotionDialog(callBack, $thisGame.side === COLORS.WHITE);
            }
            else {
                sendMove(move.san, algebraic);
            }
        }
    };

    //Default chessground board config
    var defBoardCfg = {
        turnColor: 'white',
        orientation: 'white',
        coordinates: true,
        viewOnly: false,
        lastMove: null,
        movable: {
            free: false,
            color: "null",
            events: {
                after: onMove
            }
        },
        premovable: {
            events: {
                set: function (orig, dest) {
                    $thisGame.premove = {from: orig, to: dest};
                },
                unset: function () {
                    $thisGame.premove = undefined;
                }
            }
        }
    };

    var isMyTurn = function () {
        return shortToVerbose($thisGame.game.turn()).toUpperCase() === $thisGame.side;
    };
    var shortToVerbose = function (s) {
        if (s === 'w')
            return COLORS.WHITE.toLowerCase();
        else if (s === 'b')
            return COLORS.BLACK.toLowerCase();
    };

    //Chess clock utils
    var createTimers = function (whiteTotalTime, blackTotalTime) {
        var onTrigger = function (Elem) {
            return function (currentTime) {
                Elem.html(formatTime(currentTime));
            }
        };
        var onCompleted = function (Elem) {
            return function () {
                Elem.html('00:00');
                Elem.css('color', 'red');
            }
        };
        var blckTimerElem = $gui.blackTimer;
        var whiteTimerElem = $gui.whiteTimer;
        blckTimerElem.css('color', '');
        whiteTimerElem.css('color', '');
        destroyTimers();
        $thisGame.blackTimer = createTimer(blackTotalTime, onTrigger(blckTimerElem), onCompleted(blckTimerElem));
        $thisGame.whiteTimer = createTimer(whiteTotalTime, onTrigger(whiteTimerElem), onCompleted(whiteTimerElem));
        blckTimerElem.html(formatTime(blackTotalTime));
        whiteTimerElem.html(formatTime(whiteTotalTime));
        $thisGame.whiteTimer.start();
        $thisGame.blackTimer.start();
    };
    var destroyTimers = function () {
        if ($thisGame.whiteTimer != undefined && $thisGame.blackTimer != undefined) {
            $thisGame.whiteTimer.stop();
            $thisGame.blackTimer.stop();
            $thisGame.whiteTimer = undefined;
            $thisGame.blackTimer = undefined;
        }
    };
    var toggleClocks = function () {
        $thisGame.whiteTimer.toggle();
        $thisGame.blackTimer.toggle();
    };


    var appendToEventSpan = function (elem) {
        var evt = $gui.evtSpan;
        evt.children().remove();
        evt.append(elem);
    };
    var appendToChat = function (elem, withBr) {
        var chat = $gui.chat;
        chat.append(elem);
        if (withBr)
            chat.append($("<br>"));
        var scrorableDiv = $gui.chatDiv;
        scrorableDiv.animate({scrollTop: scrorableDiv[0].scrollHeight}, 100);
    };

    var appendNewGameToGlobalList = function (gameInfoContainer) {
        gu_addNewGame(gameInfoContainer.gameId,
            gu_gameInfoFromGameContainer(gameInfoContainer),
            function () {
                joinGame(gameInfoContainer.gameId);
            }, function (toolTipDiv) {
                $gui.gamePreview = toolTipDiv;
                requestFen(gameInfoContainer.gameId);
            }, function (toolTipDiv) {
                $gui.gamePreview = undefined;
            });
    };

    var updateGameInfo = function (gameInfoContainer) {
        gu_updateGameInfo(gameInfoContainer.gameId, gu_gameInfoFromGameContainer(gameInfoContainer));
    };

    var textElement = function (className, acontent) {
        return $("<p>").addClass(className).html(acontent);
    };

    var createNotifyMessage = function (acontent) {
        return textElement("notify_msg", acontent);
    };
    var createChatMessage = function (acontent, type) {

        var chatClass = "chat_msg";
        if (type === "SPECTATOR_CHAT_MSG")
            chatClass = "chat_msg_spectator";
        else if (type === "WHITE_CHAT_MSG") {
            chatClass = "chat_msg_white";
        }
        else if (type === "BLACK_CHAT_MSG")
            chatClass = "chat_msg_black";
        else if (type === "SYSTEM")
            chatClass = "system_msg";
        return textElement(chatClass, acontent);
    };
    var createOnlineLabel = function (userInfo) {
        return $("<li>")
            .append($("<a>", {id: "online" + userInfo.id})
                .addClass("online_users")
                .html(userInfo.username));
    };
    var createClickableLinkId = function (acontent, onclickcallback, id) {
        return createClickableLink(acontent, onclickcallback).attr('id', id);
    };
    var createClickableLink = function (acontent, onclickcallback) {
        return $("<a>", {href: "#"})
            .addClass("event_label")
            .html(acontent)
            .on('click', onclickcallback);
    };


    var removeUndoMoveLinkIfExists = function () {
        if (document.getElementById('undoMoveRequest') !== null) {
            $('#undoMoveRequest').remove();
            return true;
        }
        return false;
    };
    var removeDrawRequestLinkIfExists = function () {
        if (document.getElementById('drawRequest') !== null) {
            $('#drawRequest').remove();
            return true;
        }
        return false;
    };

    var validMoves = function getValidMoves(chess) {
        var dests = {};
        chess.SQUARES.forEach(function (s) {
            var ms = chess.moves({square: s, verbose: true});
            if (ms.length) dests[s] = ms.map(function (m) {
                return m.to;
            });
        });
        return dests;
    };
    var updateBoardPosition = function () {
        $thisGame.board.set({
            fen: $thisGame.game.fen(),
            turnColor: shortToVerbose($thisGame.game.turn()),
            movable: {
                dests: validMoves($thisGame.game)
            }
        });
    };

    var updateStatus = function () {
        var moveColor = 'White';


        var status = '';

        if ($thisGame.currentGameStatus === GAME_STATUS.IN_PROGRESS) {
            if ($thisGame.game.turn() === 'b') {
                moveColor = 'Black';
            }

            // checkmate?
            if ($thisGame.game.in_checkmate() === true) {
                status = 'Game over, ' + moveColor + ' is in checkmate.';
                $thisGame.currentGameStatus = GAME_STATUS.GAME_TERMINATED;
                destroyTimers();
            }

            // draw?
            else if ($thisGame.game.in_draw() === true) {
                status = 'Game over, drawn position';
                $thisGame.currentGameStatus = GAME_STATUS.GAME_TERMINATED;
                destroyTimers();
            }
            else {
                status = moveColor + ' to move';
                // check?
                if ($thisGame.game.in_check() === true) {
                    status += ', ' + moveColor + ' is in check';
                }
            }
        } else {
            status = 'game not started ( ' + $thisGame.currentGameStatus + ")";
            destroyTimers();
        }


        $gui.statusEl.html(status);
        $gui.fenEl.html($thisGame.game.fen());
        //If analysis active do not update pgn span.
        if ($gui.analysis_canvas === undefined) {
            $gui.pgnEl.html($thisGame.game.pgn());
        }

        $gui.pgnEl.animate({scrollTop: $gui.pgnEl[0].scrollHeight}, 100);
    };

    var setupFen = function (fen) {
        if (fen !== "") {
            $thisGame.game = new Chess(fen);
            updateBoardPosition();
        }
    };
    var subscribeToGame = function (id) {
        $thisGame.gameSubscribing = stompClient.subscribe("/topic/game/" + id,
            function (data) {
                handleGameEvents(JSON.parse(data.body))
            });
    };

    var setupGameFromContainer = function (gamecontainer) {
        $thisGame.id = gamecontainer.gameId;
        if ($thisGame.gameSubscribing === undefined) {
            subscribeToGame($thisGame.id);
        }
        gu_setCurrentGame($thisGame.id);
        resetBoard();

        setupFen(gamecontainer.fen);

        //Rotate board according game side
        if (!($thisGame.side.toLowerCase() === $thisGame.board.getOrientation())) {
            $thisGame.board.toggleOrientation();
        }
        //Set actual timers value
        createTimers(gamecontainer.blackTimer / 10, gamecontainer.whiteTimer / 10);

        if (gamecontainer.isStarted) {
            $thisGame.currentGameStatus = GAME_STATUS.IN_PROGRESS;
            if ($thisGame.game.turn() === $thisGame.game.BLACK) {
                $thisGame.whiteTimer.toggle(); //Stop white timer, black still active
            }
            else if ($thisGame.game.turn() === $thisGame.game.WHITE) {
                $thisGame.blackTimer.toggle(); //Same for black timer
            }
            //Make board movable if side is player
            if ($thisGame.side === COLORS.WHITE || $thisGame.side === COLORS.BLACK) {
                $thisGame.board.set({movable: {color: $thisGame.side.toLowerCase()}});
            }
        }
        //Game not started yet, disable timers
        else {
            $thisGame.whiteTimer.stop();
            $thisGame.blackTimer.stop();
        }
        //Spectators can't move pieces
        if ($thisGame.side === COLORS.SPECTATOR) {
            $thisGame.board.set({
                viewOnly: true
            });
        }
        //Clear analysis chart and markup
        $gui.pgnEl.html("");
        removeAnalysisChartIfExists();
        removeProgressBarIfExists();

        updateStatus();
    };

    var handleGlobalEvents = function (data) {
        switch (data.event_type) {
            case 'ChatMessageFlow':
            {
                var textElement = createChatMessage(data.type !== "SYSTEM" ? data.username + ":" + data.payload : data.payload, data.type);
                appendToChat(textElement, false);
                break;
            }

            case 'UserOnlineEvent':
            {
                $gui.online.append(createOnlineLabel(data));
                break;
            }
            case 'UserOfflineEvent':
            {
                $("#online" + data.id).parent().remove();
                break;
            }
            case 'UpdateGameInfo':
            {
                updateGameInfo(data.container);
                break;
            }
            case 'GameDeletedEvent':
            {
                gu_removeGame(data.gameId);
                break;
            }
            case 'GameStartedEvent':
            {
                var notifyLabel = createNotifyMessage("Your game started!");
                appendToChat(notifyLabel, true);
                break;
            }
            case 'GameCreatedEvent':
            {
                var gameInfo = data.container;
                appendNewGameToGlobalList(gameInfo);
                break;
            }
            default:
            {
                console.log('Unknown global event! ' + data);
                break;
            }
        }
    };

    var handleGameEvents = function (data) {
        switch (data.event_type) {
            case 'GameTerminated':
            {
                $thisGame.currentGameStatus = GAME_STATUS.GAME_TERMINATED;
                switch (data.reason) {

                    case 'BLACK_WIN_CLOCK':
                    {
                        break;
                    }
                    case 'WHITE_WIN_CLOCK':
                    {
                        break;
                    }
                    case 'BLACK_WIN':
                    {
                        break;
                    }
                    case 'WHITE_WIN':
                    {
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
                appendToChat(createNotifyMessage("Server terminated game with reason: " +
                data.reason + "\tWinner = " + data.winner), true);
                updateStatus();
                break;
            }
            case 'FenPositionReply':
            {
                if ($gui.gamePreview !== undefined) {
                    $gui.gamePreview.removeClass("fa fa-cloud-download");
                    $gui.gamePreview.addClass("blue_modern yazart");
                    var preview = Chessground($gui.gamePreview[0], {viewOnly: true, fen: data.fen});
                }
                break;
            }

            case 'GamePaused' :
            {
                $thisGame.currentGameStatus = GAME_STATUS.GAME_PAUSED;
                destroyTimers();
                updateStatus();
                break;
            }
            case 'PlayerDisconnectedEvent':
            {
                var textElement = createChatMessage(data.uname + " is disconnected..", "SYSTEM");
                appendToChat(textElement, false);
                break;
            }
            case 'UndoMoveReply' :
            {
                if (data.isAccepted === true) {
                    $thisGame.game.undo();
                    if (data.fullMoveUndo === true) {
                        $thisGame.game.undo(); //Отменяем полный ход
                        $thisGame.board.set({lastMove: null}); //TODO: взять из истории
                    }
                    else {
                        toggleClocks();
                    }
                    updateBoardPosition();
                    updateStatus();
                }
                else {
                    appendToChat(createChatMessage("Undo move request declined", "SYSTEM"),false);
                }
                break;
            }
            case 'DrawRequest':
            {
                removeDrawRequestLinkIfExists();
                appendToEventSpan(createClickableLinkId("DRAW ?", function () {
                    acceptDraw($thisGame.id, true);
                    this.remove();
                }, "drawRequest"), true);
                break;
            }
            case 'UndoMoveServerRequest':
            {
                removeUndoMoveLinkIfExists();
                appendToEventSpan(createClickableLinkId("UNDO ?", function () {
                    stompClient.send("/app/achess/undomove_anwer", {},
                        JSON.stringify({'isAccepted': true, 'gameId': $thisGame.id}));
                    this.remove();
                }, "undoMoveRequest"), true);
                break;
            }
            case 'SyncGames':
            {
                $gui.games.children().remove();
                var games = data.games;
                games.forEach(function (g) {
                    appendNewGameToGlobalList(g);
                });
                break;
            }
            case 'SyncClock':
            {
                $thisGame.blackTimer.setCurrentTime(data.blackTimer / 10);
                $thisGame.whiteTimer.setCurrentTime(data.whiteTimer / 10);
                break;
            }
            case 'SyncOnlineUsers' :
            {
                $gui.online.children().remove();
                var userSet = data.users;
                console.log("syncOnlineUsers!" + userSet);
                userSet.forEach(function (e) {
                    $gui.online.append(createOnlineLabel(e));
                });
                break;
            }
            case 'ChatMessageFlow':
            {
                handleGlobalEvents(data);
                break;
            }
            case 'CreateGameResponse':
            {
                if ($thisGame.id != undefined) {
                    gu_removeCurrentGameHighlight();
                }
                appendToChat(createNotifyMessage("Game with id " + data.container.gameId + "created! Wait opponent"), false);
                appendNewGameToGlobalList(data.container);
                break;
            }
            case 'JoinGameResponse':
            {
                console.log('i successfully join the game with role = ' + data.role);
                //Очищаем чат
                $gui.chat.children().remove();
                var gamecontainer = data.gameInfo;
                $thisGame.side = data.role;

                if ($thisGame.gameSubscribing !== undefined) {
                    $thisGame.gameSubscribing.unsubscribe();
                    $thisGame.gameSubscribing = undefined;
                }
                //Если зашел наблюдателем то за игрой уже можно наблюдать
                setupGameFromContainer(gamecontainer);

                break;
            }

            case 'GameStarted':
            {
                console.log('Well! we starting a game with id = ' + $thisGame.id + " and role : " + $thisGame.side);
                handleGlobalEvents({'event_type': 'GameStartedEvent'});
                //Если зашел как игрок то пересоздаются таймеры(косяк.. todo оптимизировать!) и игра запускается.
                setupGameFromContainer(data.container);


                break;
            }
            case 'NewMove':
            {
                var move = data.move;
                var from = move.substr(0, 2);
                var to = move.substr(2, 2);
                var promotion = move.length === 5 ? move.charAt(4) : undefined;

                $thisGame.game.move({from: from, to: to, promotion: promotion});
                toggleClocks();
                removeUndoMoveLinkIfExists();
                updateStatus();
                updateBoardPosition();
                $thisGame.board.set({lastMove: [from, to]});
                if (isMyTurn() && $thisGame.premove !== undefined) {
                    //Пытаемся сделать premove ход
                    //TODO: в premovable.current хранится значение premove ["e2","e4"] | null,
                    //его не обязательно хранить внешне через set и unset события
                    onMove($thisGame.premove.from, $thisGame.premove.to);
                    $thisGame.board.set({premovable: {current: null}});
                }
                break;
            }
            case 'AnalysisReport':
            {
                removeProgressBarIfExists();
                displayAnalysis(data);
                break;
            }
            case 'AnalysisInProgress':
            {
                if ($gui.analysis_progressbar === undefined) {
                    //Init progress bar
                    var parentDiv = $("<div>").addClass("analysis_progressbar_container");
                    var label = $("<h2>").addClass("analysis_progressbar_label").html("Analysis in progress...");
                    var progressBar = $("<div>").addClass("analysis_progressbar");
                    $gui.boardGameDiv.append(parentDiv.append(label).append(progressBar));
                    $gui.analysis_progressbar = progressBar;
                }
                //Update progressbar value
                $gui.analysis_progressbar.progressbar({value: data.value});

                break;
            }
            default:
            {
                console.log('Unknown private event! ' + data);
                break;
            }
        }
    };

    var removeAnalysisChartIfExists = function () {
        if ($gui.analysis_canvas != undefined) {
            $gui.analysis_canvas.remove();
            $gui.analysis_canvas = undefined;
        }
    };
    var removeProgressBarIfExists = function () {
        if ($gui.analysis_progressbar !== undefined) {
            //Remove existed progressbar
            $gui.analysis_progressbar.parent().remove();
            $gui.analysis_progressbar = undefined;
        }
    };

    var displayAnalysis = function (analysis_data) {
        var game = new Chess(analysis_data.startpos);

        var previewDiv = $("<div>").addClass("gamePreview blue_modern yazart");
        var previewGame = new Chess(analysis_data.startpos);
        var previewGameBoard = Chessground(previewDiv[0], {viewOnly: true, fen: analysis_data.startpos});
        var activeMoveElem = null;

        function seq(analysisArr) {
            var arr = new Array(analysisArr.length);
            $.each(analysisArr, function (n, val) {
                    arr[n] = {
                        index: n,
                        toString: function () {
                            return val.san;
                        }
                    };
                }
            )
            ;
            return arr;
        }

        function score(analysisArr) {
            var arr = new Array(analysisArr.length);
            $.each(analysisArr, function (n, val) {
                arr[n] = val.score;
            });
            return arr;
        }

        function make_moves(agame, AnalysisData, mainLineMoveNumber, bestSeqMoveNumber) {
            var last;
            for (var i = 0; i <= mainLineMoveNumber; i++) {
                if (i === mainLineMoveNumber && bestSeqMoveNumber != null) {
                    for (var s = 0; s <= bestSeqMoveNumber; s++) {
                        last = agame.move(AnalysisData.analysis[i].best[s]);
                    }
                    break;
                }
                else
                    last = agame.move(AnalysisData.analysis[i].san);
            }
            return last;
        }

        //bestSeqMove = null | index;
        function playUntil(AnalysisData, mainLineMoveNumber, bestSeqMoveNumber) {
            game = new Chess(AnalysisData.startpos);

            var lastMove = make_moves(game, AnalysisData, mainLineMoveNumber, bestSeqMoveNumber);
            $thisGame.board.set({fen: game.fen(), lastMove: [lastMove.from, lastMove.to]});
            $gui.fenEl.html(game.fen());
        }

        function initPreviewBoard(AnalysisData, mainLineMoveNumber, bestSeqMoveNumber) {
            previewGame = new Chess(AnalysisData.startpos);
            var lastMove = make_moves(previewGame, AnalysisData, mainLineMoveNumber, bestSeqMoveNumber);
            previewGameBoard.set({fen: previewGame.fen(), lastMove: [lastMove.from, lastMove.to]});
        }

        function withTooltip(Elem, AnalysisData, mainLineMoveNumber, bestSeqMoveNumber) {
            Elem.attr("title", "");
            Elem.tooltip({
                content: function () {
                    return previewDiv;
                },
                open: function () {
                    initPreviewBoard(AnalysisData, mainLineMoveNumber, bestSeqMoveNumber);
                },
                close: function () {

                }
            });
        }

        var chartData = {
            labels: seq(analysis_data.analysis),
            datasets: [
                {
                    label: "Centipawns",
                    fillColor: "rgba(74,193,248,0.2)",
                    strokeColor: "rgba(194,248,106,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#F86825",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: score(analysis_data.analysis)
                }
            ]
        };

        var canvas = $("<canvas>").addClass("cp_chart");
        $gui.analysis_canvas = canvas;
        $gui.boardGameDiv.append(canvas);
        var ctx = canvas[0].getContext("2d");

        var myLineChart = new Chart(ctx).Line(chartData,
            {
                customTooltips: function (tooltip) {
                    if (!tooltip) {
                        return;
                    }
                    //Empty tooltip..
                },
                // Boolean - If we want to override with a hard coded scale
                scaleOverride: true,

                // ** Required if scaleOverride is true **
                // Number - The number of steps in a hard coded scale
                scaleSteps: 2,
                // Number - The value jump in the hard coded scale
                scaleStepWidth: 10,
                // Number - The scale starting value
                scaleStartValue: -10,
                //Number - Radius of each point dot in pixels
                pointDotRadius: 5,
                //Number - amount extra to add to the radius to cater for hit detection outside the drawn point
                pointHitDetectionRadius: 1
            });

        function highlightActiveMove(Element) {
            if (activeMoveElem != null) {
                activeMoveElem.removeClass("active_move");
            }
            Element.addClass("active_move");
            activeMoveElem = Element;
        }

        canvas.click(function (e) {
            var activePoint = myLineChart.getPointsAtEvent(e);
            if (activePoint[0] != undefined && activePoint[0].hasOwnProperty("label")) {
                playUntil(analysis_data, activePoint[0].label.index, null);

                //scroll to target move
                var targetElem = $("#mlm" + activePoint[0].label.index);
                $gui.pgnEl.animate({
                    scrollTop: targetElem.offset().top - $gui.pgnEl.offset().top + $gui.pgnEl.scrollTop()
                });
                //Highlight targetMove
                highlightActiveMove(targetElem);
            }
        });

        var analysisViewElem = $gui.pgnEl.html("");

        function appendMoveNum(num) {
            analysisViewElem.append($("<span>").addClass("move_number").html(num + "."));
        }

        function appendMove(analysis_row, index) {
            var elem = $("<a>", {href: "#", id: "mlm" + index})
                .addClass("main_line_move")
                .html(analysis_row.san)
                .click(function () {
                    playUntil(analysis_data, index, null);
                    highlightActiveMove($(this));
                }
            );
            analysisViewElem.append(elem);

            function getMateStr() {
                return analysis_row.hasOwnProperty("mateIn") ? " (in " + analysis_row.mateIn + ") " : " ";
            }

            if (analysis_row.best != null) {
                var commentSpan = $("<span>").addClass("move_comment");
                if (analysis_row.type === "BLUNDER") {
                    commentSpan.html("Blunder! Best move: ");
                }
                else if (analysis_row.type === "BLUNDER_MU") {
                    commentSpan.html("Blunder! Mate" + getMateStr() + "unavoidable now. Best move: ");
                }
                else if (analysis_row.type === "MISTAKE") {
                    commentSpan.html("Mistake! Better: ");
                }
                else if (analysis_row.type === "INACCURACY") {
                    commentSpan.html("Inaccuracy! Better: ");
                }
                else if (analysis_row.type === "MATE_DELAYED") {
                    commentSpan.html("Mistake! Mate delayed, faster: ");
                }
                else if (analysis_row.type === "MATE_LOST") {
                    commentSpan.html("Blunder! Lose mate sequence" + getMateStr() + "Best move: ");
                }
                analysisViewElem.append(commentSpan);
                $.each(analysis_row.best, function (bm_index, bm_san) {
                    var bestMoveSeqElem = $("<a>", {href: "#"}).addClass("best_move_seq").html(bm_san).click(function () {
                        playUntil(analysis_data, index, bm_index);
                        highlightActiveMove($(this));
                    });
                    if (analysis_row.best.length - 1 == bm_index) {
                        bestMoveSeqElem.css('text-decoration', 'underline');
                        withTooltip(bestMoveSeqElem, analysis_data, index, bm_index);
                    }
                    analysisViewElem.append(bestMoveSeqElem);
                });
            }
        }

        var startsFromBlackMove = (game.turn() === 'b');
        var moveNum = 2;
        $.each(analysis_data.analysis, function (n, val) {
            if (n >= 2) {
                if (!startsFromBlackMove && n % 2 == 0 || startsFromBlackMove && n % 2 == 1) {
                    appendMoveNum(moveNum);
                    moveNum++;
                }
            }
            else {
                if (n == 0) {
                    appendMoveNum(1);
                    if (startsFromBlackMove) {
                        //Game starts from black turn..
                        analysisViewElem.append($("<span>").addClass("ommision_points").html("..."));
                    }
                }
                else if (n == 1 && startsFromBlackMove) {
                    appendMoveNum(2);
                    moveNum = 3;
                }
            }
            appendMove(val, n);
        });
    };

    var sendTextMessage = function (msg) {
        if (msg !== '') {
            stompClient.send("/app/achess/chat", {}, JSON.stringify({'payload': msg, 'gameId': $thisGame.id}));
        }
    };

    var requestFen = function (gameId) {
        stompClient.send("/app/achess/fen/" + gameId);
    };
    var surrender = function (gameId) {
        stompClient.send("/app/achess/surrender", {},
            JSON.stringify({'gameId': gameId}));
    };
    var takeClockTime = function (gameId) {
        stompClient.send("/app/achess/takeclock/" + gameId + "/30");
    };
    var offerDraw = function (gameId) {
        stompClient.send("/app/achess/offerdraw/" + gameId);
    };
    var acceptDraw = function (gameId, accepted) {
        stompClient.send("/app/achess/drawaccept/" + gameId + "/" + accepted);
    };
    var joinGame = function (gameId) {
        stompClient.send("/app/achess/joingame/" + gameId);
    };
    var reconnectCount = 0;
    var delay;
    var webSockConnect = function () {
        socket = new SockJS('/wchess');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            stompClient.subscribe('/user/queue/chesslobby', function (data) {
                handleGameEvents(JSON.parse(data.body));
            });
            stompClient.subscribe('/topic/chesschat', function (data) {
                handleGlobalEvents(JSON.parse(data.body));
            });
            stompClient.subscribe('/app/init_done', function (data) {
            });
            //Значит реконнект был.
            if ($thisGame.currentGameStatus == GAME_STATUS.IN_PROGRESS && $thisGame.id != undefined) {
                joinGame($thisGame.id);
            }
        }, function (msg) {
            // alert("sock disconnected -- try reconnect! \n" + msg);
            console.log("websock onError : " + msg);
            reconnectCount++;
            if (reconnectCount < 10)
                delay = 1000;
            else if (reconnectCount > 10 && reconnectCount < 20)
                delay = 10000;
            else if (reconnectCount > 30 && reconnectCount < 40)
                delay = 20000;
            else
                delay = 60000;
            setTimeout(webSockConnect, delay);
        });
    };


    var initBoard = function () {
        $thisGame.game = new Chess();
        $thisGame.board = Chessground($gui.board[0], defBoardCfg);
    };

    var resetBoard = function () {
        $thisGame.game = new Chess();
        $thisGame.board.set(defBoardCfg);
    };
    var KP_ENTER = 13;
    $gui.chatInput.on('keydown', function (event) {
        if (event.which == KP_ENTER) { //Enter pressed
            sendTextMessage($gui.chatInput.val());
            $gui.chatInput.val("");
        }
    });
    $('#createGame').on('click', function () {
        gu_createNewGameDialogInvoke(function (color, perSide, addSec, fen, vsAI) {
            console.log("role: " + color);
            stompClient.send("/app/achess/newgame", {},
                JSON.stringify({
                    'role': color,
                    'additionalSeconds': addSec,
                    'clockControl': perSide,
                    'vsAI': vsAI,
                    'fen': fen
                }));
        });

    });
    $('#undoMove').on('click', function () {
        stompClient.send("/app/achess/undomove", {}, JSON.stringify({'gameId': $thisGame.id}));
    });
    $('#giveUp').on('click', function () {
        surrender($thisGame.id)
    });
    $('#takeClockTime').on('click', function () {
        takeClockTime($thisGame.id)
    });
    $('#offerDraw').on('click', function () {
        offerDraw($thisGame.id)
    });
    $thisGame.game = new Chess();
    initBoard();
    updateStatus();
    webSockConnect();


    var DEBUG = false;
    if (DEBUG) {
        var report = {
            "startpos": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            "analysis": [{"san": "Nf3", "score": 0.18, "type": "REGULAR"}, {
                "san": "d5",
                "score": 0.18,
                "type": "REGULAR"
            },
                {"san": "e3", "score": 0.16, "type": "REGULAR"}, {"san": "c5", "score": 0.31, "type": "REGULAR"},
                {"san": "Nc3", "score": 0.18, "type": "REGULAR"},
                {"san": "Nc6", "score": 0.06, "type": "REGULAR"},
                {"san": "d4", "score": 0.02, "type": "REGULAR"},
                {"san": "b6", "score": 1.28, "type": "INACCURACY", "best": ["Nf6"]},
                {"san": "dxc5", "score": 0.96, "type": "REGULAR"},
                {"san": "bxc5", "score": 1.27, "type": "REGULAR"}, {"san": "Qxd5", "score": 1.43, "type": "REGULAR"},
                {"san": "Qxd5", "score": 1.45, "type": "REGULAR"}, {"san": "Nxd5", "score": 1.81, "type": "REGULAR"},
                {
                    "san": "Bg4",
                    "score": 4.48,
                    "type": "BLUNDER",
                    "best": ["Rb8", "e4", "Nb4", "Nxb4", "Rxb4", "Bd3", "Nf6", "a3", "Rb8", "h3", "h6", "Ne5", "Rb6", "O-O", "e6"]
                },
                {"san": "Nc7+", "score": 4.21, "type": "REGULAR"},
                {"san": "Kd8", "score": 5.5, "type": "INACCURACY", "best": ["Kd7"]},
                {"san": "Nxa8", "score": 5.43, "type": "REGULAR"},
                {"san": "Kc8", "score": 5.24, "type": "REGULAR"},
                {"san": "Bb5", "score": 5.21, "type": "REGULAR"},
                {
                    "san": "e5",
                    "score": 8.16,
                    "type": "BLUNDER",
                    "best": ["Bxf3", "gxf3", "Kb7", "b3", "Nf6", "Bxc6+", "Kxc6", "Ba3", "Kb7", "O-O-O", "Kxa8", "Rd8+", "Kb7", "Bxc5"]
                },
                {"san": "Bxc6", "score": 8.03, "type": "REGULAR"},
                {
                    "san": "f5",
                    "score": 10.0,
                    "type": "MISTAKE",
                    "best": ["Ne7", "Be4", "f5", "Nxe5", "fxe4", "Nxg4", "Kb7", "Ne5", "Ng6", "Nf7", "Rg8", "Ng5", "Be7", "Nxe4", "Rxa8", "Bd2"]
                },
                {"san": "Nxe5", "score": 10.0, "type": "REGULAR"},
                {
                    "san": "Bd6",
                    "score": 10.0,
                    "type": "MISTAKE",
                    "best": ["Ne7", "Be8", "Ng6", "Bxg6", "hxg6", "f3", "Bh5", "g4", "Kb7", "Nf7", "Rh7", "Ng5", "Rh8", "gxh5", "Kxa8", "Ne6", "gxh5"]
                },
                {"san": "Nf7", "score": 10.0, "type": "REGULAR"},
                {
                    "san": "Nf6",
                    "score": 10.0,
                    "type": "BLUNDER",
                    "best": ["Ne7", "Bb5", "Kb8", "Nxh8", "Bh5", "Bc4", "Kxa8", "Nf7", "Bxf7", "Bxf7", "Be5", "Ke2", "g6", "h3"]
                }, {"san": "Nxd6+", "score": 10.0, "type": "REGULAR"}, {
                    "san": "Kb8",
                    "score": 10.0,
                    "type": "REGULAR"
                }, {"san": "b4", "score": 10.0, "type": "REGULAR"}, {
                    "san": "Rd8",
                    "score": 10.0,
                    "type": "BLUNDER",
                    "best": ["cxb4", "Bb2", "a5", "Be5", "Nd7", "Bxd7", "Kxa8", "f3", "Bh5", "Bc6+", "Ka7", "Bd4+", "Kb8", "Bb6", "Rf8", "Bxa5", "Bf7", "Bxb4"]
                }, {"san": "bxc5", "score": 10.0, "type": "REGULAR"}, {
                    "san": "Ne4",
                    "score": 10.0,
                    "type": "BLUNDER_MU",
                    "mateIn": 1,
                    "best": ["Rxd6", "Rb1+", "Kc8", "cxd6", "f4", "Rb7", "Bd7", "Bf3", "Bg4", "Rc7+", "Kd8", "Bxg4", "Nxg4", "Rxg7", "h5", "exf4", "Nf6", "Rxa7"]
                }, {"san": "Rb1#", "score": 10.0, "type": "REGULAR"}], "event_type": "AnalysisReport"
        };
        displayAnalysis(report);
    }
};

$(document).ready(init);
