package org.yearup.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yearup.data.ProfileDao;
import org.yearup.models.Profile;
import org.yearup.utils.LoggedInUser;

import java.security.Principal;

@RestController
@AllArgsConstructor
@RequestMapping("profile")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class ProfileController {

    LoggedInUser loggedInUser;
    ProfileDao profileDao;

    @GetMapping
    public ResponseEntity<Profile> getProfile(Principal principal){
        int userId = loggedInUser.getUserId(principal);
        Profile profile = this.profileDao.getProfile(userId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }
}
