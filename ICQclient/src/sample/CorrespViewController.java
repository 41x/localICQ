package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import static java.lang.Thread.sleep;

/**
 * Created by Alexander on 17.12.2015.
 */
public class CorrespViewController implements Runnable{

    private static CorrespViewController ourInstance;
    public WebView wbview;
    private static WebEngine webEngine;

    public static CorrespViewController getInstance() {
        return ourInstance;
    }

    public TextField messageBox;
    private Correspondence correspondence;
    private boolean kill;

    public CorrespViewController() {
        ourInstance=this;
    }

    public void onSend(ActionEvent actionEvent) {
        if(correspondence.send(messageBox.getText())) messageBox.clear();


    }


    @Override
    public void run() {
        while (!kill){
            if (correspondence==null) continue;
            if (!correspondence.isDirty()) {
                doSleep();
//                System.out.println("me");
                continue;
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    String page=correspondence.buildPage();
                    webEngine.loadContent(page);
                }
            });
            correspondence.setDirty(false);
        }

    }


    public void setCorrespondence(Correspondence correspondence) {
        this.correspondence = correspondence;
        correspondence.setDirty(true);
    }

    private void doSleep(){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void setWebEngine(WebEngine webEngine) {
        CorrespViewController.webEngine = webEngine;
    }

    public static WebEngine getWebEngine() {
        return webEngine;
    }

    public void setKill(boolean kill) {
        this.kill = kill;
    }
}
