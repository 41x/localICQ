package sample;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

/**
 * Created by Alexander on 17.12.2015.
 */
public class Contact {
    // queue where to send
    private String contactId;
    private Correspondence correspondence;
    private Button button;
    private CorrespViewController c;

    public Contact(String contactId) {
        this.contactId = contactId;
        this.correspondence =new Correspondence(this);
        this.button = new Button();
        button.setMinWidth(450);
        button.setText(getContactId());
        button.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Contacts.getInstance().setSelected(getContactId());
            }
        });

        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        // find queue to push
                        ChatServers servers=ChatServers.getInstance();
                        if (servers==null) {
                            log("Chat servers not found");
                            return;
                        }
                        Server server=servers.findByQueue(getContactId()+"queue");
                        if(server==null) {
                            log(getContactId()+" queue not found");
                            return;
                        }

                        Stage stage=new Stage();
                        stage.setTitle(Controller.getLogin()+"-"+getContactId());
                        Parent root;
                        FXMLLoader loader;
                        try {
                            loader = new FXMLLoader(getClass().getResource("CorrespView.fxml"));
                            root = loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                        stage.setScene(new Scene(root, 500, 500));
                        Controller.getStage().hide();
                        stage.show();

                        c= loader.getController();
                        WebEngine we=c.wbview.getEngine();
                        we.setJavaScriptEnabled(true);



                        we.setUserStyleSheetLocation(getClass().getResource("thestyle.css").toString());
                        CorrespViewController.setWebEngine(we);

                        c.setCorrespondence(correspondence);
                        Thread t=new Thread(c);
                        t.setDaemon(true);
                        t.start();



                        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                            public void handle(WindowEvent we) {
                                c.setKill(true);
                                Controller.getStage().show();
                            }
                        });

                    }
                }
            }
        });
    }

    public static void log(String message){
        mainWindowController.getLb().appendText("\n"+message);
    }

    public String getContactId() {
        return contactId;
    }

    public Button getButton() {
        return button;
    }

    public Correspondence getCorrespondence() {
        return correspondence;
    }
}
