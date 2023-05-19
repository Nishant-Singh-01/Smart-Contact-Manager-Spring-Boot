package com.example.smartcontactmanager.config;



import com.example.smartcontactmanager.UserRepository;
import com.example.smartcontactmanager.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.getUserByUserName(username);

        if(user==null){
            throw new UsernameNotFoundException("could not found user !!");
        }
        CustomUserDetails customUserDetails=new CustomUserDetails(user);
        return customUserDetails ;

    }
}