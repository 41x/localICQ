package sample;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.StrictExceptionHandler;
import javafx.application.Platform;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Alexander on 18.12.2015.
 */
public class QueueWatcher implements Runnable {

    private String login;
    private String password;
    private Contacts contacts;
    private boolean kill;

    public QueueWatcher(Contacts contacts,String login, String password) {
        this.login = login;
        this.password = password;
        this.contacts = contacts;
    }

    @Override
    public void run() {
        ConnectionFactory factory = new ConnectionFactory();
        String QUEUE_NAME=login+"queue";
        kill=false;
        log("Starting QueueWarcher...");
        while (!kill){
            //register inbox queue
//            log("Searching chat servers...");
            ChatServers servers=ChatServers.getInstance();
            if(servers.size()==0){
                continue;
            }
//            log("Search completed");


            //check if my queue already exists
            Server server=servers.findByQueue(QUEUE_NAME);
            //if not exists for balancing find server with minimum of registered queues and register new queue
            if(server==null){
                server=servers.findMinQue();
                boolean res=server.registerUser(login,password);
                System.out.println(res);
            }

            factory.setHost(server.getIp().toString().split("/")[1]);
            factory.setUsername(login);
            factory.setPassword(password);

            Connection connection = null;
            Channel channel = null;
            try {
                connection = factory.newConnection();
                channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            log("Waiting for messages...");

            DefaultConsumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body); //body, "UTF-8"
                    String[] arr=message.split(";");
                    Contact c=contacts.find(arr[0]);
                    if (c==null){
                        Contacts.getInstance().AddContact(arr[0]);
                        c=contacts.find(arr[0]);
                    }
                    assert c != null;
                    c.getCorrespondence().AddMessage(String.join(";",arr));
                }
            };
            try {
                assert channel != null;
                channel.basicConsume(QUEUE_NAME, true, consumer);
            } catch (IOException e) {
                e.printStackTrace();
                log(e.getMessage());
            }

            while (!kill){ // check if server alive
                try {
                    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                    Thread.sleep(2000);
//                    System.out.println("meQueue");
                } catch (Exception e) {
                    log("Current server died");
//                    e.printStackTrace();
                    log(e.getMessage());
                    break;
                }
            }
            if (connection.isOpen())
            try {
                connection.close();
            } catch (Exception e1) {
                System.out.println(e1.getMessage());
            }
        }
    }

    public void setKill(boolean kill) {
        this.kill = kill;
    }

    public static void log(String message){
        Platform.runLater(() -> Main.getLogbox().appendText("\n"+message));
    }
}
