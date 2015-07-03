This is simple web chess project which illustrate Spring websocket usage example (STOMP + SockJS) <br>

It can be helpful for illustrate example usage of:
    ApplicationListeners (handle session connect/disconnect)
    Spring Security and Spring WebSocket integration
    Handle multiple sessions from one client using HandshakeHandler (auth via spring security)
    User's queue (with SimpMessagingTemplate as alternate @SendToUser annotation)
    Stockfish java integration

Implemented:
    Play versus another player
    Play versus computer
    Spectating games
    Time control
    Actions: undo move request, draw offer, give time for opponent, surrender
    Full featured game analysis
	In-game chat

Screenshots:
    Main
    Game creation
    Analysis


