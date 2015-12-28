package sample;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.lang.invoke.SwitchPoint;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Alexander on 18.12.2015.
 */
public class DiscoveryThread implements Runnable {
//singleton
    public static DiscoveryThread getInstance() {
        return DiscoveryThreadHolder.INSTANCE;
    }
    private static class DiscoveryThreadHolder {
        private static final DiscoveryThread INSTANCE = new DiscoveryThread();
    }

    DatagramSocket socket;

    @Override
    public void run() {
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            socket = new DatagramSocket(9999, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            while (true) {
                log( "Ready to receive broadcast packets!");
                //Receive a packet
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);
                //Packet received
                log( "Packet received from: " + packet.getAddress().getHostAddress());
//                log( "Received; data: " + new String(packet.getData()));
                String client=packet.getAddress().getHostAddress();
                //See if the packet holds the right command (message)
                String message = new String(packet.getData()).trim();
                String[] parts=message.split(";");
                String answer="";
                Users users;
                switch (parts[0]){
                    case "AuthServer?":
                        log("Got discovery request from: "+client);
                        answer="AuthServer";
                        break;
                    case "signIn":
                        log("Got Sign In request from: "+client);
                        users=Main.getUsers();
                        boolean signin=users.signIn(parts[1],parts[2]);
                        answer=signin?"OK":"Wrong login/password";
                        break;
                    case "signUp":
                        log("Got Sign Up request from: "+client);
                        users=Main.getUsers();
                        boolean signup=users.signUp(parts[1],parts[2]);
                        answer=signup?"OK":"Something went Wrong (T_T)";
                        break;
                    case "userExists":
                        log("Got userExists request from: "+client);
                        boolean exist=Main.getUsers().exists(parts[1]);
                        answer=exist?"Exists":"No such user";
                        break;
                    case "import":
                        if(isThisMyIpAddress(packet.getAddress())) continue;
                        log("Got data import request from: "+client);
                        answer=Main.getUsers().toString();
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

    public static boolean isThisMyIpAddress(InetAddress addr) {
        // Check if the address is a valid special local or loop back
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
            return true;

        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {
            return false;
        }
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
