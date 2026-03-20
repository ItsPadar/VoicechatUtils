# Voicechat Utils

A PaperMC-only plugin that extends Simple Voice Chat by adding text messaging to voice chat groups.

---

## Features

- Group messaging (`/mg`)
- Spy system (optional)

---

## Commands

### `/mg <message>`

---

## Permissions

| Permission | Description |
|----------|------------|
| `voicechatutil.chat.use` | Use `/mg` |
| `voicechatutil.chat.spy` | See `/mg` messages |

---

## Configuration

```yaml
# Thanks for using Voicechat Utils by Its_Padar!
# https://modrinth.com/plugin/voicechat-util

enable_messagegroup: true
enable_messagegroup_spying: false

# <group> = group name
# <name> = player name
# <message> = message content
messagegroup_text: <gray>[<group>]</gray> <<name>> <message>
```
Formatting tool: https://webui.advntr.dev/

Planned Features

Group management commands

Global broadcasting

Moderation tools

Chat filtering
