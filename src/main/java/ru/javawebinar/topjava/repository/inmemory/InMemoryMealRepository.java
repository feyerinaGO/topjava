package ru.javawebinar.topjava.repository.inmemory;

import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.MealsUtil;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class InMemoryMealRepository implements MealRepository {
    private final Map<Integer, Meal> mealsMap = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    {
        MealsUtil.meals.forEach(meal -> save(new Meal(
                meal.getDateTime(),
                meal.getDescription(),
                meal.getCalories(),
                1 // userId for demo (user)
        ), 1));
        MealsUtil.mealsAdmin.forEach(meal -> save(new Meal(
                meal.getDateTime(),
                meal.getDescription(),
                meal.getCalories(),
                2 // userId for demo (admin)
        ), 2));
    }

    @Override
    public Meal save(Meal meal, int userId) {
        if (meal.isNew()) {
            meal.setId(counter.incrementAndGet());
            mealsMap.put(meal.getId(), meal);
            return meal;
        }
        // handle case: update, but not present in storage
        return mealsMap.computeIfPresent(meal.getId(), (id, oldMeal) -> {
            if (oldMeal.getUserId() != userId) {
                return null; //
            }
            return new Meal(meal.getId(), meal.getDateTime(), meal.getDescription(),
                    meal.getCalories(), userId);
        });
    }

    @Override
    public boolean delete(int id, int userId) {
        Meal meal = mealsMap.get(id);
        if (meal != null && meal.getUserId() == userId) {
            return mealsMap.remove(id) != null;
        }
        return false;
    }

    @Override
    public Meal get(int id, int userId) {
        Meal meal = mealsMap.get(id);
        return (meal != null && meal.getUserId() == userId) ? meal : null;
    }


    @Override
    public Collection<Meal> getAll(int userId) {
        return mealsMap.values().stream()
                .filter(meal -> meal.getUserId() == userId)
                .sorted(Comparator.comparing(Meal::getDateTime).reversed())
                .collect(Collectors.toList());
    }
}

