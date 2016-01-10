package sample;

import javafx.application.Platform;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by ваа on 24.12.2015.
 */
public class Server implements Runnable{
    private static DatagramSocket socket;
    @Override
    public void run() {
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            while (true) {
                log( "Ready to receive requests...");
                //Receive a packet
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);
                //Packet received
//                log( "Packet received; data: " + new String(packet.getData()));
                String client=packet.getAddress().getHostAddress();
                //See if the packet holds the right command (message)
                String message = new String(packet.getData()).trim();
                String[] parts=message.split(";");
                String answer;
                switch (parts[0]){
                    case "who":
                        log("Got queueWatcher request from: "+client);
                        log("Checking Rabbit...");
                        // no reply if rabbit is dead
                        if(!alive("guest","guest")){
                            log("Rabbit is DEAD!\nPlease start Rabbit");
                            continue;
                        }
                        log("Rabbit running");
                        log("Getting queues info");
                        String jsn=getQueueInfo();
                        answer="ChatServer;"+jsn;
                        break;
                    case "alive":
                        log("Got server status request from: "+client);
                        log("Getting status info");
                        boolean status= alive(parts[1],parts[2]);
                        answer=status?"OK":"Server is down";
                        break;
                    case "createRmqUser":
                        if(!alive("guest","guest")){
                            log("Rabbit is DEAD!\nPlease start Rabbit");
                            continue;
                        }
                        log("Registering new user: "+parts[1]+" with RabbitMQ");
                        boolean registered=createRmqUser(parts[1],parts[2]);
                        answer=registered?"OK":"Something went Wrong (T_T)";
                        log(answer);
                        break;
                    default:continue;
                }
                byte[] sendData = answer.getBytes();
                //Send a response
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                socket.send(sendPacket);
                log("Sent packet to: " + sendPacket.getAddress().getHostAddress());

            }
        } catch (IOException ex) {
            log(ex.getMessage());
        }

    }

    private String getQueueInfo(){
        String enc = new String( Base64.encodeBase64( "guest:guest".getBytes() ) );
        String jsonstr="";
        try{
            CloseableHttpClient client1 = HttpClients.createDefault();
            HttpGet getqueues = new HttpGet( "http://"+Main.getRmqIp()+":"+Main.getRmqPort()+"/api/queues");
            getqueues.addHeader( "Authorization", "Basic " + enc ); // RabbitMQ requires a user with create permission, create it mannually first
            getqueues.addHeader( "content-type", "application/json" );
            CloseableHttpResponse resp=client1.execute( getqueues );

            byte[] buf=new byte[(int)resp.getEntity().getContentLength()];
            resp.getEntity().getContent().read(buf);
            jsonstr=new String(buf);
        }catch( Exception ex ){
            log(ex.getMessage());
        }
        return jsonstr;
    }

    private boolean alive(String login, String password){
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


    private boolean createRmqUser(String login, String password){
        // First, save your user/pw with permission to create new users.
// NOTE: this user is alredy created on RabbitMQ with permission to create new users
        String enc = new String( Base64.encodeBase64( "guest:guest".getBytes() ) );

        try{
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPut putUser = new HttpPut( "http://"+Main.getRmqIp()+":"+Main.getRmqPort()+"/api/users/"+login );
            putUser.addHeader( "Authorization", "Basic " + enc ); // RabbitMQ requires a user with create permission, create it mannually first
            putUser.addHeader( "content-type", "application/json" );
            putUser.setEntity( new StringEntity( "{\"password\":\""+password+"\",\"tags\":\"administrator\"}" ) );
            client.execute( putUser );

//After create, configure RabbitMQ permission

            HttpPut putConfigPermissions = new HttpPut( "http://"+Main.getRmqIp()+":"+Main.getRmqPort()+"/api/permissions/%2f/"+login );
            putConfigPermissions.addHeader( "Authorization", "Basic " + enc );
            putConfigPermissions.addHeader( "content-type", "application/json" );
            putConfigPermissions.setEntity( new StringEntity( "{\"configure\":\".*\",\"write\":\".*\",\"read\":\".*\"}" ) ); // Permission you wanna. Check RabbitMQ HTTP API for details
            client.execute( putConfigPermissions );

        }catch( Exception ex ){
            log(ex.getMessage());
            return false;
        }
        return true;
    }

    public void log(String msg){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Main.getLogBox().appendText("\n"+msg);
            }
        });

    }
}
