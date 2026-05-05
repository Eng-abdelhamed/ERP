package com.erp;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource("/fxml/dashboard.fxml")
        );
        Scene scene = new Scene(root, 1100, 700);
        String mainCss = getClass().getResource("/styles/main.css").toExternalForm();
        scene.getStylesheets().add(mainCss);
        primaryStage.setTitle("ERP System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
