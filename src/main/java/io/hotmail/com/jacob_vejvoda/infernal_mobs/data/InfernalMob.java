package io.hotmail.com.jacob_vejvoda.infernal_mobs.data;

import org.bukkit.entity.Entity;

import java.util.List;
import java.util.UUID;

public class InfernalMob {
    private final boolean infernal;
    private Entity entity;
    private UUID id;
    private int lives;
    private String effect;
    private List<String> abilityList;

    public InfernalMob(Entity type, UUID id, boolean infernal, List<String> abilityList, int lives, String effect) {
        this.entity = type;
        this.id = id;
        this.infernal = infernal;
        this.abilityList = abilityList;
        this.lives = lives;
        this.effect = effect;
    }

    public Entity getEntity() {
        return entity;
    }

    public UUID getId() {
        return id;
    }

    public int getLives() {
        return lives;
    }

    public String getEffect() {
        return effect;
    }

    public List<String> getAbilityList() {
        return abilityList;
    }

    public String toString() {
        return "Name: " + this.entity.getType().name() + " Infernal: " + this.infernal + "Abilities:" + this.abilityList;
    }

    public void setLives(int i) {
        this.lives = i;
    }
}