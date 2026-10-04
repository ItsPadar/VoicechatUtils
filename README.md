This (PaperMC-only) plugin adds an easy way to message other players in your current Simple Voice Chat group!
## Commands

| Command | Permission | Description                                                                                                                  |
| --- | --- |------------------------------------------------------------------------------------------------------------------------------|
| `/mg <message>` (`/messagegroup`) | `voicechatutils.chat.use` | Message everyone in your current voice chat group                                                                            |
| `/mgtoggle [on\|off]` | `voicechatutils.chat.use` | Send all your normal chat messages to your voice chat group                                                                  |
| `/vcmute <player> [duration] [reason]` | `voicechatutils.mute` | Mute a player in voicechat. Durations look like `30s`, `10m`, `2h`, `7d`, `1w` or `1h30m`. Leave it out for a permanent mute |
| `/vcunmute <player>` | `voicechatutils.mute` | Unmute a player in voicechat                                                                                                 |
| `/vcmutes` (`/vcmutelist`) | `voicechatutils.mute` | List muted players and how long they have left                                                                               |

Voice mutes are enforced on the server, so muted players can't be heard by anyone no matter what client they use. Mutes are saved in `plugins/voicechatutils/mutes.yml` and survive restarts. Players with `voicechatutils.mute` are told whenever someone is muted or unmuted.