package io.hotmail.com.jacob_vejvoda.infernal_mobs.utils;

import com.google.common.collect.Lists;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PDC {
    @SuppressWarnings({"deprecation"})
    private static NamespacedKey key(String key) {
        return new NamespacedKey("infernalmobs", key);
    }

    public static final NamespacedKey SPAWNER_DELAY = key("spawner_delay");
    public static final NamespacedKey ABILITIES = key("abilities");

    @Nullable
    public static Integer getSpawnerDelay(CreatureSpawner spawner) {
        PersistentDataContainer pdc = spawner.getPersistentDataContainer();
        if (pdc.has(SPAWNER_DELAY, PersistentDataType.INTEGER)) {
            return pdc.get(SPAWNER_DELAY, PersistentDataType.INTEGER);
        }
        return null;
    }

    public static void setSpawnerDelay(CreatureSpawner spawner, @Nullable Integer delay) {
        PersistentDataContainer pdc = spawner.getPersistentDataContainer();
        if (delay != null) {
            pdc.set(SPAWNER_DELAY, PersistentDataType.INTEGER, delay);
        } else {
            pdc.remove(SPAWNER_DELAY);
        }
    }

    @Nullable
    public static String getAbilitiesString(Entity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (pdc.has(ABILITIES, PersistentDataType.STRING)) {
            return pdc.get(ABILITIES, PersistentDataType.STRING);
        }
        return null;
    }

    @Nullable
    public static List<String> getAbilities(Entity entity) {
        String str = getAbilitiesString(entity);
        if (str != null) {
            return Lists.newArrayList(str.split(","));
        }
        return null;
    }

    public static boolean hasAbilities(Entity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        return pdc.has(ABILITIES, PersistentDataType.STRING);
    }

    public static void setAbilities(Entity entity, @Nullable List<String> abilities) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (abilities != null) {
            pdc.set(ABILITIES, PersistentDataType.STRING, String.join(",", abilities));
        } else {
            pdc.remove(ABILITIES);
        }
    }
}
