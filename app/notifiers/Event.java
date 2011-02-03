package notifiers;

import controllers.Chat;
import jobs.IRCUser;
import models.Message;
import play.Logger;
import play.cache.Cache;
import play.mvc.Notifier;
import play.mvc.Router;
import play.mvc.Scope;
import play.notifiers.Broadcast;
import play.server.websocket.WebSocketServerHandler;

public class Event extends Notifier {

    public static void receiveMessage(String text) {
        String nick = text.substring(0, text.indexOf(":"));
        String message = text.substring(text.indexOf(":") + 1);

        // Notify all plugins
        IRCUser user = (IRCUser) Cache.get(nick);
        user.says(nick, message);

    }

    @Broadcast
    public static void sendMessage(String text) {
        renderJSON(text);
    }

}


