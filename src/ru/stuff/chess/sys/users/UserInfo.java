package ru.stuff.chess.sys.users;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Created by mark on 20.01.15.
 */
public class UserInfo extends User implements Comparable<UserInfo> {
    private int id;

    public UserInfo(int id,
                    String username,
                    String password,
                    Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserInfo userInfo = (UserInfo) o;
        return o.equals(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }


    private static String getSystemUsername(String username, long sessionId) {
        return username + "@" + sessionId;
    }


    @Override
    public int compareTo(UserInfo o) {
        return o.getUsername().compareTo(getUsername());
    }

    public int getId() {
        return id;
    }
}
