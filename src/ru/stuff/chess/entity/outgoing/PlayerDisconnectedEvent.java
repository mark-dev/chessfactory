package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;
import ru.stuff.chess.sys.game.GameRoles;

/**
 * Created by mark on 15.01.15.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class PlayerDisconnectedEvent {
    public String uname;
    public GameRoles role;

    public PlayerDisconnectedEvent(String uname, GameRoles disconnectedRole) {
        this.uname = uname;
        this.role = disconnectedRole;
    }

    public PlayerDisconnectedEvent() {
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public GameRoles getRole() {
        return role;
    }

    public void setRole(GameRoles role) {
        this.role = role;
    }
}
