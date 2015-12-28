package sample;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

import static java.lang.Thread.sleep;

/**
 * Created by Alexander on 18.12.2015.
 */
public class ContactsView implements Runnable {
//    private static ContactsView ourInstance = new ContactsView();

//    public static ContactsView getInstance() {
//        return ourInstance;
//    }

    private Contacts contacts;
    private VBox vbox;
    private boolean kill;


    ContactsView() {

        contacts=Contacts.getInstance();
    }

    @Override
    public void run() {
        if (vbox==null) throw new Error("vbox is null");
        while (!kill){
            if(!contacts.isDirty()) {
                doSleep();
//                System.out.println("meContView");
                continue;
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (vbox.getChildren().size()>0)
                        vbox.getChildren().removeAll(vbox.getChildren());
                    for (Contact c : contacts) {
                        if (vbox.getChildren().contains(c.getButton())) continue;
                        vbox.getChildren().add(c.getButton());
                    }
                }
            });
            contacts.setDirty(false);
        }
    }

    public void setKill(boolean kill) {
        this.kill = kill;
    }

    public void setVbox(VBox vbox) {
        this.vbox = vbox;
    }
    private void doSleep(){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
