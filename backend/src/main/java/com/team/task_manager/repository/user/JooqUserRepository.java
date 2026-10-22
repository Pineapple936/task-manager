package com.team.task_manager.repository.user;

import java.util.Optional;

import com.team.task_manager.jooq.generated.tables.records.UsersRecord;
import com.team.task_manager.repository.user.entity.UserDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.team.task_manager.jooq.generated.Tables.USERS;

@Repository
@RequiredArgsConstructor
public class JooqUserRepository implements UserRepository {
    private final DSLContext dsl;

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return dsl.selectFrom(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOptional(JooqUserRepository::toDto);
    }

    @Override
    public UserDto create(String username, String passwordHash) {
        UsersRecord user = dsl.insertInto(USERS)
                .set(USERS.USERNAME, username)
                .set(USERS.PASSWORD_HASH, passwordHash)
                .returning()
                .fetchSingle();

        return toDto(user);
    }

    private static UserDto toDto(UsersRecord user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getCreatedAt()
        );
    }
}
