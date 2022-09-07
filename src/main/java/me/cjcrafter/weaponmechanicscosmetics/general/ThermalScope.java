package me.cjcrafter.weaponmechanicscosmetics.general;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class ThermalScope implements Serializer<ThermalScope> {


    private double range;
    private double fov;
    private boolean blacklist;
    private List<EntityType> entities;

    /**
     * Default constructor for serializer
     */
    public ThermalScope() {
    }

    public ThermalScope(double range, double fov, boolean blacklist, List<EntityType> entities) {
        this.range = range;
        this.fov = fov;
        this.blacklist = blacklist;
        this.entities = entities;
    }

    public boolean isInclude(Entity entity) {
        if (blacklist)
            return !entities.contains(entity.getType());
        else
            return entities.contains(entity.getType());
    }

    public void enable(Player player) {
        ThermalData data = new ThermalData(player, this);
    }

    @NotNull
    @Override
    public ThermalScope serialize(SerializeData data) throws SerializerException {
        return null;
    }

    public static class ThermalData {
        Player key;
        ThermalScope activeScope;
        List<Entity> currentEntities;

        public ThermalData(Player key, ThermalScope activeScope) {
            this.key = key;
            this.activeScope = activeScope;
            this.currentEntities = new LinkedList<>();
        }

        public void Update() {
            currentEntities.clear();
            for (LivingEntity entity : key.getWorld().getLivingEntities()) {

            }
        }
    }
}
