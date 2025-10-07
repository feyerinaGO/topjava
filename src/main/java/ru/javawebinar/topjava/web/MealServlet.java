package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.MealTo;
import ru.javawebinar.topjava.repository.LocalMealRepository;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.MealUtil;
import ru.javawebinar.topjava.util.MealsUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class MealServlet extends HttpServlet {
    public static final int CALORIES_PER_DAY = 2000;
    private static final Logger log = getLogger(MealServlet.class);

    private MealRepository mealRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Override
    public void init() throws ServletException {
        super.init();
        mealRepository = new LocalMealRepository();
        // Инициализация тестовыми данными
        initTestData();
    }

    private void initTestData() {
        mealRepository.save(new Meal(LocalDateTime.of(2020, 1, 30, 10, 0), "Завтрак", 500));
        mealRepository.save(new Meal(LocalDateTime.of(2020, 1, 30, 13, 0), "Обед", 1000));
        mealRepository.save(new Meal(LocalDateTime.of(2020, 1, 30, 20, 0), "Ужин", 500));
        mealRepository.save(new Meal(LocalDateTime.of(2020, 1, 31, 0, 0), "Еда на граничное значение", 100));
        mealRepository.save(new Meal(LocalDateTime.of(2020, 1, 31, 10, 0), "Завтрак", 1000));
        mealRepository.save(new Meal(LocalDateTime.of(2020, 1, 31, 13, 0), "Обед", 500));
        mealRepository.save(new Meal(LocalDateTime.of(2020, 1, 31, 20, 0), "Ужин", 410));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            log.info("Delete meal with id={}", id);
            mealRepository.delete(id);
            response.sendRedirect("meals");
            return;
        } else if ("create".equals(action) || "update".equals(action)) {
            final Meal meal = "create".equals(action) ?
                    new Meal(LocalDateTime.now(), "", 0) :
                    mealRepository.get(Integer.parseInt(request.getParameter("id")));
            request.setAttribute("meal", meal);
            request.setAttribute("isNew", MealUtil.isNew(meal));
            request.getRequestDispatcher("/mealForm.jsp").forward(request, response);
            return;
        } 

        log.debug("forward to meals");
        List<Meal> meals = mealRepository.getAll();
        List<MealTo> mealsTo = MealsUtil.filteredByStreams(meals, LocalTime.MIN, LocalTime.MAX, CALORIES_PER_DAY);
        request.setAttribute("meals", mealsTo);
        request.getRequestDispatcher("/meals.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String id = request.getParameter("id");
        LocalDateTime dateTime = LocalDateTime.parse(request.getParameter("dateTime"), formatter);
        String description = request.getParameter("description");
        int calories = Integer.parseInt(request.getParameter("calories"));

        Meal meal = new Meal(id.isEmpty() ? null : Integer.valueOf(id), dateTime, description, calories);
        log.info(MealUtil.isNew(meal)? "Create {}" : "Update {}", meal);
        mealRepository.save(meal);
        response.sendRedirect("meals");
    }
}

