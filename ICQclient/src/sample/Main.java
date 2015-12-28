package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class Main extends Application {

    private static TextArea logbox;
    private static Stage stage;
    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage=new Stage();
        Parent root;
        FXMLLoader loader;
        try {
            loader = new FXMLLoader(getClass().getResource("sample.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        primaryStage.setScene(new Scene(root, 500, 500));
        stage=primaryStage;
        primaryStage.show();



        Controller c=loader.getController();
        logbox=c.logBox;

        c.loginBox.requestFocus();
    }

    public static Stage getStage() {
        return stage;
    }

    public static void log(String text){
        logbox.appendText("\n"+text);
    }

    public static TextArea getLogbox() {
        return logbox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
