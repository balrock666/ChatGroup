package me.balrock.chatgroup;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

public final class ChatGroupPlugin extends JavaPlugin {

    public ChatGroupPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        System.out.println("[ChatGroup] Plugin chargé");

        PermissionsJsonOpChecker opChecker = new PermissionsJsonOpChecker();
        ChatGroupConfig config = new ChatGroupConfig(getDataDirectory());

        ChatListener listener = new ChatListener(opChecker, config);

        // Enregistrement event (ta méthode qui marche déjà)
        try {
            Object registry = getEventRegistry();

            for (var m : registry.getClass().getMethods()) {
                if (!m.getName().equals("register")) continue;
                if (m.getParameterCount() != 3) continue;

                Class<?>[] params = m.getParameterTypes();
                if (!Class.class.isAssignableFrom(params[0])) continue;
                if (!java.util.function.Consumer.class.isAssignableFrom(params[2])) continue;

                java.util.function.Consumer<Object> consumer = ev -> {
                    try {
                        listener.onChat((com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent) ev);
                    } catch (Exception ignored) {}
                };

                m.invoke(
                        registry,
                        com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent.class,
                        null,
                        consumer
                );

                System.out.println("[ChatGroup] Listener chat enregistré");
                break;
            }
        } catch (Throwable t) {
            System.out.println("[ChatGroup] ERREUR enregistrement chat: " + t.getMessage());
        }
    }

    @Override
    protected void shutdown() {
        System.out.println("[ChatGroup] Plugin arrêté");
    }
}
