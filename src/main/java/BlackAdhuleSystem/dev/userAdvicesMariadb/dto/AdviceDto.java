package BlackAdhuleSystem.dev.userAdvicesMariadb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdviceDto {
    private Long id;
    private String message;
    private String status;
    private UserDto userDto;
    private LocalDateTime createdAt;
}
