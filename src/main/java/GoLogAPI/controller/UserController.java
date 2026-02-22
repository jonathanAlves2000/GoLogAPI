package GoLogAPI.controller;

import GoLogAPI.dto.user.UserCreateRequest;
import GoLogAPI.dto.user.UserPatchRequest;
import GoLogAPI.dto.user.UserResponse;
import GoLogAPI.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@Tag(name = "User")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService){
            this.userService = userService;
        }

        @Operation(summary = "Save", description = "Register new user")
        @ApiResponses({
                @ApiResponse(responseCode = "201", description = "Successfully registerd"),
                @ApiResponse(responseCode = "409", description = "User already registered")
        })
        @PostMapping
        public UserResponse save(@Valid @RequestBody UserCreateRequest userCreateRequest){
            return userService.save(userCreateRequest);
        }

        @Operation(summary = "Display", description = "Display User")
        @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
        public ResponseEntity<UserResponse> get(@PathVariable UUID id){
            UserResponse userResponse = userService.get(id);
            return ResponseEntity.ok(userResponse);
        }

        @Operation(summary = "Delete", description = "Delete User")
        @DeleteMapping("{id}")
        public void delete(@PathVariable UUID id){
            this.userService.delete(id);
        }

        @Operation(summary = "Update", description = "Update User")
        @PutMapping("{id}")
        public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UserCreateRequest userCreateRequest){
            return userService.update(id, userCreateRequest);
        }

        @Operation(summary = "Update", description = "Update User Data")
        @PatchMapping("{id}")
        public UserResponse updatePartial(@PathVariable UUID id, @Valid @RequestBody UserPatchRequest userPatchRequest){
            return userService.updatePartial(id, userPatchRequest);
        }

}
