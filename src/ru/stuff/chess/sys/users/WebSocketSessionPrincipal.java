package ru.stuff.chess.sys.users;

import java.security.Principal;

/**
 * Created by mark on 10.03.15.
 */
public class WebSocketSessionPrincipal implements Principal, Comparable<WebSocketSessionPrincipal> {
    private UserInfo user;
    private long uniqueId;

    public WebSocketSessionPrincipal(UserInfo user, long uniqueId) {
        this.user = user;
        this.uniqueId = uniqueId;
    }

    @Override
    public String getName() {
        return user.getUsername() + "@" + uniqueId;
    }

    public long getId() {
        return uniqueId;
    }

    @Override
    public int compareTo(WebSocketSessionPrincipal o) {
        return Long.compare(uniqueId, o.getId());
    }

    public UserInfo getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WebSocketSessionPrincipal that = (WebSocketSessionPrincipal) o;

        if (uniqueId != that.uniqueId) return false;
        return user.equals(that.user);

    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + (int) (uniqueId ^ (uniqueId >>> 32));
        return result;
    }
}
