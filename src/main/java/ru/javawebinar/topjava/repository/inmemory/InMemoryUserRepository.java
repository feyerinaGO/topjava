package ru.javawebinar.topjava.repository.inmemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private static final Logger log = LoggerFactory.getLogger(InMemoryUserRepository.class);

    private final Map<Integer, User> usersMap = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    {
        save(new User(null, "User", "user@yandex.ru", "password", Role.USER));
        save(new User(null, "Admin", "admin@gmail.com", "admin", Role.ADMIN));
    }

    @Override
    public User save(User user) {
        if (user.isNew()) {
            user.setId(counter.incrementAndGet());
            usersMap.put(user.getId(), user);
            return user;
        }
        return usersMap.computeIfPresent(user.getId(), (id, oldUser) -> user);
    }

    @Override
    public boolean delete(int id) {
        return usersMap.remove(id) != null;
    }

    @Override
    public User get(int id) {
        return usersMap.get(id);
    }

    @Override
    public User getByEmail(String email) {
        return usersMap.values().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> getAll() {
        return usersMap.values().stream()
                .sorted(Comparator.comparing(User::getName))
                .collect(Collectors.toList());
    }
}