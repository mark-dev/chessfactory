package ru.stuff.chess.entity.outgoing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.stuff.chess.entity.UserSerialize;
import ru.stuff.chess.entity.jsonutils.SimpleJsonSerializer;
import ru.stuff.chess.sys.users.UserInfo;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Created by mark on 12.01.15.
 */
@JsonSerialize(using = SimpleJsonSerializer.class)
public class SyncOnlineUsers {
    public Collection<UserSerialize> users;

    public SyncOnlineUsers(Collection<UserInfo> ausers) {
        users = new TreeSet<>();
        ausers.forEach((u) -> {
            users.add(new UserSerialize(u.getUsername(), u.getId()));
        });
    }

    public SyncOnlineUsers() {
    }

    public Collection<UserSerialize> getUsers() {
        return users;
    }

    public void setUsers(Collection<UserSerialize> users) {
        this.users = users;
    }
}

