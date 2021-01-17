package kr.nuyar.www.twitchbot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Channel {
    static final String RUNNING = ChatColor.GREEN + "running" + ChatColor.RESET;
    static final String STOPPED = ChatColor.RED + "stopped" + ChatColor.RESET;
    static final String RECONNECTING = ChatColor.DARK_RED + "reconnecting" + ChatColor.RESET;

    String channel;
    boolean anonymous;
    String nickname;
    String oauth;
    List<Command> commands;
    List<Reward> rewards;

    IRC irc;
    long reconnectDelay = 20;
    String status = "stopped";

    public void processMessage(Message msg) {

        if (!msg.user.equals(this.nickname))
            for (Command command : commands) {
                command.processMessage(msg);
            }
        for (Reward reward : rewards) {
            reward.processMessage(msg);
        }
    }

    void startIRC() {
        if (this.anonymous) {
            Random random = new Random(System.currentTimeMillis());
            this.nickname = "justinfan" + (random.nextInt(9000) + 1000);
            this.oauth = "miner0308";
        }

        if (this.irc != null) this.stopIRC();
        reconnectDelay = 20;
        irc = new IRC(this);
        irc.startIRC();
        this.status = RUNNING;
    }

    void stopIRC() {
        if (this.irc == null) return;
        this.irc.stopIRC();
        this.irc = null;
        this.status = STOPPED;
    }

    void reconnectIRC() {
        this.status = RECONNECTING;
        long delay = this.reconnectDelay;
        Bukkit.getScheduler().scheduleSyncDelayedTask(TwitchBot.plugin, () -> {
            Bukkit.getLogger().info(String.format("[TwitchBot] Twitch bot #%s is trying to reconnect the twitch irc server. (delay=%sticks)", this.channel, delay));
            this.stopIRC();
            this.startIRC();
        }, delay);

        this.reconnectDelay *= 2;
        if (this.reconnectDelay > 1200) this.reconnectDelay = 1200;
    }

    public static Channel parseFromConfig(ConfigurationSection section) {
        Channel channel = new Channel();
        channel.channel = section.getName();
        channel.anonymous = section.getBoolean("anonymous", false);
        channel.nickname = section.getString("nickname", null);
        channel.oauth = section.getString("oauth", null);
        if (channel.anonymous || channel.nickname == null || channel.nickname.isEmpty() ||
                channel.oauth == null || channel.oauth.isEmpty()) {
            channel.anonymous = true;
        }
        channel.commands = new ArrayList<>();
        if (section.getConfigurationSection("commands") != null)
            for (String commandName : section.getConfigurationSection("commands").getKeys(false)) {
                channel.commands.add(Command.parseFromConfig(section.getConfigurationSection("commands").getConfigurationSection(commandName)));
            }
        channel.rewards = new ArrayList<>();
        if (section.getConfigurationSection("rewards") != null)
            for (String rewardName : section.getConfigurationSection("rewards").getKeys(false)) {
                channel.rewards.add(Reward.parseFromConfig(section.getConfigurationSection("rewards").getConfigurationSection(rewardName)));
            }

        Bukkit.getLogger().info(String.format("[TwitchBot] Twitch bot #%s is successfully loaded with %d command(s) and %d reward(s)", channel.channel, channel.commands.size(), channel.rewards.size()));
        return channel;
    }
}
