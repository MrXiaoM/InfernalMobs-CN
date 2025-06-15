package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.commands.Commands;
import io.hotmail.com.jacob_vejvoda.infernal_mobs.data.Enchant;
import io.hotmail.com.jacob_vejvoda.infernal_mobs.data.InfernalMob;
import io.hotmail.com.jacob_vejvoda.infernal_mobs.events.InfernalSpawnEvent;
import io.hotmail.com.jacob_vejvoda.infernal_mobs.listeners.EntityListener;
import io.hotmail.com.jacob_vejvoda.infernal_mobs.listeners.GUI;
import io.hotmail.com.jacob_vejvoda.infernal_mobs.utils.PDC;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PluginMain extends JavaPlugin implements Listener {
    public GUI gui;
    public long serverTime = 0L;
    public List<InfernalMob> infernalList = new ArrayList();
    public File lootYML = new File(getDataFolder(), "loot.yml");
    public YamlConfiguration lootFile = YamlConfiguration.loadConfiguration(this.lootYML);
    public final HashMap<Entity, Entity> mountList = new HashMap();
    public List<Player> errorList = new ArrayList();
    public List<Player> levitateList = new ArrayList();
    public List<Player> fertileList = new ArrayList();

    @SuppressWarnings("deprecation")
	public void onEnable() {
        //Register Events
        getServer().getPluginManager().registerEvents(this, this);
        EntityListener events = new EntityListener(this);
        getServer().getPluginManager().registerEvents(events, this);
        this.gui = new GUI(this);
        getServer().getPluginManager().registerEvents(this.gui, this);
        this.getLogger().log(Level.INFO, "Registered Events.");
        //Folder
        File dir = new File(this.getDataFolder().getParentFile().getPath(), this.getName());
        if (!dir.exists())
            dir.mkdir();
        //Old config check
        if (new File(getDataFolder(), "config.yml").exists()) {
            if (getConfig().getString("configVersion") == null) {
                this.getLogger().log(Level.INFO, "No config version found!");
                getConfig().set("configVersion", Bukkit.getVersion().split(":")[1].replace(")", "").trim());
                saveConfig();
            }
            if (!Bukkit.getVersion().equals(getConfig().getString("configVersion"))) {
                System.out.println(Bukkit.getVersion() + " contains " + getConfig().getString("configVersion"));
                this.getLogger().log(Level.INFO, "Old config found, deleting!");
                new File(this.getDataFolder() + File.separator + "config.yml").delete();
            }
        }
        //Register Config

        String configVersion = null;
        if (Bukkit.getVersion().contains("1.13") ||
                Bukkit.getVersion().contains("1.14") ||
                Bukkit.getVersion().contains("1.15")) {
            configVersion = "1_15";
        }
        if (Bukkit.getVersion().contains("1.16")) {
            configVersion = "1_16";
        }
        if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.18") || Bukkit.getVersion().contains("1.19") || Bukkit.getVersion().contains("1.20")) {
            configVersion = "1_18";
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            //saveDefaultConfig();
            this.getLogger().log(Level.INFO, "No config.yml found, generating...");
            //Generate Config
            boolean generatedConfig = false;
            //for(String version : Arrays.asList("1.12","1.11","1.10","1.9","1.8"))

            if (configVersion != null) {

                try (InputStream in = getResource(configVersion + "_config.yml")) {
                    if (in == null) {
                        throw new IllegalArgumentException("The embedded resource '" + configVersion + "_config.yml' cannot be found in " + getFile());
                    }
                     try (OutputStream out = Files.newOutputStream(configFile.toPath())) {
                         byte[] buf = new byte[1024];
                         int len;
                         while ((len = in.read(buf)) > 0) {
                             out.write(buf, 0, len);
                         }
                     }
                } catch (Exception ex) {
                    getLogger().log(Level.SEVERE, "Could not save " + configVersion + "_config.yml to " + configFile, ex);
                }
                getConfig().set("configVersion", Bukkit.getVersion());
                getConfig().options().header(
                        "Chance is the chance that a mob will not be infernal, the lower the number the higher the chance. (min 1)\n" +
                                "Enabledworlds are the worlds that infernal mobs can spawn in.\n" +
                                "Enabledmobs are the mobs that can become infernal.\n" +
                                "Loot is the items that are dropped when an infernal mob dies. (You can have up to 64)\n" +
                                "Item is the item, Amount is the amount, Durability is how damaged it will be (0 is undamaged).\n" +
                                "nameTagsLevel is the visibility level of the name tags, 0 = no tag, \n" +
                                "1 = tag shown when your looking at the mob, 2 = tag always shown.\n" +
                                "Note, if you have name tags set to 0, on server restart all infernal mobs will turn normal.\n" +
                                "If you want to enable the boss bar you must have BarAPI on your server.\n" +
                                "nameTagsName and bossBarsName have these special tags: <mobLevel> = the amount of powers the boss has.\n" +
                                "<abilities> = A list of about 3-5 (whatever can fit) names of abilities the boss has.\n" +
                                "<mobName> = Name of the mob, so if the mob is a creeper the mobName will be \"Creeper\".");
                saveConfig();
                this.getLogger().log(Level.INFO, "Config successfully generated!");
                generatedConfig = true;
            }

            if (!generatedConfig) {
                this.getLogger().log(Level.SEVERE, "No config available, " + Bukkit.getVersion() + " is not supported!");
                Bukkit.getPluginManager().disablePlugin(this);
            }
            reloadConfig();
        }
        //Register Loots
        if (!lootYML.exists()) {
            this.getLogger().log(Level.INFO, "No loot.yml found, generating...");
            //Generate Config
            boolean generatedConfig = false;

            if (configVersion != null) {
                this.saveResource(configVersion + "loot.yml", false);
                new File(this.getDataFolder(), configVersion + "loot.yml").renameTo(new File(this.getDataFolder(), "loot.yml"));
                this.getLogger().log(Level.INFO, Bukkit.getVersion() + " Loot successfully generated!");
                generatedConfig = true;
            }

            if (!generatedConfig) {
                this.getLogger().log(Level.SEVERE, "No loot available, " + Bukkit.getVersion() + " is not supported!");
                Bukkit.getPluginManager().disablePlugin(this);
            }
            reloadLoot();
        }
        new Commands(this);
        applyEffect();
        reloadPowers();
        Bukkit.getScheduler().runTaskTimer(this, this::showEffect, 20L, 20L);
        addRecipes();
    }

    private void reloadPowers() {
        ArrayList<World> wList = new ArrayList();
        for (Player p : getServer().getOnlinePlayers()) {
            if (!wList.contains(p.getWorld())) {
                wList.add(p.getWorld());
            }
        }
        for (World world : wList) {
            giveMobsPowers(world);
        }
    }

    private void scoreCheck() {
        for (Player p : getServer().getOnlinePlayers())
            GUI.fixBar(p);
        HashMap<Entity, Entity> tmp = (HashMap<Entity, Entity>) mountList.clone();
        for (Map.Entry<Entity, Entity> hm : tmp.entrySet()) {
            if ((hm.getKey() != null) && (!hm.getKey().isDead())) {
                if ((hm.getValue().isDead()) && ((hm.getKey() instanceof LivingEntity))) {
                    String fate = getConfig().getString("mountFate", "nothing");
                    if (fate.equals("death")) {
                        LivingEntity le = (LivingEntity) hm.getKey();
                        le.damage(9.99999999E8D);
                        this.mountList.remove(hm.getKey());
                    } else if (fate.equals("removal")) {
                        hm.getKey().remove();
                        this.getLogger().log(Level.INFO, "Entity remove due to Fate");
                        this.mountList.remove(hm.getKey());
                    }
                }
            } else {
                this.mountList.remove(hm.getKey());
            }
        }
    }

    public void giveMobsPowers(World world) {
        for (Entity ent : world.getEntities()) {
            if (((ent instanceof LivingEntity)) && PDC.hasAbilities(ent)) {
                giveMobPowers(ent);
            }
        }
    }

    public void giveMobPowers(Entity ent) {
        UUID id = ent.getUniqueId();
        if (idSearch(id) == -1) {
            List<String> aList = null;
            for (MetadataValue v : ent.getMetadata("infernalMetadata")) {
                aList = new ArrayList(Arrays.asList(v.asString().split(",")));
            }
            if (aList == null) {
                List<String> oldAbilities = PDC.getAbilities(ent);
                if (oldAbilities != null) {
                    aList = new ArrayList(oldAbilities);
                    String list = getPowerString(ent, aList);
                    ent.setMetadata("infernalMetadata", new FixedMetadataValue(this, list));
                } else {
                    aList = getAbilitiesAmount(ent);
                }
            }
            InfernalMob newMob;
            if (aList.contains("1up")) {
                newMob = new InfernalMob(ent, id, true, aList, 2, getEffect());
            } else {
                newMob = new InfernalMob(ent, id, true, aList, 1, getEffect());
            }
            if (aList.contains("flying")) {
                makeFly(ent);
            }
            this.infernalList.add(newMob);
        }
    }

    public void makeInfernal(final Entity e, final boolean fixed) {
        String entName = e.getType().name();
        if ((!e.hasMetadata("NPC")) && (!e.hasMetadata("shopkeeper"))) {
            if (!fixed) {
                ArrayList<String> babyList = (ArrayList) getConfig().getList("disabledBabyMobs", new ArrayList<>());
                if (e instanceof Ageable) {
                    Ageable age = (Ageable) e;
                    boolean baby = !age.isAdult();
                    if (baby && babyList.contains(entName)) {
                        return;
                    }
                }
            }
            final UUID id = e.getUniqueId();
            final int chance = getConfig().getInt("chance");
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                String entName1 = e.getType().name();
                if ((!e.isDead()) && (e.isValid()) && (
                        ((getConfig().getStringList("enabledmobs").contains(entName1))) || ((fixed) &&
                                (idSearch(id) == -1)))) {
                    //Default
                    int min = 1;
                    int max = chance;
                    //Pe InfernalMob
                    int mc = getConfig().getInt("mobChances." + entName1);
                    if (mc > 0)
                        max = mc;
                    if (fixed)
                        max = 1;
                    //int randomNum = new Random().nextInt(max - min) + min;
                    int randomNum = rand(min, max);
                    if (randomNum == 1) {
                        List<String> aList = getAbilitiesAmount(e);
                        if (PluginMain.this.getConfig().getString("levelChance." + aList.size()) != null) {
                            int sc = PluginMain.this.getConfig().getInt("levelChance." + aList.size());
                            int randomNum2 = new Random().nextInt(sc - min) + min;
                            if (randomNum2 != 1) {
                                return;
                            }
                        }
                        InfernalMob newMob;
                        if (aList.contains("1up")) {
                            newMob = new InfernalMob(e, id, true, aList, 2, PluginMain.this.getEffect());
                        } else {
                            newMob = new InfernalMob(e, id, true, aList, 1, PluginMain.this.getEffect());
                        }

                        //fire event
                        InfernalSpawnEvent infernalEvent = new InfernalSpawnEvent(e, newMob);
                        Bukkit.getPluginManager().callEvent(infernalEvent);
                        if (infernalEvent.isCancelled()) {
                            return;
                        }

                        if (aList.contains("flying")) {
                            PluginMain.this.makeFly(e);
                        }
                        PluginMain.this.infernalList.add(newMob);
                        PluginMain.this.gui.setName(e);
                        PluginMain.this.giveMobGear(e, true);
                        PluginMain.this.addHealth(e, aList);
                        if (PluginMain.this.getConfig().getBoolean("enableSpawnMessages")) {
                            if (PluginMain.this.getConfig().getList("spawnMessages") != null) {
                                List<String> spawnMessageList = PluginMain.this.getConfig().getStringList("spawnMessages");
                                Random randomGenerator = new Random();
                                int index = randomGenerator.nextInt(spawnMessageList.size());
                                String spawnMessage = spawnMessageList.get(index);

                                spawnMessage = ChatColor.translateAlternateColorCodes('&', spawnMessage);
                                if (e.getCustomName() != null) {
                                    spawnMessage = spawnMessage.replace("mob", e.getCustomName());
                                } else {
                                    spawnMessage = spawnMessage.replace("mob", e.getType().toString().toLowerCase());
                                }
                                int r = PluginMain.this.getConfig().getInt("spawnMessageRadius");
                                if (r == -1) {
                                    for (Player p : e.getWorld().getPlayers()) {
                                        p.sendMessage(spawnMessage);
                                    }
                                } else if (r == -2) {
                                    Bukkit.broadcastMessage(spawnMessage);
                                } else {
                                    for (Entity e1 : e.getNearbyEntities(r, r, r)) {
                                        if ((e1 instanceof Player)) {
                                            Player p = (Player) e1;
                                            p.sendMessage(spawnMessage);
                                        }
                                    }
                                }
                            } else {
                                System.out.println("No valid spawn messages found!");
                            }
                        }
                    }
                }
            }, 10L);
        }
    }

    public void addHealth(Entity ent, List<String> powerList) {
        //double maxHealth = ((org.bukkit.entity.Damageable) ent).getHealth();
    	double maxHealth = ((LivingEntity) ent).getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        float setHealth;
        if (getConfig().getBoolean("healthByPower")) {
            int mobIndex = idSearch(ent.getUniqueId());
            try {
                InfernalMob m = this.infernalList.get(mobIndex);
                setHealth = (float) (maxHealth * m.getAbilityList().size());
            } catch (Exception e) {
                setHealth = (float) (maxHealth * 5.0D);
            }
        } else {
            if (getConfig().getBoolean("healthByDistance")) {
                Location l = ent.getWorld().getSpawnLocation();
                int m = (int) l.distance(ent.getLocation()) / getConfig().getInt("addDistance");
                if (m < 1) {
                    m = 1;
                }
                int add = getConfig().getInt("healthToAdd");
                setHealth = m * add;
            } else {
                int healthMultiplier = getConfig().getInt("healthMultiplier");
                setHealth = (float) (maxHealth * healthMultiplier);
            }
        }
        if (setHealth >= 1.0F) {
            try {
                ((LivingEntity) ent).getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(setHealth);
                ((LivingEntity) ent).setHealth(setHealth);
            } catch (Exception e) {
                System.out.println("addHealth: " + e);
            }
        }
        String list = getPowerString(ent, powerList);
        ent.setMetadata("infernalMetadata", new FixedMetadataValue(this, list));
        PDC.setAbilities(ent, powerList);
    }

    private String getPowerString(Entity ent, List<String> powerList) {
        StringJoiner joiner = new StringJoiner(",");
        for (String s : powerList) {
            joiner.add(s);
        }
        return joiner.toString();
    }

    public void spawnGhost(Location l) {
        boolean evil = new Random().nextInt(3) == 1;
        Zombie g = (Zombie) l.getWorld().spawnEntity(l, EntityType.ZOMBIE);
        g.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 199999980, 1));
        g.setCanPickupItems(false);

        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        ItemStack skull;
        if (evil) {
            skull = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
            dye(chest, Color.BLACK);
        } else {
            skull = new ItemStack(Material.SKELETON_SKULL, 1);
            dye(chest, Color.WHITE);
        }
        chest.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, new Random().nextInt(10) + 1);
        ItemMeta m = skull.getItemMeta();
        if (m != null) {
            m.setDisplayName("§fGhost Head");
            skull.setItemMeta(m);
        }
        EntityEquipment equipment = g.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(skull);
            equipment.setChestplate(chest);
            equipment.setHelmetDropChance(0.0F);
            equipment.setChestplateDropChance(0.0F);
            int min = 1;
            int max = 5;
            int rn = new Random().nextInt(max - min) + min;
            if (rn == 1) {
                equipment.setItemInMainHand(new ItemStack(Material.STONE_HOE, 1));
                equipment.setItemInMainHandDropChance(0.0F);
            }
        }
        ghostMove(g);

        List<String> aList = new ArrayList();
        aList.add("ender");
        if (evil) {
            aList.add("necromancer");
            aList.add("withering");
            aList.add("blinding");
        } else {
            aList.add("ghastly");
            aList.add("sapper");
            aList.add("confusing");
        }
        InfernalMob newMob;
        if (evil) {
            newMob = new InfernalMob(g, g.getUniqueId(), false, aList, 1, "smoke:2:12");
        } else {
            newMob = new InfernalMob(g, g.getUniqueId(), false, aList, 1, "cloud:0:8");
        }
        this.infernalList.add(newMob);
    }

    private void ghostMove(final Entity g) {
        if (g.isDead()) {
            return;
        }
        Vector v = g.getLocation().getDirection().multiply(0.3D);
        g.setVelocity(v);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            try {
                PluginMain.this.ghostMove(g);
            } catch (Exception ignored) {
            }
        }, 2L);
    }

    private void dye(ItemStack item, Color color) {
        try {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            if (meta != null) {
                meta.setColor(color);
                item.setItemMeta(meta);
            }
        } catch (Exception ignored) {
        }
    }

    private boolean mobPowerLevelFine(int lootId, int mobPowers) {
        int min = 0;
        int max = 99;
        if (lootFile.getString("loot." + lootId + ".powersMin") != null) {
            min = lootFile.getInt("loot." + lootId + ".powersMin");
        }
        if (lootFile.getString("loot." + lootId + ".powersMax") != null)
            max = lootFile.getInt("loot." + lootId + ".powersMax");
        if (getConfig().getBoolean("debug"))
            this.getLogger().log(Level.INFO, "Loot " + lootId + " min = " + min + " and max = " + max);
        return (mobPowers >= min) && (mobPowers <= max);
    }

    public ItemStack getRandomLoot(Player player, String mob, int powers) {
        List<Integer> lootList = new ArrayList();
        //for (int i = 0; i <= 512; i++) {
        ConfigurationSection section = lootFile.getConfigurationSection("loot");
        if (section != null) for (String i : section.getKeys(false)) {
            if ((lootFile.getString("loot." + i) != null) &&
                    ((lootFile.getList("loot." + i + ".mobs") == null) ||
                            (this.lootFile.getList("loot." + i + ".mobs", new ArrayList<>()).contains(mob))) &&
                    (lootFile.getString("loot." + i + ".chancePercentage") == null ||
                            rand(1, 100) <= lootFile.getInt("loot." + i + ".chancePercentage"))) {
                if (mobPowerLevelFine(Integer.parseInt(i), powers)) {
                    lootList.add(Integer.valueOf(i));
                }
            }
        }
        try {
            if (getConfig().getBoolean("debug"))
                this.getLogger().log(Level.INFO, "Loot List " + lootList);
            if (!lootList.isEmpty()) {
                return getLoot(player, lootList.get(rand(1, lootList.size()) - 1));
            } else {
                return null;
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Error in get random loot ", e);
            getLogger().warning("Error: No valid drops found!");
        }
        return null;
    }

    public ItemStack getLoot(Player player, int loot) {
    	ItemStack i = null;
    	try {
	        if (!this.lootFile.getStringList("loot." + loot + ".commands").isEmpty()) {
	            List<String> commandList = this.lootFile.getStringList("loot." + loot + ".commands");
	            for (String command : commandList) {
	                command = ChatColor.translateAlternateColorCodes('&', command);
	                command = command.replace("player", player.getName());
	                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
	            }
	        }
	//        if (this.lootFile.getString("loot." + loot + ".staff.id") != null) {
	//            int id = this.lootFile.getInt("loot." + loot + ".staff.id");
	//            ArrayList<String> spells = new ArrayList();
	//            if (!this.lootFile.getStringList("loot." + loot + ".staff.spells").isEmpty()) {
	//                spells = (ArrayList) this.lootFile.getStringList("loot." + loot + ".staff.spells");
	//            }
	//            return this.wMagic.getStaffWithSpells(id, spells);
	//        }
	        i = getItem(loot);
    	}catch(Exception x) {getServer().getLogger().log(Level.WARNING, "No loot found with ID: " + loot);}
    	return i;
    }

    private Material getMaterial(String s) {
        return Material.valueOf(s);
    }

    public ItemStack getItem(int loot) {
        //System.out.println("Get Loot: " + loot);
        try {
            String setItem = this.lootFile.getString("loot." + loot + ".item");

            String setAmountString = this.lootFile.getString("loot." + loot + ".amount");
            int setAmount;
            if (setAmountString != null) {
                setAmount = getIntFromString(setAmountString);
            } else
                setAmount = 1;
            ItemStack stack = new ItemStack(getMaterial(setItem), setAmount);
            //Name
            String name = null;
            if (lootFile.getString("loot." + loot + ".name") != null && lootFile.isString("loot." + loot + ".name")) {
                name = lootFile.getString("loot." + loot + ".name");
                name = prosessLootName(name, stack);
            } else if (lootFile.isList("loot." + loot + ".name")) {
                List<String> names = lootFile.getStringList("loot." + loot + ".name");
                if (!names.isEmpty()) {
                    name = names.get(rand(1, names.size()) - 1);
                    name = prosessLootName(name, stack);
                }
            }
            //Lore
            List<String> loreList = new ArrayList();
            for (int i = 0; i <= 32; i++) {
                if (this.lootFile.getString("loot." + loot + ".lore" + i) != null) {
                    String lore = this.lootFile.getString("loot." + loot + ".lore" + i, "");
                    lore = ChatColor.translateAlternateColorCodes('&', lore);
                    loreList.add(lore);
                }
            }
            if (!lootFile.getStringList("loot." + loot + ".lore").isEmpty()) {
                List<String> l = lootFile.getStringList("loot." + loot + ".lore");
                int min = l.size();
                if (lootFile.getString("loot." + loot + ".minLore") != null)
                    min = lootFile.getInt("loot." + loot + ".minLore");
                int max = l.size();
                if (lootFile.getString("loot." + loot + ".maxLore") != null)
                    max = lootFile.getInt("loot." + loot + ".maxLore");
                if (!l.isEmpty())
                    for (int i = 0; i < rand(min, max); i++) {
                        String lore = l.get(rand(1, l.size()) - 1);
                        l.remove(lore);
                        loreList.add(prosessLootName(lore, stack));
                    }
            }
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                //Durability
                String durabilityString = this.lootFile.getString("loot." + loot + ".durability");
                if (durabilityString != null) {
                    int durability = getIntFromString(durabilityString);
                    if (meta instanceof Damageable) {
                        ((Damageable) meta).setDamage(durability);
                    }
                }
                if (name != null) {
                    meta.setDisplayName(name);
                }
                if (!loreList.isEmpty()) {
                    meta.setLore(loreList);
                }
                stack.setItemMeta(meta);
            }
            //Colour
            if (stack.getType().toString().toLowerCase().contains("leather")) {
                String c = this.lootFile.getString("loot." + loot + ".colour");
                if (c != null) {
                    String[] split = c.split(",");
                    Color colour = Color.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                    dye(stack, colour);
                }
            }
            //Book
            if ((stack.getType().equals(Material.WRITTEN_BOOK)) || (stack.getType().equals(Material.WRITABLE_BOOK))) {
                ItemMeta meta1 = stack.getItemMeta();
                if (meta1 instanceof BookMeta) {
                    BookMeta bMeta = (BookMeta) meta1;
                    String author = this.lootFile.getString("loot." + loot + ".author");
                    if (author != null) {
                        author = ChatColor.translateAlternateColorCodes('&', author);
                        bMeta.setAuthor(author);
                    }
                    String title = this.lootFile.getString("loot." + loot + ".title");
                    if (title != null) {
                        title = ChatColor.translateAlternateColorCodes('&', title);
                        bMeta.setTitle(title);
                    }
                    ConfigurationSection pages = this.lootFile.getConfigurationSection("loot." + loot + ".pages");
                    if (pages != null) for (String i : pages.getKeys(false)) {
                        String page = this.lootFile.getString("loot." + loot + ".pages." + i, "");
                        page = ChatColor.translateAlternateColorCodes('&', page);
                        bMeta.addPage(page);
                    }
                    stack.setItemMeta(bMeta);
                }
            }
            //Banners
            if (stack.getType().toString().contains("BANNER")) {
                ItemMeta meta1 = stack.getItemMeta();
                if (meta1 instanceof BannerMeta) {
                    BannerMeta b = (BannerMeta) meta1;
                    List<Pattern> patList = (List<Pattern>) lootFile.getList("loot." + loot + ".patterns");
                    if (patList != null && (!patList.isEmpty()))
                        b.setPatterns(patList);
                    stack.setItemMeta(b);
                }
            }
            //Shield
            if (stack.getType().equals(Material.SHIELD)) {
                ItemMeta im = stack.getItemMeta();
                if (im instanceof BlockStateMeta) {
                    BlockStateMeta bMeta = (BlockStateMeta) im;

                    Banner b = (Banner) bMeta.getBlockState();
                    List<Pattern> patList = (List<Pattern>) lootFile.getList("loot." + loot + ".patterns");
                    String c = lootFile.getString("loot." + loot + ".colour");
                    if (patList != null && !patList.isEmpty() && c != null) {
                        b.setBaseColor(DyeColor.valueOf(c));
                        b.setPatterns(patList);
                        b.update();
                    }
                    bMeta.setBlockState(b);
                    stack.setItemMeta(bMeta);
                }
            }
            //Owner
            if (stack.getType().equals(Material.PLAYER_HEAD)) {
                String owner = this.lootFile.getString("loot." + loot + ".owner");
                ItemMeta meta1 = stack.getItemMeta();
                if (meta1 instanceof SkullMeta && owner != null) {
                    SkullMeta sm = (SkullMeta) meta1;
                    sm.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)));
                    stack.setItemMeta(sm);
                }
            }
            //Potions
            String pn = lootFile.getString("loot." + loot + ".potion");
            if (pn != null) {
                ItemMeta meta1 = stack.getItemMeta();
                if (meta1 instanceof PotionMeta) {
                    PotionMeta pMeta = (PotionMeta) meta1;
                    pMeta.setBasePotionData(new PotionData(PotionType.valueOf(pn), false, false));
                    stack.setItemMeta(pMeta);
                }
            }
            int enchAmount = 0;
            for (int e = 0; e <= 10; e++) {
                if (this.lootFile.getString("loot." + loot + ".enchantments." + e) != null) {
                    enchAmount++;
                }
            }
            //System.out.println("Enchantments Found: " + enchAmount);
            if (enchAmount > 0) {
                int enMin = enchAmount/2;
                if(enMin<1) {enMin=1;}
                int enMax = enchAmount;
                if ((this.lootFile.getString("loot." + loot + ".minEnchantments") != null) && (this.lootFile.getString("loot." + loot + ".maxEnchantments") != null)) {
                    enMin = this.lootFile.getInt("loot." + loot + ".minEnchantments");
                    enMax = this.lootFile.getInt("loot." + loot + ".maxEnchantments");
                }
                //int enchNeeded = new Random().nextInt(enMax + 1 - enMin) + enMin;
                int enchNeeded = rand(enMin,enMax);
                //System.out.println("Enchantments Needed: " + enchNeeded);
                List<Enchant> enchList = new ArrayList();
                int safety = 0;
                int j = 0;
                int chance;
                do {
                    if (this.lootFile.getString("loot." + loot + ".enchantments." + j) != null) {
                        int enChance = 1;
                        if (this.lootFile.getString("loot." + loot + ".enchantments." + j + ".chance") != null) {
                            enChance = this.lootFile.getInt("loot." + loot + ".enchantments." + j + ".chance");
                        }
                        chance = new Random().nextInt(enChance - 1 + 1) + 1;
                        if (chance == 1) {
                            String enchantment = this.lootFile.getString("loot." + loot + ".enchantments." + j + ".enchantment", "").toLowerCase();
                            String levelString = this.lootFile.getString("loot." + loot + ".enchantments." + j + ".level");
                            int level = getIntFromString(levelString);
                            //System.out.print("1: " + NamespacedKey.minecraft(enchantment));
                            //System.out.print("2: " + Enchantment.getByKey(NamespacedKey.minecraft(enchantment)));
                            if (Enchantment.getByKey(NamespacedKey.minecraft(enchantment)) != null) {
                                if (level < 1) {
                                    level = 1;
                                }
                                Enchant le = new Enchant(Enchantment.getByKey(NamespacedKey.minecraft(enchantment)), level);

                                boolean con = false;
                                for (Enchant testE : enchList) {
                                    if (testE.getEnchantment.equals(le.getEnchantment)) {
                                        con = true;
                                        break;
                                    }
                                }
                                if (!con) {
                                    enchList.add(le);
                                }
                            } else {
                                System.out.println("Error: No valid drops found!");
                                System.out.println("Error: " + enchantment + " is not a valid enchantment!");
                                return null;
                            }
                        }
                    }
                    j++;
                    if (j > enchAmount) {
                        j = 0;
                        safety++;
                    }
                    if (safety >= enchAmount * 100) {
                        //System.out.println("Error: No valid drops found!");
                        //System.out.println("Error: Please increase chance for enchantments on item " + loot);
                        //return null;
                    	break;
                    }
                } while (enchList.size() != enchNeeded);
                for (Enchant le : enchList) {
                    ItemMeta meta1 = stack.getItemMeta();
                    if (meta1 instanceof EnchantmentStorageMeta) {
                        EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) meta1;
                        enchantMeta.addStoredEnchant(le.getEnchantment, le.getLevel, true);
                        stack.setItemMeta(enchantMeta);
                    } else {
                        stack.addUnsafeEnchantment(le.getEnchantment, le.getLevel);
                    }
                }
            }
            return stack;
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "", e);
        }
        return null;
    }

    public void setItem(ItemStack s, String path, FileConfiguration fc) {
        if (s != null) {
            fc.set(path + ".item", s.getType().toString());
            fc.set(path + ".amount", s.getAmount());
            fc.set(path + ".durability", ((Damageable)s).getDamage());
            if (s.getItemMeta() != null) {
                fc.set(path + ".name", s.getItemMeta().getDisplayName());
                if (s.getItemMeta().getLore() != null) {
                    for (int l = 0; l < s.getItemMeta().getLore().size(); l++) {
                        if (s.getItemMeta().getLore().get(l) != null) {
                            fc.set(path + ".lore" + l, s.getItemMeta().getLore().get(l));
                        }
                    }
                }
            }
            ItemMeta meta = s.getItemMeta();
            Enchantment e;
            for (Map.Entry<Enchantment, Integer> hm : s.getEnchantments().entrySet()) {
                e = hm.getKey();
                int level = hm.getValue();
                for (int ei = 0; ei < 13; ei++) {
                    if (fc.getString(path + ".enchantments." + ei) == null) {
                        fc.set(path + ".enchantments." + ei + ".enchantment", e.getKey());
                        fc.set(path + ".enchantments." + ei + ".level", level);
                        break;
                    }
                }
            }
            if (s.getType().equals(Material.ENCHANTED_BOOK)) {
                if (meta instanceof EnchantmentStorageMeta) {
                    EnchantmentStorageMeta em = (EnchantmentStorageMeta) meta;
                    for (Object hm : em.getStoredEnchants().entrySet()) {
                        e = (Enchantment) ((Map.Entry) hm).getKey();
                        int level = (Integer) ((Map.Entry) hm).getValue();
                        for (int ei = 0; ei < 13; ei++) {
                            if (fc.getString(path + ".enchantments." + ei) == null) {
                                fc.set(path + ".enchantments." + ei + ".enchantment", e.toString());
                                fc.set(path + ".enchantments." + ei + ".level", level);
                                break;
                            }
                        }
                    }
                }
            }
            if ((s.getType().equals(Material.WRITTEN_BOOK)) || (s.getType().equals(Material.WRITABLE_BOOK))) {
                if (meta instanceof BookMeta) {
                    BookMeta bMeta = (BookMeta) meta;
                    if (bMeta.getAuthor() != null) {
                        fc.set(path + ".author", bMeta.getAuthor());
                    }
                    if (bMeta.getTitle() != null) {
                        fc.set(path + ".title", bMeta.getTitle());
                    }
                    int i = 0;
                    for (String p : bMeta.getPages()) {
                        fc.set(path + ".pages." + i, p);
                        i++;
                    }
                }
            }
            //Banner
            if (s.getType().toString().contains("BANNER")) {
                BannerMeta b = (BannerMeta) s.getItemMeta();
                if (b != null) {
                    List patList = b.getPatterns();
                    if (!patList.isEmpty())
                        fc.set(path + ".patterns", patList);
                }
            }
            //Shield
            if (s.getType().equals(Material.SHIELD)) {
                ItemMeta im = s.getItemMeta();
                if (im instanceof BlockStateMeta) {
                    BlockStateMeta bMeta = (BlockStateMeta) im;
                    Banner b = (Banner) bMeta.getBlockState();

                    fc.set(path + ".colour", b.getBaseColor().toString());
                    List patList = b.getPatterns();
                    if (!patList.isEmpty())
                        fc.set(path + ".patterns", patList);
                }
            }
            //Potions
            if (s.getType().equals(Material.POTION) || s.getType().equals(Material.SPLASH_POTION) || s.getType().equals(Material.LINGERING_POTION)) {
                if (meta instanceof PotionMeta) {
                    PotionMeta pMeta = (PotionMeta) meta;
                    org.bukkit.potion.PotionData pd = pMeta.getBasePotionData();
                    PotionEffectType effectType = pd.getType().getEffectType();
                    if (effectType != null) {
                        fc.set(path + ".potion", effectType.getName());
                    }
                }
            }
            if ((s.getType().equals(Material.LEATHER_BOOTS)) || (s.getType().equals(Material.LEATHER_CHESTPLATE)) || (s.getType().equals(Material.LEATHER_HELMET)) || (s.getType().equals(Material.LEATHER_LEGGINGS))) {
                if (meta instanceof LeatherArmorMeta) {
                    LeatherArmorMeta l = (LeatherArmorMeta) meta;
                    Color c = l.getColor();
                    String color = c.getRed() + "," + c.getGreen() + "," + c.getBlue();
                    fc.set(path + ".colour", color);
                }
            }
            if (s.getType().equals(Material.PLAYER_HEAD)) {
                if (meta instanceof SkullMeta) {
                    SkullMeta sm = (SkullMeta) meta;
                    OfflinePlayer p = sm.getOwningPlayer();
                    if (p != null) {
                        fc.set(path + ".owner", p.getUniqueId().toString());
                    }
                }
            }
            if (meta != null) {
                List<String> flags = new ArrayList<>();
                for (ItemFlag f : meta.getItemFlags())
                    if (f != null)
                        flags.add(f.name());
                if (!flags.isEmpty())
                    fc.set(path + ".flags", flags);
            }
        } else {
            System.out.println("Item is null!");
        }
        try {
            this.lootFile.save(this.lootYML);
        } catch (IOException ignored) {
        }
        saveConfig();
    }

    private String prosessLootName(String name, ItemStack stack) {
        name = ChatColor.translateAlternateColorCodes('&', name);
        String itemName = stack.getType().name();
        itemName = itemName.replace("_", " ");
        itemName = itemName.toLowerCase();
        name = name.replace("<itemName>", itemName);
        return name;
    }

    private int getIntFromString(String setAmountString) {
        int setAmount = 1;
        try {
	        if (setAmountString.contains("-")) {
	            String[] split = setAmountString.split("-");
	            try {
	                int minSetAmount = Integer.parseInt(split[0]);
	                int maxSetAmount = Integer.parseInt(split[1]);
	                setAmount = new Random().nextInt(maxSetAmount - minSetAmount + 1) + minSetAmount;
	            } catch (Exception e) {
	                System.out.println("getIntFromString: " + e);
	            }
	        } else {
	            setAmount = Integer.parseInt(setAmountString);
	        }
        } catch(Exception ignored) {}
        return setAmount;

    }

    private boolean isBaby(Entity mob) {
    	if(mob instanceof Ageable) {
    		return !((Ageable)mob).isAdult();
    	}
        return false;
    }

    public String getEffect() {
        String effect = "mobSpawnerFire";
        try {
            //Get Enabled Particles
            List<String> partTypes = getConfig().getStringList("mobParticles");
            //Get Random Particle
            effect = partTypes.get(new Random().nextInt(partTypes.size()));
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        return effect;
    }

    private void displayEffect(Location l, String effect) {
        if (effect == null) {
            try {
                //Get Particles
                effect = getEffect();
            } catch (Exception e) {
                effect = "mobSpawnerFire";
            }
        }
        //Get Effect and Datas
        String[] split = effect.split(":");
        effect = split[0];
        int data1 = Integer.parseInt(split[1]);
        int data2 = Integer.parseInt(split[2]);
        try {
            String f = "FLAME";
            switch (effect) {
                case "potionBrake":
                    f = Particle.SPELL.toString();
                    break;
                case "smoke":
                    f = Particle.SMOKE_NORMAL.toString();
                    break;
                case "blockBrake":
                    f = Particle.BLOCK_CRACK.toString();
                    break;
                case "hugeExplode":
                    f = Particle.EXPLOSION_HUGE.toString();
                    break;
                case "angryVillager":
                    f = Particle.VILLAGER_ANGRY.toString();
                    break;
                case "cloud":
                    f = Particle.CLOUD.toString();
                    break;
                case "criticalHit":
                    f = Particle.CRIT.toString();
                    break;
                case "mobSpell":
                    f = Particle.SPELL_MOB.toString();
                    break;
                case "enchantmentTable":
                    f = Particle.ENCHANTMENT_TABLE.toString();
                    break;
                case "ender":
                    f = Particle.PORTAL.toString();
                    break;
                case "explode":
                    f = Particle.EXPLOSION_NORMAL.toString();
                    break;
                case "greenSparkle":
                    f = Particle.VILLAGER_HAPPY.toString();
                    break;
                case "heart":
                    f = Particle.HEART.toString();
                    break;
                case "largeExplode":
                    f = Particle.EXPLOSION_LARGE.toString();
                    break;
                case "splash":
                    f = Particle.WATER_SPLASH.toString();
                    break;
                case "largeSmoke":
                    f = Particle.SMOKE_LARGE.toString();
                    break;
                case "lavaSpark":
                    f = Particle.LAVA.toString();
                    break;
                case "magicCriticalHit":
                    f = Particle.CRIT_MAGIC.toString();
                    break;
                case "noteBlock":
                    f = Particle.NOTE.toString();
                    break;
                case "tileDust":
                    f = Particle.BLOCK_DUST.toString();
                    break;
                case "colouredDust":
                    f = Particle.REDSTONE.toString();
                    break;
                case "flame":
                    f = Particle.FLAME.toString();
                    break;
                case "witchMagic":
                    f = Particle.SPELL_WITCH.toString();
                    break;
            }
            if (f != null) {
                displayParticle(f, l, 1.0, data1, data2);
            } else
                l.getWorld().playEffect(l, Effect.MOBSPAWNER_FLAMES, data2);
        } catch (Exception x) {
            //x.printStackTrace();
        }
    }

    private void showEffect() {
        try {
            //GUI Bars And Stuff
            scoreCheck();
            //InfernalMob Stuff
            List<InfernalMob> tmp = new ArrayList<>(infernalList);
            for (InfernalMob m : tmp) {
                final Entity mob = m.getEntity();
                UUID id = mob.getUniqueId();
                int index = idSearch(id);
                if (mob.isValid() && (!mob.isDead()) && (index != -1) && (mob.getLocation().getChunk().isLoaded())) {
                    //System.out.println("PE2");
                    Location feet = mob.getLocation();
                    Location head = mob.getLocation();
                    head.setY(head.getY() + 1);
                    if (getConfig().getBoolean("enableParticles")) {
                        displayEffect(feet, m.getEffect());
                        //mob.getWorld().playEffect(feet, Effect.ENDER_SIGNAL, 1);
                        if (!isSmall(mob)) {
                            displayEffect(head, m.getEffect());
                            //mob.getWorld().playEffect(head, Effect.ENDER_SIGNAL, 1);
                        }
                        if ((mob.getType().equals(EntityType.ENDERMAN)) || (mob.getType().equals(EntityType.IRON_GOLEM))) {
                            head.setY(head.getY() + 1);
                            displayEffect(head, m.getEffect());
                            //mob.getWorld().playEffect(head, Effect.ENDER_SIGNAL, 1);
                        }
                    }
                    //Ability's
                    List<String> abilityList = findMobAbilities(id);
                    //System.out.println("PE1");
                    if (!mob.isDead()) {
                        for (String ability : abilityList) {
                            Random rand = new Random();
                            int min = 1;
                            int max = 10;
                            int randomNum = rand.nextInt(max - min) + min;
                            //System.out.println("PE: " + ability);
                            if (ability.equals("cloaked")) {
                                ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1));
                            } else if (ability.equals("armoured")) {
                                if ((!(mob instanceof Skeleton)) && (!(mob instanceof Zombie))) {
                                    ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 1));
                                }
                            } else if (ability.equals("1up")) {
                                if (((org.bukkit.entity.Damageable) mob).getHealth() <= 5) {
                                    InfernalMob oneUpper = infernalList.get(index);
                                    if (oneUpper.getLives() > 1) {
                                        //System.out.print("1");//-------------------------------Debug
                                       // ((org.bukkit.entity.Damageable) mob).setHealth(((org.bukkit.entity.Damageable) mob).);

                                        //System.out.print("UP!");//-------------------------------Debug
                                        //InfernalMob newMob = new InfernalMob(mob, id, mob.getWorld(), oneUpper.infernal, abilityList, 1, getEffect());
                                        //infernalList.set(index, newMob);
                                    	((LivingEntity) mob).setHealth(((LivingEntity) mob).getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                                        oneUpper.setLives(oneUpper.getLives() - 1);
                                    }
                                }
                            } else if (ability.equals("sprint")) {
                                ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1));
                            } else if (ability.equals("molten")) {
                                ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 1));
                            } else if (ability.equals("tosser")) {
                                if (randomNum < 6) {
                                    double radius = 6D;
                                    ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
                                    for (Player player : near) {
                                        if (player.getLocation().distance(mob.getLocation()) <= radius) {
                                            if ((!player.isSneaking()) && (!player.getGameMode().equals(GameMode.CREATIVE))) {
                                                player.setVelocity(mob.getLocation().toVector().subtract(player.getLocation().toVector()));
                                            }
                                        }
                                    }
                                }
                            } else if (ability.equals("gravity")) {
                                if (randomNum >= 9) {
                                    double radius = 10D;
                                    ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
                                    for (Player player : near) {
                                        if (player.getLocation().distance(mob.getLocation()) <= radius) {
                                            Location feetBlock = player.getLocation();
                                            feetBlock.setY(feetBlock.getY() - 2);
                                            Block block = feetBlock.getWorld().getBlockAt(feetBlock);
                                            if ((!block.getType().equals(Material.AIR)) && (!player.getGameMode().equals(GameMode.CREATIVE))) {
                                                int amount = 6;
                                                if (getConfig().getString("gravityLevitateLength") != null) {
                                                    amount = getConfig().getInt("gravityLevitateLength");
                                                }
                                                levitate(player, amount);
                                            }
                                        }
                                    }
                                }
                            } else if ((ability.equals("ghastly")) || (ability.equals("necromancer"))) {
                                if ((randomNum == 6) && (!mob.isDead())) {
                                    double radius = 20D;
                                    ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
                                    for (Player player : near) {
                                        if ((player.getLocation().distance(mob.getLocation()) <= radius) && (!player.getGameMode().equals(GameMode.CREATIVE))) {
                                            Fireball fb;
                                            if (ability.equals("ghastly")) {
                                                fb = ((LivingEntity) mob).launchProjectile(Fireball.class);
                                                player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_CAVE, 5, 1);
                                            } else {
                                                fb = ((LivingEntity) mob).launchProjectile(WitherSkull.class);
                                            }
                                            //Location loc1 = player.getEyeLocation();
                                            //Location loc2 = mob.getLocation();
                                            //int arrowSpeed = 1;
                                            //loc2.setY(loc2.getBlockY()+2);
                                            //loc2.setX(loc2.getBlockX()+0.5);
                                            //loc2.setZ(loc2.getBlockZ()+0.5);
                                            //Arrow ar = mob.getWorld().spawnArrow(loc2, new Vector(loc1.getX()-loc2.getX(), loc1.getY()-loc2.getY(), loc1.getZ()-loc2.getZ()), arrowSpeed, 12);
                                            //Vector vel = ar.getVelocity();
                                            //fb.setVelocity(vel);
                                            //ar.remove();
                                            moveToward(fb, player.getLocation(), 0.6);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "", ex);
        }
        serverTime = serverTime + 1;
    }

    public boolean isSmall(Entity mob) {
        return (isBaby(mob)) && (mob.getType().equals(EntityType.BAT)) && (mob.getType().equals(EntityType.CAVE_SPIDER)) && (mob.getType().equals(EntityType.CHICKEN)) && (mob.getType().equals(EntityType.COW)) && (mob.getType().equals(EntityType.MUSHROOM_COW)) && (mob.getType().equals(EntityType.PIG)) && (mob.getType().equals(EntityType.OCELOT)) && (mob.getType().equals(EntityType.SHEEP)) && (mob.getType().equals(EntityType.SILVERFISH)) && (mob.getType().equals(EntityType.SPIDER)) && (mob.getType().equals(EntityType.WOLF));
    }

    public void moveToward(final Entity e, final Location to, final double speed) {
        if (e.isDead()) {
            return;
        }
        Vector direction = to.toVector().subtract(e.getLocation().toVector()).normalize();
        e.setVelocity(direction.multiply(speed));
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            try {
                PluginMain.this.moveToward(e, to, speed);
            } catch (Exception ignored) {
            }
        }, 1L);
    }

    public void applyEffect() {
        //Check Players
        for (Player p : this.getServer().getOnlinePlayers()) {
            World world = p.getWorld();
            if (getConfig().getStringList("effectworlds").contains(world.getName()) || (getConfig().getStringList("effectworlds").contains("<all>"))) {
                HashMap<Integer, ItemStack> itemMap = new HashMap<>();
                for (int i : (ArrayList<Integer>) getConfig().getList("enabledCharmSlots", new ArrayList<>())) {
                    ItemStack in;
                    in = p.getInventory().getItem(i);
                    itemMap.put(i, in);
                }
                int ai = 100;
                for (ItemStack ar : p.getInventory().getArmorContents())
                    if (ar != null) {
                        itemMap.put(ai, ar);
                        ai = ai + 1;
                    }
                //for(int i = 0; i < 256; i++){
                ConfigurationSection section = lootFile.getConfigurationSection("potionEffects");
                if (section != null) for (String id : section.getKeys(false))
                    if ((lootFile.getString("potionEffects." + id) != null)
                            && (lootFile.getString("potionEffects." + id + ".attackEffect") == null)
                            && (lootFile.getString("potionEffects." + id + ".attackHelpEffect") == null)
                    ) {
                        List<ItemStack> itemsPlayerHas = new ArrayList<>();
                        for (int neededItemIndex : lootFile.getIntegerList("potionEffects." + id + ".requiredItems")) {
                            ItemStack neededItem = getItem(neededItemIndex);
                            for (Map.Entry<Integer, ItemStack> hm : itemMap.entrySet()) {
                                ItemStack check = hm.getValue();
                                try {
                                    ItemMeta needMeta = neededItem.getItemMeta();
                                    ItemMeta checkMeta = check.getItemMeta();
                                    if (checkMeta == null) continue;
                                    if ((needMeta == null) || (checkMeta.getDisplayName().equals(needMeta.getDisplayName()))) {
                                        if (check.getType().equals(neededItem.getType())) {
                                            if (!isArmor(neededItem) || hm.getKey() >= 100)
                                                itemsPlayerHas.add(neededItem);
                                        }
                                    }
                                } catch (Exception ignored) {}
                            }
                        }

                        if (itemsPlayerHas.size() >= lootFile.getIntegerList("potionEffects." + id + ".requiredItems").size()) {
                            applyEffects(p, Integer.parseInt(id));
                        }
                    }
            }
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, this::applyEffect, (10 * 20));
    }

    private boolean isArmor(ItemStack s) {
        String t = s.getType().toString().toLowerCase();
        return t.contains("helm") || t.contains("plate") || t.contains("leg") || t.contains("boot");
    }

    public void applyEffects(LivingEntity e, int effectID) {
        int level = this.lootFile.getInt("potionEffects." + effectID + ".level");
        String name = this.lootFile.getString("potionEffects." + effectID + ".potion");
        PotionEffectType type = name == null ? null : PotionEffectType.getByName(name);
        if (type != null) {
            if ((type == PotionEffectType.HARM) || (type == PotionEffectType.HEAL)) {
                e.addPotionEffect(new PotionEffect(type, 1, level - 1));
            } else {
                e.addPotionEffect(new PotionEffect(type, 400, level - 1));
            }
        }
        if (this.lootFile.getString("potionEffects." + effectID + ".particleEffect") != null) {
            String effect = this.lootFile.getString("potionEffects." + effectID + ".particleEffect");
            showEffectParticles(e, effect, 15);
        }
    }

    public void applyEatEffects(LivingEntity e, int effectID) {
    	for(String s : this.lootFile.getStringList("consumeEffects." + effectID + ".potionEffects")) {
    		String[] split = s.split(":");
    		String name = split[0];
    		int level = Integer.parseInt(split[1]);
	        int time = Integer.parseInt(split[2]);
	        if ((name.equalsIgnoreCase("fertility")) && (e instanceof Player)) {
	        	fertileList.add(((Player)e));
	        	final Player p = (Player) e;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> fertileList.remove(p), time * 20L);
	        } else {
                PotionEffectType type = PotionEffectType.getByName(name);
                if (type != null) {
                    e.addPotionEffect(new PotionEffect(type, time * 20, level - 1));
                }
            }
    	}
        if (e instanceof Player) {
            e.sendMessage(this.lootFile.getString("consumeEffects." + effectID + ".message", "").replace("&", "§"));
        }
    }

    private void showEffectParticles(final Entity p, final String e, int time) {
        displayEffect(p.getLocation(), e);
        final int nt = time - 1;
        if (time > 0) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> PluginMain.this.showEffectParticles(p, e, nt), 20L);
        }
    }

    private void levitate(final Entity e, final int time) {
        if ((e instanceof LivingEntity)) {
            ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, time * 20, 0));
        }
    }

    public void doEffect(Player player, final Entity mob, boolean playerIsVictom) {
        //Do Player Loot Effects
        if (!playerIsVictom) {
            //Get Player Item In Hand
            ItemStack itemUsed = player.getInventory().getItemInMainHand();
            //Get Player Items
            ArrayList<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                ItemStack in = player.getInventory().getItem(i);
                if (in != null)
                    items.add(in);
            }
            for (ItemStack ar : player.getInventory().getArmorContents())
                if (ar != null)
                    items.add(ar);
            for (int i = 0; i < 256; i++) {
                if (lootFile.getString("potionEffects." + i) != null) {
                    if (lootFile.getString("potionEffects." + i + ".attackEffect") != null) {
                        boolean effectsPlayer = !lootFile.getString("potionEffects." + i + ".attackEffect", "target").equals("target");
                        for (int neededItemIndex : lootFile.getIntegerList("potionEffects." + i + ".requiredItems")) {
                            ItemStack neededItem = getItem(neededItemIndex);
                            try {
                                ItemMeta needMeta = neededItem.getItemMeta();
                                ItemMeta usedMeta = itemUsed.getItemMeta();
                                if (usedMeta == null) continue;
                                if ((needMeta == null) || (usedMeta.getDisplayName().equals(needMeta.getDisplayName()))) {
                                    if (itemUsed.getType().equals(neededItem.getType())) {
                                        //if ((neededItem.getType().getMaxDurability() > 0) || (itemUsed.getDurability() == (neededItem.getDurability()))) {
                                            //Player Using Item
                                            if (effectsPlayer) {
                                                applyEffects(player, i);
                                            } else {
                                                if (mob instanceof LivingEntity)
                                                    applyEffects((LivingEntity) mob, i);
                                            }
                                        //}
                                    }
                                }
                            } catch (Exception e) {/*System.out.println("Error: " + e);**/}
                        }
                    } else if (lootFile.getString("potionEffects." + i + ".attackHelpEffect") != null) {
                        boolean effectsPlayer = !lootFile.getString("potionEffects." + i + ".attackHelpEffect", "target").equals("target");
                        List<ItemStack> itemsPlayerHas = new ArrayList<>();
                        for (int neededItemIndex : lootFile.getIntegerList("potionEffects." + i + ".requiredItems")) {
                            ItemStack neededItem = getItem(neededItemIndex);
                            for (ItemStack check : items) {
                                try {
                                    ItemMeta needMeta = neededItem.getItemMeta();
                                    ItemMeta checkMeta = check.getItemMeta();
                                    if (checkMeta == null) continue;
                                    if ((needMeta == null) || (checkMeta.getDisplayName().equals(needMeta.getDisplayName()))) {
                                        if (check.getType().equals(neededItem.getType())) {
                                            //if ((neededItem.getType().getMaxDurability() > 0) || (check.getDurability() == (neededItem.getDurability()))) {
                                                if (!itemsPlayerHas.contains(neededItem)) {
                                                    itemsPlayerHas.add(neededItem);
                                                }
                                            //}
                                        }
                                    }
                                } catch (Exception e) {/*System.out.println("Error: " + e);**/}
                            }
                        }
                        if (itemsPlayerHas.size() >= lootFile.getIntegerList("potionEffects." + i + ".requiredItems").size()) {
                            //Player Using Item
                            if (effectsPlayer) {
                                applyEffects(player, i);
                            } else {
                                if (mob instanceof LivingEntity)
                                    applyEffects((LivingEntity) mob, i);
                            }
                        }
                    }
                }
            }
        }
        //Do InfernalMob Effects
        try {
            UUID id = mob.getUniqueId();
            if (idSearch(id) != -1) {
                List<String> abilityList = findMobAbilities(id);
                if ((!player.isDead()) && (!mob.isDead())) {
                    for (String ability : abilityList)
                        doMagic(player, mob, playerIsVictom, ability, id);
                }
            }
        } catch (Exception e) {/*System.out.println("Do Effect Error: " + e);**/}
    }

    private void doMagic(Entity vic, Entity atc, boolean playerIsVictom, String ability, UUID id) {
        int min = 1;
        int max = 10;
        int randomNum = new Random().nextInt(max - min) + min;
        if ((atc instanceof Player)) {
            randomNum = 1;
        }
        try {
            if ((atc instanceof Player)) {
                switch (ability) {
                    case "tosser":
                        if ((!(vic instanceof Player)) || ((!((Player) vic).isSneaking()) && (!((Player) vic).getGameMode().equals(GameMode.CREATIVE)))) {
                            vic.setVelocity(atc.getLocation().toVector().subtract(vic.getLocation().toVector()));
                        }
                        break;
                    case "gravity":
                        if ((!(vic instanceof Player)) || ((!((Player) vic).isSneaking()) && (!((Player) vic).getGameMode().equals(GameMode.CREATIVE)))) {
                            Location feetBlock = vic.getLocation();
                            feetBlock.setY(feetBlock.getY() - 2.0D);
                            Block block = feetBlock.getWorld().getBlockAt(feetBlock);
                            if (!block.getType().equals(Material.AIR)) {
                                int amount = 6;
                                if (getConfig().getString("gravityLevitateLength") != null) {
                                    amount = getConfig().getInt("gravityLevitateLength");
                                }
                                levitate(vic, amount);
                            }
                        }
                        break;
                    case "ghastly":
                    case "necromancer":
                        if ((!vic.isDead()) && ((!(vic instanceof Player)) || ((!((Player) vic).isSneaking()) && (!((Player) vic).getGameMode().equals(GameMode.CREATIVE))))) {
                            Fireball fb;
                            if (ability.equals("ghastly")) {
                                fb = ((LivingEntity) atc).launchProjectile(Fireball.class);
                            } else {
                                fb = ((LivingEntity) atc).launchProjectile(WitherSkull.class);
                            }
                            moveToward(fb, vic.getLocation(), 0.6D);
                        }
                        break;
                }
            }
            if (ability.equals("ender")) {
                atc.teleport(vic.getLocation());
            } else if ((ability.equals("poisonous")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
            } else if ((ability.equals("morph")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                try {
                    Entity newEnt;
                    int mc = new Random().nextInt(25) + 1;
                    if (mc != 20) {
                        return;
                    }
                    Location l = atc.getLocation().clone();
                    double h = ((org.bukkit.entity.Damageable) atc).getHealth();
                    List<String> aList = this.infernalList.get(idSearch(id)).getAbilityList();
                    //Remove old
                    double dis = 46.0D;
                    for (Entity e : atc.getNearbyEntities(dis, dis, dis))
                        if (e instanceof Player)
                            GUI.fixBar(((Player) e));
                    atc.teleport(new Location(atc.getWorld(), l.getX(), 0.0D, l.getZ()));
                    atc.remove();
                    this.getLogger().log(Level.INFO, "Entity remove due to Morph");
                    List<String> mList = getConfig().getStringList("enabledmobs");
                    int index = new Random().nextInt(mList.size());
                    String mobName = mList.get(index);

                    newEnt = null;
                    EntityType[] arrayOfEntityType;
                    int j = (arrayOfEntityType = EntityType.values()).length;
                    for (int i = 0; i < j; i++) {
                        EntityType e = arrayOfEntityType[i];
                        try {
                            if ((e.getName() != null) && (e.getName().equalsIgnoreCase(mobName))) {
                                newEnt = vic.getWorld().spawnEntity(l, e);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    if (newEnt == null) {
                        System.out.println("Infernal Mobs can't find mob type: " + mobName + "!");
                        return;
                    }
                    InfernalMob newMob;
                    if (aList.contains("1up")) {
                        newMob = new InfernalMob(newEnt, newEnt.getUniqueId(), true, aList, 2, getEffect());
                    } else {
                        newMob = new InfernalMob(newEnt, newEnt.getUniqueId(), true, aList, 1, getEffect());
                    }
                    if (aList.contains("flying")) {
                        makeFly(newEnt);
                    }
                    this.infernalList.set(idSearch(id), newMob);
                    this.gui.setName(newEnt);

                    giveMobGear(newEnt, true);

                    addHealth(newEnt, aList);
                    if (h >= ((LivingEntity) newEnt).getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()) {
                        return;
                    }
                    ((org.bukkit.entity.Damageable) newEnt).setHealth(h);
                } catch (Exception ex) {
                    getLogger().log(Level.WARNING, "Morph Error: ", ex);
                }
            }
            if ((ability.equals("molten")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                int amount;
                if (getConfig().getString("moltenBurnLength") != null) {
                    amount = getConfig().getInt("moltenBurnLength");
                } else {
                    amount = 5;
                }
                vic.setFireTicks(amount * 20);
            } else if ((ability.equals("blinding")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
            } else if ((ability.equals("confusing")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 2));
            } else if ((ability.equals("withering")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 180, 1));
            } else if ((ability.equals("thief")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                if ((vic instanceof Player)) {
                    if ((!((Player) vic).getInventory().getItemInMainHand().getType().equals(Material.AIR)) && ((randomNum <= 1) || (randomNum == 1))) {
                        vic.getWorld().dropItemNaturally(atc.getLocation(), ((Player) vic).getInventory().getItemInMainHand());
                        int slot = ((Player) vic).getInventory().getHeldItemSlot();
                        ((Player) vic).getInventory().setItem(slot, null);
                    }
                } else if (vic instanceof Zombie || vic instanceof Skeleton) {
                    EntityEquipment eq = ((LivingEntity) vic).getEquipment();
                    if (eq != null) {
                        vic.getWorld().dropItemNaturally(atc.getLocation(), eq.getItemInMainHand());
                        eq.setItemInMainHand(null);
                    }
                }
            } else if ((ability.equals("quicksand")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 180, 1));
            } else if ((ability.equals("bullwark")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                ((LivingEntity) atc).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 500, 2));
            } else if ((ability.equals("rust")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                ItemStack damItem = ((Player) vic).getInventory().getItemInMainHand();
                if (((randomNum <= 3) || (randomNum == 1)) && (damItem.getMaxStackSize() == 1)) {
                    int cDur = ((Damageable)damItem.getItemMeta()).getDamage();
                    ((Damageable)damItem.getItemMeta()).setDamage(cDur + 20);
                }
            } else if ((ability.equals("sapper")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 500, 1), true);
            } else if ((!ability.equals("1up")) || (!isLegitVictim(atc, playerIsVictom, ability))) {
                Location needAir2;
                if ((ability.equals("ender")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                    Location targetLocation = vic.getLocation();
                    if (randomNum >= 8) {
                        Random rand2 = new Random();
                        int min2 = 1;
                        int max2 = 4;
                        int randomNum2 = rand2.nextInt(max2 - min2 + 1) + min2;
                        if (randomNum2 == 1) {
                            targetLocation.setZ(targetLocation.getZ() + 6.0D);
                        } else if (randomNum2 == 2) {
                            targetLocation.setZ(targetLocation.getZ() - 5.0D);
                        } else if (randomNum2 == 3) {
                            targetLocation.setX(targetLocation.getX() + 8.0D);
                        } else if (randomNum2 == 4) {
                            targetLocation.setX(targetLocation.getX() - 10.0D);
                        }
                        needAir2 = targetLocation;
                        needAir2.setY(needAir2.getY() + 1.0D);
                        targetLocation.setY(targetLocation.getY() + 2.0D);
                        if (((targetLocation.getBlock().getType().equals(Material.AIR)) || (targetLocation.getBlock().getType().equals(Material.TORCH))) &&
                                ((needAir2.getBlock().getType().equals(Material.AIR)) || (needAir2.getBlock().getType().equals(Material.TORCH))) && (
                                (targetLocation.getBlock().getType().equals(Material.AIR)) || (targetLocation.getBlock().getType().equals(Material.TORCH)))) {
                            atc.teleport(targetLocation);
                        }
                    }
                } else if ((ability.equals("lifesteal")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                    ((LivingEntity) atc).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 1));
                } else if ((!ability.equals("cloaked")) || (!isLegitVictim(atc, playerIsVictom, ability))) {
                    if ((ability.equals("storm")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                        if (((randomNum <= 2) || (randomNum == 1)) && (!atc.isDead())) {
                            vic.getWorld().strikeLightning(vic.getLocation());
                        }
                    } else if ((!ability.equals("sprint")) || (!isLegitVictim(atc, playerIsVictom, ability))) {
                        if ((ability.equals("webber")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                            if ((randomNum >= 8) || (randomNum == 1)) {
                                Location feet = vic.getLocation();
                                feet.getBlock().setType(Material.COBWEB);
                                setAir(feet, 60);

                                int rNum = new Random().nextInt(max - min) + min;
                                if ((rNum == 5) && (
                                        (atc.getType().equals(EntityType.SPIDER)) || (atc.getType().equals(EntityType.CAVE_SPIDER)))) {
                                    Location l = atc.getLocation();
                                    Block b = l.getBlock();
                                    List<Block> blocks = getSphere(b);
                                    for (Block bl : blocks) {
                                        if (bl.getType().equals(Material.AIR)) {
                                            bl.setType(Material.COBWEB);
                                            setAir(bl.getLocation(), 30);
                                        }
                                    }
                                }
                            }
                        } else if ((ability.equals("vengeance")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                            if ((randomNum >= 5) || (randomNum == 1)) {
                                int amount;
                                if (getConfig().getString("vengeanceDamage") != null) {
                                    amount = getConfig().getInt("vengeanceDamage");
                                } else {
                                    amount = 6;
                                }
                                if ((vic instanceof LivingEntity)) {
                                    ((LivingEntity) vic).damage((int) Math.round(2.0D * amount));
                                }
                            }
                        } else if ((ability.equals("weakness")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                            ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, 1));
                        } else if ((ability.equals("berserk")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                            if ((randomNum >= 5) && (!atc.isDead())) {
                                double health = ((org.bukkit.entity.Damageable) atc).getHealth();
                                ((org.bukkit.entity.Damageable) atc).setHealth(health - 1.0D);
                                int amount;
                                if (getConfig().getString("berserkDamage") != null) {
                                    amount = getConfig().getInt("berserkDamage");
                                } else {
                                    amount = 3;
                                }
                                if ((vic instanceof LivingEntity)) {
                                    ((LivingEntity) vic).damage((int) Math.round(2.0D * amount));
                                }
                            }
                        } else if ((ability.equals("potions")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                            ItemStack iStack = new ItemStack(Material.POTION);
                            PotionMeta potion = (PotionMeta) iStack.getItemMeta();
                            switch (randomNum) {
                                case 5:
                                    potion.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, 2), true);
                                case 6:
                                    potion.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, 1), true);
                                case 7:
                                    potion.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, (20 * 15), 2), true);
                                case 8:
                                    potion.addCustomEffect(new PotionEffect(PotionEffectType.POISON, (20 * 5), 2), true);
                                case 9:
                                    potion.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, (20 * 10), 2), true);
                            }
                            iStack.setItemMeta(potion);
                            Location sploc = atc.getLocation();
                            sploc.setY(sploc.getY() + 3.0D);
                            ThrownPotion thrownPotion = (ThrownPotion) vic.getWorld().spawnEntity(sploc, EntityType.SPLASH_POTION);
                            thrownPotion.setItem(iStack);
                            Vector direction = atc.getLocation().getDirection();
                            direction.normalize();
                            direction.add(new Vector(0.0D, 0.2D, 0.0D));

                            double dist = atc.getLocation().distance(vic.getLocation());

                            dist /= 15.0D;
                            direction.multiply(dist);
                            thrownPotion.setVelocity(direction);
//                }
                        } else if ((ability.equals("mama")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                            if (randomNum == 1) {
                                int amount;
                                if (getConfig().getString("mamaSpawnAmount") != null) {
                                    amount = getConfig().getInt("mamaSpawnAmount");
                                } else {
                                    amount = 3;
                                }
                                if (atc.getType().equals(EntityType.MUSHROOM_COW)) {
                                    for (int i = 0; i < amount; i++) {
                                        MushroomCow minion = (MushroomCow) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.MUSHROOM_COW);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.COW)) {
                                    for (int i = 0; i < amount; i++) {
                                        Cow minion = (Cow) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.COW);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.SHEEP)) {
                                    for (int i = 0; i < amount; i++) {
                                        Sheep minion = (Sheep) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.SHEEP);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.PIG)) {
                                    for (int i = 0; i < amount; i++) {
                                        Pig minion = (Pig) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.PIG);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.CHICKEN)) {
                                    for (int i = 0; i < amount; i++) {
                                        Chicken minion = (Chicken) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.CHICKEN);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.WOLF)) {
                                    for (int i = 0; i < amount; i++) {
                                        Wolf minion = (Wolf) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.WOLF);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.ZOMBIE)) {
                                    for (int i = 0; i < amount; i++) {
                                        Zombie minion = (Zombie) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.ZOMBIE);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.PIGLIN)) {
                                    for (int i = 0; i < amount; i++) {
                                        PigZombie minion = (PigZombie) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.PIGLIN);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.OCELOT)) {
                                    for (int i = 0; i < amount; i++) {
                                        Ocelot minion = (Ocelot) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.OCELOT);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.HORSE)) {
                                    for (int i = 0; i < amount; i++) {
                                        Horse minion = (Horse) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.HORSE);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.VILLAGER)) {
                                    for (int i = 0; i < amount; i++) {
                                        Villager minion = (Villager) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.VILLAGER);
                                        minion.setBaby();
                                    }
                                } else {
                                    for (int i = 0; i < amount; i++) {
                                        atc.getWorld().spawnEntity(atc.getLocation(), atc.getType());
                                    }
                                }
                            }
                        } else if ((ability.equals("archer")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                            if ((randomNum > 7) || (randomNum == 1)) {
                                ArrayList<Arrow> arrowList = new ArrayList();
                                Location loc1 = vic.getLocation();
                                Location loc2 = atc.getLocation();
                                if (!isSmall(atc)) {
                                    loc2.setY(loc2.getY() + 1.0D);
                                }
                                Arrow a = ((LivingEntity) atc).launchProjectile(Arrow.class);
                                int arrowSpeed = 1;
                                loc2.setY(loc2.getBlockY() + 2);
                                loc2.setX(loc2.getBlockX() + 0.5D);
                                loc2.setZ(loc2.getBlockZ() + 0.5D);
                                Arrow a2 = a.getWorld().spawnArrow(loc2, new Vector(loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ()), arrowSpeed, 12.0F);
                                a2.setShooter((LivingEntity) atc);
                                loc2.setY(loc2.getBlockY() + 2);
                                loc2.setX(loc2.getBlockX() - 1);
                                loc2.setZ(loc2.getBlockZ() - 1);
                                Arrow a3 = a.getWorld().spawnArrow(loc2, new Vector(loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ()), arrowSpeed, 12.0F);
                                a3.setShooter((LivingEntity) atc);
                                arrowList.add(a);
                                arrowList.add(a2);
                                arrowList.add(a3);
                                for (Arrow ar : arrowList) {
                                    double minAngle = 6.283185307179586D;
                                    Entity minEntity = null;
                                    for (Entity entity : atc.getNearbyEntities(64.0D, 64.0D, 64.0D)) {
                                        if ((((LivingEntity) atc).hasLineOfSight(entity)) && ((entity instanceof LivingEntity)) && (!entity.isDead())) {
                                            Vector toTarget = entity.getLocation().toVector().clone().subtract(atc.getLocation().toVector());
                                            double angle = ar.getVelocity().angle(toTarget);
                                            if (angle < minAngle) {
                                                minAngle = angle;
                                                minEntity = entity;
                                            }
                                        }
                                    }
                                    if (minEntity != null) {
                                        new ArrowHomingTask(ar, (LivingEntity) minEntity, this);
                                    }
                                }
                            }
                        } else if ((ability.equals("firework")) && (isLegitVictim(atc, playerIsVictom, ability))) {
                            int red = getConfig().getInt("fireworkColour.red");
                            int green = getConfig().getInt("fireworkColour.green");
                            int blue = getConfig().getInt("fireworkColour.blue");
                            ItemStack tmpCol = new ItemStack(Material.LEATHER_HELMET, 1);
                            LeatherArmorMeta tmpCol2 = (LeatherArmorMeta) tmpCol.getItemMeta();
                            tmpCol2.setColor(Color.fromRGB(red, green, blue));

                            Color col = tmpCol2.getColor();
                            launchFirework(atc.getLocation(), col, 1);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static List<Block> getSphere(Block block1) {
        List<Block> blocks = new LinkedList();
        double xi = block1.getLocation().getX() + 0.5D;
        double yi = block1.getLocation().getY() + 0.5D;
        double zi = block1.getLocation().getZ() + 0.5D;
        for (int v1 = 0; v1 <= 90; v1++) {
            double y = Math.sin(0.017453292519943295D * v1) * 4;
            double r = Math.cos(0.017453292519943295D * v1) * 4;
            if (v1 == 90) {
                r = 0.0D;
            }
            for (int v2 = 0; v2 <= 90; v2++) {
                double x = Math.sin(0.017453292519943295D * v2) * r;
                double z = Math.cos(0.017453292519943295D * v2) * r;
                if (v2 == 90) {
                    z = 0.0D;
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi + z)));
                }
            }
        }
        return blocks;
    }

    private void launchFirework(Location l, Color c, int speed) {
        Firework fw = l.getWorld().spawn(l, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(c).with(FireworkEffect.Type.BALL_LARGE).build());
        fw.setFireworkMeta(meta);
        fw.setVelocity(l.getDirection().multiply(speed));
        detonate(fw);
    }

    private void detonate(final Firework fw) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            try {
                fw.detonate();
            } catch (Exception ignored) {
            }
        }, 2L);
    }

    private boolean isLegitVictim(Entity e, boolean playerIsVictom, String ability) {
        if ((e instanceof Player)) {
            return true;
        }
        if (getConfig().getBoolean("effectAllPlayerAttacks")) {
            return true;
        }
        ArrayList<String> attackAbilityList = new ArrayList();
        attackAbilityList.add("poisonous");
        attackAbilityList.add("blinding");
        attackAbilityList.add("withering");
        attackAbilityList.add("thief");
        attackAbilityList.add("sapper");
        attackAbilityList.add("lifesteal");
        attackAbilityList.add("storm");
        attackAbilityList.add("webber");
        attackAbilityList.add("weakness");
        attackAbilityList.add("berserk");
        attackAbilityList.add("potions");
        attackAbilityList.add("archer");
        attackAbilityList.add("confusing");
        if ((playerIsVictom) && (attackAbilityList.contains(ability))) {
            return true;
        }
        ArrayList<String> defendAbilityList = new ArrayList();
        defendAbilityList.add("thief");
        defendAbilityList.add("storm");
        defendAbilityList.add("webber");
        defendAbilityList.add("weakness");
        defendAbilityList.add("potions");
        defendAbilityList.add("archer");
        defendAbilityList.add("quicksand");
        defendAbilityList.add("bullwark");
        defendAbilityList.add("rust");
        defendAbilityList.add("ender");
        defendAbilityList.add("vengeance");
        defendAbilityList.add("mama");
        defendAbilityList.add("firework");
        defendAbilityList.add("morph");
        return (!playerIsVictom) && (defendAbilityList.contains(ability));
    }

    private void setAir(final Location block, int time) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (block.getBlock().getType().equals(Material.COBWEB)) {
                block.getBlock().setType(Material.AIR);
            }
        }, time * 20);
    }

    public List<String> getAbilitiesAmount(Entity e) {
        int power;
        if (getConfig().getBoolean("powerByDistance")) {
            Location l = e.getWorld().getSpawnLocation();
            int m = (int) l.distance(e.getLocation()) / getConfig().getInt("addDistance");
            if (m < 1) {
                m = 1;
            }
            int add = getConfig().getInt("powerToAdd");
            power = m * add;
        } else {
            int min = getConfig().getInt("minpowers");
            int max = getConfig().getInt("maxpowers");
            power = rand(min, max);
        }
        return getAbilities(power);
    }

    private List<String> getAbilities(int amount) {
        List<String> allAbilitiesList = new ArrayList<>(Arrays.asList("confusing", "ghost", "morph", "mounted", "flying", "gravity", "firework", "necromancer", "archer", "molten", "mama", "potions", "explode", "berserk", "weakness", "vengeance", "webber", "storm", "sprint", "lifesteal", "ghastly", "ender", "cloaked", "1up", "sapper", "rust", "bullwark", "quicksand", "thief", "tosser", "withering", "blinding", "armoured", "poisonous"));
        List<String> abilityList = new ArrayList();
        int min = 1;
        for (int i = 0; i < amount; i++) {
            int max = allAbilitiesList.size();
            int randomNum = new Random().nextInt(max - min) + min;
            String ab = allAbilitiesList.get(randomNum);
            if (getConfig().getString(ab) != null) {
                if ((getConfig().getString(ab, "always").equals("always")) || (getConfig().getBoolean(ab))) {
                    abilityList.add(ab);
                    allAbilitiesList.remove(randomNum);
                } else {
                    allAbilitiesList.remove(randomNum);
                    i = i - 1;
                }
            } else
                this.getLogger().log(Level.WARNING, "Ability: " + ab + " is not set!");
        }
        return abilityList;
    }

    public int idSearch(UUID id) {
        InfernalMob idMob = null;
        for (InfernalMob mob : this.infernalList) {
            if (mob.getId().equals(id)) {
                idMob = mob;
            }
        }
        if (idMob != null) {
            return this.infernalList.indexOf(idMob);
        }
        return -1;
    }

    public List<String> findMobAbilities(UUID id) {
        for (InfernalMob mob : this.infernalList) {
            if (mob.getId().equals(id)) {
                return mob.getAbilityList();
            }
        }
        return null;
    }

    public Entity getTarget(final Player player) {

        BlockIterator iterator = new BlockIterator(player.getWorld(), player
                .getLocation().toVector(), player.getEyeLocation()
                .getDirection(), 0, 100);
        while (iterator.hasNext()) {
            Block item = iterator.next();
            for (Entity entity : player.getNearbyEntities(100, 100, 100)) {
                int acc = 2;
                for (int x = -acc; x < acc; x++)
                    for (int z = -acc; z < acc; z++)
                        for (int y = -acc; y < acc; y++)
                            if (entity.getLocation().getBlock()
                                    .getRelative(x, y, z).equals(item)) {
                                return entity;
                            }
            }
        }
        return null;
    }

    public void makeFly(Entity ent) {
        Entity bat = ent.getWorld().spawnEntity(ent.getLocation(), EntityType.BAT);
        bat.setVelocity(new Vector(0, 1, 0));
        //bat.setPassenger(ent);
        bat.addPassenger(ent);
        ((LivingEntity) bat).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1));
    }

    public void giveMobGear(Entity mob, boolean naturalSpawn) {
        UUID mobId = mob.getUniqueId();
        List<String> mobAbilityList = null;
        boolean armoured = false;
        if (idSearch(mobId) != -1) {
            mobAbilityList = findMobAbilities(mobId);
            if (mobAbilityList.contains("armoured")) {
                armoured = true;
                ((LivingEntity) mob).setCanPickupItems(false);
            }
        }
        ItemStack helm = new ItemStack(Material.DIAMOND_HELMET, 1);
        ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        ItemStack pants = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS, 1);
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
        EntityEquipment ee = ((LivingEntity) mob).getEquipment();
        if (mob.getType().equals(EntityType.WITHER_SKELETON)) {
            if (armoured) {
                ee.setHelmetDropChance(0.0F);
                ee.setChestplateDropChance(0.0F);
                ee.setLeggingsDropChance(0.0F);
                ee.setBootsDropChance(0.0F);
                ee.setItemInMainHandDropChance(0.0F);
                ee.setHelmet(helm);
                ee.setChestplate(chest);
                ee.setLeggings(pants);
                ee.setBoots(boots);
                ee.setItemInMainHand(sword);
            }
        } else if (mob.getType().equals(EntityType.SKELETON)) {
            ItemStack bow = new ItemStack(Material.BOW, 1);
            ee.setItemInMainHand(bow);
            if (armoured) {
                ee.setHelmetDropChance(0.0F);
                ee.setChestplateDropChance(0.0F);
                ee.setHelmet(helm);
                ee.setChestplate(chest);
                if (!mobAbilityList.contains("cloaked")) {
                    ee.setLeggingsDropChance(0.0F);
                    ee.setBootsDropChance(0.0F);
                    ee.setLeggings(pants);
                    ee.setBoots(boots);
                }
                ee.setItemInMainHandDropChance(0.0F);
                ee.setItemInMainHand(sword);
            } else if (mobAbilityList.contains("cloaked")) {
                ItemStack skull = new ItemStack(Material.GLASS_BOTTLE, 1);
                ee.setHelmet(skull);
            }
        } else if (mob instanceof Zombie) {
            if (armoured) {
                ee.setHelmetDropChance(0.0F);
                ee.setChestplateDropChance(0.0F);
                ee.setHelmet(helm);
                ee.setChestplate(chest);
                if (!mobAbilityList.contains("cloaked")) {
                    ee.setLeggings(pants);
                    ee.setBoots(boots);
                }
                ee.setLeggingsDropChance(0.0F);
                ee.setBootsDropChance(0.0F);
                ee.setItemInMainHandDropChance(0.0F);
                ee.setItemInMainHand(sword);
            } else if (mobAbilityList.contains("cloaked")) {
                ItemStack skull = new ItemStack(Material.GLASS_BOTTLE);
                //skull.setDurability((short) 2);
                ee.setHelmet(skull);
            }
        }
        if (((mobAbilityList.contains("mounted")) && (getConfig().getStringList("enabledRiders").contains(mob.getType().name()))) || ((!naturalSpawn) && (mobAbilityList.contains("mounted")))) {
            List<String> mounts;

            mounts = getConfig().getStringList("enabledMounts");

            Random randomGenerator = new Random();
            int index = randomGenerator.nextInt(mounts.size());
            String mount = mounts.get(index);
            //Type
            String type = null;
            if (mount.contains(":")) {
                String[] s = mount.split(":");
                mount = s[0];
                type = s[1];
            }
            if (EntityType.fromName(mount) != null && (!EntityType.fromName(mount).equals(EntityType.ENDER_DRAGON))) {
                Entity liveMount = mob.getWorld().spawnEntity(mob.getLocation(), EntityType.fromName(mount));

                this.mountList.put(liveMount, mob);
                liveMount.addPassenger(mob);
                if (liveMount.getType().equals(EntityType.HORSE)) {
                    Horse hm = (Horse) liveMount;
                    if (getConfig().getBoolean("horseMountsHaveSaddles")) {
                        ItemStack saddle = new ItemStack(Material.SADDLE);
                        hm.getInventory().setSaddle(saddle);
                    }
                    hm.setTamed(true);
                    int randomNum3 = rand(1, 7);
                    if (randomNum3 == 1) {
                        hm.setColor(Horse.Color.BLACK);
                    } else if (randomNum3 == 2) {
                        hm.setColor(Horse.Color.BROWN);
                    } else if (randomNum3 == 3) {
                        hm.setColor(Horse.Color.CHESTNUT);
                    } else if (randomNum3 == 4) {
                        hm.setColor(Horse.Color.CREAMY);
                    } else if (randomNum3 == 5) {
                        hm.setColor(Horse.Color.DARK_BROWN);
                    } else if (randomNum3 == 6) {
                        hm.setColor(Horse.Color.GRAY);
                    } else {
                        hm.setColor(Horse.Color.WHITE);
                    }
                    if ((armoured) && (getConfig().getBoolean("armouredMountsHaveArmour"))) {
                        ItemStack armour = new ItemStack(Material.DIAMOND_HORSE_ARMOR, 1);
                        hm.getInventory().setArmor(armour);
                    }
                } else if (liveMount.getType().equals(EntityType.SHEEP)) {
                    Sheep sh = (Sheep) liveMount;
                    if (type != null) {
                        sh.setColor(DyeColor.valueOf(type));
                    }
                }
            } else {
                System.out.println("Can't spawn mount!");
                System.out.println(mount + " is not a valid Entity!");
            }
        }
    }

    private void displayParticle(String effect, Location l, double radius, int speed, int amount) {
        displayParticle(effect, l.getWorld(), l.getX(), l.getY(), l.getZ(), radius, speed, amount);
    }

    public void displayParticle(String effect, World w, double x, double y, double z, double radius, int speed, int amount) {
        amount = (amount <= 0) ? 1 : amount;
        Location l = new Location(w, x, y, z);
        try {
            if (radius <= 0) {
                w.spawnParticle(Particle.valueOf(effect), l, 0, 0, 0, speed, amount);
            } else {
                List<Location> ll = getArea(l, radius, 0.2);
                if (ll.size() > 0){
                    for (int i = 0; i < amount; i++) {
                        int index = new Random().nextInt(ll.size());
                        w.spawnParticle(Particle.valueOf(effect), ll.get(index), 1, 0, 0, 0, 0);
                        ll.remove(index);
                    }
                }
            }
        } catch (Exception ex) {
           // System.out.println("V: " + getServer().getVersion());
           // ex.printStackTrace();
        }
    }

    private List<Location> getArea(Location l, double r, double t) {
        List<Location> ll = new ArrayList();
        for (double x = l.getX() - r; x < l.getX() + r; x += t) {
            for (double y = l.getY() - r; y < l.getY() + r; y += t) {
                for (double z = l.getZ() - r; z < l.getZ() + r; z += t) {
                    ll.add(new Location(l.getWorld(), x, y, z));
                }
            }
        }
        return ll;
    }

    public String getRandomMob() {
        List<String> mobList = getConfig().getStringList("enabledmobs");
        if (mobList.isEmpty()) {
            return "Zombie";
        }
        String mob = mobList.get(rand(1, mobList.size()) - 1);
        if (mob != null) {
            return mob;
        }
        return "Zombie";
    }

    String generateString(int maxNames, List<String> names) {
        StringBuilder namesString = new StringBuilder();
        if (maxNames > names.size()) {
            maxNames = names.size();
        }
        for (int i = 0; i < maxNames; i++) {
            namesString.append(names.get(i)).append(" ");
        }
        if (names.size() > maxNames) {
            namesString.append("... ");
        }
        return namesString.toString();
    }

    private static String translateAbilityToZHCN(String ability) {
        switch (ability) {
            case "mama":
                return "妈妈";
            case "molten":
                return "熔火";
            case "weakness":
                return "虚弱";
            case "vengeance":
                return "复仇";
            case "webber":
                return "织网";
            case "storm":
                return "风暴";
            case "sprint":
                return "疾速";
            case "lifesteal":
                return "吸血鬼";
            case "ghastly":
                return "恶魂";
            case "ender":
                return "末影";
            case "cloaked":
                return "隐匿";
            case "berserk":
                return "狂暴";
            case "1up":
                return "不死图腾";
            case "sapper":
                return "饥荒";
            case "rust":
                return "锈蚀";
            case "bullwark":
                return "壁垒";
            case "quicksand":
                return "流沙";
            case "thief":
                return "盗贼";
            case "tosser":
                return "投掷";
            case "withering":
                return "凋零";
            case "blinding":
                return "失明";
            case "armoured":
                return "装甲";
            case "poisonous":
                return "剧毒";
            case "potions":
                return "女巫";
            case "explode":
                return "临别礼物";
            case "gravity":
                return "失重";
            case "archer":
                return "箭雨";
            case "necromancer":
                return "死灵法师";
            case "firework":
                return "烟花";
            case "flying":
                return "飞行";
            case "mounted":
                return "骑兵";
            case "morph":
                return "变形";
            case "ghost":
                return "亡魂";
            case "confusing":
                return "混乱";
            default:
                return "";
        }
    }

    public String generateStringZHCN(int maxNames, List<String> names) {
        StringBuilder namesString = new StringBuilder();
        if (maxNames > names.size()) {
            maxNames = names.size();
        }
        for (int i = 0; i < maxNames; i++) {
            namesString.append(translateAbilityToZHCN(names.get(i))).append(" ");
        }
        if (names.size() > maxNames) {
            namesString.append("... ");
        }
        return namesString.toString();
    }

    public void reloadLoot() {
        if (this.lootYML == null) {
            this.lootYML = new File(getDataFolder(), "loot.yml");
        }
        this.lootFile = YamlConfiguration.loadConfiguration(this.lootYML);

        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(lootYML);
        this.lootFile.setDefaults(defConfig);
    }

    public String getLocationName(Location l) {
        return (l.getX() + "." + l.getY() + "." + l.getZ() + l.getWorld().getName()).replace(".", "");
    }

    @Nullable
    public Block blockNear(Location l, Material mat, int radius) {
        double xTmp = l.getX();
        double yTmp = l.getY();
        double zTmp = l.getZ();
        int finalX = (int) Math.round(xTmp);
        int finalY = (int) Math.round(yTmp);
        int finalZ = (int) Math.round(zTmp);
        for (int x = finalX - radius; x <= finalX + radius; x++) {
            for (int y = finalY - radius; y <= finalY + radius; y++) {
                for (int z = finalZ - radius; z <= finalZ + radius; z++) {
                    Location loc = new Location(l.getWorld(), x, y, z);
                    Block block = loc.getBlock();
                    if (block.getType().equals(mat)) {
                        return block;
                    }
                }
            }
        }
        return null;
    }

    public boolean cSpawn(CommandSender sender, String mob, Location l, List<String> abList) {
        //cspawn <mob> <world> <x> <y> <z> <ability> <ability>
        if ((EntityType.fromName(mob) != null)) {
            Entity ent = l.getWorld().spawnEntity(l, EntityType.fromName(mob));//
            InfernalMob newMob;
            UUID id = ent.getUniqueId();
            if (abList.contains("1up")) {
                newMob = new InfernalMob(ent, id, true, abList, 2, getEffect());
            } else {
                newMob = new InfernalMob(ent, id, true, abList, 1, getEffect());
            }
            if (abList.contains("flying")) {
                makeFly(ent);
            }
            this.infernalList.add(newMob);
            this.gui.setName(ent);

            giveMobGear(ent, false);
            addHealth(ent, abList);
            return true;
        } else {
            sender.sendMessage("Can't spawn a " + mob + "!");
            return false;
        }
    }

    public int rand(int min, int max) {
        return min + (int) (Math.random() * (1 + max - min));
    }

    public ItemStack getDiviningStaff(){
    	ItemStack s = getItem(Material.BLAZE_ROD, "§6§lDivining Rod", 1, Collections.singletonList("Click to find infernal mobs."));
    	ItemMeta m = s.getItemMeta();
        if (m != null) {
            m.addEnchant(Enchantment.CHANNELING, 1, true);
            s.setItemMeta(m);
        }
    	return s;
    }

    public void addRecipes() {
    	ItemStack staff = getDiviningStaff();
    	NamespacedKey key = new NamespacedKey(this, "divining_staff");
    	ShapedRecipe sr = new ShapedRecipe(key, staff);
		sr.shape("ANA", "ASA", "ASA");
		sr.setIngredient('N', Material.NETHER_STAR);
		sr.setIngredient('S', Material.BLAZE_ROD);
		//sr.setIngredient('A', Material.AIR);
		Bukkit.addRecipe(sr);
    }

    private ItemStack getItem(Material mat, String name, int amount, List<String> loreList){
    	ItemStack item = new ItemStack(mat, amount);
    	ItemMeta m = item.getItemMeta();
        if (m != null) {
            if (name != null)
                m.setDisplayName(name);
            if (loreList != null)
                m.setLore(loreList);
            item.setItemMeta(m);
        }
  	  	return item;
    }

}