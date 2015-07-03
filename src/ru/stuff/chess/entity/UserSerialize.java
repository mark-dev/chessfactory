package ru.stuff.chess.entity;

/**
 * Created by mark on 14.05.15.
 */
public class UserSerialize implements Comparable<UserSerialize> {
    public String username;
    public int id;

    public UserSerialize(String username, int id) {
        this.username = username;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(UserSerialize o) {
        return Integer.compare(o.getId(), id);
    }
}
