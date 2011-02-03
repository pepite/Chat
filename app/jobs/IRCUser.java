package jobs;

import f00f.net.irc.martyr.GenericCommandAutoService;
import f00f.net.irc.martyr.IRCConnection;
import f00f.net.irc.martyr.InCommand;
import f00f.net.irc.martyr.clientstate.ClientState;
import f00f.net.irc.martyr.commands.MessageCommand;
import f00f.net.irc.martyr.commands.QuitCommand;
import f00f.net.irc.martyr.services.AutoJoin;
import f00f.net.irc.martyr.services.AutoReconnect;
import f00f.net.irc.martyr.services.AutoRegister;
import f00f.net.irc.martyr.services.AutoResponder;
import jobs.IRCBot;
import models.ChatRoom;
import org.jibble.pircbot.PircBot;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.*;

import java.io.Serializable;
import java.util.Date;

public class IRCUser extends PircBot implements Serializable, Notification {

    @Required
    @CheckWith(value = NickCheck.class, message = "Already logged in")
    public String nickname;

    @Required
    public String pass;

    @Equals(value = "pass", message = "The two passwords should match")
    public transient String verifiedPassword;

    public Date lastSignin;

    public IRCUser() {};

    public IRCUser(String nick, String password) {
        this.nickname = nick;
        this.pass = password;
    }

    static class NickCheck extends Check {

        public boolean isSatisfied(Object user, Object nickname) {
            return IRCBot.isNickAvailable(nickname.toString());
        }
    }

    public void connect() throws Exception {
        setName(nickname);
        if (Play.mode == Play.Mode.DEV) {
                setVerbose(true);
        }
        setAutoNickChange(true);
        setEncoding("UTF-8");
        connect(Play.configuration.getProperty("irc.server", "irc.lunatech.com"));
        joinChannel(Play.configuration.getProperty("irc.channel", "#test2"));
    }

    public void says(String nick, String text) {
        String channel = Play.configuration.getProperty("irc.channel", "#test2");
        sendMessage(channel, text);
     }

}