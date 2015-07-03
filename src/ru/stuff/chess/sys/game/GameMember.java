package ru.stuff.chess.sys.game;

import org.springframework.security.core.GrantedAuthority;
import ru.stuff.chess.sys.users.UserInfo;
import ru.stuff.chess.sys.users.WebSocketSessionPrincipal;

import java.util.Collections;

/**
 * Created by mark on 27.12.14.
 */
public class GameMember implements Comparable<GameMember> {
    private WebSocketSessionPrincipal session;
    private GameRoles role = GameRoles.SPECTATOR;

    public GameMember(WebSocketSessionPrincipal user) {
        this.session = session;
    }


    public GameMember(WebSocketSessionPrincipal session, GameRoles role) {
        this.session = session;
        this.role = role;
    }

    public WebSocketSessionPrincipal getSession() {
        return session;
    }

    public UserInfo getUser() {
        return session.getUser();
    }

    public GameRoles getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameMember that = (GameMember) o;

        return session.equals(that.session);

    }

    @Override
    public int hashCode() {
        return session.hashCode();
    }

    @Override
    public int compareTo(GameMember o) {
        return session.compareTo(o.getSession());
    }
}

class AIPlayer extends GameMember {
    private static String NAME_PLACEHOLDER = "AI";

    public AIPlayer(GameRoles role) {
        super(new WebSocketSessionPrincipal(new UserInfo(0,NAME_PLACEHOLDER, "", Collections.<GrantedAuthority>emptyList()), 0), role);
    }

    @Override
    public int hashCode() {
        return (int) (super.hashCode() + getUser().hashCode());
    }

    public long getId() {
        return getSession().getId();
    }

    @Override
    public int compareTo(GameMember o) {
        if (o instanceof AIPlayer) {
            return Long.compare(getId(), ((AIPlayer) o).getId());
        } else
            return getUser().compareTo(o.getUser());
    }
}