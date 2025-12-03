package BlackAdhuleSystem.dev.userAdvicesMariadb.mapper;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.AdviceDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Advice;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;

public class AdviceMapper {

    public static AdviceDto mapToAdviceDto(Advice advice) {
        UserDto userDto = advice.getUser() != null ? UserMapper.toDto(advice.getUser()) : null;
        return new AdviceDto(
                advice.getId(),
                advice.getMessage(),
                advice.getStatus(),
                userDto,
                advice.getCreatedAt()
        );
    }

    public static Advice mapToAdvice(AdviceDto adviceDto) {
        User user = adviceDto.getUserDto() != null ? UserMapper.toEntity(adviceDto.getUserDto()) : null;
        Advice advice = new Advice();
        advice.setId(adviceDto.getId());
        advice.setMessage(adviceDto.getMessage());
        advice.setStatus(adviceDto.getStatus());
        advice.setUser(user);
        // ⚠️ createdAt est auto-géré par Hibernate, inutile de le setter
        return advice;
    }
}
