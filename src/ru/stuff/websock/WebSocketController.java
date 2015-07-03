package ru.stuff.websock;

/**
 * Created by mark on 15.12.14.
 */

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.stuff.chess.entity.*;
import ru.stuff.chess.entity.outgoing.*;
import ru.stuff.chess.sys.analysis.engine.MoveAnalysis;
import ru.stuff.chess.sys.analysis.engine.UciInfo;
import ru.stuff.chess.sys.analysis.report.MoveType;
import ru.stuff.chess.sys.events.ChatEventsTypes;
import ru.stuff.chess.sys.game.GameInfo;
import ru.stuff.chess.sys.game.GameMember;
import ru.stuff.chess.sys.game.GameOverReasons;
import ru.stuff.chess.sys.game.GameRoles;
import ru.stuff.chess.sys.users.WebSocketSessionPrincipal;
import ru.stuff.service.ChessEngineService;
import ru.stuff.service.GameService;
import ru.stuff.service.SessionService;

import javax.annotation.PostConstruct;
import java.security.Principal;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

//There is main websocket logic controller
@Controller
public class WebSocketController {
    private static final Logger log = Logger.getLogger(WebSocketController.class);

    private final String CHESS_LOBBY = "/queue/chesslobby";
    private final String CHESS_EVENTS = "/topic/chesschat";
    @Autowired
    private GameService gameService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    private ChessEngineService chessAI;

    //When one player disconnected we can terminate the game (winner=opponent)
    //or pause game and wait until player reconnected
    private boolean terminateGameOnPlayerDisconnect = false;

    @MessageMapping("/achess/newgame")
    public void createGame(CreateGame info, WebSocketSessionPrincipal session) {


        GameInfo game = gameService.createGame(Duration.ofMinutes(info.getClockControl()),
                Duration.ofSeconds(info.getAdditionalSeconds()),
                info.isVsAI(), info.getFen());
        //Game created, now game owner should join to game
        joinGame(game.getGameId(), Optional.of(info.getRole()), session);

        broadcastEvent(new GameCreatedEvent(new GameInfoContainer(game)));
    }

    static final String GLOBAL_CHAT_PREFIX = "@all ";
    @MessageMapping("/achess/chat")
    public void handleChatNew(ChessChatMessage msg, WebSocketSessionPrincipal session) {


        Optional<Collection<GameInfo>> games = gameService.getPlayerGames(session);

        ChatMessageFlow flow = new ChatMessageFlow(session.getUser().getUsername(), msg.getPayload(), ChatEventsTypes.GLOBAL_CHAT);
        //User is member of some game
        if (games.isPresent()) {
            //TODO: in fact there can be only one game, size(games) == 1 !
            games.get().forEach((game) -> {
                Optional<GameRoles> playerRole = game.getGameRole(session);

                if (playerRole.isPresent()) {
                    ChatEventsTypes type;
                    //Hack for global chatting when you play game
                    if (msg.getPayload().startsWith(GLOBAL_CHAT_PREFIX)) {
                        type = ChatEventsTypes.GLOBAL_CHAT;
                    } else {
                        //Set message type according user role
                        GameRoles role = playerRole.get();
                        if (role.isBlack())
                            type = ChatEventsTypes.BLACK_CHAT_MSG;
                        else if (role.isWhite())
                            type = ChatEventsTypes.WHITE_CHAT_MSG;
                        else
                            type = ChatEventsTypes.SPECTATOR_CHAT_MSG;
                    }

                    flow.setType(type);

                    if (type.equals(ChatEventsTypes.GLOBAL_CHAT)) {
                        //Just Substract @all from message.
                        flow.setPayload(msg.getPayload().substring(GLOBAL_CHAT_PREFIX.length(),
                                msg.getPayload().length()));
                        broadcastEvent(flow);
                    } else
                        broadcastToGame(game, flow);
                } else {
                    log.warn(String.format("Game role is not present. GameId = %d User = %s", game.getGameId(), session.getName()));
                }
            });
        } else {
            broadcastEvent(flow);
        }
    }


    private void joinGame(int id, Optional<GameRoles> prefRole, WebSocketSessionPrincipal session) {
        //Assuming one websocket session = one game.
        gameService.removePlayerFromAllGames(session);

        Optional<GameInfo> gameOpt = gameService.gameById(id);
        if (gameOpt.isPresent()) {
            GameInfo game = gameOpt.get();

            //If preferable gamerole is present we try join with this gamerole
            //Otherwise - join as random
            Optional<GameRoles> role;
            if (!prefRole.isPresent() || prefRole.get().equals(GameRoles.RANDOM)) {
                role = gameService.joinGame(game, session);
            } else {
                role = gameService.joinGame(game, prefRole.get(), session);
            }

            if (role.isPresent()) { //Join success
                log.info(String.format("User %s join to game %s as %s", session.getUser().getUsername(), id, role));

                //We join as player
                if (role.get().isPlayer()) {
                    //game has 2 players?
                    if (game.readyToStart()) {
                        //Game staffing -> let's go!
                        gameService.startGame(game);
                        broadcastToGame(id, new GameStarted(GameInfoContainer.make(game)));
                    } else {
                        //Waiting for opponent, it was new game, created by this player.
                        log.info("Game not ready yet! Wait opponent");
                    }
                }
                //Answer to user
                sendToUser(session, new JoinGameResponse(role.get(), GameInfoContainer.make(game)));
                //If AI plays white -> make first move
                if (game.isVersusAI() &&
                        (game.getAIPlayer().getRole().isWhite() == game.isWhiteMove()) &&
                        !game.hasAnyMoves()) {
                    makeAIMove(game);
                }
                //We should update game list
                if (role.get().isPlayer())
                    broadcastEvent(new UpdateGameInfo(new GameInfoContainer(game)));

            } else {
                //Something wrong, can't join the game.
                //There we can handle error like: player try join as white player but white player already present
                log.info(String.format("Join to game is FAILED! %s \tgameId:%d", session.getName(), id));
            }
        }
    }

    @MessageMapping("/achess/joingame/{id}")
    public void joinGame(@DestinationVariable int id, WebSocketSessionPrincipal session) {
        joinGame(id, Optional.empty(), session);
    }

    //Called when player want gift additional time to opponent
    @MessageMapping("/achess/takeclock/{gameId}/{secAmount}")
    public void takeClock(@DestinationVariable int gameId,
                          @DestinationVariable int secAmount,
                          WebSocketSessionPrincipal session) {
        Optional<GameInfo> gameOpt = gameService.gameById(gameId);
        if (gameOpt.isPresent()) {
            GameInfo game = gameOpt.get();
            GameMember gm = game.getOpponent(session);
            if (gm != null) {
                game.addClockTimeTo(gm.getRole(), secAmount);
                broadcastToGame(gameId, SyncClock.fromGame(game),
                        new ChatMessageFlow(String.format("%s plus %d seconds!", gm.getRole(), secAmount), ChatEventsTypes.SYSTEM));
            } else {
                log.info("game.getOpponent return null -- spectators actions ignored");
            }
        }
    }

    //Draw offer
    @MessageMapping("/achess/offerdraw/{gameId}")
    public void draw(@DestinationVariable int gameId, WebSocketSessionPrincipal session) {
        Optional<GameInfo> gameOpt = gameService.gameById(gameId);
        if (gameOpt.isPresent()) {
            GameInfo game = gameOpt.get();
            Optional<GameRoles> role = game.getGameRole(session);
            //Somebody offers a draw, assert he is player
            if (role.isPresent() && role.get().isPlayer()) {
                //AI draw decision-making
                if (game.isVersusAI()) {
                    final int DRAW_THRESHOLD = 200;
                    //TODO: AI very fair there - we don't accept draw even if player has better position than AI
                    //TODO: if now is AI move, we should first make MOVE and then use centipawns from analysis
                    //TODO: (Player can make bad move)
                    if (Math.abs(game.getCentipawns()) < DRAW_THRESHOLD) {
                        drawGame(game);
                    } else {
                        declineDraw(game);
                    }
                }
                //Forward request to opponent
                else {
                    sendToUser(game.getOpponent(session).getSession(), new DrawRequest());
                    broadcastToGame(gameId, new ChatMessageFlow(String.format("%s offer draw ", session.getUser().getUsername()), ChatEventsTypes.SYSTEM));
                }
            } else {
                log.info(String.format("user %s is not player, draw request ignored.", session.getName()));
            }
        }
    }

    //Draw acception
    //TODO: after draw offer we should keep some flag signaling about draw is really was offered
    //TODO: and there - terminate game(draw) only if draw really has been offered
    @MessageMapping("/achess/drawaccept/{gameId}/{accepted}")
    public void drawAccept(@DestinationVariable int gameId,
                           @DestinationVariable boolean accepted,
                           WebSocketSessionPrincipal session) {
        Optional<GameInfo> gameOpt = gameService.gameById(gameId);
        if (gameOpt.isPresent()) {
            GameInfo game = gameOpt.get();
            if (accepted) {
                log.info("draw accepted!");
                drawGame(game);
            } else {
                declineDraw(game);
            }
        }
    }

    @MessageMapping("/achess/amove")
    public void move(Move info, WebSocketSessionPrincipal session) {
        log.debug(String.format("Move processing: %s %s %s",
                info.getGameId(), info.getMove(), session.getName()));
        Optional<GameInfo> gameOpt = gameService.gameById(info.getGameId());
        if (gameOpt.isPresent()) {
            GameInfo game = gameOpt.get();
            if (game.move(info.getAlgebraic())) {

                //Sync clocks and broadcast new move.
                broadcastToGame(game, SyncClock.fromGame(game),
                        new NewMove(info.getGameId(), info.getAlgebraic()));

                if (game.isMate()) {
                    terminateGame(game, GameOverReasons.MATE, game.getGameRole(session));
                }
                //TODO: calculate draw on server side, not client. (3x position reply, insufficient material, 50 move rule etc)
                if (info.isDraw()) {
                    drawGame(game);
                }
                //Well now AI turn...
                if (game.isVersusAI()) {
                    makeAIMove(game);
                }
            } else {
                log.warn(String.format("%s is bad algebraic move. Parse error.", info.getAlgebraic()));
            }
        }
    }

    private void makeAIMove(GameInfo game) {
        chessAI.scheduleBestMove(game.getFen(),
                game.getAIPlayer().getRole().isWhite() ? game.getWhiteClock() : game.getBlackClock(),
                (bm) -> {
                    //Make move
                    NewMove moveObj = new NewMove(game.getGameId(), bm.getFirstMove());

                    //TODO: if we make first AI move we not sure about user already subscribe to related queue
                    //TODO: make first move only after successfull subscribe to game topic
                    //TODO: current implementation - first AI move send though user queue
                    if (!game.hasAnyMoves() && game.getAIPlayer().getRole().isWhite()) {
                        Principal user = game.getPlayer(GameRoles.BLACK).getSession();
                        sendToUser(user, moveObj);
                    } else {
                        broadcastToGame(game.getGameId(), moveObj);
                    }

                    if (game.move(bm.getFirstMove())) {
                        //Store game estimage to implement draw desition making
                        if (bm.isMate()) {
                            //Mate unavoidable now, set maximum estimate
                            game.setCentipawns(Integer.MAX_VALUE);
                            //Mate in one move, terminate the game.
                        }
                        //Has engine position estimate, save it.
                        else
                            game.setCentipawns(bm.getScore());

                        if (game.isMate()) {
                            terminateGame(game, GameOverReasons.MATE, Optional.of(game.getAIPlayer().getRole()));
                        }

                        //TODO: we can also use game.getDomination() and game.getMaterial()
                        log.info(String.format("centipawns: %s(mate %b), domination: %s, material: %s",
                                bm.getScore(), bm.isMate(), game.getDomination(), game.getMaterial()));
                    } else {
                        log.warn(String.format("%s is bad algebraic move. Parse error.", bm.getFirstMove()));
                    }
                }

        );
    }

    @MessageMapping("/achess/undomove")
    public void undoMoveRequest(UndoMoveRequest request, WebSocketSessionPrincipal session) {
        log.debug(String.format("flow undoMoveRequest: %d", request.gameId));
        Optional<GameInfo> gameOpt = gameService.gameById(request.getGameId());
        if (gameOpt.isPresent()) {
            GameInfo game = gameOpt.get();
            Optional<GameRoles> role = game.getGameRole(session);
            if (role.isPresent() && role.get().isPlayer()) {
                //Always accept undo move request if opponent=AI
                if (game.isVersusAI()) {
                    makeUndoMove(game, true, role.get().isWhite());
                } else {
                    //Forward request to opponent
                    GameMember opponent = game.getOpponent(session);
                    if (opponent != null) {
                        sendToUser(opponent.getSession(), new UndoMoveServerRequest());
                    } else {
                        log.info("unable locate opponent for game: " + request.getGameId());
                    }
                }
            } else {
                //Ignore request
                log.info(String.format("user %s is not player, undo move request ignored.", session.getName()));
            }
        }
    }

    private void makeUndoMove(GameInfo game, boolean accepted, boolean fromWhite) {
        final boolean fullMoveUndo = accepted && game.undoMove(fromWhite);
        broadcastToGame(game.getGameId(), new UndoMoveReply(accepted, fullMoveUndo));
    }

    @MessageMapping("/achess/undomove_anwer")
    public void undoMoveReply(UndoMoveClientReply reply, WebSocketSessionPrincipal session) {
        log.debug(String.format("flow undoMoveReply: %s\tgameid: %d", reply.isAccepted, reply.gameId));
        Optional<GameInfo> gameOpt = gameService.gameById(reply.getGameId());
        if (gameOpt.isPresent()) {
            GameInfo game = gameOpt.get();
            Optional<GameRoles> role = game.getGameRole(session);
            if (role.isPresent()) {
                boolean fromWhite = !role.get().isWhite();
                makeUndoMove(game, reply.isAccepted(), fromWhite);
            }
        }
    }

    @MessageMapping("/achess/fen/{id}")
    public void fenRequest(@DestinationVariable int id, WebSocketSessionPrincipal session) {
        Optional<GameInfo> gameOpt = gameService.gameById(id);
        if (gameOpt.isPresent()) {
            GameInfo game = gameOpt.get();
            sendToUser(session, new FenPositionReply(game.getFen()));
        }
    }


    @MessageMapping("/achess/surrender")
    public void surrender(SimpleGameIdContainer gameInfo, WebSocketSessionPrincipal session) {
        Optional<GameInfo> gameOpt = gameService.gameById(gameInfo.getGameId());
        if (gameOpt.isPresent()) {
            GameInfo game = gameOpt.get();
            Optional<GameRoles> surrenderRole = game.getGameRole(session);
            if (surrenderRole.isPresent() && surrenderRole.get().isPlayer())
                terminateGame(game, GameOverReasons.SURRENDER, Optional.of(GameRoles.opposite(surrenderRole.get())));
            else {
                //Ignore spectator surrender click
                log.info(String.format("user %s is not player, surrender ignored.", session.getName()));
            }
        }
    }


    @SubscribeMapping("/init_done")
    public void onClientSubscribe(WebSocketSessionPrincipal session) {
        log.info("INIT DONE QUEUE SUBSCRIBE: " + session.getName());
        sendToUser(session, new SyncOnlineUsers(sessionService.getOnlineUsers()));
        Collection<GameInfo> existedGames = gameService.getExistedGames();
        Collection<GameInfoContainer> containers = new LinkedList<>();
        existedGames.forEach((gi) -> {
            containers.add(new GameInfoContainer(gi));
        });
        sendToUser(session, new SyncGames(containers));
    }

    @SubscribeMapping("/topic/game/{id}")
    public void gameSubscribe(@DestinationVariable int id, WebSocketSessionPrincipal session) {
        log.info("CONTROLLER GAME SUBSCRIBE TRIGGER: " + id);
    }

    @SubscribeMapping("/game/{id}")
    public void gameSubscribe2(@DestinationVariable int id, WebSocketSessionPrincipal session) {
        log.info("CONTROLLER GAME SUBSCRIBE2 TRIGGER: " + id);
    }

    @RequestMapping(value = "/")
    public String root() {
        return "chess_lobby";
    }

    @RequestMapping(value = "/chess")
    public String chess() {
        return "chess_lobby";
    }

    @RequestMapping(value = "/auth")
    public String login() {
        return "auth_page";
    }

    @PostConstruct
    public void postConstruct() {
        sessionService.addListener((session, isOnline) -> {
            List<WebSocketSessionPrincipal> sessions = sessionService.getSessions(session.getUser());
            if (isOnline) {
                //Notify about user connect only if it was first session
                //(we dont care if same user created multiple instances)
                if (sessions.size() == 1) {
                    broadcastEvent(new UserOnlineEvent(session.getUser().getId(), session.getUser().getUsername()));
                }
            } else {
                //Notify only when it was last session ( -> no more sessions from that client)
                if (sessions == null || sessions.size() == 0) {
                    broadcastEvent(new UserOfflineEvent(session.getUser().getId(), session.getUser().getUsername()));
                }
            }

            //Handle user disconnect
            if (!isOnline) {
                //Foreach games with this client..
                Optional<Collection<GameInfo>> affectedGames = gameService.getPlayerGames(session);
                if (affectedGames.isPresent()) {
                    affectedGames.get().forEach((game) -> {
                                Optional<GameRoles> disconnectedRole = game.getGameRole(session);
                                if (disconnectedRole.isPresent()) {
                                    //Remove client
                                    gameService.leaveGame(game, session);
                                    //Notify other players
                                    broadcastToGame(game.getGameId(), new PlayerDisconnectedEvent(session.getUser().getUsername(), disconnectedRole.get()));
                                    //Check our terminateGameOnPlayerDisconnect policy
                                    if (disconnectedRole.get().isPlayer()) {
                                        if (terminateGameOnPlayerDisconnect) {
                                            //Terminate the game
                                            terminateGame(game, GameOverReasons.DISCONNECT, Optional.of(GameRoles.opposite(disconnectedRole.get())));
                                        } else {
                                            //Pause the game until somebody join
                                            broadcastToGame(game.getGameId(), new GamePaused());
                                            gameService.pauseGame(game);
                                        }
                                        //Send information about game lobby changed (e.g. update opponents)
                                        broadcastEvent(new UpdateGameInfo(new GameInfoContainer(game)));
                                    }
                                }
                            }
                    );
                }
            }
        });
        gameService.addClockWatchListener((gi) -> {
            GameMember losePlayer = gi.getPlayer(gi.isWhiteMove() ? GameRoles.WHITE : GameRoles.BLACK);
            terminateGame(gi, GameOverReasons.CLOCK, Optional.of(GameRoles.opposite(losePlayer.getRole())));
        });
        gameService.addGameDeletedListener((gi) -> {
            broadcastEvent(new GameDeletedEvent(gi.getGameId()));
        });
    }

    private void broadcastToGame(int gameId, Object evt) {
        template.convertAndSend("/topic/game/" + gameId, evt);
    }

    private void broadcastToGame(int gameId, Object... evts) {
        for (Object evt : evts) broadcastToGame(gameId, evt);
    }

    private void broadcastToGame(GameInfo game, Object... evts) {
        broadcastToGame(game.getGameId(), evts);
    }

    private void broadcastToGame(GameInfo game, Object evt) {
        broadcastToGame(game.getGameId(), evt);
    }

    private void sendToUser(Principal p, Object evt) {
        template.convertAndSendToUser(p.getName(), CHESS_LOBBY, evt);
    }


    private void broadcastEvent(Object evt) {
        System.out.println("Try to broadcast event: " + evt);
        template.convertAndSend(CHESS_EVENTS, evt);
    }


    private void terminateGame(GameInfo game, GameOverReasons reason, Optional<GameRoles> winner) {
        if (!game.isRemoved()) {

            //Send GameTerminated event
            if (reason.equals(GameOverReasons.DRAW)) {
                broadcastToGame(game.getGameId(), GameTerminated.draw());
            } else if (winner.isPresent()) {
                broadcastToGame(game.getGameId(), new GameTerminated(reason, winner.get()));
            }
            //Now game not avaliable for user connections
            gameService.hideGame(game);
            //Ask chessAI about game analysis
            chessAI.scheduleAnalysis(game, (list) -> {
                //When analysis done: send AnalysisReport for this game
                Collection<AnalysisReportEntity> report = analysisReport(list, game.startsFromBlackMove());
                broadcastToGame(game, new AnalysisReport(game.getInitialPosition().getFEN(), report));
            }, (percentDone) -> {
                //Send progress report
                broadcastToGame(game, new AnalysisInProgress(percentDone));
            });
        } else {
            log.warn(String.format("terminateGame(gameId = %d) failed, reason: already removed", game.getGameId()));
        }
    }

    private void drawGame(GameInfo game) {
        terminateGame(game, GameOverReasons.DRAW, Optional.<GameRoles>empty());
    }

    private void declineDraw(GameInfo game) {
        broadcastToGame(game.getGameId(), new ChatMessageFlow("Draw request declined!", ChatEventsTypes.SYSTEM));
    }


    //Search mistakes/mates etc in given list of MoveAnalysis objects
    private Collection<AnalysisReportEntity> analysisReport(List<MoveAnalysis> moves, boolean startsFromBlackMove) {

        boolean isBlackTurn = startsFromBlackMove;
        UciInfo userMove;
        UciInfo bestMove;

        //Game estimate +MAX_SCORE - WHITE WINS, -MAX_SCORE - BLACK WINS
        float score;

        float MAX_SCORE = 10;
        int MAX_CENTIPAWNS = 1000; //MAX_SCORE reach on MAX_CENTIPAWNS
        //(If we lose queen it enough difference for win)

        //3 move mistake levels: low medium high
        //This is centipawns difference
        int INACCURACY_THRESH = 100;
        int MISTAKE_THRESH = 150;
        int BLUNDER_THRESH = 250;

        Collection<AnalysisReportEntity> report = new LinkedList<>();
        AnalysisReportEntity moveAnalysis;

        for (MoveAnalysis result : moves) {
            userMove = result.getPlayed();
            bestMove = result.getBestMove();
            //Calculate score based on mate or centipawns estimate from uci engine analysis

            if (userMove.isMate()) {
                score = userMove.getScore() > 0 ? MAX_SCORE : -MAX_SCORE;
            } else {
                if (Math.abs(userMove.getScore()) < MAX_CENTIPAWNS)
                    score = MAX_SCORE * userMove.getScore() / MAX_CENTIPAWNS;
                else
                    score = userMove.getScore() > 0 ? MAX_SCORE : -MAX_SCORE;
            }
            //UCI engine got estimate from side which should make next move
            //We need absolute score, if score > 0 -> white has advantage otherwise -> black
            score = isBlackTurn ? -score : score;

            moveAnalysis = new AnalysisReportEntity(result.getMove(), score);

            // Check uci estimates of best move and user move
            if (userMove.isMate()) {
                //User can mate by this after N moves, Good move.
                if (userMove.getScore() > 0) {
                    //Can we make mate faster than user?
                    if (bestMove.isMate() && userMove.getScore() > bestMove.getScore()) {
                        //bestMove.isMate() should always true ( if engine uci settings correct)
                        moveAnalysis.setType(MoveType.MATE_DELAYED);
                        moveAnalysis.setBest(result.getBestMoveSeqSan());
                    } else {
                        //Regular move.
                    }
                } else {  //User got mate after this move.. check maybe it unavoidable?
                    if (bestMove.isMate() && bestMove.getScore() < 0) {
                        //Mate (from engine view point) unavoidable -> any move regular
                    } else {
                        //Mate avoidable. Best Move is bestMove.
                        //TODO: what if we lost huge material here? It will be mate anyway.
                        moveAnalysis.setType(MoveType.BLUNDER_MU);
                        moveAnalysis.setBest(result.getBestMoveSeqSan());
                        moveAnalysis.setMateIn(userMove.getScore() * -1);
                    }
                }
            } else {
                //UserMove is not mate but bestMove is mate
                if (bestMove.isMate()) {
                    if (bestMove.getScore() > 0) {
                        //Blunder.. best move was bestMove with mate, userMove != mate
                        //Lose mate sequence
                        moveAnalysis.setType(MoveType.MATE_LOST);
                        moveAnalysis.setBest(result.getBestMoveSeqSan());
                        moveAnalysis.setMateIn(bestMove.getScore());
                    } else {
                        //Unreachable condition (with correct uci settings)
                        //User move is not mate when bestmove is mate to engine
                    }
                } else {
                    //Check centipawns difference.

                    int absDiff = Math.abs(bestMove.getScore() - userMove.getScore());

                    if (absDiff > INACCURACY_THRESH && absDiff < MISTAKE_THRESH) {
                        //Blunder.. best move was bestMove
                        moveAnalysis.setType(MoveType.INACCURACY);
                        //We send only first move here
                        moveAnalysis.setBest(result.getBestMoveSeqSan().subList(0, 1));
                    } else if (absDiff > MISTAKE_THRESH && absDiff < BLUNDER_THRESH) {
                        moveAnalysis.setType(MoveType.MISTAKE);
                        moveAnalysis.setBest(result.getBestMoveSeqSan());
                    } else if (absDiff > BLUNDER_THRESH) {
                        moveAnalysis.setType(MoveType.BLUNDER);
                        moveAnalysis.setBest(result.getBestMoveSeqSan());
                    } else {
                        //It is regular move.
                    }
                }
            }
            report.add(moveAnalysis);
            isBlackTurn = !isBlackTurn;
        }
        return report;
    }
}
