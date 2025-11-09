package SmartRouteAPI.controller;

    import SmartRouteAPI.service.UserService;
    import SmartRouteAPI.model.User;
    import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/user")
    public class UserController {

        private final UserService userService;

        public UserController(UserService userService){
            this.userService = userService;
        }

        @PostMapping
        public User SaveUser(@RequestBody User user){
            return userService.saveUser(user);
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
