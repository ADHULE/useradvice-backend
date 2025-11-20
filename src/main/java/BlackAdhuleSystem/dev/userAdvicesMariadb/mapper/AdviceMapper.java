package BlackAdhuleSystem.dev.userAdvicesMariadb.mapper;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.AdviceDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Advice;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;

public class AdviceMapper {


    public static AdviceDto mapToAdviceDto(Advice advice) {
        UserDto userDto=UserMapper.mapToUserDto(advice.getUser());
        return new AdviceDto(
                advice.getId(),
                advice.getMessage(),
                advice.getStatus() ,
                userDto


        );
    }

    public static Advice mapToAdvice(AdviceDto adviceDto) {
        User user =UserMapper.mapToUser(adviceDto.getUserDto());
        Advice advice = new Advice();
        advice.setId(adviceDto.getId());
        advice.setMessage(adviceDto.getMessage());
        advice.setStatus(adviceDto.getStatus());
        advice.setUser(user);
        return advice;
    }
}
