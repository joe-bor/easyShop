package org.yearup.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;
import org.yearup.models.User;
import org.yearup.models.authentication.LoginResponseDto;
import org.yearup.security.jwt.TokenProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@RestController
@RequestMapping("/oauth2")
@CrossOrigin
@PreAuthorize("permitAll()")
@AllArgsConstructor
public class OAuth2Controller {
    private TokenProvider tokenProvider;
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    private ProfileDao profileDao;
    private UserDao userDao;

    @GetMapping("/success")
    public ResponseEntity<LoginResponseDto> success(@AuthenticationPrincipal OAuth2User oAuth2User) throws JsonProcessingException {
        /*
        - verify existence of user
        - if not found: register
        - login
        - create jwt and send it back through url params
         */

        User user = createUserIfNotExist(oAuth2User);

        Profile profile = createProfile(oAuth2User);
        profile.setUserId(user.getId());
        this.profileDao.create(profile);

        // then login -> return JWTs
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(user.getUsername(), oAuth2User.getAttribute("node_id"));

        Authentication authentication =
                authenticationManagerBuilder.getObject().authenticate(usernamePasswordAuthenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication, false);

        String userJson = new ObjectMapper().writeValueAsString(user);

        String frontendRedirectUrl = "http://127.0.0.1:5500/index.html?token=" + jwt + "&user=" + URLEncoder.encode(userJson, StandardCharsets.UTF_8);

        // redirect
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, frontendRedirectUrl)
                .build();

    }

    @GetMapping("/failure")
    public ResponseEntity<String> failure() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed!");
    }

    private Profile createProfile(OAuth2User oAuth2User) {
        // Extract name
        String fullName = oAuth2User.getAttribute("name");
        String firstName = "";
        String lastName = "";
        if (fullName != null) {
            String[] names = fullName.split(" ");
            if (names.length > 1) {
                firstName = names[0];
                lastName = names[1];
            }
        }

        // Extract email
        String email = oAuth2User.getAttribute("email");

        Profile profile = new Profile();
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setEmail(email);
        return profile;
    }

    private User createUserIfNotExist(OAuth2User oAuth2User) {
        String userName = oAuth2User.getAttribute("login");
        String node_id = oAuth2User.getAttribute("node_id");

        User user = userDao.getByUserName(userName);
        if (user == null) {
            user = new User(0, userName, node_id, "ROLE_ADMIN");
            user = userDao.create(user);
        }

        return user;
    }
}
