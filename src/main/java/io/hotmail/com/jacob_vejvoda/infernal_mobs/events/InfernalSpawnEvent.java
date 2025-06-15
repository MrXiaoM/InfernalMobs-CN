package io.hotmail.com.jacob_vejvoda.infernal_mobs.events;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.data.InfernalMob;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class InfernalSpawnEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final InfernalMob infernal;
    private boolean cancelled;

    public InfernalSpawnEvent(Entity entity, InfernalMob infernal) {
        this.entity = entity;
        this.infernal = infernal;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public InfernalMob getInfernal() {
        return this.infernal;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}