package edu.goorm.userservice.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class UserSignupRequestDto {
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;

    @NotBlank(message = "사용자 이름은 필수 입력값입니다.")
    private String username;

    private String level;

    private Date birthDate;

    private String gender;

    private List<Long> categoryIdList;
}
