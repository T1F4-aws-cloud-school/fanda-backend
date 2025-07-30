package com.fanda.auth.jwt.userdetails;

import com.fanda.auth.entity.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final Role role;

    public CustomUserDetails(String username, String password, Role role){
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Role getRole(){
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }

    @Override
    public String getPassword(){
        return password;
    }

    @Override
    public String getUsername(){
        return username;
    }
}
