package me.balrock.chatgroup;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.Set;
import java.util.UUID;

public final class ChatListener {

    private final PermissionsJsonOpChecker opChecker;
    private final ChatGroupConfig config;

    public ChatListener(PermissionsJsonOpChecker opChecker, ChatGroupConfig config) {
        this.opChecker = opChecker;
        this.config = config;
    }

    public void onChat(PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        UUID uuid = getUuid(sender);

        boolean op = (uuid != null) && opChecker.isOp(uuid);
        Set<String> groups = (uuid != null) ? opChecker.getGroups(uuid) : Set.of();

        // Choix du "groupe principal"
        String groupKey = pickGroupKey(op, groups);

        String format = config.getGroupFormat(groupKey);

        event.setFormatter((PlayerRef playerRef, String msg) -> {
            String name = safeName(playerRef);
            String full = ChatGroupConfig.apply(format, name, msg);
            return makeMessage(full);
        });
    }

    private String pickGroupKey(boolean op, Set<String> groups) {
        // Si OP -> admin (clé config)
        if (op) return "admin";

        // Sinon on suit l'ordre de priorité du fichier config
        for (String p : config.getPriority()) {
            if ("default".equalsIgnoreCase(p)) continue;
            if (groups.contains(p.toLowerCase())) return p.toLowerCase();
        }
        return "default";
    }

    private UUID getUuid(PlayerRef ref) {
        try {
            Object r = ref.getClass().getMethod("getUuid").invoke(ref);
            if (r instanceof UUID u) return u;
        } catch (Exception ignored) {}
        try {
            Object r = ref.getClass().getMethod("getUniqueId").invoke(ref);
            if (r instanceof UUID u) return u;
        } catch (Exception ignored) {}
        return null;
    }

    private String safeName(PlayerRef ref) {
        try { return (String) ref.getClass().getMethod("getName").invoke(ref); } catch (Exception ignored) {}
        try { return (String) ref.getClass().getMethod("getUsername").invoke(ref); } catch (Exception ignored) {}
        return "Player";
    }

    private Message makeMessage(String text) {
        try {
            for (var m : Message.class.getMethods()) {
                if (!m.getName().equals("translation")) continue;

                if (m.getParameterCount() == 2
                        && m.getParameterTypes()[0] == String.class
                        && m.getParameterTypes()[1].isArray()) {
                    return (Message) m.invoke(null, text, new Object[0]);
                }

                if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
                    return (Message) m.invoke(null, text);
                }
            }
        } catch (Throwable ignored) {}

        return null;
    }
}
