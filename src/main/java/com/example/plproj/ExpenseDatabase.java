package com.example.plproj;

import java.sql.*;
import java.util.Vector;

public class ExpenseDatabase {

    private static final String DATABASE_URL = "jdbc:sqlite:expense_tracker.db";
    private Connection connection;

    public ExpenseDatabase() {
        connectToDatabase();
        createExpenseTable();
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void createExpenseTable() {
        try (Statement statement = connection.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "date DATE," +
                    "category TEXT," +
                    "amount REAL," +
                    "description TEXT)";
            statement.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addExpense(Object[] expense) { //Method for adding new records
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO expenses (date, category, amount, description) VALUES (?, ?, ?, ?)")) {

            preparedStatement.setString(1, expense[1].toString());
            preparedStatement.setString(2, expense[2].toString());
            preparedStatement.setDouble(3, Double.parseDouble(expense[3].toString()));
            preparedStatement.setString(4, expense[4].toString());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateExpense(Object[] expense) { //Method for updating existing records
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE expenses SET date=?, category=?, amount=?, description=? WHERE id=?")) {

            preparedStatement.setString(1, expense[1].toString());
            preparedStatement.setString(2, expense[2].toString());
            preparedStatement.setDouble(3, Double.parseDouble(expense[3].toString()));
            preparedStatement.setString(4, expense[4].toString());
            preparedStatement.setInt(5, Integer.parseInt(expense[0].toString()));

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteExpense(int expenseId) {//Deleting
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM expenses WHERE id=?")) {
            preparedStatement.setInt(1, expenseId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Vector<Vector<Object>> getAllExpenses(int sortOrder) {//Method for getting all the records in the database
        Vector<Vector<Object>> expenses = new Vector<>();
        String query = null;

        //sortOrder determines what order should the records be displayed
        if(sortOrder == 1) { //Default sort order
            query = "SELECT * FROM expenses";
        }
        else if(sortOrder == 2) {//Sort by Date
            query = "SELECT * FROM expenses ORDER BY strftime('%Y-%m-%d', date) ASC";
        }
        else if(sortOrder == 3) {//Sprt by Category
            query = "SELECT * FROM expenses ORDER BY category ASC";
        }
        else if(sortOrder == 4) {//Sort by Amount
            query = "SELECT * FROM expenses ORDER BY amount ASC";
        }

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            //Iterates through the database records and gets all the column values
            while (resultSet.next()) {
                Vector<Object> rowData = new Vector<>();
                rowData.add(resultSet.getString("id"));
                rowData.add(resultSet.getString("date"));
                rowData.add(resultSet.getString("category"));
                rowData.add(resultSet.getDouble("amount"));
                rowData.add(resultSet.getString("description"));

                expenses.add(rowData);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return expenses;
    }


    //This method receives specific queries that any user desires
    public Vector<Vector<Object>> searchAllExpenses(String query) {
        Vector<Vector<Object>> expenses = new Vector<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Vector<Object> rowData = new Vector<>();
                rowData.add(resultSet.getString("id"));
                rowData.add(resultSet.getString("date"));
                rowData.add(resultSet.getString("category"));
                rowData.add(resultSet.getDouble("amount"));
                rowData.add(resultSet.getString("description"));

                expenses.add(rowData);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return expenses;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
