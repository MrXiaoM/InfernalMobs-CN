package io.hotmail.com.jacob_vejvoda.infernal_mobs.data;

import org.bukkit.enchantments.Enchantment;

public class Enchant {
    public Enchantment getEnchantment;
    public int getLevel;

    public Enchant(Enchantment enchantment, int level) {
        this.getEnchantment = enchantment;
        this.getLevel = level;
    }
}
