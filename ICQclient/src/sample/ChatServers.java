package sample;

import javafx.application.Platform;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Created by ваа on 22.12.2015.
 */
public class ChatServers extends ArrayList<Server> {
    private static ChatServers ourInstance = new ChatServers();

    public static ChatServers getInstance() {
        ourInstance.refresh();
        return ourInstance;
    }

    private ChatServers() {
    }


    private void refresh(){
        ourInstance.clear();
        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            byte[] sendData = "who".getBytes();
            //Try the 255.255.255.255 first
            try {
//                log("Searching chat servers...");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName("255.255.255.255"), 8888);
                socket.send(sendPacket);
//                log("Request packet sent to: 255.255.255.255");
            } catch (Exception e) {
                log(e.getMessage());
            }
//            Main.log("Waiting for a Server reply!");

            try{
                while (true){
                    byte[] recvBuf = new byte[30000];
                    DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.setSoTimeout(300);
                    socket.receive(receivePacket);
                    //We have a response
//                    log("Got response from: " + receivePacket.getAddress().getHostAddress());
                    //Check if the message is correct
                    String[] message = new String(receivePacket.getData()).trim().split(";");
                    if (message[0].equals("ChatServer")) {
                        log("Found chat server: "+receivePacket.getAddress().toString());
                        JSONArray jsnarr=new JSONArray(message[1]);
                        ourInstance.add(new Server(receivePacket.getAddress(), jsnArrToQueArr(jsnarr)));
                    }
                }
            }catch(SocketTimeoutException ex){
                //Close the port!
//                log("Search finished.");
                socket.close();
            }
        } catch (Exception ex) {
            log(ex.getMessage());
        }



    }

    private void log(String message){
        Platform.runLater(() -> Main.getLogbox().appendText("\n"+message));
    }

    /*
    * search server containing the queue
    * */
    public Server findByQueue(String queue){
        int i=0;
        while (i<size() && !get(i).containsQueue(queue)) i++;
        if(i<size()) return get(i);
        return null;
    }

    /*
    * search server with minimum queues
    * */
    public Server findMinQue(){
        if (size()==0) return null;
        int k=0;
        int min=get(k).getQueues().length;

        for(int i=0;i<size();i++){
            if (get(i).getQueues().length<min){
                min=get(i).getQueues().length;
                k=i;
            }
        }
        return get(k);
    }

    private String[] jsnArrToQueArr(JSONArray jsnarr){
        String[] res=new String[jsnarr.length()];
        for(int i=0;i<jsnarr.length();i++){
            res[i]=((JSONObject)(jsnarr.get(i))).get("name").toString();
        }
        return res;
    }
}
