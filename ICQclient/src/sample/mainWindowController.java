package sample;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Created by Alexander on 17.12.2015.
 */
public class mainWindowController {
    public TextField usernameBox;
    public TextArea logBox;
    public VBox ContactVBox;
    private static TextArea lb;

    private static mainWindowController ourInstance= new mainWindowController();
    public static mainWindowController getInstance() {
        return ourInstance;
    }

    public mainWindowController() {
    }

    public void onAddContact(ActionEvent actionEvent) {
        if (usernameBox.getText().equals(Controller.getLogin())) return;
        Contacts.getInstance().AddContact(usernameBox.getText());
        usernameBox.requestFocus();
    }

    public static TextArea getLb() {
        return lb;
    }

    public static void setLb(TextArea lb) {
        mainWindowController.lb = lb;
    }

    public VBox getContactVBox() {
        return ContactVBox;
    }

    public void onDeleteContact(ActionEvent actionEvent) {
        Contacts.getInstance().DeleteContact();
    }
}
