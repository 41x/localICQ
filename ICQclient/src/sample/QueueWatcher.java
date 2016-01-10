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
        Connection connection = null;
        Channel channel = null;
        ConnectionFactory factory = new ConnectionFactory();
        String QUEUE_NAME=login+"queue";
        kill=false;
        log("Starting QueueWarcher...");
        while (!kill){
            //register inbox queue
            ChatServers servers=ChatServers.getInstance();
            if(servers.size()==0){
                log("No chatServers found!");
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log(e.getMessage());
                }
                continue;
            }
            //check if my queue already exists
            Server server=servers.findByQueue(QUEUE_NAME);

            if(server==null)
                server=servers.findMinQue();
            //if not exists for balancing find server with minimum of registered queues and register new queue
            boolean res=server.registerUser(login,password);
            System.out.println(res);

            factory.setHost(server.getIp().toString().split("/")[1]);
            factory.setUsername(login);
            factory.setPassword(password);

            try {
                connection = factory.newConnection();
                channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
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
//                assert channel != null;
                channel.basicConsume(QUEUE_NAME, true, consumer);
            } catch (IOException e) {
                e.printStackTrace();
                log(e.getMessage());
            }

            while (!kill){
                servers=ChatServers.getInstance();
                if(servers.size()==0){
                    break;
                }
                server=servers.findByQueue(QUEUE_NAME);
                if(server==null)break;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (connection!=null && connection.isOpen())
            try {
                connection.close();
            } catch (Exception e1) {
                log(e1.getMessage());
            }
    }

    public void setKill(boolean kill) {
        this.kill = kill;
    }

    public static void log(String message){
        Platform.runLater(() -> Main.getLogbox().appendText("\n"+message));
    }
}
