package fr.qmn.mamoucalendari.tasks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TasksSelect {

    public List<Tasks> getAllTasksByDate(String date) {
        ArrayList<Tasks> tasksList = new ArrayList<>();
        int countTasks = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");
            if (connection != null) {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM USERS WHERE DATE = '" + date + "'");
                while (resultSet.next()) {
                    Tasks tasks = new Tasks(resultSet.getString("DATE"), resultSet.getInt("HOURS"), resultSet.getInt("MINUTES"), resultSet.getString("TASKS"));
                    tasksList.add(tasks);
                    countTasks++;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tasksList;
    }

    public List<Tasks> getTasksByHour(String date, int hours) {
        List<Tasks> tasksList = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");

            if (connection != null) {
                Statement statement = connection.createStatement();

                // Tâches passées
                String pastTasksQuery = "SELECT * FROM USERS WHERE DATE = '" + date + "' AND HOURS < " + hours + " ORDER BY HOURS DESC LIMIT 1";
                ResultSet pastTasksResultSet = statement.executeQuery(pastTasksQuery);
                while (pastTasksResultSet.next()) {
                    Tasks pastTasks = new Tasks(pastTasksResultSet.getString("DATE"), pastTasksResultSet.getInt("HOURS"), pastTasksResultSet.getInt("MINUTES"), pastTasksResultSet.getString("TASKS"));
                    tasksList.add(pastTasks);
                    System.out.println("Tâches passées infos : " + pastTasks.getDate() + " " + pastTasks.getHours() + " " + pastTasks.getMinutes() + " " + pastTasks.getTasks());
                }

                // Tâche actuelle
                String currentTaskQuery = "SELECT * FROM USERS WHERE DATE = '" + date + "' AND HOURS = " + hours;
                ResultSet currentTaskResultSet = statement.executeQuery(currentTaskQuery);
                while (currentTaskResultSet.next()) {
                    Tasks currentTask = new Tasks(currentTaskResultSet.getString("DATE"), currentTaskResultSet.getInt("HOURS"), currentTaskResultSet.getInt("MINUTES"), currentTaskResultSet.getString("TASKS"));
                    tasksList.add(currentTask);
                    System.out.println("Tâches actuelle infos : " + currentTask.getDate() + " " + currentTask.getHours() + " " + currentTask.getMinutes() + " " + currentTask.getTasks());
                }

                // Tâches futures
                String futureTasksQuery = "SELECT * FROM USERS WHERE DATE = '" + date + "' AND HOURS > " + hours + " ORDER BY HOURS ASC LIMIT 1";
                ResultSet futureTasksResultSet = statement.executeQuery(futureTasksQuery);
                while (futureTasksResultSet.next()) {
                    Tasks futureTasks = new Tasks(futureTasksResultSet.getString("DATE"), futureTasksResultSet.getInt("HOURS"), futureTasksResultSet.getInt("MINUTES"), futureTasksResultSet.getString("TASKS"));
                    tasksList.add(futureTasks);
                    System.out.println("Tâches  futur infos : " + futureTasks.getDate() + " " + futureTasks.getHours() + " " + futureTasks.getMinutes() + " " + futureTasks.getTasks());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tasksList;
    }





}