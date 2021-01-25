# TwitchBot

minecraft plugin can make twitch chat bot

![gamha](https://user-images.githubusercontent.com/7782247/104819932-87a32a00-5874-11eb-93c7-7d45f2911a6e.png)



# Edit config.yml

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
                arguments: 1 #required command argument. if -1, it don't check number of arguments.(default) if 0, user shouldn't type anything behind command. if 1 or bigger, it requires arguments to use command.
                msg.wrongusage: "Wrong usage!, '!whitelist (minecraft name)'" #response message when user uses command incorrectly.
                msg.response: "(user), You are registered in the server! Welcome (arguments[0])!" #response message when user uses command correctly.
                msg.broadcast: "(arguments[0]) registered in the server!" #broadcast message to everyone in the server when user uses command correctly.
                execute: #execute commands when user uses command correctly.
                    - "whitelist add (arguments[0])"
                    - "say you can add the commands you want."
        rewards:
            "your-custom-reward-id": #you can find the id in https://www.instafluff.tv/TwitchCustomRewardID/?channel=YOUR_CHANNEL
                subscription: 0 #same as command's one.
                msg.nosubscription: "Sorry, This is subscriber-only command." #same as command's one.
                arguments: 1 #same as command's one except 0. you shouldn't set to 0. and custom reward setting in twitch MUST CHECKED 'Require Viewer to Enter Text'
                msg.wrongusage: "Wrong usage!, '!whitelist (minecraft name)'" #same as command's one.
                msg.response: "(user), You are registered in the server! Welcome (arguments[0])!" #same as command's one.
                msg.broadcast: "(arguments[0]) registered in the server!" #same as command's one.
                execute: #same as command's one.
                    - "whitelist add (arguments[0])"
                    - "say you can add the commands you want."
```

###### `logirc:`
if true, plugin will log every irc communication in console.
when you have trouble with this plugin, it will help you to find problems

###### `channels:`
name of channel to make a bot.
you can make multiple bots.
``` yaml
channels:
    "channel1":
        #[channel1 attributes]
    "channel2":
        #[channel2 attributes]
```

###### `anonymous:`
whether the bot works in anonymous.
if true, bot don't login to chat, so bot doesn't need nickname and oauth token.
anonymous bot don't response(msg.nosubscription, msg.wrongusage, msg.response) to twitch chat.
if false(default), bot login to chat and send messages if bot has msg attributes.

###### `nickname:`
name of the bot.

###### `oauth:`
oauth token of the bot.
you can make your bot's token in [Twitch Chat OAuth Password Generator](https://twitchapps.com/tmi/)

###### `commands:`
commands that the bot listens to.
you can make multiple commands in one bot.
```` yaml
channels:
    "channel":
        #[channel1 attributes]
        commands:
            "!command1":
                #[!command1 attributes]
            "!command2":
                #[!command2 attributes]
````

###### `aliases:`
alternative commands a user may use instead.

###### `rewards:`
custom-reward-id of custom reward in channel.
you can find the id with [instafluff/TwitchCustomRewardID](https://github.com/instafluff/TwitchCustomRewardID)

###### `subscription:`
required number of months to use command/reward.
if 0(default), every user can use command/reward.
if 1, only subscriber can use command/reward.
if 2 or more, user have to be n-month subscriber.

###### `msg.nosubscription:`
response message in twitch chat when user doesn't have enough subscribe months.

###### `arguments:`
required number of arguments to use command/reward.
if -1(default), command/reward don't check number of arguments.
if 0 or more, user have to use command correctly.

###### `msg.wrongusage:`
response message in twitch chat when user used command wrongly.

###### `msg.response:`
response message in twitch chat when user used command correctly.

###### `msg.broadcast:`
broadcast message in minecraft server when user used command correctly.

###### `execute:`
execute commands in minecraft server when user used command correctly.
you can use multiple minecraft commands in one command.



# Message Tokens

the messages that bot responses and server executed are replaced token by plugin.
you can use tokens to make commands/rewards dynamic.

| Token | Description | Output Examples | msg.nosubscription</br>msg.wrongusage|msg.response</br>msg.broadcast</br>execute|
|:-:|-|-|:-:|:-:|
| (channel) | channel name | miner0308 | O | O |
| (user) | user name | potato3523 | O | O |
| (subscribe) | number of months user subscribed | 0, 1, 2, ... | O | O |
| (broadcaster) | whether user is broadcaster | true, false | O | O |
| (moderator) | whether user is moderator | true, false | O | O |
| (subscriber) | whether user is subscriber | true, false | O | O |
| (display-name) | display name in chat</br>it is normally used in non-english channel and user. | 감자__ | O | O |
| (room-id) | channel's unique id | 12345678 | O | O |
| (user-id) | user's unique id | 87654321 | O | O |
| (arguments[n]) | argument user typed | diamond, chat | X | O |
| (arguments) | full arguments user typed | my message! yea! | X | O |



# Plugin Commands

moderator of minecraft server can enable or disable bots with plugin commands.

| Command | Description | Required Permission |
|-|-|-|
| /twitchbot status | show brief status of all bots. | twitchbot.status.* or</br>twitchbot.status.\<channel\> |
| /twitchbot status \<channel\> | show detailed status of a bot. | twitchbot.status.* or</br>twitchbot.status.\<channel\> |
| /twitchbot start \<channel\> | turn on a bot. | twitchbot.manage.* or</br>twitchbot.manage.\<channel\> |
| /twitchbot stop \<channel\> | turn off a bot. | twitchbot.manage.* or</br>twitchbot.manage.\<channel\> |
| /twitchbot restart \<channel\> | restart a bot. | twitchbot.manage.* or</br>twitchbot.manage.\<channel\> |
| /twitchbot reload | reload config and restart all bots | twitchbot.reload |
| /twitchbot sendmessage \<channel\> \<message\> | send a message to channel | twitchbot.sendmessage.* or</br>twitchbot.sendmessage.\<channel\> |
