package GoLogAPI.mapper;

import GoLogAPI.dto.user.UserCreateRequest;
import GoLogAPI.dto.user.UserResponse;
import GoLogAPI.dto.user.UserResponseList;
import GoLogAPI.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "Spring", uses = {CompanyMapper.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    User toEntity(UserCreateRequest userCreateRequest);

    UserResponse toResponse(User user);

    List<UserResponseList> toResponses(List<User> users);
}
