# ChessFactory

This is simple web chess project which illustrate Spring websocket usage example (STOMP + SockJS) <br>

###### It can be helpful for illustrate example usage of: 
    ApplicationListeners (handle session connect/disconnect) 
    Spring Security and Spring WebSocket integration 
    Handle multiple sessions from one client using HandshakeHandler (auth via spring security) 
    User's queue (with SimpMessagingTemplate as alternate @SendToUser annotation)
    Stockfish java integration (UCI example)

###### Implemented:
    Play versus another player
    Play versus engine
    Spectator mode
    Time control
    Actions: undo move request, draw offer, give time for opponent, surrender
    Full featured game analysis
    In-game chat
    Original piece icons :)

Screenshots:
## Game
![](https://raw.githubusercontent.com/mark-dev/chessfactory/master/screenshots/cf_game.jpg)
## Game creation
![](https://raw.githubusercontent.com/mark-dev/chessfactory/master/screenshots/cf_newgame.jpg)
## Analysis
![](https://raw.githubusercontent.com/mark-dev/chessfactory/master/screenshots/cf_analysis.jpg)
