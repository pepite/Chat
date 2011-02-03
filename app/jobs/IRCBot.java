package jobs;

import com.google.gson.Gson;
import controllers.Chat;
import f00f.net.irc.martyr.commands.MessageCommand;
import models.ChatRoom;
import models.Message;
import notifiers.Event;
import org.jibble.pircbot.PircBot;

import org.jibble.pircbot.User;
import play.Logger;
import play.Play;
import play.Play.Mode;
import play.cache.Cache;

import java.util.Hashtable;

public class IRCBot extends PircBot {

    public static Hashtable<String, IRCBot> refs = new Hashtable<String, IRCBot>();

    private static IRCBot getBot() {
        return refs.get("irc-bot");
    }

    public static void start() throws Exception {
        IRCBot bot = getBot();
        if (bot != null) {
            Logger.info("disconnecting first");
            bot.disconnect();
            Thread.sleep(1000);
            Logger.info("disconnecting done");
        } else {
            bot = new IRCBot() {
                {
                    setName(Play.configuration.getProperty("irc.nick", "ChatBot"));
                }
            };
            refs.put("irc-bot", bot);
            if (Play.mode == Mode.DEV)
                bot.setVerbose(true);
        }
        bot.setAutoNickChange(true);
        bot.setEncoding("UTF-8");
        bot.connect(Play.configuration.getProperty("irc.server", "irc.lunatech.com"));
        bot.joinChannel(Play.configuration.getProperty("irc.channel", "#test2"));
    }

    /**
     * This method is called when someone post a message to IRC.
     *
     * @param channel
     * @param sender
     * @param login
     * @param hostname
     * @param message
     */
    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        if (Play.configuration.getProperty("irc.channel", "#test").equals(channel)) {
            ChatRoom chatRoom = ((ChatRoom) Cache.get(channel));
            if (chatRoom != null) {
                chatRoom.says(sender, message);
                Event.sendMessage(new Gson().toJson(new Message(sender, message)));
            }
        }
    }

    /**
     * This method checks that the nickname is available in this room.
     *
     * @param nick
     * @return
     */
    public static boolean isNickAvailable(String nick) {
        IRCBot bot = getBot();

        User[] users = bot.getUsers(Play.configuration.getProperty("irc.channel", "#test2"));
        for (User user : users) {
            if (user.getNick().equals(nick)) {
                return false;
            }
        }

        return true;
    }


}
