package controllers;

import com.google.gson.Gson;
import jobs.IRCUser;
import models.ChatRoom;
import models.Message;
import notifiers.Event;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdesktop.swingx.graphics.GraphicsUtilities;
import org.jdesktop.swingx.graphics.ReflectionRenderer;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.classloading.enhancers.ControllersEnhancer;
import play.data.validation.Valid;
import play.mvc.Before;
import play.mvc.Controller;
import play.notifiers.Broadcast;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Chat extends Controller {

    private final static String name = Play.configuration.getProperty("irc.channel");

    @Before(unless = {"signin", "register", "unlock", "addPicture", "upload"})
    static void checkLogged() {
        ChatRoom chatRoom = ((ChatRoom) Cache.get(name));
        if (chatRoom == null) {
            session.clear();
            signin();
        }
        if (!session.contains("nick")) {
            signin();
        }
    }

    public static void index() {
        ChatRoom chatRoom = ((ChatRoom) Cache.get(name));

        List<Message> msgs = chatRoom.messages;
        render(msgs);
    }

    public static void postMessage(String message) {
        final String nick = session.get("nick");
        IRCUser IRCUser = (IRCUser) Cache.get(nick);
        IRCUser.says(nick, message);
    }

    public static void newMessages() {
        ChatRoom chatRoom = ((ChatRoom) Cache.get(name));
        List<Message> messages = chatRoom.getLastMessages(request.date);
        Logger.info("request.date " + request.date + " message size [" + messages.size() + "]");
        if (messages.isEmpty()) {
            suspend("5s");
        }
        renderJSON(messages);
    }

    public static void signin() {
        render();
    }

    public static void unlock(String nickname, String password) {
        IRCUser IRCUser = (IRCUser) Cache.get(nickname);

        if (IRCUser != null && password.equals(IRCUser.pass)) {
            session.put("nick", IRCUser.nickname);
            index();
        } else {
            flash.error("Screen not locked or incorrect password");
            signin();
        }
    }

    public static void register(@Valid final IRCUser user, String picture) {
        if (validation.hasErrors()) {
            flash.put("register", true);
            flash.error("Please give a nick name");
            signin();
        }

        // Use the name of the nickname for the picture as well
        File file = Play.getFile("/data/" + picture + ".png");


        // TODO: Move that to the entity... No business logic in the controller
        // If picture is not specified, use a default one.
        if (!file.exists() || "".equals(picture)) {
            int number = (new Random().nextInt(12) + 1);
            file = Play.getFile("/public/images/" + number + ".png");
            File dest = Play.getFile("/data/" + user.nickname + "-thumb.png");
            try {
                FileUtils.copyFile(file, dest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            File dest = Play.getFile("/data/" + user.nickname + "-thumb.png");
            try {
                //Logger.info(file + " -> " + user.nickname + ".png");
                if (dest.exists()) {
                    dest.delete();
                }
                FileUtils.moveFile(file, dest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ChatRoom chatRoom = (ChatRoom) Cache.get(name);
        if (chatRoom == null) {
            chatRoom = new ChatRoom(name);
            Cache.add(name, chatRoom);
        }

        IRCUser cachedIRCUser = (IRCUser) Cache.get(user.nickname);
        if (cachedIRCUser == null) {
            cachedIRCUser = new IRCUser(user.nickname, user.pass);
            try {
                cachedIRCUser.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Cache.add(user.nickname, cachedIRCUser);
            // This is a one time operation, not sure it belongs here...
            //chatRoom.says("notice", IRCUser.nick + " has joined the room");
        }
        session.put("nick", user.nickname);
        index();

    }

    public static void disconnect() {
        ((ChatRoom) Cache.get(name)).says("notice", session.get("nick") + " has left the room");
        IRCUser cachedIRCUser = (IRCUser) Cache.get(session.get("nick"));
        Cache.delete(session.get("nick"));
        session.clear();
        cachedIRCUser.disconnect();
        signin();
    }

    public static void lock() {
        IRCUser cachedIRCUser = (IRCUser) Cache.get(session.get("nick"));
        cachedIRCUser.lastSignin = new Date();
        session.clear();
        signin();
    }

    public static void addPicture(File picture) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        renderThumbnail(picture, os, 64);
        String thumbnail = "/data/" + picture.getName() + ".png";
        IOUtils.write(os.toByteArray(), new FileOutputStream(Play.getFile(thumbnail)));
        os = new ByteArrayOutputStream();

        renderImage(picture, os, 120);
        String reflection = "/data/" + picture.getName() + "-reflection.png";
        IOUtils.write(os.toByteArray(), new FileOutputStream(Play.getFile(reflection)));
        renderText(reflection);
    }

    protected static void renderImage(final File file, final OutputStream out, int height) throws IOException {
        FileInputStream is = new FileInputStream(file);

        BufferedImage src = ImageIO.read(is);
        if (src.getHeight() <= height) {
            height = src.getHeight() - 1;
        }
        BufferedImage thumb = GraphicsUtilities.createThumbnail(src, height);

        ReflectionRenderer reflectionRenderer = new ReflectionRenderer();
        BufferedImage reflection = reflectionRenderer.appendReflection(thumb);
        reflection.flush();
        ImageIO.write(reflection, "png", out);
    }

    protected static void renderThumbnail(final File file, final OutputStream out, int height) throws IOException {
        FileInputStream is = new FileInputStream(file);

        BufferedImage src = ImageIO.read(is);
         if (src.getHeight() <= height) {
            height = src.getHeight() - 1;
        }
        BufferedImage thumb = GraphicsUtilities.createThumbnail(src, height);

        ImageIO.write(thumb, "png", out);
    }

    public static void upload(File f, String title) throws Exception {
        //Logger.info("" + f.length());
        FileUtils.moveFile(f, Play.getFile("/data/" + System.nanoTime()));
        renderText("ok");
    }


}