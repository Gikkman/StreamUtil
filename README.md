# StreamUtil for Twitch.tv (aka GikkBot)
This is a submission for the Summer of Code, initiated by Speedment.

## Intentions
The program is intended to be a utility for streaming, for those who stream to Twitch.tv 
It offers some basic funtionalities such as setting game and stream title, as well as monitoring how many viewers the channel currently has, who the latest follower and subscriber is and which users are online.
The program also logs how long each user has been online and how many chat messages each user has written. These data are stored in a local database.

###### User
Each user has the following fields that are stored in the database
- User name
- Status [Admin, Moderator, Regular]
- Subscriber status
- Follower status
- Trusted status
- Time online
- Lines written

###### Chat commands
The program connects a bot to the Twitch IRC channel. This bot is mostly used for monitoring who is online and similar IRC status, but it also supports a few commands. There are two type of commands: 
- Prefix commands : These commands usually have a command pattern, and sometimes an argument. The command pattern must be the first word written in a chat line, and the argument must come directly after the first space.
- Content commands: These commands check the entire chat line for a specific pattern. If the pattner is found, the Content command fires
```
PREFIX COMMANDS
- !stats 
  - !stats  : Reports the sending user's stats
  - !stats [user] : Reports user [user]'s status
  - !stat : Another way of invoking !stats
  - !stat [user]  : Another way of invoking !stats [user]
- !time
  - !time : Reports the sending user's time online
  - !time [user]  : Reports the user [user]'s time online
  - !time [X, range 1 - 10]  : Reports the X users with the longest online time
- !lines
  - !lines  : Reports the sending user's chat lines written
  - !lines [user] : Reports the user [user]'s chat lines written
  - !lines [X, range 1 - 10]  : Reports the X users with the most chat lines written
  - !lurker : Finds a random user with online time >= 10 and lines written <= 10 (the user does not have to be online)
  
CONTENT COMMANDS
- tick  : If the pattner 'tick' is seen in a chat message, the bot responds with 'tock'
```
## Instructions (Setup):
1. Download the project and import it as a Maven project.
2. Run the project and follow the initialization instructions.
3. After the initialization, re-launch the program.

##### OPTIONAL
It is possible to import and run it via JitPack, it seems. However, sources and javadoc does not get attached unfortiunetly (I don't know why though), so I would recommend against it.
  
##Instructions (Initialization)
The first time the program is launched the initializer will fire up. The initialization should be quite self-explanatory. But just in case, here are the in-depth instructions for how to initialize the program:

1. Step 1
  1. Make sure you have a Twitch.tv account from which you stream. If not, make one. This is you *streaming account*
  1. Select a location for storing local data. The data that'll be stored is a properties file, containing Twitch connections details.
2. Step 2
  1. Download and instal MySQL community server. Install as much as possible (I am unsure what is vital and what is not).
  2. Execite the following SQL command:
  ```
  CREATE DATABASE if not exists gikk_stream_util;
  USE gikk_stream_util;

  drop table if exists USERS;

  create table if not exists USERS (
    ID integer(32) not null auto_increment,
    USERNAME varchar(32) not null,
    STATUS varchar(32) not null,
    TIME_ONLINE int(32)not null,
    LINES_WRITTEN int(32)not null,
    IS_TRUSTED varchar(5) not null,
    IS_FOLLOWER varchar(5) not null,
    IS_SUBSCRIBER varchar(5) not null,
    primary key (ID),
    unique key USERNAME (USERNAME)
  ) DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;

  CREATE USER if not exists 'gikkbot_user'@'localhost';
  GRANT ALL on gikk_stream_util.* TO 'gikkbot_user'@'localhost';
  FLUSH PRIVILEGES;
  ```
3. Step 3
  1. Create a new account for your bot. This is your *bot account*
  2. Log into your new bot account
  3. Create a new Twitch developer application: [Link](https://www.twitch.tv/kraken/oauth2/clients/new)
    * Set Name anything unique (I recommend your bot account's name)
    * Set Redirect URI to http://127.0.0.1:23522
    * After clicking register, a new field apperas. This is your bot's *Client ID*
  4. Create a IRC password for you bot: [Link](https://twitchapps.com/tmi/). This password is you *oAuth token*
4. Step 4
  1. Log back into your streaming accountä
  2. Click the 'Authorize button'
    * Make sure that the applcation you are accepting is the same as you created under 3.3
    * Make sure that the account that is logged in is your stream account.
    * Click Allow
  3. If the Authorization process failed, you will get an error message telling you what went wrong. Try to fix it and go back to 4.2
  4. Click the Finnish button. This will check that all the components are correct. If not, it will print an error. Try to fix that error.
5. Step 5
  1. This window will just tell you that everything is set up. Press the button to finnalize the process. The program will terminate.
  2. Relaunch the program, this will take you to the main dashboard.
  3. Rember to mod your bot! Type **.mod [bot account]** into your chat. Otherwise, the bot's ability to interact will be limited

From the main dashboard, it is possible to change your channels Title- and Game fields. You can also se whom are online in your IRC channel (and some data related to them). There is also a debug tab which allows you to add users directly to the database, and a button for clearing preferences (will launch the init chain again on startup). If you enter users via the debug tab, be careful and follow the stated rules. Adding custom users that aren't correct my cause errors in the program.

  
## Known issued
Twitch's API and IRC is difficult to work with. There is a lot of undocumented details and quirks that I have to work around, but there are still a few details I know I don't cover 100%

1. Follower alerts are slow and sometimes arrive in incorrect order
2. Sometimes, old follower alerts are triggered again. I am not sure why, it seems that the API gets confused when people un-follows channels.
3. JOIN/PART from IRC are delayed a lot, sometimes PART messages are dropped
4. .mod messages are very slow. It might take up to 5 minutes for them to take effect (even if they show up much quicker in twitch chat)
5. .mod status is not revoked if a user is unmodded whilst the bot is disconnected. To revoke mod status, re-mod and then unmod the user while the bot is connected.
6. The IRC socket resets sometimes. Might be my connection where I debug, but I know it happens sometimes.
7. Newly created Twitch account have some really strange quirks to them. Amongst other, they are not indexed within the first 10-15 minutes, which might cause them to not show up when searching for. This might cause some strange behaviours, which I cannot predict nor help.

## Contant
If there are any issued, or any questions, you can reach me via twitter @Gikkman

