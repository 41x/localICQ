package sample;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by Alexander on 18.12.2015.
 */
public class Users extends ArrayList<User> {
    private DiscoveryThread dt;

    public Users() {
        try (BufferedReader br = new BufferedReader(new FileReader("./registeredUsers.txt")))
        {
            String userData;
            while ((userData = br.readLine()) != null) {
                if(userData.trim().equals("")) continue;

                String[] Data=userData.trim().split(";");
                add(new User(Data[0],Data[1]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Users(String message) {
        String[] users=message.split("\n");
        for (String u:users){
            if (u.trim().equals("")) continue;
            String[] cr=u.split(";");
            if(cr.length<2 || cr[0].equals("")||cr[1].equals("")) continue;
            add(new User(cr[0],cr[1]));
        }
    }


    public boolean exists(String user){
        int i=0;
        while (i<size() && !get(i).getLogin().equals(user)) i++;
        return i < size();
    }

    public boolean signIn(String login,String password){
        dt.log("Trying to sign in "+login+"...");
        int i=0;
        while (i<size() && (!get(i).getLogin().equals(login)|| !get(i).getPassword().equals(password))) i++;
        if (i<size()) {
            dt.log(login+" Signed In");
            return true;
        }
        dt.log("Login "+login+" or password is wrong");
        return false;
    }

    private int find(String login){
        int i=0;
        while (i<size() && !get(i).login.equals(login)) i++;
        if (i<size()) return i;
        return -1;
    }

    public void save(){
        PrintWriter writer;
        try {
            writer = new PrintWriter("./registeredUsers.txt", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        for(User u:this){
            writer.println(u.getLogin()+";"+u.getPassword());
        }
        writer.close();

    }

    public boolean signUp(String login, String password) {
        if(!Pattern.compile("[A-Za-z][A-Za-z0-9]*").matcher(login).matches()) return false;
        dt.log("Trying sign up "+login+"...");
        if(find(login)!=-1)
        {
            dt.log("User "+login+" is already registered");
            return false;
        }
        try{
            add(new User(login,password));
            save();
        }catch(Exception ex){
            dt.log("Could not add or save user\n"+ex.getMessage());
            return false;
        }
        dt.log("User "+login+" Registered");
        return true;
    }

    @Override
    public String toString(){
        String res="";
        for (User u:this){
            res+="\n"+u.getLogin()+";"+u.getPassword();
        }
        return res;
    }

    public void merge(Users users) {
        for (User u:users){
            if(this.find(u.login)==-1)
                add(u);
        }
    }

    public void setDt(DiscoveryThread dt) {
        this.dt = dt;
    }
}
