package ru.javawebinar.topjava.repository;

import ru.javawebinar.topjava.model.Meal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalMealRepository implements MealRepository {
    private final Map<Integer, Meal> repository = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Meal save(Meal meal) {
        if (meal.isNew()) {
            meal.setId(counter.incrementAndGet());
            repository.put(meal.getId(), meal);
            return meal;
        }
        return repository.replace(meal.getId(), meal) != null ? meal : null;
    }

    @Override
    public boolean delete(int id) {
        return repository.remove(id) != null;
    }

    @Override
    public Meal get(int id) {
        return repository.get(id);
    }

    @Override
    public List<Meal> getAll() {
        return new ArrayList<>(repository.values());
    }
}
