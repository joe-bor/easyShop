package org.yearup.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.yearup.data.UserDao;
import org.yearup.models.User;

import java.security.Principal;

@Component
@AllArgsConstructor
public class LoggedInUser {

    private UserDao userDao;

    public int getUserId(Principal principal) {
        try {
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getByUserName(userName);
            return user.getId();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
