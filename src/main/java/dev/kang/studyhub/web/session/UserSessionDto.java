package dev.kang.studyhub.web.session;

import dev.kang.studyhub.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserSessionDto {
    private final Long id;
    private final String name;

    public UserSessionDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
    }
}