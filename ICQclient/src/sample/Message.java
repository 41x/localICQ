package sample;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by Alexander on 17.12.2015.
 */
public class Message {
    private String text;
    private String status;
    private Date time;
    private boolean me;
    public Message(String text, Date time, boolean me) {
        this.me=me;
        this.text=text;
        this.time=time;
    }

    public boolean isMe() {
        return me;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }
}

