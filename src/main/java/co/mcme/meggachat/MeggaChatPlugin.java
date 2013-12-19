/*  This file is part of MeggaChat.
 * 
 *  MeggaChat is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MeggaChat is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MeggaChat.  If not, see <http://www.gnu.org/licenses/>.
 */
package co.mcme.meggachat;

import co.mcme.meggachat.commands.MeggaChatCommand;
import co.mcme.meggachat.configuration.ChatChannel;
import co.mcme.meggachat.configuration.MeggaChatConfig;
import co.mcme.meggachat.listeners.ColoredListListener;
import co.mcme.meggachat.listeners.ColoredSignListener;
import co.mcme.meggachat.listeners.DispenserListener;
import co.mcme.meggachat.listeners.DroppedItemsListener;
import co.mcme.meggachat.listeners.DupeFlintListener;
import co.mcme.meggachat.listeners.EnchantmentListener;
import co.mcme.meggachat.listeners.FlyingListener;
import co.mcme.meggachat.listeners.ItemUseListener;
import co.mcme.meggachat.listeners.PipeListener;
import co.mcme.meggachat.utilities.Logger;
import co.mcme.meggachat.utilities.Permissions;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import lombok.Getter;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

public class MeggaChatPlugin extends JavaPlugin implements Listener {

    @Getter
    private static Server serverInstance;
    @Getter
    private static MeggaChatPlugin pluginInstance;
    @Getter
    private static File pluginDataFolder;
    @Getter
    private static final ObjectMapper jsonMapper = new ObjectMapper()
            .configure(SerializationConfig.Feature.INDENT_OUTPUT, true)
            .configure(SerializationConfig.Feature.WRITE_EMPTY_JSON_ARRAYS, true);
    @Getter
    private static final String fileSeperator = System.getProperty("file.separator");
    @Getter
    private static MeggaChatConfig conf = new MeggaChatConfig();
    @Getter
    private static final HashMap<String, ChatChannel> channelCommands = new HashMap();
    @Getter
    private static final Permissions permissionsUtil = new Permissions();
    private static final String configVersionExpected = "5.0";

    @Override
    public void onEnable() {
        serverInstance = getServer();
        pluginInstance = this;
        pluginDataFolder = pluginInstance.getDataFolder();
        if (!pluginDataFolder.exists()) {
            pluginDataFolder.mkdirs();
        }
        File config = new File(pluginDataFolder, "config.json");
        if (!config.exists()) {
            writeDefaultConfig(config);
        } else {
            loadConfig(config);
        }
        getCommand("MeggaChat").setExecutor(new MeggaChatCommand());
    }

    public static void registerEvents() {
        if (conf.getFeatures().isAdminchat()) {
            Logger.info("Enabling chat channels.");
            serverInstance.getPluginManager().registerEvents(pluginInstance, pluginInstance);
            for (ChatChannel cc : conf.getChannels()) {
                serverInstance.getPluginManager().registerEvents(cc, pluginInstance);
                channelCommands.put(cc.getCommand(), cc);
            }
        }
        if (conf.getFeatures().isColoredsigns()) {
            Logger.info("Enabling colored signs.");
            serverInstance.getPluginManager().registerEvents(new ColoredSignListener(), pluginInstance);
        }
        if (conf.getFeatures().isDispenserblocking()) {
            Logger.info("Enabling dispenser blocking.");
            serverInstance.getPluginManager().registerEvents(new DispenserListener(), pluginInstance);
        }
        if (conf.getFeatures().isDupeflint()) {
            Logger.info("Enabling dupe flint.");
            serverInstance.getPluginManager().registerEvents(new DupeFlintListener(), pluginInstance);
            int dupecount = conf.getDupeblacklist().toMaterials().size();
            Logger.info("Blocking " + dupecount + " materials from being duped.");
        }
        if (conf.getFeatures().isEnchantmentblocking()) {
            Logger.info("Enabling enchantment blocking.");
            serverInstance.getPluginManager().registerEvents(new EnchantmentListener(), pluginInstance);
        }
        if (conf.getFeatures().isEntityblocking()) {
            Logger.info("Enabling entity blocking.");
            serverInstance.getPluginManager().registerEvents(new DroppedItemsListener(), pluginInstance);
        }
        if (conf.getFeatures().isFlyingpermission()) {
            Logger.info("Enabling flying permission.");
            serverInstance.getPluginManager().registerEvents(new FlyingListener(), pluginInstance);
        }
        if (conf.getFeatures().isItemuseblocking()) {
            Logger.info("Enabling item use blocking.");
            serverInstance.getPluginManager().registerEvents(new ItemUseListener(), pluginInstance);
            int usecount = conf.getItemuseblacklist().toMaterials().size();
            Logger.info("Blocking " + usecount + " materials from being used.");
        }
        if (conf.getFeatures().isPipes()) {
            serverInstance.getPluginManager().registerEvents(new PipeListener(), pluginInstance);
            Logger.info("Enabling pipes.");
        }
        if (conf.getFeatures().isSoundeffects()) {
            Logger.info("Enabling sound effects.");
        }
        if (conf.getFeatures().isTablist()) {
            serverInstance.getPluginManager().registerEvents(new ColoredListListener(), pluginInstance);
            Logger.info("Enabling player list coloring.");
        }
        Logger.info("Registered " + HandlerList.getRegisteredListeners((Plugin) pluginInstance).size() + " event listeners");
    }

    public static void registerEvents(CommandSender sender, boolean verbose) {
        if (!(sender instanceof ConsoleCommandSender)) {
            Logger.info(sender.getName() + " reloaded the config.");
        }
        if (conf.getFeatures().isAdminchat()) {
            if (verbose) {
                sender.sendMessage("Enabling chat channels.");
            }
            serverInstance.getPluginManager().registerEvents(pluginInstance, pluginInstance);
            for (ChatChannel cc : conf.getChannels()) {
                serverInstance.getPluginManager().registerEvents(cc, pluginInstance);
                channelCommands.put(cc.getCommand(), cc);
            }
        }
        if (conf.getFeatures().isColoredsigns()) {
            if (verbose) {
                sender.sendMessage("Enabling colored signs.");
            }
            serverInstance.getPluginManager().registerEvents(new ColoredSignListener(), pluginInstance);
        }
        if (conf.getFeatures().isDispenserblocking()) {
            if (verbose) {
                sender.sendMessage("Enabling dispenser blocking.");
            }
            serverInstance.getPluginManager().registerEvents(new DispenserListener(), pluginInstance);
        }
        if (conf.getFeatures().isDupeflint()) {
            if (verbose) {
                sender.sendMessage("Enabling dupe flint.");
            }
            serverInstance.getPluginManager().registerEvents(new DupeFlintListener(), pluginInstance);
            int dupecount = conf.getDupeblacklist().toMaterials().size();
            if (verbose) {
                sender.sendMessage("Blocking " + dupecount + " materials from being duped.");
            }
        }
        if (conf.getFeatures().isEnchantmentblocking()) {
            if (verbose) {
                sender.sendMessage("Enabling enchantment blocking.");
            }
            serverInstance.getPluginManager().registerEvents(new EnchantmentListener(), pluginInstance);
        }
        if (conf.getFeatures().isEntityblocking()) {
            if (verbose) {
                sender.sendMessage("Enabling entity blocking.");
            }
            serverInstance.getPluginManager().registerEvents(new DroppedItemsListener(), pluginInstance);
        }
        if (conf.getFeatures().isFlyingpermission()) {
            if (verbose) {
                sender.sendMessage("Enabling flying permission.");
            }
            serverInstance.getPluginManager().registerEvents(new FlyingListener(), pluginInstance);
        }
        if (conf.getFeatures().isItemuseblocking()) {
            if (verbose) {
                sender.sendMessage("Enabling item use blocking.");
            }
            serverInstance.getPluginManager().registerEvents(new ItemUseListener(), pluginInstance);
            int usecount = conf.getItemuseblacklist().toMaterials().size();
            if (verbose) {
                sender.sendMessage("Blocking " + usecount + " materials from being used.");
            }
        }
        if (conf.getFeatures().isPipes()) {
            if (verbose) {
                sender.sendMessage("Enabling pipes.");
            }
            serverInstance.getPluginManager().registerEvents(new PipeListener(), pluginInstance);
        }
        if (conf.getFeatures().isSoundeffects()) {
            if (verbose) {
                sender.sendMessage("Enabling sound effects.");
            }
        }
        if (conf.getFeatures().isTablist()) {
            if (verbose) {
                sender.sendMessage("Enabling player list coloring.");
            }
            serverInstance.getPluginManager().registerEvents(new ColoredListListener(), pluginInstance);
        }
        sender.sendMessage("Registered " + HandlerList.getRegisteredListeners((Plugin) pluginInstance).size() + " event listeners");
    }

    @EventHandler
    public void onCommandPre(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().split(" ")[0].replaceAll("/", "");
        if (channelCommands.containsKey(command)) {
            channelCommands.get(command).processCommand(command, event.getMessage(), event.getPlayer());
            event.setCancelled(true);
        }
    }

    public void writeDefaultConfig(File config) {
        InputStream defaultConfigStream = this.getResource("config.json");
        try {
            conf = jsonMapper.readValue(defaultConfigStream, MeggaChatConfig.class);
            jsonMapper.writeValue(config, conf);
        } catch (IOException ex) {
            Logger.severe(ex.getMessage());
        }
    }

    public static void reloadConfig(File config) {
        channelCommands.clear();
        conf = new MeggaChatConfig();
        HandlerList.unregisterAll((Plugin) pluginInstance);
        try {
            conf = jsonMapper.readValue(config, MeggaChatConfig.class);
        } catch (IOException ex) {
            Logger.severe(ex.getMessage());
            return;
        }
        if (conf.getConfigVersion() == null) {
            Logger.severe("Your config version is imcompatible with the current plugin, MeggaCHat has been disabled.");
            Logger.info("To generate a new config, rename the old config, and restart the server.");
            serverInstance.getPluginManager().disablePlugin(pluginInstance);
        } else if (!conf.getConfigVersion().equals(configVersionExpected)) {
            Logger.severe("Your config version is imcompatible with the current plugin, MeggaCHat has been disabled.");
            Logger.info("To generate a new config, rename the old config, and restart the server.");
            serverInstance.getPluginManager().disablePlugin(pluginInstance);
        } else {
            registerEvents();
        }
    }

    public static void reloadConfig(File config, CommandSender sender, boolean verbose) {
        channelCommands.clear();
        conf = new MeggaChatConfig();
        HandlerList.unregisterAll((Plugin) pluginInstance);
        try {
            conf = jsonMapper.readValue(config, MeggaChatConfig.class);
        } catch (IOException ex) {
            Logger.severe(ex.getMessage());
            return;
        }
        if (conf.getConfigVersion() == null) {
            sender.sendMessage("Your config version is imcompatible with the current plugin, MeggaCHat has been disabled.");
            sender.sendMessage("To generate a new config, rename the old config, and restart the server.");
            serverInstance.getPluginManager().disablePlugin(pluginInstance);
        } else if (!conf.getConfigVersion().equals(configVersionExpected)) {
            sender.sendMessage("Your config version is imcompatible with the current plugin, MeggaCHat has been disabled.");
            sender.sendMessage("To generate a new config, rename the old config, and restart the server.");
            serverInstance.getPluginManager().disablePlugin(pluginInstance);
        } else {
            registerEvents(sender, verbose);
        }
    }

    public void loadConfig(File config) {
        try {
            conf = jsonMapper.readValue(config, MeggaChatConfig.class);
        } catch (IOException ex) {
            Logger.severe(ex.getMessage());
            return;
        }
        if (conf.getConfigVersion() == null) {
            Logger.severe("Your config version is imcompatible with the current plugin, MeggaCHat has been disabled.");
            Logger.info("To generate a new config, rename the old config, and restart the server.");
            serverInstance.getPluginManager().disablePlugin(this);
        } else if (!conf.getConfigVersion().equals(configVersionExpected)) {
            Logger.severe("Your config version is imcompatible with the current plugin, MeggaCHat has been disabled.");
            Logger.info("To generate a new config, rename the old config, and restart the server.");
            serverInstance.getPluginManager().disablePlugin(this);
        } else {
            registerEvents();
        }
    }

    public void saveConfig(File config) {
        try {
            jsonMapper.writeValue(config, conf);
        } catch (IOException ex) {
            Logger.severe(ex.getMessage());
        }
    }
}
