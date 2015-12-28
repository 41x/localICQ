package sample;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.StringJoiner;

/**
 * Created by Alexander on 19.12.2015.
 */
public class AuthServer {
    private static AuthServer ourInstance = new AuthServer();

    private InetAddress ip;


    public static AuthServer getInstance() {
        ourInstance.findServer();
        return ourInstance;
    }

    public boolean userExists(String user){
        Main.log("Checking if user: "+user+" exists");
        DatagramSocket socket=null;
        try {
            //Open a random port to send the package
            socket = new DatagramSocket();
//            socket.setBroadcast(true);

            byte[] sendData = ("userExists;"+user).getBytes();
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, 9999);
                socket.send(sendPacket);
            } catch (Exception e) {
            }

            Main.log("Waiting for a Server reply...");
            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(receivePacket);
            //We have a response
            Main.log("Got response from Server: " + receivePacket.getAddress().getHostAddress());
            //Check if the message is correct
            String message = new String(receivePacket.getData()).trim();
            if (message.equals("Exists")) {
                Main.log(message);
                return true;
            }else {
                Main.log(message);
                return false;
            }
        } catch (IOException ex) {
            Main.log(ex.getMessage());
            return false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public boolean signIn(String login,String password){
        Main.log("Trying to sign in "+login+"...");
        DatagramSocket socket=null;
        try {
            //Open a random port to send the package
            socket = new DatagramSocket();
//            socket.setBroadcast(true);
            if (login.trim().equals("") || password.trim().equals("")) throw new Exception("Wrong login/password");
            byte[] sendData = ("signIn;"+login+";"+password).getBytes();
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, 9999);
                socket.send(sendPacket);
                Main.log("SignIn packet sent to: "+ip.toString());
            } catch (Exception e) {
            }

            Main.log("Waiting for a Server reply...");
            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.setSoTimeout(1000);
            socket.receive(receivePacket);
            //We have a response
            Main.log("Got response from Server: " + receivePacket.getAddress().getHostAddress());
            //Check if the message is correct
            String message = new String(receivePacket.getData()).trim();
            if (message.equals("OK")) {
                Main.log("Success");
                return true;
            }else {
                Main.log(message);
                return false;
            }
        } catch (Exception ex) {
            Main.log(ex.getMessage());
            return false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }


    public boolean signUp(String login,String password,String cpassword){
        Main.log("Trying to sign up "+login+"...");
        if(!password.equals(cpassword)) {
            Main.log("Passwords are not equal");
            return false;
        }
        DatagramSocket socket=null;
        try {
            //Open a random port to send the package
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            if (login.trim().equals("") || password.trim().equals("")) throw new Exception("Wrong login/password");
            byte[] sendData = ("signUp;"+login+";"+password).getBytes();
            //Try the 255.255.255.255 first
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName("255.255.255.255"), 9999);
                socket.send(sendPacket);
                Main.log("SignUp packet sent to: 255.255.255.255");
            } catch (Exception e) {
            }
            Main.log("Waiting for a Server reply...");
            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.setSoTimeout(2000);
            socket.receive(receivePacket);
            //We have a response
            Main.log("Got response from Server: " + receivePacket.getAddress().getHostAddress());
            //Check if the message is correct
            String message = new String(receivePacket.getData()).trim();
            if (message.equals("OK")) {
                Main.log("Registration Successful");
                return true;
            }else {
                Main.log(message);
                return false;
            }

        } catch (Exception ex) {
            Main.log(ex.getMessage());
            return false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }


    private AuthServer() {
    }

    public void findServer(){
        // Find the server using UDP broadcast
        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            byte[] sendData = "AuthServer?".getBytes();
            //Try the 255.255.255.255 first
            try {
                Main.log("Searching AuthServer...");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName("255.255.255.255"), 9999);
                socket.send(sendPacket);
                Main.log("Request packet sent to: 255.255.255.255 (DEFAULT)");
            } catch (Exception e) {
            }
            Main.log("Waiting for a Server reply!");
            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.setSoTimeout(1000);
            socket.receive(receivePacket);
            //We have a response
            Main.log("Got response from: " + receivePacket.getAddress().getHostAddress());
            //Check if the message is correct
            String message = new String(receivePacket.getData()).trim();
            if (message.equals("AuthServer")) {
                Main.log("Found AuthServer: "+receivePacket.getAddress().toString());
                ip=receivePacket.getAddress();
            }
            //Close the port!
            socket.close();
        } catch (IOException ex) {
            Main.log(ex.getMessage());
        }
    }
}
