package ru.javawebinar.topjava.service;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.to.MealTo;
import ru.javawebinar.topjava.util.MealsUtil;

import java.util.List;

import static ru.javawebinar.topjava.util.ValidationUtil.checkNotFound;

@Service
public class MealService {

    private final MealRepository repository;

    public MealService(MealRepository repository) {
        this.repository = repository;
    }

    public Meal get(int id, int userId) {
        return checkNotFound(repository.get(id, userId), "id=" + id);
    }

    public void delete(int id, int userId) {
        checkNotFound(repository.delete(id, userId), id);
    }

    public List<Meal> getAll(int userId) {
        return repository.getAll(userId).stream()
                .collect(java.util.stream.Collectors.toList());
    }

    public void update(Meal meal, int userId) {
        Assert.notNull(meal, "meal must not be null");
        checkNotFound(repository.save(meal, userId), meal.getId());
    }

    public Meal create(Meal meal, int userId) {
        Assert.notNull(meal, "meal must not be null");
        return repository.save(meal, userId);
    }

    public List<MealTo> getAllWithExcess(int userId, int caloriesPerDay) {
        List<Meal> meals = getAll(userId);
        return MealsUtil.getTos(meals, caloriesPerDay);
    }

}