package kr.nuyar.www.twitchbot;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class Command {
    String command;
    List<String> aliases;
    int subscription;
    String msgNoSubscription;
    int arguments;
    String msgWrongUsage;
    String msgResponse;
    String msgBroadcast;
    String execute;

    public void processMessage(Message msg) {
        boolean is = false;
        is = is || msg.message.equals(this.command) || msg.message.startsWith(this.command + " ");
        for (String alias : aliases) {
            is = is || msg.message.equals(alias) || msg.message.startsWith(alias + " ");
        }
        if(!is) return;

        Bukkit.getLogger().info(String.format("[TwitchBot] %s used command %s in #%s : %s", msg.user, this.command, msg.channel, msg.message));

        if(this.subscription > msg.subscribe) {
            if(this.msgNoSubscription != null) msg.responseMessage(this.msgNoSubscription);
            return;
        }

        String[] arguments = msg.message.split(" ");
        if(this.arguments != -1 && arguments.length-1 != this.arguments) {
            if(this.msgWrongUsage != null) msg.responseMessage(this.msgWrongUsage);
            return;
        }

        String response = this.msgResponse;
        String broadcast = this.msgBroadcast;
        String execute = this.execute;
        if(response != null) {
            response = msg.replaceKeys(response);
            for (int i = 1; i < arguments.length; i++) {
                response = response.replaceAll("\\(arguments\\["+(i-1)+"\\]\\)", arguments[i]);
            }
            if (arguments.length > 1) {
                response = response.replaceAll("\\(arguments\\)", msg.message.substring(msg.message.indexOf(' ')+1));
            }
        }
        if(broadcast != null) {
            broadcast = msg.replaceKeys(broadcast);
            for (int i = 1; i < arguments.length; i++) {
                broadcast = broadcast.replaceAll("\\(arguments\\["+(i-1)+"\\]\\)", arguments[i]);
            }
            if (arguments.length > 1) {
                broadcast = broadcast.replaceAll("\\(arguments\\)", msg.message.substring(msg.message.indexOf(' ')+1));
            }
        }
        if(execute != null) {
            execute = msg.replaceKeys(execute);
            for (int i = 1; i < arguments.length; i++) {
                execute = execute.replaceAll("\\(arguments\\["+(i-1)+"\\]\\)", arguments[i]);
            }
            if (arguments.length > 1) {
                execute = execute.replaceAll("\\(arguments\\)", msg.message.substring(msg.message.indexOf(' ')+1));
            }
        }


        if(response != null)  msg.responseMessage(response);
        String finalExecute = execute;
        String finalBroadcast = broadcast;
        Bukkit.getScheduler().runTask(TwitchBot.plugin, () -> {
            if(finalBroadcast != null) Bukkit.broadcastMessage(finalBroadcast);
            if(finalExecute != null) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalExecute);
        });
    }

    public static Command parseFromConfig(ConfigurationSection section) {
        Command command = new Command();
        command.command = section.getName();
        command.aliases = section.getStringList("aliases");
        command.subscription = section.getInt("subscription", 0);
        command.msgNoSubscription = section.getString("msg.nosubscription",null);
        if(command.msgNoSubscription != null && command.msgNoSubscription.trim().isEmpty()) command.msgNoSubscription = null;
        command.arguments = section.getInt("arguments", -1);
        command.msgWrongUsage = section.getString("msg.wrongusage", null);
        if(command.msgWrongUsage != null && command.msgWrongUsage.trim().isEmpty()) command.msgWrongUsage = null;
        command.msgResponse = section.getString("msg.response", null);
        if(command.msgResponse != null && command.msgResponse.trim().isEmpty()) command.msgResponse = null;
        command.msgBroadcast = section.getString("msg.broadcast", null);
        if(command.msgBroadcast != null && command.msgBroadcast.trim().isEmpty()) command.msgBroadcast = null;
        command.execute = section.getString("execute",null);
        if(command.execute != null && command.execute.trim().isEmpty()) command.execute = null;

        return command;
    }
}
