package kr.nuyar.www.twitchbot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TwitchBot extends JavaPlugin {
    public static TwitchBot plugin;

    boolean logIRC;
    List<Channel> channels;

    @Override
    public void onEnable() {
        plugin = this;

        this.getCommand("twitchbot").setExecutor(this);
        this.getCommand("twitchbot").setTabCompleter(this);


        readConfig();
        startBots();
    }

    void readConfig() {
        this.logIRC = this.getConfig().getBoolean("logirc", false);
        this.channels = loadChannels();
    }

    void startBots() {
        for (Channel channel : this.channels) {
            channel.startIRC();
        }
    }

    void stopBots() {
        for (Channel channel : this.channels) {
            channel.stopIRC();
        }
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override
    public void onDisable() {
        stopBots();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("status")) {
            List<Channel> chs = this.channels.stream().filter(ch -> sender.hasPermission("twitchbot.status.*") || sender.hasPermission("twitchbot.status." + ch.channel)).collect(Collectors.toList());

            if (channels.size() == 0) {
                sender.sendMessage("There is no bot to show status.");
                return true;
            }

            if (chs.size() == 0) {
                sender.sendMessage(ChatColor.RED + "You do not have permission!");
                return true;
            }

            chs.forEach(channel -> sender.sendMessage(String.format("#%s(%s) nick=%s    %dcommand(s)  %dreward(s)", channel.channel, channel.status, channel.nickname, channel.commands.size(), channel.rewards.size())));
            return true;
        }

        if (args.length == 2 && args[0].equals("status")) {
            Channel channel = null;
            for (Channel ch : this.channels) {
                if (ch.channel.equals(args[1]))
                    channel = ch;
            }

            if (channel == null) {
                sender.sendMessage(ChatColor.RED + "there is no matching bot.");
                return true;
            }

            if (!sender.hasPermission("twitchbot.status.*") && !sender.hasPermission("twitchbot.status." + channel.channel)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission!");
                return true;
            }

            sender.sendMessage(String.format("#%s(%s)  nick=%s", channel.channel, channel.status, channel.nickname));

            if(channel.irc != null) {
                String lastComm;
                long seconds = (System.currentTimeMillis() - channel.irc.lastCommunication) / 1000;
                if (seconds / 60 >= 1) {
                    lastComm = String.format("%d minute%s ago", seconds / 60, seconds / 60 == 1 ? "" : "s");
                } else if (seconds > 5) {
                    lastComm = String.format("%d seconds ago", seconds);
                } else {
                    lastComm = "just now";
                }
                sender.sendMessage("  last communication : " + lastComm);
            }

            sender.sendMessage(String.format("  %d command(s):", channel.commands.size()));
            channel.commands.forEach(c -> sender.sendMessage("    " + c.command));

            sender.sendMessage(String.format("  %d reward(s):", channel.rewards.size()));
            channel.rewards.forEach(r -> sender.sendMessage("    " + r.reward));

            return true;
        }

        if (args.length == 2 && args[0].equals("start")) {
            Channel channel = null;
            for (Channel ch : this.channels) {
                if (ch.channel.equals(args[1]))
                    channel = ch;
            }

            if (channel == null) {
                sender.sendMessage(ChatColor.RED + "there is no matching bot.");
                return true;
            }

            if (!sender.hasPermission("twitchbot.manage.*") && !sender.hasPermission("twitchbot.manage." + channel.channel)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission!");
                return true;
            }

            if(channel.irc != null) {
                sender.sendMessage(ChatColor.RED + "Bot is already running.");
                return true;
            }

            sender.sendMessage("Bot is starting.");
            channel.startIRC();
            return true;
        }

        if (args.length == 2 && args[0].equals("stop")) {
            Channel channel = null;
            for (Channel ch : this.channels) {
                if (ch.channel.equals(args[1]))
                    channel = ch;
            }

            if (channel == null) {
                sender.sendMessage(ChatColor.RED + "there is no matching bot");
                return true;
            }

            if (!sender.hasPermission("twitchbot.manage.*") && !sender.hasPermission("twitchbot.manage." + channel.channel)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission!");
                return true;
            }

            if(channel.irc == null) {
                sender.sendMessage(ChatColor.RED + "Bot is already stopped.");
                return true;
            }

            sender.sendMessage("Bot is closing.");
            channel.stopIRC();
            return true;
        }

        if (args.length == 2 && args[0].equals("restart")) {
            Channel channel = null;
            for (Channel ch : this.channels) {
                if (ch.channel.equals(args[1]))
                    channel = ch;
            }

            if (channel == null) {
                sender.sendMessage(ChatColor.RED + "there is no matching bot");
                return true;
            }

            if (!sender.hasPermission("twitchbot.manage.*") && !sender.hasPermission("twitchbot.manage." + channel.channel)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission!");
                return true;
            }

            sender.sendMessage("Bot is restarting.");
            channel.stopIRC();
            channel.startIRC();
            return true;
        }

        if (args.length == 1 && args[0].equals("reload")) {
            if (!sender.hasPermission("twitchbot.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission!");
                return true;
            }

            this.stopBots();
            this.reloadConfig();
            this.readConfig();
            this.startBots();
            return true;
        }

        sender.sendMessage("/%s status : show all bots' short status");
        sender.sendMessage("/%s status (channel) : show bot's status");
        sender.sendMessage("/%s start (channel) : start bot");
        sender.sendMessage("/%s stop (channel) : stop bot");
        sender.sendMessage("/%s restart (channel) : restart bot");
        sender.sendMessage("/%s reload : reload config and restart all bots");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Stream.of("status", "start", "stop", "restart", "reload").filter(cmd -> cmd.startsWith(args[0])).collect(Collectors.toList());
        }
        if (args.length == 2 && Arrays.asList("status", "start", "stop", "restart").contains(args[0])) {
            return channels.stream().map(channel -> channel.channel).filter(channel -> channel.startsWith(args[1])).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<Channel> loadChannels() {
        List<Channel> channels = new ArrayList<Channel>();
        for (String channelName : this.getConfig().getConfigurationSection("channels").getKeys(false)) {
            channels.add(Channel.parseFromConfig(this.getConfig().getConfigurationSection("channels").getConfigurationSection(channelName)));
        }
        return channels;
    }
}
