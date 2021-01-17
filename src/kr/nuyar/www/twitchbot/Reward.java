package kr.nuyar.www.twitchbot;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.stream.Collectors;

public class Reward {
    String reward;
    int subscription;
    String msgNoSubscription;
    int arguments;
    String msgWrongUsage;
    String msgResponse;
    String msgBroadcast;
    List<String> execute;

    public void processMessage(Message msg) {
        if(!this.reward.equals(msg.tags.get("custom-reward-id")))
            return;

        Bukkit.getLogger().info(String.format("[TwitchBot] %s used reward %s in #%s : %s", msg.user, this.reward, msg.channel, msg.message));

        if(this.subscription > msg.subscribe) {
            if(this.msgNoSubscription != null) msg.responseMessage(this.msgNoSubscription);
            return;
        }

        String argstr = msg.message.trim().replaceAll("\\(arguments(\\[\\d+\\])*\\)","").replaceAll("[ ]{2,}"," ");

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

    public static Reward parseFromConfig(ConfigurationSection section) {
        Reward reward = new Reward();
        reward.reward = section.getName();
        reward.subscription = section.getInt("subscription", 0);
        reward.msgNoSubscription = section.getString("msg.nosubscription",null);
        if(reward.msgNoSubscription != null && reward.msgNoSubscription.trim().isEmpty()) reward.msgNoSubscription = null;
        reward.arguments = section.getInt("arguments", -1);
        if(reward.arguments == 0) {
            Bukkit.getLogger().warning("[TwitchBot] Reward " + reward.reward + " in #" + section.getParent().getParent().getName() + " may not work. Make sure you checked \"Require Viewer to Enter Text\" in custom reward settings.");
            reward.arguments = -1;
        }
        reward.msgWrongUsage = section.getString("msg.wrongusage", null);
        if(reward.msgWrongUsage != null && reward.msgWrongUsage.trim().isEmpty()) reward.msgWrongUsage = null;
        reward.msgResponse = section.getString("msg.response", null);
        if(reward.msgResponse != null && reward.msgResponse.trim().isEmpty()) reward.msgResponse = null;
        reward.msgBroadcast = section.getString("msg.broadcast", null);
        if(reward.msgBroadcast != null && reward.msgBroadcast.trim().isEmpty()) reward.msgBroadcast = null;
        reward.execute = section.getStringList("execute");

        return reward;
    }
}
