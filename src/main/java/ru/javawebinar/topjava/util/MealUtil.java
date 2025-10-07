package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.Meal;

public class MealUtil {
    public static boolean isNew(Meal meal) {
        return meal.getId() == null;
    }
}
