This (PaperMC-only) plugin adds an easy way to message other players in your current Simple Voice Chat group, as well as a way to mute players in voicechat!


- `/mg <message>` command to forward a message to the other players in your group, helping people who wish to not use a microphone and still use Simple Voice Chat's group feature.
- `/mgtoggle [<on|off>]` command that toggles a mode where if you are in a group, all chat messages will be sent as `/mg` messages instead.
- Join leave messages when players join or leave a group (can be configured and disabled in the config.yml).
- Support for prefixes and suffixes set by plugins (such as Luckperms) in group messages. This requires the Vault plugin (can be found [here](https://www.spigotmc.org/resources/vault.34315/) or [here](https://dev.bukkit.org/projects/vault))  to be installed to work.
- `/vcmute <player> [duration] [reason]` and `/vcunmute <player>` commands to prevent players from speaking in voicechat for a certain length of time.

This plugin must be installed alongside Simple Voice Chat and does not automatically install it.

Feel free to offer ideas, bug reports and feedback in the Discord channel!

<details>
  <summary>Commands</summary>

| Command                                | Permission                | Description                                                                                                                   |
|----------------------------------------|---------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `/mg <message>` (`/messagegroup`)      | `voicechatutils.chat.use` | Send a message to everyone in your current voicechat group                                                                    |
| `/mgtoggle [<on\|off>]`                | `voicechatutils.chat.use` | `voicechatutils.chat.use`                                                                                                     | Send all your normal chat messages to your voicechat group                                                                    |
| `/vcmute <player> [duration] [reason]` | `voicechatutils.mute`     | Mute a player in voicechat. Durations look like `30s`, `10m`, `2h`, `7d`, `1w` or `1h30m`. Leave it out for a permanent mute. |
| `/vcunmute <player>`                   | `voicechatutils.mute`     | Unmute a player in voicechat.                                                                                                 |
| `/vcmutes` (`/vcmutelist`)             | `voicechatutils.mute`     | List muted players, how long they have left on their mute, who muted them, and their reason.                                  |

</details>
<details>
  <summary>Permissions</summary>

| Permission                | Default   | Description                                                                                                  |
|---------------------------|-----------|--------------------------------------------------------------------------------------------------------------|
| `voicechatutils.chat.use` | Everyone  | Use `/messagegroup` and `/mgtoggle` to message users in current voice chat group                             |
| `voicechatutils.chat.spy` | Operators | See all messages sent using `/messagegroup`                                                                  |
| `voicechatutils.mute`     | Operators | Mute and unmute players in voice chat with `/vcmute`, `/vcunmute` and `/vcmutes`, and see mute announcements |
</details>
<details>
  <summary>Default Config.yml</summary>

```
# Thanks for using Voicechat Utils by Its_Padar!
# https://modrinth.com/plugin/voicechat-utils

# Do not edit this unless you absolutely know what you are doing
CONFIG_VERSION: 4



# Whether /mg and /messagegroup should be enabled.
# true/false
enable_messagegroup: true

# Whether messages sent with /mg should be forwarded to everyone with the voicechatutils.chat.spy permission.
# true/false
enable_messagegroup_spying: false

# Customise the message sent to players from /mg.
# Use https://webui.advntr.dev/ to test what it looks like and help customise it with colours.
# <group> is replaced by the name of the group, <name> is replaced by the name of the player, and <message> is replaced by the message that is sent
messagegroup_text: <gray>[<group>]</gray> <prefix><<name>><suffix><reset> <message>


# How messages should be sent to players if they join/leave a group
# chat - Send message in chat
# actionbar - Send message to the player's actionbar in the middle of the screen
# disable - Disable join/leave messages
join_leave_group_messages_mode: chat

# Customise the join/leave messages sent if join_leave_group_messages_mode is not set to disable
# Use https://webui.advntr.dev/ to test what it looks like and help customise it with colours.
# <group> is replaced by the name of the group, and <name> is replaced by the name of the player
join_group_message_text: <gray>[<group>]</gray> <green>+</green> <prefix><name><suffix>
leave_group_message_text: <gray>[<group>]</gray> <red>-</red> <prefix><name><suffix>


# Enable <prefix> and <suffix> being replaced by the prefix/suffix of the player.
# This requires the Vault API and a permissions plugin such as Luckperms to be installed to work!
# Vault can be found at https://www.spigotmc.org/resources/vault.34315/ or https://dev.bukkit.org/projects/vault
enable_prefix_suffix_support: true

# Set the parse mode for prefixes and suffixes.
# Its recommended to set this away from automatic if possible
# minimessage - <red> <#FF0000> - The MiniMessage format, this is recommended
# legacy - &#FF0000 - The legacy formatting code format
# automatic - If there is a & in the prefix/suffix parse as legacy, otherwise assume minimessage
prefix_suffix_colour_mode: automatic


# Whether players muted in voicechat with /vcmute are also blocked from sending messages with /mg and /mgtoggle.
# true/false
voice_mute_blocks_messagegroup: true
```


</details>

<details>
  <summary>Planned Features</summary>

- config reload command

- /vcforcejoin command to let admins bypass passwords and join groups

- support for language configuration files

- toggle vc join leave messages?

- create/manage group commands

- broadcasting voice to all online players with simple voice chat

- chat filtering (regexes, replacement and blocking)?
</details>