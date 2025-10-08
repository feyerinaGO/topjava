<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="ru">
<c:set var="formTitle" value="${meal.id==null ? 'Добавление' : 'Изменение'}"/>

<head>
    <title>${formTitle}</title>
</head>
<body>
<h3><a href="meals">Back to Meals</a></h3>
<hr>
<h2>${formTitle}</h2>

<form method="post" action="meals">
    <input type="hidden" name="id" value="${meal.id}">

    <dl>
        <dt>Дата/Время:</dt>
        <dd><input type="datetime-local" value="${meal.dateTime}" name="dateTime" required></dd>
    </dl>
    <dl>
        <dt>Описание:</dt>
        <dd><input type="text" value="${meal.description}" size=40 name="description" required></dd>
    </dl>
    <dl>
        <dt>Калории:</dt>
        <dd><input type="number" value="${meal.calories}" name="calories" required></dd>
    </dl>
    <button type="submit">Сохранить</button>
    <button onclick="window.history.back()" type="button">Отменить</button>
</form>
</body>
</html>