package GoLogAPI.controller;

import GoLogAPI.dto.user.UserCreateRequest;
import GoLogAPI.dto.user.UserPatchRequest;
import GoLogAPI.dto.user.UserResponse;
import GoLogAPI.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService){
            this.userService = userService;
        }

        @PostMapping
        public UserResponse SaveUser(@Valid @RequestBody UserCreateRequest userCreateRequest){
            return userService.saveUser(userCreateRequest);
        }

        @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
        public ResponseEntity<UserResponse> GetUser(@PathVariable UUID id){
            UserResponse userResponse = userService.getUser(id);
            return ResponseEntity.ok(userResponse);
        }

        @DeleteMapping("{id}")
        public void DeleteUser(@PathVariable UUID id){
            this.userService.deleteUser(id);
        }

        @PutMapping("{id}")
        public UserResponse PutUser(@PathVariable UUID id, @Valid @RequestBody UserCreateRequest userCreateRequest){
            return userService.putUser(id, userCreateRequest);
        }

        @PatchMapping("{id}")
        public UserResponse patchUser(@PathVariable UUID id, @Valid @RequestBody UserPatchRequest userPatchRequest){
            return userService.patchUser(id, userPatchRequest);
        }

}
