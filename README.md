# ChatGroup

ChatGroup is a lightweight and flexible Hytale server plugin that allows you to fully customize the in-game chat based on player groups and permissions.

The plugin automatically reads the server `permissions.json` file to detect operator/admin status and player groups, then applies the correct chat format without requiring any additional permission plugins.

## Features

- Automatic OP and admin detection via `permissions.json`
- Support for custom groups (admin, mod, vip, etc.)
- Fully configurable chat formats
- Group priority system
- Compatible with hosted servers
- No hardcoded paths or OS-specific configuration
- Lightweight and efficient

## How It Works

ChatGroup listens to player chat events and dynamically formats messages depending on the player’s role or group.

All permissions and groups are read directly from the server `permissions.json` file, ensuring seamless integration with the default Hytale permission system.

## Installation

1. Place `ChatGroup-1.0.0.jar` into the server `mods/` folder  
2. Start or restart the server  
3. The configuration file will be generated automatically  

## Configuration

After the first launch, ChatGroup creates the following file:

mods/ChatGroup/config/chatgroup.json


### Example configuration

```json
{
  "priority": ["admin", "mod", "vip", "default"],
  "groups": {
    "admin": { "format": "[Admin-OP] {name}: {message}" },
    "mod": { "format": "[Mod] {name}: {message}" },
    "vip": { "format": "[VIP] {name}: {message}" },
    "default": { "format": "[Joueur] {name}: {message}" }
  }
}
```

## Placeholders

{name} – Player name

{message} – Chat message content

## Group Detection

ChatGroup automatically detects player roles using permissions.json, including:

Operator status (op: true)

Single or multiple groups (group or groups)

Wildcard permissions (*)

Dedicated operator lists (ops)

No additional configuration is required.

## Example Chat Output

[Admin-OP] Balrock: Hello everyone
[VIP] Steve: Hi!
[Joueur] Alex: Test message

## Compatibility

Designed for Hytale server plugins

Works on local servers and hosted environments

Does not bundle or modify the Hytale server API

## License

This project is open-source.
You are free to use, modify, and redistribute it.

## Author

Balrock

