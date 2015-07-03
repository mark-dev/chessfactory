package ru.stuff.chess.sys.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.stuff.service.GameService;

/**
 * Created by mark on 16.01.15.
 */
@Component
public class ChessClockManager {
    @Autowired
    private GameService gameService;


    @Scheduled(fixedRate = 100)
    public void tickTimers() {

        gameService.getActiveGames().forEach((gi) -> {
            if (gi.isWhiteMove() ? gi.tickWhiteClock() : gi.tickBlackClocks()) {
                gameService.timeElapsed(gi);
            }
        });
    }
}
