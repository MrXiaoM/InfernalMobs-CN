package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import org.bukkit.entity.Entity;

import java.util.List;
import java.util.UUID;

public class InfernalMob {
    private final boolean infernal;
    Entity entity;
    UUID id;
    int lives;
    String effect;
    List<String> abilityList;

    InfernalMob(Entity type, UUID id, boolean infernal, List<String> abilityList, int lives, String effect) {
        this.entity = type;
        this.id = id;
        this.infernal = infernal;
        this.abilityList = abilityList;
        this.lives = lives;
        this.effect = effect;
    }

    public String toString() {
        return "Name: " + this.entity.getType().name() + " Infernal: " + this.infernal + "Abilities:" + this.abilityList;
    }

    void setLives(int i) {
        this.lives = i;
    }
}