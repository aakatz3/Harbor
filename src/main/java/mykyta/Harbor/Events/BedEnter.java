package mykyta.Harbor.Events;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;

import mykyta.Harbor.Config;
import mykyta.Harbor.Util;

public class BedEnter implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Util util = new Util();
        Config config = new Config();
        
        // Block bed entry if enabled
        if (config.getBoolean("features.block")) {
            if (config.getString("messages.chat.blocked").length() > 0) event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.chat.blocked")));	
            util.sendActionbar(event.getPlayer(), config.getString("messages.actionbar.blocked"));
            event.setCancelled(true);
            return;	
        }

        // 1.13.2 API change bypass
        boolean success = false;
        try {
            if (event.getBedEnterResult() == BedEnterResult.OK) success = true;
        }
        catch (NoSuchMethodError e) {
            success = true;
        }

        if (success) {
            Player p = event.getPlayer();
            World w = p.getWorld();
            ArrayList<Player> excluded = util.getExcluded(w);

            if (!excluded.contains(p)) {
                util.add(w, p);

                // Chat messages
                if (config.getBoolean("messages.chat.chat") && (config.getString("messages.chat.sleeping").length() != 0)) {
                    Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.chat.sleeping")
                    .replace("[sleeping]", String.valueOf(util.getSleeping(w))))
                    .replace("[online]", String.valueOf(util.getOnline(w)))
                    .replace("[player]", p.getName())
                    .replace("[needed]", String.valueOf(util.getNeeded(w))));
                }
                if (config.getBoolean("messages.title.title")) {
                    util.sendTitle(p, config.getString("messages.title.sleeping.top"), config.getString("messages.title.sleeping.bottom"));
                }

                // Skip night if possible
                if (!Util.skipping) util.skip(w);
            }
            else if (config.getString("messages.chat.bypass").length() != 0) p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.chat.bypass")));
        }
    }
}