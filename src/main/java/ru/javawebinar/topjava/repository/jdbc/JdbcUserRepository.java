package ru.javawebinar.topjava.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
@Transactional(readOnly = true)
public class JdbcUserRepository implements UserRepository {

    private static final BeanPropertyRowMapper<User> ROW_MAPPER = BeanPropertyRowMapper.newInstance(User.class);

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final SimpleJdbcInsert insertUser;

    private final Validator validator;


    @Autowired
    public JdbcUserRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, Validator validator) {
        this.insertUser = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.validator = validator;
    }

    private final ResultSetExtractor<List<User>> userWithRolesExtractor = resultSet -> {
        Map<Integer, User> userMap = new LinkedHashMap<>();

        while (resultSet.next()) {
            int userId = resultSet.getInt("id");
            User user = userMap.get(userId);

            if (user == null) {
                user = ROW_MAPPER.mapRow(resultSet, resultSet.getRow());
                user.setRoles(EnumSet.noneOf(Role.class));
                userMap.put(userId, user);
            }

            String role = resultSet.getString("role");
            if (role != null) {
                user.getRoles().add(Role.valueOf(role));
            }
        }

        return new ArrayList<>(userMap.values());
    };

    @Override
    @Transactional
    public User save(User user) {
        validate(user);

        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(user);

        if (user.isNew()) {
            Number newKey = insertUser.executeAndReturnKey(parameterSource);
            user.setId(newKey.intValue());
            insertRoles(user);
        } else {
            if (namedParameterJdbcTemplate.update("""
                   UPDATE users SET name=:name, email=:email, password=:password, 
                   registered=:registered, enabled=:enabled, calories_per_day=:caloriesPerDay WHERE id=:id
                """, parameterSource) == 0) {
                return null;
            }
            deleteRoles(user);
            insertRoles(user);
        }
        return user;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        jdbcTemplate.update("DELETE FROM user_role WHERE user_id=?", id);
        return jdbcTemplate.update("DELETE FROM users WHERE id=?", id) != 0;
    }

    @Override
    public User get(int id) {
        List<User> users = jdbcTemplate.query("""
                SELECT u.*, ur.role 
                FROM users u 
                LEFT JOIN user_role ur ON u.id = ur.user_id 
                WHERE u.id=?
                """, userWithRolesExtractor, id);
        return DataAccessUtils.singleResult(users);
    }

    @Override
    public User getByEmail(String email) {
//        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE email=?", ROW_MAPPER, email);
        List<User> users = jdbcTemplate.query("""
                SELECT u.*, ur.role 
                FROM users u 
                LEFT JOIN user_role ur ON u.id = ur.user_id 
                WHERE u.email=?
                """, userWithRolesExtractor, email);
        User user = DataAccessUtils.singleResult(users);
        if (user != null) {
            return user;
        }
        return null;
    }

    @Override
    public List<User> getAll() {
        return jdbcTemplate.query("""
                SELECT u.*, ur.role 
                FROM users u 
                LEFT JOIN user_role ur ON u.id = ur.user_id 
                ORDER BY u.name, u.email
                """, userWithRolesExtractor);
    }

    private void insertRoles(User user) {
        Set<Role> roles = user.getRoles();
        if (roles != null && !roles.isEmpty()) {
            jdbcTemplate.batchUpdate("INSERT INTO user_role (user_id, role) VALUES (?, ?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                            Role role = new ArrayList<>(roles).get(i);
                            preparedStatement.setInt(1, user.getId());
                            preparedStatement.setString(2, role.name());
                        }

                        @Override
                        public int getBatchSize() {
                            return roles.size();
                        }
                    });
        }
    }

    private void deleteRoles(User user) {
        jdbcTemplate.update("DELETE FROM user_role WHERE user_id=?", user.getId());
    }

    private void validate(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
