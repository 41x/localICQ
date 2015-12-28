package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Main extends Application {

    private static FXMLLoader loader;
    private static Users users;
    private static TextArea logBox;
    @Override
    public void start(Stage primaryStage) throws Exception{
        File f = new File("./registeredUsers.txt");
        if(!f.exists()) {
            f.getParentFile().mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        Stage stage=new Stage();
        stage.setTitle("Auth Server");
        Parent root;
        try {
            loader = new FXMLLoader(getClass().getResource("sample.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        logBox=((Controller)loader.getController()).logBox;
        stage.setScene(new Scene(root, 500, 450));
        stage.show();

        DiscoveryThread discovery=DiscoveryThread.getInstance();
        Main.setUsers(new Users());

        Thread t=new Thread(discovery);
        t.setDaemon(true);
        t.start();

        importData();
        Main.getUsers().setDt(discovery);
    }

    public void importData(){
        Main.log("Sending data import request...");
        DatagramSocket socket;
        // Find the server using UDP broadcast
        try {
            //Open a random port to send the package
            socket = new DatagramSocket();
            socket.setBroadcast(true);

            byte[] sendData = "import".getBytes();
            //Try the 255.255.255.255 first
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName("255.255.255.255"), 9999);
                socket.send(sendPacket);
                Main.log("Request packet sent to: 255.255.255.255");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Main.log("Waiting for a Server reply...");
            //Wait for a response
            byte[] recvBuf = new byte[50000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.setSoTimeout(1000);
            socket.receive(receivePacket);
            //We have a response
            Main.log("Got response from: " + receivePacket.getAddress().getHostAddress());
            //Check if the message is correct
            String message = new String(receivePacket.getData()).trim();
            if (message.equals("")) return;

            Users users=new Users(message);
            users.merge(getUsers());
            setUsers(users);
            users.save();

            socket.close();
        } catch (Exception ex) {
            Main.log(ex.getMessage());
        }
    }



    public static void setUsers(Users users) {
        Main.users = users;
    }

    public static Users getUsers() {
        return users;
    }

    public static TextArea getLogBox() {
        return logBox;
    }

    public static void log(String text){
        logBox.appendText("\n"+text);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
