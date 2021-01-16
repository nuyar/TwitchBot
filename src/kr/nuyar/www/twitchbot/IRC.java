package kr.nuyar.www.twitchbot;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IRC implements Runnable {
    public static final String SERVER = "irc.chat.twitch.tv";
    public static final int PORT = 6667;

    private final Channel channel;

    private BukkitTask task;

    private Socket socket;
    private OutputStreamWriter out;
    private BufferedReader in;

    long lastCommunication;
    boolean socketclosing = false;

    public IRC(Channel channel) {
        this.channel = channel;
    }

    void startIRC() {
        this.task = Bukkit.getScheduler().runTaskAsynchronously(TwitchBot.plugin, this);
    }

    void stopIRC() {
        this.socketclosing = true;
        try {
            this.out.close();
        } catch (IOException ignored) {
        }
        try {
            this.in.close();
        } catch (IOException ignored) {
        }
        try {
            this.socket.close();
        } catch (IOException ignored) {
        }
        this.task.cancel();
    }

    public void sendMessage(String message) {
        if(!this.channel.anonymous)
            this.write(String.format("PRIVMSG #%s :%s", this.channel.channel, message));
    }

    private void write(String str) {
        try {
            this.out.write(str);
            this.out.write('\n');
            this.out.flush();
            if (TwitchBot.plugin.logIRC)
                Bukkit.getLogger().info(String.format("[TwitchBot] #%s < %s", channel.channel, str));
        } catch (IOException e) {
            Bukkit.getLogger().warning(String.format("[TwitchBot] Failed to send \"%s\" to #%s", str, channel.channel));
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            this.socket = new Socket(SERVER, PORT);
            this.out = new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));

            this.out.write(String.format(
                    "PASS %s\n" +
                    "NICK %s\n" +
                    "CAP REQ :twitch.tv/tags\n" +
                    "JOIN #%s\n",
                    this.channel.oauth, this.channel.nickname, this.channel.channel));
            this.out.flush();

            Bukkit.getLogger().warning(String.format("[TwitchBot] Twitch bot #%s is connected to the twitch irc server.", this.channel.channel));

            String line;
            while((line = this.in.readLine()) != null) {
                this.lastCommunication = System.currentTimeMillis();
                if (TwitchBot.plugin.logIRC)
                    Bukkit.getLogger().info(String.format("[TwitchBot] #%s > %s", channel.channel, line));

                if(line.startsWith("PING")) {
                    this.write("PONG");
                    Bukkit.getLogger().info("[TwitchBot] Twitch bot #" + this.channel.channel + " pinged connection.");
                    continue;
                }

                if(line.contains(":tmi.twitch.tv NOTICE * :Improperly formatted auth")) {
                    Bukkit.getLogger().warning(String.format("[TwitchBot] Twitch bot #%s has improperly formatted oauth token! Oauth token needs prefix 'oauth:'.", this.channel.channel));
                    this.channel.stopIRC();
                }

                if(line.contains(":tmi.twitch.tv NOTICE * :Login authentication failed")) {
                    Bukkit.getLogger().warning(String.format("[TwitchBot] Twitch bot #%s has wrong oauth token! Please update your oauth token.", this.channel.channel));
                    this.channel.stopIRC();
                }

                if(line.contains("Welcome, GLHF!")) {
                    Matcher matcher = Pattern.compile(":tmi\\.twitch\\.tv 001 ([\\w\\W]+) :Welcome, GLHF!").matcher(line);
                    if(matcher.find()) {
                        if(!this.channel.nickname.equals(matcher.group(1))) {
                            this.channel.nickname = matcher.group(1);
                            Bukkit.getLogger().warning(String.format("[TwitchBot] Twitch bot #%s has mismatched nickname and oauth token. Please check your config.", this.channel.channel));
                        }
                        Bukkit.getLogger().warning(String.format("[TwitchBot] Twitch bot #%s is successfully logged in. Nickname is %s", this.channel.channel, this.channel.nickname));
                    }
                }

                if(line.contains("PRIVMSG")) {
                    Message msg = Message.parseMessage(this, line);
                    if(msg != null)
                        this.channel.processMessage(msg);
                }
            }

            if(this.socketclosing) {
                this.channel.stopIRC();
                Bukkit.getLogger().warning(String.format("[TwitchBot] Twitch bot #%s is disconnected from twitch irc server.", this.channel.channel));
            } else {
                Bukkit.getLogger().warning(String.format("[TwitchBot] Twitch bot #%s is disconnected from twitch irc channel with exception.", this.channel.channel));
                this.channel.reconnectIRC();
            }
        } catch (Exception e) {
            if(this.socketclosing) {
                this.channel.stopIRC();
                Bukkit.getLogger().warning(String.format("[TwitchBot] Twitch bot #%s is disconnected from twitch irc server.", this.channel.channel));
            } else {
                Bukkit.getLogger().warning(String.format("[TwitchBot] Twitch bot #%s is disconnected from twitch irc channel with exception.", this.channel.channel));
                e.printStackTrace();
                this.channel.reconnectIRC();
            }
        }
    }
}
