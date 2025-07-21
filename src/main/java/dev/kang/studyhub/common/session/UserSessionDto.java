package dev.kang.studyhub.common.session;

import dev.kang.studyhub.user.entity.User;
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