package sample;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Alexander on 17.12.2015.
 */
public class Contacts extends ArrayList<Contact> {
    private static Contacts ourInstance = new Contacts();
    private boolean dirty;
    private String selected;
    private String file;

    public static Contacts getInstance() {
        return ourInstance;
    }


    public Contacts() {

    }

    public void fill(String user){
        ourInstance.clear();
        ourInstance.file="./"+user+"Contacts.txt";
        File f = new File(file);
        if(!f.exists()) {
            f.getParentFile().mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String contactname;
            while ((contactname = br.readLine()) != null) {
                if(contactname.trim().equals("")) continue;
                ourInstance.add(new Contact(contactname));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        ourInstance.dirty=true;
    }

    public void DeleteContact(){
        if (selected.trim().equals("")) return;
        int i=findIndex(selected);
        if (i==-1) return;
        ourInstance.remove(i);
        ourInstance.save();
        ourInstance.setDirty(true);
    }

    public void AddContact(String username){
        if(username.trim().equals("") || ourInstance.containsUser(username)) return;
        if(!AuthServer.getInstance().userExists(username.trim())) return;
        ourInstance.add(new Contact(username));
        ourInstance.save();
        ourInstance.setDirty(true);
    }

    private void save() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(ourInstance.file, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        for(Contact c:ourInstance){
            writer.println(c.getContactId());
        }
        writer.close();
    }

    private boolean containsUser(String contact){
        int i=0;
        while (i<ourInstance.size() && !ourInstance.get(i).getContactId().equals(contact)) i++;
        if(i<ourInstance.size()) return true;
        return false;
    }

    public int findIndex(String c){
        int i=0;
        while (i<ourInstance.size() && !ourInstance.get(i).getContactId().equals(c)) i++;
        if(i<ourInstance.size()) return i;
        return -1;
    }

    public Contact find(String c){
        int i=0;
        while (i<ourInstance.size() && !ourInstance.get(i).getContactId().equals(c)) i++;
        if(i<ourInstance.size()) return get(i);
        return null;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }
}
