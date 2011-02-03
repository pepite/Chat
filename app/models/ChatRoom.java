/**
 *
 * Copyright 2010, Nicolas Leroux.
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * IRCUser: ${user}
 * Date: ${date}
 *
 */
package models;

import play.Play;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;

public class ChatRoom implements Serializable {

    public List<Message> messages = new ArrayList<Message>();

    private String name;

    public ChatRoom(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void say(Message msg) {
        messages.add(msg);
    }

    public void says(String nick, String message) {
        // Is the nick has  a picture?
        checkPicture(nick);
        messages.add(new Message(nick, message));
    }

    public void checkPicture(String nick) {
        // Use the name of the nickname for the picture as well
        File file = Play.getFile("/data/" + nick + "-thumb.png");

        // If picture is not specified, use a default one.
        if (!file.exists()) {
            int number = (new Random().nextInt(12) + 1);
            file = Play.getFile("/public/images/" + number + ".png");
            File dest = Play.getFile("/data/" + nick + "-thumb.png");
            try {
                FileUtils.copyFile(file, dest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

     public List<Message> getLastMessages(Date date) {
        List<Message> filteredMessages = new ArrayList<Message>();
        for (Message msg : messages) {
            if (msg.date.after(date)) {
                filteredMessages.add(msg);
            }
        }
        return filteredMessages;
    }

}
