package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {
    private final infernal_mobs plugin;
    public Commands(infernal_mobs plugin) {
        this.plugin = plugin;
        PluginCommand command = plugin.getCommand("infernalmobs");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }
    
    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if ((cmd.getName().equalsIgnoreCase("infernalmobs")) || (cmd.getName().equalsIgnoreCase("im"))) {
            try {
                Player player;
                if (!(sender instanceof Player)) {
                    if (args.length > 0 && (!args[0].equalsIgnoreCase("cspawn"))
                            && (!args[0].equalsIgnoreCase("pspawn"))
                            && (!args[0].equalsIgnoreCase("giveloot"))
                            && (!args[0].equalsIgnoreCase("reload"))
                            && (!args[0].equalsIgnoreCase("killall"))
                    ) {
                        sender.sendMessage("This command can only be run by a player!");
                        return true;
                    }
                    player = null;
                } else {
                    player = (Player) sender;
                }
                if (sender.hasPermission("infernal_mobs.commands")) {
                    if (args.length == 0) {
                        throwError(sender);
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("slotTest") && player != null) {
                        for (int i : getConfig().getIntegerList("enabledCharmSlots")) {
                            player.getInventory().setItem(i, new ItemStack(Material.RED_STAINED_GLASS_PANE));
                        }
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("fixloot"))) {
                        ArrayList<String> list = new ArrayList<>();
                        ConfigurationSection items = getConfig().getConfigurationSection("items");
                        if (items != null) list.addAll(items.getKeys(false));
                        ConfigurationSection section = plugin.lootFile.getConfigurationSection("loot");
                        if (section != null) for (String i : section.getKeys(false)) {
                            String oid = plugin.lootFile.getInt("loot." + i + ".item") + "";
                            System.out.println(i);
                            System.out.println("loot." + i + ".item");
                            System.out.println(oid + ": " + list.contains(oid));
                            if (list.contains(oid)) {
                                plugin.lootFile.set("loot." + i + ".item", getConfig().getString("items." + oid));
                            } else
                                System.out.println("ERROR: " + oid);
                        }
                        try {
                            plugin.lootFile.save(plugin.lootYML);
                        } catch (IOException ignored) {
                        }
                        sender.sendMessage("§eLoot Fixed!");
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("reload"))) {
                        plugin.reloadConfig();
                        plugin.reloadLoot();
                        sender.sendMessage("§eConfig reloaded!");
                    } else if (args[0].equals("mobList")) {
                        sender.sendMessage("§6Mob List:");
                        for (EntityType et : EntityType.values())
                            if (et != null && et.getName() != null)
                                sender.sendMessage("§e" + et.getName());
                        return true;
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("error"))) {
                        plugin.errorList.add(player);
                        sender.sendMessage("§eClick on a mob to send an error report about it.");
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("info"))) {
                        sender.sendMessage("§eMounts: " + plugin.mountList.size());
                        sender.sendMessage("§eInfernals: " + plugin.infernalList.size());
                    } else if (args.length == 1 && args[0].equalsIgnoreCase("worldInfo") && player != null) {
                        List<String> enWorldList = getConfig().getStringList("mobworlds");
                        World world = player.getWorld();
                        String enabled = "is not";
                        if (enWorldList.contains(world.getName()) || enWorldList.contains("<all>")) {
                            enabled = "is";
                        }
                        sender.sendMessage("The world you are currently in, " + world + " " + enabled + " enabled.");
                        sender.sendMessage("All the worlds that are enabled are: " + enWorldList);
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("help"))) {
                        throwError(sender);
                    } else if (args.length == 1 && args[0].equalsIgnoreCase("getloot") && player != null) {
                        int min = getConfig().getInt("minpowers");
                        int max = getConfig().getInt("maxpowers");
                        int powers = plugin.rand(min, max);
                        ItemStack gottenLoot = plugin.getRandomLoot(player, plugin.getRandomMob(), powers);
                        if (gottenLoot != null) {
                            player.getInventory().addItem(gottenLoot);
                        }
                        sender.sendMessage("§eGave you some random loot!");
                    } else if (args.length == 2 && args[0].equalsIgnoreCase("getloot") && player != null) {
                        try {
                            int index = Integer.parseInt(args[1]);
                            ItemStack i = plugin.getLoot(player, index);
                            if (i != null) {
                                player.getInventory().addItem(i);
                                sender.sendMessage("§eGave you the loot at index §9" + index);
                                return true;
                            }
                        } catch (Exception ignored) {
                        }
                        sender.sendMessage("§cUnable to get that loot!");
                    } else if ((args.length == 3) && (args[0].equalsIgnoreCase("giveloot"))) {
                        try {
                            Player p = Bukkit.getServer().getPlayer(args[1]);
                            if (p != null) {
                                int index = Integer.parseInt(args[2]);
                                ItemStack i = plugin.getLoot(p, index);
                                if (i != null) {
                                    p.getInventory().addItem(i);
                                    sender.sendMessage("§eGave the player the loot at index §9" + index);
                                    return true;
                                }
                            } else {
                                sender.sendMessage("§cPlayer not found!!");
                                return true;
                            }
                        } catch (Exception ignored) {
                        }
                        sender.sendMessage("§cUnable to get that loot!");
                    } else if (((args.length == 2) && (args[0].equalsIgnoreCase("spawn"))) || ((args[0].equalsIgnoreCase("cspawn")) && (args.length == 6))) {
                        EntityType type = EntityType.fromName(args[1]);
                        if ((type != null)) {
                            boolean exmsg;
                            World world;
                            Entity ent;
                            if ((args[0].equalsIgnoreCase("cspawn")) && (args[2] != null) && (args[3] != null) && (args[4] != null) && (args[5] != null)) {
                                if (Bukkit.getServer().getWorld(args[2]) == null) {
                                    sender.sendMessage(args[2] + " dose not exist!");
                                    return true;
                                }
                                world = Bukkit.getServer().getWorld(args[2]);
                                Location spoint = new Location(Bukkit.getServer().getWorld(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                                ent = world.spawnEntity(spoint, type);
                                exmsg = true;
                            } else {
                                Location farSpawnLoc = player.getTargetBlock(null, 200).getLocation();
                                farSpawnLoc.setY(farSpawnLoc.getY() + 1.0D);
                                ent = player.getWorld().spawnEntity(farSpawnLoc, type);
                                exmsg = false;
                            }
                            List<String> abList = plugin.getAbilitiesAmount(ent);
                            InfernalMob newMob;
                            UUID id = ent.getUniqueId();
                            if (abList.contains("1up")) {
                                newMob = new InfernalMob(ent, id, true, abList, 2, plugin.getEffect());
                            } else {
                                newMob = new InfernalMob(ent, id, true, abList, 1, plugin.getEffect());
                            }

                            if (abList.contains("flying")) {
                                plugin.makeFly(ent);
                            }
                            plugin.infernalList.add(newMob);
                            plugin.gui.setName(ent);

                            plugin.giveMobGear(ent, false);
                            plugin.addHealth(ent, abList);
                            if (!exmsg) {
                                sender.sendMessage("Spawned a " + args[1]);
                            } else if (sender instanceof Player) {
                                sender.sendMessage("Spawned a " + args[1] + " in " + args[2] + " at " + args[3] + ", " + args[4] + ", " + args[5]);
                            }
                        } else {
                            sender.sendMessage("Can't spawn a " + args[1] + "!");
                            return true;
                        }
                    } else if ((args.length >= 3 && args[0].equalsIgnoreCase("spawn"))
                            || (args[0].equalsIgnoreCase("cspawn") && args.length >= 6)
                            || (args[0].equalsIgnoreCase("pspawn") && args.length >= 3)) {
                        if (args[0].equalsIgnoreCase("spawn") && player != null) {
                            EntityType type = EntityType.fromName(args[1]);
                            if ((type != null)) {
                                Location farSpawnLoc = player.getTargetBlock(null, 200).getLocation();
                                farSpawnLoc.setY(farSpawnLoc.getY() + 1.0D);
                                Entity ent = player.getWorld().spawnEntity(farSpawnLoc, type);
                                List<String> spesificAbList = new ArrayList<>();
                                for (int i = 0; i <= args.length - 3; i++) {
                                    if (getConfig().getString(args[(i + 2)]) != null) {
                                        spesificAbList.add(args[(i + 2)]);
                                    } else {
                                        sender.sendMessage(args[(i + 2)] + " is not a valid ability!");
                                        return true;
                                    }
                                }
                                InfernalMob newMob;
                                UUID id = ent.getUniqueId();
                                if (spesificAbList.contains("1up")) {
                                    newMob = new InfernalMob(ent, id, true, spesificAbList, 2, plugin.getEffect());
                                } else {
                                    newMob = new InfernalMob(ent, id, true, spesificAbList, 1, plugin.getEffect());
                                }
                                if (spesificAbList.contains("flying")) {
                                    plugin.makeFly(ent);
                                }
                                plugin.infernalList.add(newMob);
                                plugin.gui.setName(ent);
                                plugin.giveMobGear(ent, false);

                                plugin.addHealth(ent, spesificAbList);

                                sender.sendMessage("Spawned a " + args[1] + " with the abilities:");
                                sender.sendMessage(spesificAbList.toString());
                            } else {
                                sender.sendMessage("Can't spawn a " + args[1] + "!");
                            }
                        } else if (args[0].equalsIgnoreCase("cspawn")) {
                            //cspawn <mob> <world> <x> <y> <z> <ability> <ability>
                            if (Bukkit.getServer().getWorld(args[2]) == null) {
                                sender.sendMessage(args[2] + " dose not exist!");
                                return true;
                            }
                            World world = Bukkit.getServer().getWorld(args[2]);
                            Location spoint = new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                            List<String> abList = new ArrayList<>(Arrays.asList(args).subList(6, args.length));
                            if (plugin.cSpawn(sender, args[1], spoint, abList)) {
                                sender.sendMessage("Spawned a " + args[1] + " in " + args[2] + " at " + args[3] + ", " + args[4] + ", " + args[5] + " with the abilities:");
                                sender.sendMessage(abList.toString());
                            }
                        } else {
                            //pspawn <mob> <player> <ability> <ability>
                            Player p = Bukkit.getServer().getPlayer(args[2]);
                            if (p == null) {
                                sender.sendMessage(args[2] + " is not online!");
                                return true;
                            }
                            List<String> abList = new ArrayList<>(Arrays.asList(args).subList(3, args.length));
                            if (plugin.cSpawn(sender, args[1], p.getLocation(), abList)) {
                                sender.sendMessage("Spawned a " + args[1] + " at " + p.getName() + " with the abilities:");
                                sender.sendMessage(abList.toString());
                            }
                        }
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("abilities"))) {
                        sender.sendMessage("--Infernal Mobs Abilities--");
                        sender.sendMessage("mama, molten, weakness, vengeance, webber, storm, sprint, lifesteal, ghastly, ender, cloaked, berserk, 1up, sapper, rust, bullwark, quicksand, thief, tosser, withering, blinding, armoured, poisonous, potions, explode, gravity, archer, necromancer, firework, flying, mounted, morph, ghost, confusing");
                    } else {
                        List<String> oldMobAbilityList;
                        if ((args.length == 1) && (args[0].equalsIgnoreCase("showAbilities"))) {
                            Entity targeted = player == null ? null : plugin.getTarget(player);
                            if (targeted != null) {
                                UUID mobId = targeted.getUniqueId();
                                if (plugin.idSearch(mobId) != -1) {
                                    oldMobAbilityList = plugin.findMobAbilities(mobId);
                                    if (!targeted.isDead()) {
                                        sender.sendMessage("--Targeted InfernalMob's Abilities--");
                                        sender.sendMessage(oldMobAbilityList.toString());
                                    }
                                } else {
                                    sender.sendMessage("§cThis " + targeted.getType().getName() + " §cis not an infernal mob!");
                                }
                            } else {
                                sender.sendMessage("§cUnable to find mob!");
                            }
                        } else if ((args[0].equalsIgnoreCase("setInfernal")) && (args.length == 2) && player != null) {
                            Block block = player.getTargetBlock(null, 25);
                            BlockState state = block.getState();
                            if (state instanceof CreatureSpawner) {
                                int delay = Integer.parseInt(args[1]);

                                CreatureSpawner spawner = (CreatureSpawner) state;
                                PDC.setSpawnerDelay(spawner, delay);

                                sender.sendMessage("§cSpawner set to infernal with a " + delay + " second delay!");
                            } else {
                                sender.sendMessage("§cYou must be looking a spawner to make it infernal!");
                            }
                        } else if ((args[0].equalsIgnoreCase("kill")) && (args.length == 2) && player != null) {
                            int size = Integer.parseInt(args[1]);
                            for (Entity e : player.getNearbyEntities(size, size, size)) {
                                int id = plugin.idSearch(e.getUniqueId());
                                if (id != -1) {
                                    PDC.setAbilities(e, null);
                                    e.remove();
                                    plugin.getLogger().log(Level.INFO, "Entity remove due to /kill");
                                }
                            }
                            sender.sendMessage("§eKilled all infernal mobs near you!");
                        } else if ((args[0].equalsIgnoreCase("killall")) && (args.length == 1 || args.length == 2)) {
                            World w = null;
                            if (args.length == 1 && sender instanceof Player){
                                w = ((Player) sender).getWorld();
                            } else if (args.length == 2){
                                w = Bukkit.getServer().getWorld(args[1]);
                            }

                            if (w != null) {
                                for (Entity e : w.getEntities()) {
                                    int id = plugin.idSearch(e.getUniqueId());
                                    if (id != -1) {
                                        PDC.setAbilities(e, null);
                                        if (e instanceof LivingEntity) {
                                            e.setCustomName(null);
                                        }
                                        plugin.getLogger().log(Level.INFO, "Entity remove due to /killall");
                                        e.remove();
                                    }
                                }
                                sender.sendMessage("§eKilled all loaded infernal mobs in that world!");
                            } else {
                                sender.sendMessage("§cWorld not found!");
                            }
                        } else if (args[0].equalsIgnoreCase("mobs")) {
                            sender.sendMessage("§6List of Mobs:");
                            for (EntityType e : EntityType.values())
                                if (e != null)
                                    sender.sendMessage(e.toString());
                        } else if (args[0].equalsIgnoreCase("setloot") && player != null) {
                            plugin.setItem(player.getInventory().getItemInMainHand(), "loot." + args[1], plugin.lootFile);
                            sender.sendMessage("§eSet loot at index " + args[1] + " §eto item in hand.");
                        } else {
                            throwError(sender);
                        }
                    }
                } else {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                }
            } catch (Exception ex) {
                throwError(sender);
                plugin.getLogger().log(Level.WARNING, "", ex);
            }
        }
        return true;
    }

    private void throwError(CommandSender sender) {
        sender.sendMessage("--Infernal Mobs v" + plugin.getDescription().getVersion() + "--");
        sender.sendMessage("Usage: /im reload");
        sender.sendMessage("Usage: /im worldInfo");
        sender.sendMessage("Usage: /im error");
        sender.sendMessage("Usage: /im getloot <index>");
        sender.sendMessage("Usage: /im setloot <index>");
        sender.sendMessage("Usage: /im giveloot <player> <index>");
        sender.sendMessage("Usage: /im abilities");
        sender.sendMessage("Usage: /im showAbilities");
        sender.sendMessage("Usage: /im setInfernal <time delay>");
        sender.sendMessage("Usage: /im spawn <mob> <ability> <ability>");
        sender.sendMessage("Usage: /im cspawn <mob> <world> <x> <y> <z> <ability> <ability>");
        sender.sendMessage("Usage: /im pspawn <mob> <player> <ability> <ability>");
        sender.sendMessage("Usage: /im kill <size>");
        sender.sendMessage("Usage: /im killall <world>");
    }

    @Override
    @Nullable
    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
        List<String> allAbilitiesList = new ArrayList<>(Arrays.asList("confusing", "ghost", "morph", "mounted", "flying", "gravity", "firework", "necromancer", "archer", "molten", "mama", "potions", "explode", "berserk", "weakness", "vengeance", "webber", "storm", "sprint", "lifesteal", "ghastly", "ender", "cloaked", "1up", "sapper", "rust", "bullwark", "quicksand", "thief", "tosser", "withering", "blinding", "armoured", "poisonous"));
        Set<String> commands = new HashSet<>(Arrays.asList("reload", "worldInfo", "error", "getloot", "setloot", "giveloot", "abilities", "showAbilities", "setInfernal", "spawn", "cspawn", "pspawn", "kill", "killall"));
        if (sender.hasPermission("infernal_mobs.commands")) {

            List<String> newTab = new ArrayList<>();
            if (args.length == 1) {
                if (args[0].isEmpty()) {
                    return new ArrayList<>(commands);
                }
                for (String tab : commands) {
                    if (tab.toLowerCase().startsWith(args[0].toLowerCase())) {
                        newTab.add(tab);
                    }
                }
            }
            if (args[0].equalsIgnoreCase("getloot") || args[0].equalsIgnoreCase("setloot")){
                if (args.length == 2){
                    newTab.add("1");
                }
            }
            if (args[0].equalsIgnoreCase("giveloot")){
                if (args.length == 2){
                    newTab.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
                }
                if (args.length == 3){
                    newTab.add("1");
                }
            }
            if (args[0].equalsIgnoreCase("setinfernal")){
                if (args.length == 2){
                    newTab.add("10");
                }
            }
            if (args.length == 2){
                if (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("cspawn") || args[0].equalsIgnoreCase("pspawn")){
                    if (args[1].isEmpty())
                        newTab.addAll(Arrays.stream(EntityType.values()).filter(m->m.isSpawnable() && m.isAlive()).map(Enum::name).collect(Collectors.toList()));
                    else
                        Arrays.stream(EntityType.values()).filter(m->m.isSpawnable() && m.isAlive()).map(Enum::name).collect(Collectors.toList()).forEach(tab->{
                            if (tab.toLowerCase().startsWith(args[1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
                if (args[0].equalsIgnoreCase("killall")){
                    if (args[args.length - 1].isEmpty())
                        newTab.addAll(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
                    else
                        Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()).forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
                if (args[0].equalsIgnoreCase("kill")){
                    newTab.add("1");
                }
            }
            if (args[0].equalsIgnoreCase("cspawn")) {
                if (args.length == 3) {
                    if (args[args.length - 1].isEmpty())
                        newTab.addAll(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
                    else
                        Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()).forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
                if (args.length > 3 && args.length < 7) {
                    newTab.add("~");
                }
                if (args.length >= 7){
                    if (args[args.length-1].isEmpty())
                        newTab.addAll(allAbilitiesList);
                    else
                        allAbilitiesList.forEach(tab->{
                            if (tab.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
            }
            if (args[0].equalsIgnoreCase("pspawn")) {
                if (args.length == 3) {
                    if (args[args.length - 1].isEmpty())
                        newTab.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
                    else
                        Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()).forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
                if (args.length > 3){
                    if (args[args.length-1].isEmpty())
                        newTab.addAll(allAbilitiesList);
                    else
                        allAbilitiesList.forEach(tab->{
                            if (tab.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
            }
            if (args.length >= 3){
                if (args[0].equalsIgnoreCase("spawn")){
                    if (args[args.length-1].isEmpty())
                        newTab.addAll(allAbilitiesList);
                    else
                        allAbilitiesList.forEach(tab->{
                            if (tab.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
            }
            return newTab;
        }
        return null;
    }

}
