package sample;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Alexander on 19.12.2015.
 */
public class Server {
    private InetAddress ip;
    private String[] queues;

    public boolean containsQueue(String queue){
        int i=0;
        while (i<queues.length && !queues[i].equals(queue))i++;
        return i<queues.length?true:false;
    }

    public Server(InetAddress ip, String[] queues) {
        this.ip = ip;
        this.queues = queues;
    }

    public boolean registerUser(String login,String password){
        QueueWatcher.log("Trying to register new user: "+login+"...");
        DatagramSocket socket=null;
        try {
            //Open a random port to send the package
            socket = new DatagramSocket();

            byte[] sendData = ("createRmqUser;"+login+";"+password).getBytes();
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, 8888);
                socket.send(sendPacket);
            } catch (Exception e) {
            }

            QueueWatcher.log("Waiting for a Server reply...");
            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.setSoTimeout(200);
            socket.receive(receivePacket);
            //We have a response
            QueueWatcher.log("Got response from Server: " + receivePacket.getAddress().getHostAddress());
            //Check if the message is correct
            String message = new String(receivePacket.getData()).trim();
            if (message.equals("OK")) {
                QueueWatcher.log("Success");
                return true;
            }else {
                QueueWatcher.log(message);
                return false;
            }
        } catch (IOException ex) {
            QueueWatcher.log(ex.getMessage());
            return false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public boolean alive(String login, String password){
        QueueWatcher.log("Server status...");
        DatagramSocket socket=null;
        try {
            socket = new DatagramSocket();
            byte[] sendData = ("alive;"+login+";"+password).getBytes();
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, 8888);
                socket.send(sendPacket);
            } catch (Exception e) {
                QueueWatcher.log(e.getMessage());
            }

            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(receivePacket);
            //We have a response
            //Check if the message is correct
            String message = new String(receivePacket.getData()).trim();
            if (message.equals("OK")) {
                QueueWatcher.log("Running");
                return true;
            }else {
                QueueWatcher.log(message);
                return false;
            }
        } catch (IOException ex) {
            QueueWatcher.log(ex.getMessage());
            return false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public InetAddress getIp() {
        return ip;
    }

    public String[] getQueues() {
        return queues;
    }


}
