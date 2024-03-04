package com.example.plproj;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    ExpenseDatabase expenseDb = new ExpenseDatabase();
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("ExpenseTrackerView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Expense Tracker");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception {
        expenseDb.closeConnection();
        super.stop();
    }

}