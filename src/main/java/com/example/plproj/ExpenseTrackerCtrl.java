package com.example.plproj;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.time.LocalDate;
import java.util.DoubleSummaryStatistics;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

public class ExpenseTrackerCtrl {
    private boolean isChecked;
    @FXML
    private TableView<Expenses> tblExpenses;
    @FXML
    private TableColumn<Expenses, String> colID, colDate, colCatg, colDesc;
    @FXML
    private TableColumn<Expenses, Double> colAmnt;
    @FXML
    private TextField txtID, txtAmnt;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> cboCatg;
    @FXML
    private TextArea txtDesc;
    @FXML
    private Label lblExp, lblTotal;
    @FXML
    private Button btnAdd, btnUpd, btnDel, btnClr;
    @FXML
    private MenuItem itemStDate, itemStAmnt, itemStCatg;

    private ObservableList<Expenses> expensesList = FXCollections.observableArrayList();
    private ObservableList<String> items;
    private ExpenseDatabase expenseDb = new ExpenseDatabase();

    public void initialize() {
        tblExpenses.setOnMouseClicked(this::tableClicked); //Adding a mouse listener to the table
        //Initialize the combo box
        items = cboCatg.getItems();
        items.add("Food");
        items.add("Transportation");
        items.add("Bills");
        items.add("Utilities");
        items.add("Entertainment");
        items.add("Miscellaneous");

        /*
        Set up columns mapping,
        this retrieves the items in the table as different
        data types for to be stored in the Expense Class
         */
        colID.setCellValueFactory(cellData -> cellData.getValue().idProperty().asString());
        colDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        colCatg.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        colAmnt.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        colDesc.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        //Load data from database and update the table and the total expenses
        loadDataFromDatabase(1);
        updateTotal();
    }

    @FXML
    private void buttons(ActionEvent e) { //Adding functions for the buttons

        if (e.getSource() == btnAdd) { //Function for the add button
            //This checks if the table is empty
            if (expensesList.isEmpty()) { //If the table is empty, then it will set the field for the ID as 1
                txtID.setText("1");
            } else { // If the table is not empty, then it get the ID of the last row in the table and adds 1 for the new ID
                int id = expensesList.getLast().getId() + 1;
                txtID.setText(String.valueOf(id));
            }
            //Gets all the values in the fields and stores them in an Object array
            Object[] record = new Object[]{txtID.getText(), datePicker.getValue(), cboCatg.getValue(), txtAmnt.getText(), txtDesc.getText()}; //gets the texts inside the textFields
            checkFields(record); //Checks if the fields have no issues

            if (isChecked) {// The button function will run if there are no issues within the fields
                Expenses expense = new Expenses(Integer.parseInt(txtID.getText()), datePicker.getValue().toString(), cboCatg.getValue(), Double.parseDouble(txtAmnt.getText()), txtDesc.getText());
                expensesList.add(expense); //Adds it to the expense class
                addExpenseToDatabase(record); //Adds it to the database
                updateTotal(); //Updates the total expenses
            }
        } else if (e.getSource() == btnClr) { //Clears all the entry fields
            datePicker.setValue(null);
            cboCatg.setValue(null);
            txtAmnt.clear();
            txtDesc.clear();
            txtID.clear();

        } else if (e.getSource() == btnUpd) { //Function for update button
            int row = tblExpenses.getSelectionModel().getSelectedIndex(); //Gets the row number of the clicked row
            Object[] record = new Object[]{txtID.getText(), datePicker.getValue(), cboCatg.getValue(), txtAmnt.getText(), txtDesc.getText()}; //gets the texts inside the textFields
            checkFields(record);
            if (isChecked) {
                Expenses expense = expensesList.get(row); //Gets the expense object using the row variable
                //Updates the expense object by getting the entry field values
                expense.setId(Integer.parseInt(txtID.getText()));
                expense.setDate(datePicker.getValue().toString());
                expense.setCategory(cboCatg.getValue());
                expense.setAmount(Double.parseDouble(txtAmnt.getText()));
                expense.setDescription(txtDesc.getText());
                updateExpenseInDatabase(record); // Updates the database
                updateTotal();
            }
        } else if (e.getSource() == btnDel) { //Delete button function
            int row = tblExpenses.getSelectionModel().getSelectedIndex();
            int id = expensesList.get(row).getId();
            deleteExpenseFromDatabase(id);
            expensesList.remove(row);
            updateTotal();
        }
    }

    @FXML
    private void tableClicked(MouseEvent e) { //Sets the entry fields for the selected row
        Expenses selectedExpense = tblExpenses.getSelectionModel().getSelectedItem();
        if (selectedExpense != null) {
            txtID.setText(Integer.toString(selectedExpense.getId()));
            datePicker.setValue(LocalDate.parse(selectedExpense.getDate()));
            cboCatg.setValue((selectedExpense.getCategory()));
            txtAmnt.setText(String.format("%.2f", selectedExpense.getAmount()));
            txtDesc.setText(selectedExpense.getDescription());
        }
    }

    public void updateTotal() { //Updates the total expenses
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        tblExpenses.getItems().stream().forEach(expenses -> stats.accept(expenses.getAmount()));

        lblExp.setText(String.format("%.2f", stats.getSum()));
    }

    public void checkFields(Object[] fields){ // Checks all the entry fields to make sure they are valid
        isChecked = false;
        boolean isNotDouble = false;
        boolean isAllEmpty = true;
        for (Object element : fields) {
            if (element instanceof String && !((String) element).isEmpty()) {
                // If any non-empty string is found, set isAllEmpty to false and break
                isAllEmpty = false;
                break;
            } else if (element instanceof LocalDate) {
                // If any non-null date is found, set isAllEmpty to false and break
                isAllEmpty = false;
                break;
            } else if (element instanceof Double) {
                // If any non-null double is found, set isAllEmpty to false and break
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("All fields are empty!");
            alert.setContentText("Please fill in all fields.");
            alert.showAndWait();
        } else if (datePicker.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Date field is empty!");
            alert.setContentText("Please enter a date.");
            alert.showAndWait();
        } else if (cboCatg.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Category field is empty!");
            alert.setContentText("Please enter a category.");
            alert.showAndWait();
        } else if (txtAmnt.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Amount field is empty!");
            alert.setContentText("Please enter an amount.");
            alert.showAndWait();
        } else {
            try {
                Double.parseDouble(txtAmnt.getText());
            } catch (NumberFormatException e) {
                isNotDouble = true;
            }

            if (isNotDouble) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid amount!");
                alert.setContentText("The amount should be a number.");
                alert.showAndWait();
            } else {
                double amt = Double.parseDouble(txtAmnt.getText());
                if (amt < 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid amount!");
                    alert.setContentText("The amount should be positive.");
                    alert.showAndWait();
                } else {
                    isChecked = true;
                }
            }
        }
    }

    @FXML
    private void resetTable(ActionEvent event) {
        loadDataFromDatabase(1); //Reset the table by just reloading the table in the GUI
    }

    @FXML
    private void sortTable(ActionEvent event) { //Logic for the sorting menu
        MenuItem source = (MenuItem) event.getSource(); //Gets the selected MenuItem

        //Runs the corresponding action for each MenuItem
        if(source == itemStDate) {
            loadDataFromDatabase(2);
        }
        else if(source == itemStCatg) {
            loadDataFromDatabase(3);
        }
        else if(source == itemStAmnt) {
            loadDataFromDatabase(4);
        }
    }


    //Algorithm for searching
    @FXML
    private void searchTable(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource(); //Gets the MenuItem selected by the user

        switch (source.getText()){
            case "Date":
                char criteria = dateOptions();
                try { //Try-catch block to detect if the user did not choose any date

                    /*  The Optional class provides various methods to check if a value is present,
                    retrieve the value, or perform actions based on the presence or absence of a value. */
                    Optional<LocalDate> dateResult = showDatePicker(); //opens the date picker

                    if (dateResult.isPresent()) {
                        LocalDate chosenDate = dateResult.get();
                        /* The values of criteria and chosenDate will be inserted to the query, this makes the query complete
                        and will be sent to the database class to correctly query what is needed */
                        String query = "SELECT * FROM expenses WHERE date " + criteria + " '" + chosenDate + "' ORDER BY strftime('%Y-%m-%d', date);";
                        searchDataFromDatabase(query);
                        updateTotal();
                    }
                } catch (NullPointerException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Empty input!");
                    alert.setContentText("Please input a date");
                    alert.showAndWait();
                }
                break;
            case "Category":
                promptCategory(); //opens the category search prompt
                updateTotal();
                break;
            case "Amount":
                promptAmount(); //open the amount search prompt
                updateTotal();
                break;
        }
    }

    private void promptAmount() { //Dialog box for amount
        String query = "";

        //Dialog box
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search");
        dialog.setHeaderText(null);

        dialog.setContentText("Enter search criteria:" +
                "\n'=yourInput' means it will display records at that exact amount." +
                "\n'>yourInput' means it will display records greater than that amount." +
                "\n'<yourInput' means it will display records less than that amount.");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String searchCriteria = result.get();
            if (searchCriteria.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No Input!");
                alert.setContentText("Please enter an input.");
                alert.showAndWait();
                return;
            }

            String firstChar;
            String searchValue;
            /* This gets the search criteria input by the user
            * if the user has input "=amount" it will get the '=' symbol */
            firstChar = searchCriteria.substring(0, 1);
            /* This will get the amount input by the user,
            if the user has input "=amount" it will get the "amount" number */
            searchValue = searchCriteria.substring(1);

            try { //This will check if the input is valid
                Double.parseDouble(searchValue);
            } catch (NumberFormatException exception) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid input!");
                alert.setContentText("Enter numbers ONLY after the symbol.");
                alert.showAndWait();
                promptAmount();
                return;
            }

            switch (firstChar) { //Switch statements for the queries based on the search criteria
                case "=" -> {
                    query = "SELECT * FROM expenses WHERE amount" + " = '" + searchValue + "'";
                    searchDataFromDatabase(query);
                }
                case "<" -> {
                    query = "SELECT * FROM expenses WHERE amount" + " < '" + searchValue + "'";
                    searchDataFromDatabase(query);
                }
                case ">" -> {
                    query = "SELECT * FROM expenses WHERE amount" + " > '" + searchValue + "'";
                    searchDataFromDatabase(query);
                }
                default -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid search criteria!");
                    alert.setContentText("Please enter a valid search criteria.");
                    alert.showAndWait();
                }
            }
        }
    }

    private void promptCategory() { //Dialog box for category
        //String is an atomic reference due to the query being in a lambda expression
        AtomicReference<String> query = new AtomicReference<>("");

        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("Food", "Transportation", "Bills", "Utilities", "Entertainment", "Miscellaneous");
        categoryComboBox.getSelectionModel().select(0); // select the first item by default

        //This is the lambda expression
        categoryComboBox.setOnAction(event -> {
            String category = categoryComboBox.getSelectionModel().getSelectedItem();
            if (category != null) {
                query.set("SELECT * FROM expenses WHERE category = '" + category + "'");
            }
        });

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Search");
        dialog.setHeaderText("Please choose a category:");
        dialog.getDialogPane().setContent(categoryComboBox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) { //proceeds if the user has chosen a category and clicked OK
            searchDataFromDatabase(query.get());
        } else {
            return;
        }
    }

    private Optional<LocalDate> showDatePicker() {
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Choose a date");

        //Creates and displays the dialog, checks whether the user clicked OK or CANCEL
        Optional<ButtonType> result = showDialog(datePicker);

        if (result.isPresent() && result.get() == ButtonType.OK) {
            return Optional.of(datePicker.getValue());
        } else {
            return Optional.empty();
        }
    }

    private Optional<ButtonType> showDialog(DatePicker datePicker) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Choose a date");
        dialog.setHeaderText("Please choose a date:");
        dialog.getDialogPane().setContent(datePicker);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        return dialog.showAndWait();
    }

    private char dateOptions() { //This method gives the user options on how they want to sort the searched date
        final char[] criteria = new char[1];
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Choose a search criteria");
        dialog.setHeaderText("Please choose an option:");

        //Arrangements for the buttons
        ButtonType beforeBtnType = new ButtonType("Before", ButtonBar.ButtonData.SMALL_GAP);
        ButtonType afterBtnType = new ButtonType("After", ButtonBar.ButtonData.SMALL_GAP);
        ButtonType exactBtnType = new ButtonType("Exact", ButtonBar.ButtonData.SMALL_GAP);

        //Adds the button to the dialog
        dialog.getDialogPane().getButtonTypes().addAll(beforeBtnType, afterBtnType, exactBtnType);

        //Casts button type to the buttons for event handling
        Button beforeButton = (Button) dialog.getDialogPane().lookupButton(beforeBtnType);
        Button afterButton = (Button) dialog.getDialogPane().lookupButton(afterBtnType);
        Button exactButton = (Button) dialog.getDialogPane().lookupButton(exactBtnType);

        /* The user will click one of the buttons and each of them will do different functions
        Each button will set a search criteria for the SQL query
         */
        beforeButton.setOnAction(event -> {
            criteria[0] = '<';
            dialog.close();
        });

        afterButton.setOnAction(event -> {
            criteria[0] = '>';
            dialog.close();
        });

        exactButton.setOnAction(event -> {
            criteria[0] = '=';
            dialog.close();
        });

        dialog.showAndWait();

        return criteria[0];
    }

    /*Loads the table retrieved from the database
    The sort order determines what type of sorting it should do
     */
    private void loadDataFromDatabase(int sortOrder) {
        //Gets all the records from the table and stores them
        Vector<Vector<Object>> data = expenseDb.getAllExpenses(sortOrder);
        expensesList.clear();//Clears the array first so it won't append new records

        //Iterates through the records and stores them in the Expenses Class
        for (Vector<Object> rowData : data) {
            //Initialize the expenses class
            Expenses expenses = new Expenses(0, "", "", 0.0, "");

            //Sets the correct data types for each value in the records
            expenses.setId(Integer.parseInt((String) rowData.get(0)));
            expenses.setDate((String) rowData.get(1));
            expenses.setCategory((String) rowData.get(2));
            expenses.setAmount((Double) rowData.get(3));
            expenses.setDescription((String) rowData.get(4));

            expensesList.add(expenses);
        }

        //Loads them into the table
        tblExpenses.setItems(expensesList);
    }


    /* Method for searching specific properties in the table
    This method receives a query to get the specified records in the database
     */
    private void searchDataFromDatabase(String query) { //query is sent as a parameter to the database class
        Vector<Vector<Object>> data = expenseDb.searchAllExpenses(query);
        expensesList.clear();
        for (Vector<Object> rowData : data) {
            Expenses expenses = new Expenses(0, "", "", 0.0, "");

            expenses.setId(Integer.parseInt((String) rowData.get(0)));
            expenses.setDate((String) rowData.get(1));
            expenses.setCategory((String) rowData.get(2));
            expenses.setAmount((Double) rowData.get(3));
            expenses.setDescription((String) rowData.get(4));

            expensesList.add(expenses);
        }

        tblExpenses.setItems(expensesList);
    }


    private void addExpenseToDatabase(Object[] expense) {
        expenseDb.addExpense(expense);
    }

    private void updateExpenseInDatabase(Object[] expense) {
        expenseDb.updateExpense(expense);
    }

    private void deleteExpenseFromDatabase(int expenseId) {
        expenseDb.deleteExpense(expenseId);
    }

}
