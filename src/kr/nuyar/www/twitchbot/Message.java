package kr.nuyar.www.twitchbot;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {
    static final Pattern PATTERN_PRIVMSG = Pattern.compile("@([\\s\\S]+):(\\w+)!\\w+@\\w+\\.tmi\\.twitch\\.tv PRIVMSG #(\\w+) :([\\s\\S]+)");
    static final String[] REPLACE_KEY = new String[]{"(subscribe)", "(broadcaster)", "(moderator)", "(subscriber)", "(display-name)", "(room-id)", "(user-id)", "(user)", "(channel)"};

    IRC irc;
    String user;
    String channel;
    String message;

    Map<String, String> tags;
    int subscribe;
    boolean badge_subscriber;
    boolean badge_broadcaster;
    boolean badge_moderator;
    String user_display_name;
    String room_id;
    String user_id;

    public static Message parseMessage(IRC irc, String line) {
        Matcher matcher = PATTERN_PRIVMSG.matcher(line);
        if(!matcher.find())
            return null;

        Message message = new Message();
        message.irc = irc;
        message.user = matcher.group(2);
        message.channel = matcher.group(3);
        message.message = matcher.group(4);

        message.tags = new HashMap<>();
        for (String s : matcher.group(1).split(";")) {
            int i = s.indexOf('=');
            message.tags.put(s.substring(0,i), s.substring(i+1));
        }

        Matcher temp1 = Pattern.compile("subscriber\\/(\\d+)").matcher(message.tags.get("badge-info"));
        if(temp1.find())
            message.subscribe = Integer.parseInt(temp1.group(1));
        else
            message.subscribe = 0;
        message.badge_subscriber = message.tags.get("badges").contains("subscriber");
        message.badge_broadcaster = message.tags.get("badges").contains("broadcaster");
        message.badge_moderator = message.tags.get("badges").contains("moderator");
        message.user_display_name = message.tags.get("display-name");
        message.room_id = message.tags.get("room-id");
        message.user_id = message.tags.get("user-id");

        return message;
    }

    public void responseMessage(String message) {
        this.irc.sendMessage(message);
    }

    public String replaceKeys(String message) {
        if(message == null) return null;
        return StringUtils.replaceEach(message, REPLACE_KEY, new String[]{
                String.valueOf(this.subscribe),
                String.valueOf(this.badge_broadcaster),
                String.valueOf(this.badge_moderator),
                String.valueOf(this.badge_subscriber),
                this.user_display_name,
                this.room_id,
                this.user_id,
                this.user,
                this.channel
        });
    }

    static String replaceArguments(String message, String[] args, String argstr) {
        if(message == null) return null;
        for (int i = 0; i < args.length; i++) {
            message = message.replaceAll("\\(arguments\\["+i+"\\]\\)", args[i]);
        }
        if (args.length > 0) {
            message = message.replaceAll("\\(arguments\\)", argstr);
        }
        return message;
    }
}
