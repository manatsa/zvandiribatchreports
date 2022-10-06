package zw.org.zvandiri.batch.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zw.org.zvandiri.business.domain.User;
import zw.org.zvandiri.business.service.UserService;

/**
 * @author :: codemaster
 * created on :: 4/10/2022
 * Package Name :: zw.org.zvandiri.batch.configs
 */

@Configuration
public class BatchConfiguration {
    @Autowired
    UserService userService;

    @Bean("currentUser")
    public User getCurrentUser(){
        return userService.getCurrentUser();
    }
}
