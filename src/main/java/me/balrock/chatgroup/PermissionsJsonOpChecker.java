package me.balrock.chatgroup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class PermissionsJsonOpChecker {

    private final Gson gson = new Gson();
    private final Path permissionsJsonPath = Path.of("permissions.json"); // serveur

    private long lastModified = -1L;
    private JsonObject cache = null;

    public boolean isOp(UUID uuid) {
        JsonObject root = load();
        if (root == null) return false;

        String id = uuid.toString();

        // 1) "ops": ["uuid"]
        if (hasUuidInArray(root, "ops", id)) return true;

        JsonObject user = getUserObject(root, id);
        if (user == null) return false;

        // 2) "op": true
        Boolean op = getBoolean(user, "op");
        if (op != null) return op;

        // 3) groupe op/admin
        if (hasGroup(user, "op") || hasGroup(user, "admin") || hasGroup(user, "operator")) return true;

        // 4) permissions ["*"]
        return hasStarPermission(user);
    }

    /** Retourne les groupes du joueur depuis permissions.json (si présents) */
    public Set<String> getGroups(UUID uuid) {
        Set<String> out = new HashSet<>();
        JsonObject root = load();
        if (root == null) return out;

        String id = uuid.toString();
        JsonObject user = getUserObject(root, id);
        if (user == null) return out;

        // "group": "vip"
        JsonElement group = user.get("group");
        if (group != null && group.isJsonPrimitive()) {
            out.add(group.getAsString().toLowerCase(Locale.ROOT));
        }

        // "groups": ["vip","mod"]
        JsonElement groups = user.get("groups");
        if (groups != null && groups.isJsonArray()) {
            for (JsonElement g : groups.getAsJsonArray()) {
                if (g.isJsonPrimitive()) out.add(g.getAsString().toLowerCase(Locale.ROOT));
            }
        }

        return out;
    }

    private JsonObject load() {
        try {
            if (!Files.exists(permissionsJsonPath)) return null;

            long lm = Files.getLastModifiedTime(permissionsJsonPath).toMillis();
            if (lm == lastModified && cache != null) return cache;

            try (Reader r = Files.newBufferedReader(permissionsJsonPath)) {
                JsonElement el = gson.fromJson(r, JsonElement.class);
                if (el == null || !el.isJsonObject()) return null;
                cache = el.getAsJsonObject();
                lastModified = lm;
                return cache;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static JsonObject getUserObject(JsonObject root, String uuidStr) {
        JsonObject users = getObject(root, "users");
        if (users == null) users = getObject(root, "players");
        if (users == null) return null;
        return getObject(users, uuidStr);
    }

    private static JsonObject getObject(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && el.isJsonObject()) ? el.getAsJsonObject() : null;
    }

    private static Boolean getBoolean(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean())
                ? el.getAsBoolean()
                : null;
    }

    private static boolean hasGroup(JsonObject user, String groupName) {
        groupName = groupName.toLowerCase(Locale.ROOT);

        JsonElement group = user.get("group");
        if (group != null && group.isJsonPrimitive()) {
            if (group.getAsString().toLowerCase(Locale.ROOT).equals(groupName)) return true;
        }

        JsonElement groups = user.get("groups");
        if (groups != null && groups.isJsonArray()) {
            for (JsonElement g : groups.getAsJsonArray()) {
                if (g.isJsonPrimitive() && g.getAsString().toLowerCase(Locale.ROOT).equals(groupName)) return true;
            }
        }

        return false;
    }

    private static boolean hasStarPermission(JsonObject user) {
        JsonElement perms = user.get("permissions");
        if (perms != null && perms.isJsonArray()) {
            for (JsonElement p : perms.getAsJsonArray()) {
                if (p.isJsonPrimitive() && p.getAsString().trim().equals("*")) return true;
            }
        }
        return false;
    }

    private static boolean hasUuidInArray(JsonObject root, String key, String uuidStr) {
        JsonElement el = root.get(key);
        if (el == null || !el.isJsonArray()) return false;
        for (JsonElement e : el.getAsJsonArray()) {
            if (e.isJsonPrimitive() && e.getAsString().equalsIgnoreCase(uuidStr)) return true;
        }
        return false;
    }
}
