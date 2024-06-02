package com.example.demo.service;

import com.example.demo.entity.user.Role;
import com.example.demo.repository.IUserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@AllArgsConstructor
public class UserDetailService implements UserDetailsService {
    private IUserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username);
        if (user!=null)
        {
            return new org.springframework.security.core.userdetails.User
                    (user.getUsername(), user.getPassword(), mapRolesToAuthorities(Set.of(user.getRole())));
        }
        throw new UsernameNotFoundException("Пользователь не найден");
    }
    public UserDetails loadUserById(Long id)
    {
        var user = userRepository.findById(id);
        if (user.isPresent())
        {
            var current_user = user.get();
            return new org.springframework.security.core.userdetails.User
                    (current_user.getUsername(), current_user.getPassword(), mapRolesToAuthorities(Set.of(current_user.getRole())));
        }
        throw new UsernameNotFoundException("Пользователь не найден");
    }
    private List<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles)
    {
        return roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" +r.name())).collect(Collectors.toList());
    }
}
