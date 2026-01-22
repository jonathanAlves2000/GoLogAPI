package GoLogAPI.mapper;

import GoLogAPI.dto.UserDto;
import GoLogAPI.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "Spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    User toEntity(UserDto userDto);

    //@Mapping(target = "password", ignore = true)
    @Mapping(source = "company.id", target = "companyId")
    UserDto toDto(User user);
}
