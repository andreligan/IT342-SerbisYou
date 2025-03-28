package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.service.UserAuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/user-auth")
public class UserAuthController {

    private final UserAuthService userAuthService;

    public UserAuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    // DELETE user authentication record
    @DeleteMapping("/{authId}")
    public String deleteUserAuth(@PathVariable Long authId) {
        return userAuthService.deleteUserAuth(authId);
    }
}
