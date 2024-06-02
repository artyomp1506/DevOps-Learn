package com.example.demo.service;

import com.example.demo.dto.JwtRequest;
import com.example.demo.entity.user.Role;
import com.example.demo.entity.user.User;
import com.example.demo.exception.IncorrectDataException;
import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.repository.IUserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Getter
@AllArgsConstructor
public class UserService {
    private IUserRepository userRepository;
    private BCryptPasswordEncoder encoder;

    public User createUser(String username, String password) throws UserAlreadyExistsException {
        if (userRepository.existsByUsername(username)){
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }  else {
            var user = new User();
            user.setUsername(username);
            user.setPassword(encoder.encode(password));
            user.setRole(Role.Student);
            userRepository.save(user);

            return userRepository.findByUsername(user.getUsername());
        }

    }
    public User login(JwtRequest request)
    {
       if (encoder.matches(request.getPassword(), userRepository.findByUsername(request.getUsername()).getPassword()))
           return userRepository.findByUsername(request.getUsername());
       throw new IncorrectDataException("Введены неверные данные");
    }
    public User getUserFromPrincipal(Principal principal)
    {
        return userRepository.findByUsername(principal.getName());
    }
    public User getById(long userId) {
        return  userRepository.findById(userId).get();
    }


}
