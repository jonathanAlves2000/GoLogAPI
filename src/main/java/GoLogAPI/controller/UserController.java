package GoLogAPI.controller;

import GoLogAPI.service.UserService;
import GoLogAPI.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/user")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService){
            this.userService = userService;
        }

        @PostMapping
        public User SaveUser(@RequestBody User user){
            try{
                return userService.saveUser(user);
            }catch (IllegalArgumentException e){
                var messageError = e.getMessage();
                throw new ResponseStatusException(HttpStatus.CONFLICT, messageError);
            }
        }

        @GetMapping("{id}")
        public User GetUserById(@PathVariable Integer id){
            return userService.getUserById(id);
        }

        @DeleteMapping("{id}")
        public void DeleteUser(@PathVariable Integer id){
            this.userService.deleteUser(id);
        }

        @PutMapping("{id}")
        public User UpdateUser(@PathVariable Integer id, @RequestBody User user){
            return userService.updateUser(id, user);
        }

}
