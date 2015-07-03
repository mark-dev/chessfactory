package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;

/**
 * Created by mark on 30.12.14.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class UserOnlineEvent {
    public String username;
    public int id;

    public String getUsername() {
        return username;
    }

    public UserOnlineEvent(int id,String username) {
        this.username = username;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;

    }
}
