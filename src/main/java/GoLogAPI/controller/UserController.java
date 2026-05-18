package GoLogAPI.controller;

import GoLogAPI.dto.user.UserCreateRequest;
import GoLogAPI.dto.user.UserUpdateRequest;
import GoLogAPI.dto.user.UserResponse;
import GoLogAPI.dto.user.UserResponseList;
import GoLogAPI.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@Tag(name = "User")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService){
            this.userService = userService;
        }

        @Operation(summary = "Create", description = "Create new user")
        @PostMapping
        public ResponseEntity<UserResponse> save(@Valid @RequestBody UserCreateRequest userCreateRequest){
            UserResponse userResponse = userService.save(userCreateRequest);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("{id}")
                    .buildAndExpand(userResponse.id())
                    .toUri();
            return ResponseEntity.created(uri).body(userResponse);
        }

        @Operation(summary = "Display", description = "Display User")
        @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
        public ResponseEntity<UserResponse> get(@PathVariable("id") UUID id){
            UserResponse userResponse = userService.get(id);
            return ResponseEntity.ok(userResponse);
        }

        @Operation(summary = "Display List", description = "Display User List")
        @GetMapping
        public ResponseEntity<List<UserResponseList>> getAll(){
            List<UserResponseList> users = userService.getAll();
            return ResponseEntity.ok().body(users);
        }

        @Operation(summary = "Delete", description = "Delete User")
        @DeleteMapping("{id}")
        public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
            userService.delete(id);
            return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Update", description = "Update User")
        @PutMapping("{id}")
        public ResponseEntity<UserResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody UserCreateRequest userCreateRequest){
            UserResponse userResponse = userService.update(id, userCreateRequest);
            return ResponseEntity.ok().body(userResponse);
        }

        @Operation(summary = "Update Partial", description = "Update User Partial")
        @PatchMapping("{id}")
        public ResponseEntity<UserResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody UserUpdateRequest userUpdateRequest){
            UserResponse userResponse = userService.updatePartial(id, userUpdateRequest);
            return ResponseEntity.ok().body(userResponse);
        }

}
