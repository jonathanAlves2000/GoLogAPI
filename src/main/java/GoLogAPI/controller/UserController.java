package GoLogAPI.controller;

import GoLogAPI.dto.UserDto;
import GoLogAPI.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService){
            this.userService = userService;
        }

        @PostMapping
        public UserDto SaveUser(@RequestBody UserDto userDto){
            return userService.saveUser(userDto);
        }

        @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
        public ResponseEntity<UserDto> GetUserById(@PathVariable Integer id){
            UserDto userDto = userService.getUserById(id);
            return ResponseEntity.ok(userDto);
        }

        @DeleteMapping("{id}")
        public void DeleteUser(@PathVariable Integer id){
            this.userService.deleteUser(id);
        }

        @PutMapping("{id}")
        public UserDto UpdateUser(@PathVariable int id, @RequestBody UserDto userDto){
            return userService.updateUser(id, userDto);
        }

}
