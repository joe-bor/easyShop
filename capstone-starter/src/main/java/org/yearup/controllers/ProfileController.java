package org.yearup.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
        Profile profile = this.profileDao.getProfileById(userId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<Profile> updateProfile(Principal principal, @RequestBody Profile profile){
        int userId = loggedInUser.getUserId(principal);
        Profile updatedProfile = this.profileDao.updateProfile(userId, profile);

        return ResponseEntity.ok(updatedProfile);
    }
}
