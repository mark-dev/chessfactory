package ru.stuff.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.stuff.chess.sys.users.UserInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mark on 24.01.15.
 */
@Component
public class AuthService implements UserDetailsService {
    @Autowired
    private SessionService storage;

    private final Map<String, Integer> users = new TreeMap<>();

    private final AtomicInteger userId = new AtomicInteger(0);

    //There we can implement loading user details
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        boolean enabled = true,
                accountNonExpired = true,
                credentialsNonExpired = true,
                accountNonLocked = true;
        List<GrantedAuthority> authorities = Arrays.asList(() -> "ROLE_USER");
        Integer id = users.get(username);
        if (id == null) {
            id = userId.incrementAndGet();
            users.put(username, id);
        }
        return new UserInfo(id, username, "", authorities);
    }
}
