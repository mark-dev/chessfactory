package ru.stuff.chess.sys.game;

/**
 * Created by mark on 27.12.14.
 */
public enum GameRoles {
    BLACK, WHITE, SPECTATOR, RANDOM;

    public boolean isWhite() {
        return equals(WHITE);
    }

    public boolean isBlack() {
        return equals(BLACK);
    }

    public boolean isPlayer() {
        return isBlack() || isWhite();
    }

    public static GameRoles opposite(GameRoles role) {
        return role.equals(BLACK) ? WHITE : BLACK;
    }
}
