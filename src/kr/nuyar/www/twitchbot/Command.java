package kr.nuyar.www.twitchbot;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.stream.Collectors;

public class Command {
    String command;
    List<String> aliases;
    int subscription;
    String msgNoSubscription;
    int arguments;
    String msgWrongUsage;
    String msgResponse;
    String msgBroadcast;
    List<String> execute;

    public void processMessage(Message msg) {
        String cmd = null;
        if(msg.message.equals(this.command) || msg.message.startsWith(this.command + " "))
            cmd = this.command;
        for (String alias : aliases) {
            if(msg.message.equals(alias) || msg.message.startsWith(alias + " "))
                cmd = alias;
        }
        if(cmd == null) return;

        Bukkit.getLogger().info(String.format("[TwitchBot] %s used command %s in #%s : %s", msg.user, this.command, msg.channel, msg.message));

        if(this.subscription > msg.subscribe) {
            if(this.msgNoSubscription != null) msg.responseMessage(msg.replaceKeys(this.msgNoSubscription));
            return;
        }

        String argstr = msg.message.substring(cmd.length()).trim().replaceAll("\\(arguments(\\[\\d+\\])*\\)","").replaceAll("[ ]{2,}"," ");

        String[] arguments;
        if (argstr.isEmpty())
            arguments = new String[]{};
        else
            arguments = argstr.split(" ");
        if(this.arguments != -1 && arguments.length != this.arguments) {
            if(this.msgWrongUsage != null) msg.responseMessage(msg.replaceKeys(this.msgWrongUsage));
            return;
        }
        
        String response = Message.replaceArguments(msg.replaceKeys(this.msgResponse),arguments,argstr);
        String broadcast = Message.replaceArguments(msg.replaceKeys(this.msgBroadcast),arguments,argstr);
        List<String> execute = this.execute.stream().map(ex -> Message.replaceArguments(msg.replaceKeys(ex),arguments,argstr)).collect(Collectors.toList());

        if(response != null)  msg.responseMessage(response);
        Bukkit.getScheduler().runTask(TwitchBot.plugin, () -> {
            if(broadcast != null) Bukkit.broadcastMessage(broadcast);
            execute.forEach(ex -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ex));
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
        command.execute = section.getStringList("execute");

        return command;
    }
}
