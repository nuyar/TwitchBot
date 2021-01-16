# TwitchBot

minecraft plugin can make twitch chat bot

![gamha](https://user-images.githubusercontent.com/7782247/104819932-87a32a00-5874-11eb-93c7-7d45f2911a6e.png)


# config.yml
``` yaml
logirc: false #if true, bot will log every irc communication in console.

channels:
    "YOUR_CHANNEL":
        anonymous: false #if true, bot don't login and it cannot response to command/reward.
        nickname: "BOTS_NAME"
        oauth: "oauth:BOTS_OAUTH_TOKEN" #make bot's token in https://twitchapps.com/tmi/
        commands:
            "!whitelist":
                aliases: ["!minecraft", "!register"]
                subscription: 1 #required number of months the user has been a subscriber
                msg.nosubscription: "Sorry, This is subscriber-only command." #response message when user is not subscriber or not fulfill months
                arguments: 1 #required command argument. if -1, it don't check number of arguments. if 0, user shouldn't type anything behind command. if 1 or bigger, it requires arguments to use command.
                msg.wrongusage: "Wrong usage!, '!whitelist (minecraft name)'" #response message when user uses command incorrectly.
                msg.response: "(user), You are registered in the server! Welcome (arguments[0])!" #response message when user uses command correctly.
                msg.broadcast: "(arguments[0]) registered in the server!" #broadcast message to everyone in the server when user uses command correctly.
                execute: "whitelist add (arguments[0])" #execute command when user uses command correctly.
        rewards:
            "your-custom-reward-id": #you can find the id in https://www.instafluff.tv/TwitchCustomRewardID/?channel=YOUR_CHANNEL
                subscription: 0 #same as command's one.
                msg.nosubscription: "Sorry, This is subscriber-only command." #same as command's one.
                arguments: 1 #same as command's one except 0. you shouldn't set to 0. and custom reward setting in twitch MUST CHECKED 'Require Viewer to Enter Text' 
                msg.wrongusage: "Wrong usage!, '!whitelist (minecraft name)'" #same as command's one.
                msg.response: "(user), You are registered in the server! Welcome (arguments[0])!" #same as command's one.
                msg.broadcast: "(arguments[0]) registered in the server!" #same as command's one.
                execute: "whitelist add (arguments[0])" #same as command's one.
```

#### `logirc:`
if true, plugin will log every irc communication in console.
when you have trouble with this plugin, it will help you to find problems

#### `channels:`
the name of channel you want to make bot.
you can make multiple bots.
``` yaml
channels:
    "channel1":
        #[channel1 attributes]
    "channel2":
        #[channel2 attributes]
```

