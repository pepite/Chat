package models;

import java.io.Serializable;
import java.util.*;

public class Message implements Serializable {
    
    public String user;
    public Date date;
    public String text;

    public Message(String user, String text) {
        this.user = user;
        this.text = text;
        this.date = new Date();
    }


}