package sample;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.StrictExceptionHandler;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Authenticator;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.AuthProvider;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import javafx.stage.WindowEvent;

public class Controller {
    public TextArea logBox;
    public TextField loginBox;
    public TextField passwordBox;
    public Button startButton;
    public TextField confPasswordBox;
    public Button signInUpButton;
    private static String login;
    private static String password;
    private static Stage stage;
    private static QueueWatcher qw;
    private ContactsView cv;

    public void onStartButton() {
        boolean auth=signInUpButton.getText().equals("Sign In")
                ?AuthServer.getInstance().signIn(loginBox.getText(),passwordBox.getText())
                :AuthServer.getInstance().signUp(loginBox.getText(),passwordBox.getText(),confPasswordBox.getText());
        if(!auth) return;
        login=loginBox.getText();
        password=passwordBox.getText();

        if (stage!=null)
            stage.close();
        stage=new Stage();
        Parent root;
        FXMLLoader loader;
        try {
            loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        stage.setTitle(login);
        stage.setScene(new Scene(root, 500, 500));
        Main.getStage().hide();
        stage.show();

        //init contacts
        Contacts contacts=Contacts.getInstance();
        contacts.fill(getLogin());
        contacts.setDirty(true);
        //start contacts view
        mainWindowController c= loader.getController();
        mainWindowController.setLb(c.logBox);

//        cv=ContactsView.getInstance();
        cv=new ContactsView();
        cv.setVbox(c.getContactVBox());
        Thread t=new Thread(cv);
        t.setDaemon(true);
        t.start();

        //start message receiver
        qw=new QueueWatcher(contacts,loginBox.getText(),passwordBox.getText());
        Thread t2=new Thread(qw);
//        t2.setDaemon(true);
        t2.start();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                cv.setKill(true);
                qw.setKill(true);
                Main.getStage().show();
            }
        });
    }


    public void onSign(ActionEvent actionEvent) {
        signInUpButton.setText(signInUpButton.getText().equals("Sign In")?"Sign Up":"Sign In");
        if(signInUpButton.getText().equals("Sign Up")) {
            confPasswordBox.clear();
            confPasswordBox.setVisible(true);
        }else
            confPasswordBox.setVisible(false);
    }

    public static QueueWatcher getQw() {
        return qw;
    }

    public static Stage getStage() {
        return stage;
    }

    public static String getLogin() {
        return login;
    }

    public static String getPassword() {
        return password;
    }
}
