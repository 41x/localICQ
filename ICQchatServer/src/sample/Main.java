package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.entity.StrictContentLengthStrategy;

import java.io.IOException;

public class Main extends Application {
    private static String RmqIp = "localhost";
    private static int RmqPort = 15672;
    private static TextArea logBox;

    @Override
    public void start(Stage stage) throws Exception{
//        stage=new Stage();
        stage.setTitle("Chat Server");
        Parent root;
        FXMLLoader loader;
        try {
            loader = new FXMLLoader(getClass().getResource("sample.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        logBox=((Controller)loader.getController()).logBox;
        logBox.setPrefSize(Double.MAX_VALUE,Double.MAX_VALUE);
        stage.setScene(new Scene(root, 500, 450));
        stage.show();

        log("Starting chatServer...");
        if(!alive("guest","guest")) {
            log("Rabbit is dead");
            return;
        }

        Thread t = new Thread(new Server());
        t.setDaemon(true);
        t.start();

        log("ChatServer listening...");
    }

    private void log(String message){
        logBox.appendText("\n"+message);
    }

    public static String getRmqIp() {
        return RmqIp;
    }

    public static int getRmqPort() {
        return RmqPort;
    }

    public boolean alive(String login, String password){
        String enc = new String( Base64.encodeBase64((login+ ":"+password).getBytes() ) );
        String jsonstr="";
        try{
            CloseableHttpClient client1 = HttpClients.createDefault();
            HttpGet alive = new HttpGet( "http://"+Main.getRmqIp()+":"+Main.getRmqPort()+"/api/aliveness-test/%2f");
            alive.addHeader( "Authorization", "Basic " + enc ); // RabbitMQ requires a user with create permission, create it mannually first
            alive.addHeader( "content-type", "application/json" );
            CloseableHttpResponse resp=client1.execute( alive );

            byte[] buf=new byte[(int)resp.getEntity().getContentLength()];
            resp.getEntity().getContent().read(buf);
            jsonstr=new String(buf);
            if (jsonstr.contains("\"status\":\"ok\""))
                return true;
        }catch( Exception ex ){
            log(ex.getMessage());
            return false;
        }
        return false;
    }

    public static TextArea getLogBox() {
        return logBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
