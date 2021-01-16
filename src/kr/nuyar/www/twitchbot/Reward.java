package kr.nuyar.www.twitchbot;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public class Reward {
    String reward;
    int subscription;
    String msgNoSubscription;
    int arguments;
    String msgWrongUsage;
    String msgResponse;
    String msgBroadcast;
    String execute;

    public void processMessage(Message msg) {
        if(!this.reward.equals(msg.tags.get("custom-reward-id")))
            return;

        Bukkit.getLogger().info(String.format("[TwitchBot] %s used reward %s in #%s : %s", msg.user, this.reward, msg.channel, msg.message));

        if(this.subscription > msg.subscribe) {
            if(this.msgNoSubscription != null) msg.responseMessage(this.msgNoSubscription);
            return;
        }

        String[] arguments = msg.message.split(" ");
        if(this.arguments != -1 && arguments.length != this.arguments) {
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
            if (arguments.length > 0) {
                response = response.replaceAll("\\(arguments\\)", msg.message.substring(msg.message.indexOf(' ')));
            }
        }
        if(broadcast != null) {
            broadcast = msg.replaceKeys(broadcast);
            for (int i = 1; i < arguments.length; i++) {
                broadcast = broadcast.replaceAll("\\(arguments\\["+(i-1)+"\\]\\)", arguments[i]);
            }
            if (arguments.length > 0) {
                broadcast = broadcast.replaceAll("\\(arguments\\)", msg.message.substring(msg.message.indexOf(' ')));
            }
        }
        if(execute != null) {
            execute = msg.replaceKeys(execute);
            for (int i = 1; i < arguments.length; i++) {
                execute = execute.replaceAll("\\(arguments\\["+(i-1)+"\\]\\)", arguments[i]);
            }
            if (arguments.length > 0) {
                execute = execute.replaceAll("\\(arguments\\)", msg.message.substring(msg.message.indexOf(' ')));
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
        reward.execute = section.getString("execute",null);
        if(reward.execute != null && reward.execute.trim().isEmpty()) reward.execute = null;

        return reward;
    }
}
