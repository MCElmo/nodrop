package me.MC_Elmo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import static org.bukkit.ChatColor.GREEN;

/**
 * Created by Elom on 6/5/16.
 */
public class NoBlockDrop extends JavaPlugin implements Listener
{
    FileConfiguration config = getConfig();
    final String prefix  = "§8[§4No§bBlock§3Drop§8]";
    String title = ChatColor.STRIKETHROUGH + "-----" + ChatColor.RESET + prefix + ChatColor.RESET + ChatColor.STRIKETHROUGH + "-----";
    public void onEnable()
    {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Plugin By MC_Elmo");
    }

    void displayHelp(CommandSender sender){
        sender.sendMessage(title);
        sender.sendMessage(GREEN + "/noblockdrop help : " + ChatColor.DARK_GREEN + "Display Plugin help.");
        if(sender.hasPermission("noblockdrop.reload"))
        {
            sender.sendMessage(GREEN + "/noblockdrop reload : " + ChatColor.DARK_GREEN + "Reload the Config.");
        }
    }
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("noblockdrop") || cmd.getName().equalsIgnoreCase("nbd"))
        {
            if ((args.length == 0))
            {
                displayHelp(sender);
                return true;
            } else if (args.length == 1)
            {
                if (args[0].equalsIgnoreCase("help"))
                {
                    displayHelp(sender);
                    return true;
                }else if(args[0].equalsIgnoreCase("reload"))
                {
                    if(sender.hasPermission("noblockdrop.reload"))
                    {
                        reloadConfig();
                        config = getConfig();
                        saveConfig();
                        sender.sendMessage(prefix + "§bSuccessfully reloaded the config!");
                        return true;
                    }else{
                        sender.sendMessage(prefix + ChatColor.RED + "You Do Not Have Permission To Run this command!");
                    }
                }else{
                    sender.sendMessage(prefix + "§4Incorrect Usage. Try /nbd help.");
                }

            }else {
                sender.sendMessage(prefix + "§4Too many Arguments!");
            }

        }
        return true;
    }

    @EventHandler (ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event)
    {
        if(config.getBoolean("NoBlockDrop.enabled"))
        {
            Player player = event.getPlayer();
            Inventory inv = player.getInventory();
            Block block = event.getBlock();
            String string;
            List<String> blocks = config.getStringList("NoBlockDrop.allowed_blocks");
            int typeID = 0;
            byte data = -1;
            for(String s: blocks)
            {
                if(s == null)
                {
                    break;
                }
                int count = 0;
                for(String str: s.split(":",2))
                {
                    if(count == 0)
                    {
                        typeID = Integer.parseInt(str);
                        count++;
                    }else{
                        data = Byte.parseByte(str);
                    }

                    if(!(data == -1))
                    {
                        if(block.getTypeId() == typeID && block.getData() == data)
                        {
                            return;
                        }
                    }else{
                        if(block.getTypeId() == typeID)
                        {
                            return;
                        }
                    }
                }

            }

            if (player.hasPermission("NoBlockDrop.bypass") || player.getGameMode() == GameMode.CREATIVE)
            {
                return;
            }
            for (ItemStack drop : event.getBlock().getDrops(player.getItemInHand()))
            {

                for (ItemStack stack : inv.getContents()) {
                    if (stack == null) {
                        return;
                    }
                    if (stack.getType().equals(drop.getType())
                            && stack.getAmount() + drop.getAmount() <= 64) {
                        return;
                    }
                }
            }
            String fullmessage = ChatColor.translateAlternateColorCodes('&', config.getString("NoBlockDrop.message"));
            if (config.getString("NoBlockDrop.cancel_break").equalsIgnoreCase("true"))
            {
                event.setCancelled(true);
                player.sendMessage(fullmessage);
                return;
            }
            else if(config.getString("NoBlockDrop.cancel_break").equalsIgnoreCase("false"))
            {
                block.setType(Material.AIR);
                player.sendMessage(fullmessage);
                return;
            }else if(config.getString("NoBlockDrop.cancel_break").equalsIgnoreCase("message"))
            {
                player.sendMessage(fullmessage);
                return;
            }else{
                getLogger().severe(prefix + " Error! Value \"cancel_break\" in config.yml is Invalid!");
                return;
            }
        }

    }

}