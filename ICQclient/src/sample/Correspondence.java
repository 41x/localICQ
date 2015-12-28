package sample;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

/**
 * Created by Alexander on 17.12.2015.
 */
public class Correspondence extends ArrayList<Message> {
    private boolean dirty;
    private Comparator<Message> comparator;
    private Contact contact;
    private Server server;



    public Correspondence(Contact contact) {
        this.contact=contact;
        comparator  = (m1, m2) -> {
            if(m1.getTime().after(m2.getTime())) return 1;
            if(m1.getTime().before(m2.getTime())) return -1;
            return 0;
        };
    }

    public void AddMessage(String text){
        if(text.trim().equals("")) return;
        String[] arr=text.split(";");
        boolean me=arr[0].equals("me");
        Calendar cal = Calendar.getInstance();
        add(new Message(arr[1],cal.getTime(),me));
        this.sort(comparator);
        setDirty(true);
    }

    public boolean send(String message){
        if (message.trim().equals("")) return false;
        if(server==null){
            server=findDestinationQueueServer();
        }
        Connection connection = null;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(server.getIp().toString().split("/")[1]);
            factory.setUsername(Controller.getLogin());
            factory.setPassword(Controller.getPassword());

            connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String QUEUE_NAME=getContact().getContactId()+"queue";
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, (Controller.getLogin()+";"+message).getBytes());
            Main.log("Sent message to "+getContact().getContactId() );

        }catch(Exception ex){
            Main.log("onSend error");
            server=null;
            closeConnection(connection);
            return false;
        }
        AddMessage("me;"+message);
        closeConnection(connection);
        return true;
    }

    private void closeConnection(Connection c){
        if(c.isOpen())
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private Server findDestinationQueueServer(){
        Server server;
        ChatServers servers;
        Main.log("Current server is invalid Searching chat server to send...");
        servers=ChatServers.getInstance();
        if (servers.size()==0) return null;
        Main.log("Success");
        Main.log("Searching destination queue...");
        server=servers.findByQueue(getContact().getContactId()+"queue");
        if (server!=null) {
            Main.log("Success");
            server.registerUser(Controller.getLogin(),Controller.getPassword());
        }
        return server;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public String buildPage() {
        String template="<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">"+
                "<script language=\"javascript\" type=\"text/javascript\">" +
                "function toBottom(){" +
                "var objDiv = document.getElementById(\"mymain\");"+
                "window.scrollTo(0,objDiv.scrollHeight);"+
                "}"+
                "</script>" +
                "</head>" +
                "<body onload='toBottom()'>"+
                "<div id=\"mymain\">content</div>" +
                "</body>"+
                "</html>";
        String content="";
        for (Message m:this){
            String styleclass=m.isMe()?"user":"contact";
            content+="<p class=\""+styleclass+"\">"+m.getText()+"</p>";
        }
        template=template.replace("content",content);
        return template;
    }

    public Contact getContact() {
        return contact;
    }
}

