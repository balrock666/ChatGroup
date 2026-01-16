package me.balrock.chatgroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ChatGroupConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configFile;

    private long lastModified = -1L;
    private JsonObject cache = null;

    public ChatGroupConfig(Path pluginDataDir) {
        Path configDir = pluginDataDir.resolve("config");
        this.configFile = configDir.resolve("chatgroup.json");
        ensureDefaultExists(configDir);
    }

    /** Reload auto si le fichier change */
    public JsonObject get() {
        try {
            long lm = Files.getLastModifiedTime(configFile).toMillis();
            if (cache != null && lm == lastModified) return cache;

            try (Reader r = Files.newBufferedReader(configFile)) {
                cache = GSON.fromJson(r, JsonObject.class);
                lastModified = lm;
                return cache;
            }
        } catch (Exception e) {
            return defaultConfigObject();
        }
    }

    /** Récupère un format de groupe: groups.<key>.format */
    public String getGroupFormat(String key) {
        JsonObject root = get();
        JsonObject groups = root.has("groups") && root.get("groups").isJsonObject()
                ? root.getAsJsonObject("groups")
                : new JsonObject();

        if (groups.has(key) && groups.get(key).isJsonObject()) {
            JsonObject g = groups.getAsJsonObject(key);
            if (g.has("format")) return g.get("format").getAsString();
        }

        // fallback: groups.default.format
        if (groups.has("default") && groups.get("default").isJsonObject()) {
            JsonObject g = groups.getAsJsonObject("default");
            if (g.has("format")) return g.get("format").getAsString();
        }

        // fallback ultime
        return "[Joueur] {name}: {message}";
    }

    /** Ordre de priorité des groupes (optionnel) */
    public String[] getPriority() {
        JsonObject root = get();
        if (root.has("priority") && root.get("priority").isJsonArray()) {
            var arr = root.getAsJsonArray("priority");
            String[] out = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++) out[i] = arr.get(i).getAsString();
            return out;
        }
        return new String[]{"admin", "op", "mod", "vip", "default"};
    }

    private void ensureDefaultExists(Path configDir) {
        try {
            Files.createDirectories(configDir);
            if (Files.exists(configFile)) return;

            JsonObject def = defaultConfigObject();
            try (Writer w = Files.newBufferedWriter(configFile)) {
                GSON.toJson(def, w);
            }
        } catch (Exception ignored) {
        }
    }

    private JsonObject defaultConfigObject() {
        // JSON par défaut
        JsonObject root = new JsonObject();

        // priorité
        var prio = new com.google.gson.JsonArray();
        prio.add("admin");
        prio.add("op");
        prio.add("mod");
        prio.add("vip");
        prio.add("default");
        root.add("priority", prio);

        JsonObject groups = new JsonObject();

        JsonObject admin = new JsonObject();
        admin.addProperty("format", "[Admin-OP] {name}: {message}");
        groups.add("admin", admin);

        JsonObject def = new JsonObject();
        def.addProperty("format", "[Joueur] {name}: {message}");
        groups.add("default", def);

        // exemples futurs :
        JsonObject vip = new JsonObject();
        vip.addProperty("format", "[VIP] {name}: {message}");
        groups.add("vip", vip);

        root.add("groups", groups);
        return root;
    }

    /** Remplace les placeholders dans un format */
    public static String apply(String format, String name, String message) {
        return format
                .replace("{name}", name)
                .replace("{message}", message);
    }
}
