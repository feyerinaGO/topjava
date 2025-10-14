package ru.javawebinar.topjava.web;

import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class UserServlet extends HttpServlet {
    private static final Logger log = getLogger(UserServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("forward to users");

        String userIdParam = request.getParameter("userId");
        if (userIdParam != null) {
            try {
                int userId = Integer.parseInt(userIdParam);
                SecurityUtil.setAuthUserId(userId);
                log.info("Set auth user id: {}", userId);
            } catch (NumberFormatException e) {
                log.warn("Invalid user id parameter: {}", userIdParam);
            }
        }
        request.getRequestDispatcher("/users.jsp").forward(request, response);
    }
}
