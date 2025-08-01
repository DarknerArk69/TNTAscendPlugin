
package me.yourname.tntascend;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;

public class TNTAscend extends JavaPlugin implements Listener {

    private final String detonatorName = ChatColor.RED + "Detonator";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equals("DarknerArk69")) {
                giveDetonator(p);
            }
        }
    }

    private ItemStack getDetonator() {
        ItemStack item = new ItemStack(Material.FIRE_CHARGE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(detonatorName);
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.setLore(List.of(ChatColor.GRAY + "Right-click to Detonate"));
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void giveDetonator(Player player) {
        boolean hasIt = false;
        for (ItemStack item : player.getInventory()) {
            if (item != null && item.getType() == Material.FIRE_CHARGE && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && detonatorName.equals(meta.getDisplayName())) {
                    hasIt = true;
                    break;
                }
            }
        }
        if (!hasIt) {
            player.getInventory().addItem(getDetonator());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (e.getPlayer().getName().equals("DarknerArk69")) {
            giveDetonator(e.getPlayer());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (e.getPlayer().getName().equals("DarknerArk69")) {
            Bukkit.getScheduler().runTaskLater(this, () -> giveDetonator(e.getPlayer()), 2L);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!p.getName().equals("DarknerArk69")) return;

        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.FIRE_CHARGE) return;
        if (!item.hasItemMeta() || !detonatorName.equals(item.getItemMeta().getDisplayName())) return;

        // Trigger the detonation
        Location loc = p.getLocation();
        loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 10);
        loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 100);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);

        // Damage entities nearby
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 10, 10, 10)) {
            if (entity instanceof LivingEntity && entity != p) {
                ((LivingEntity) entity).damage(100, p);
            }
        }

        // Launch upward
        p.setVelocity(new Vector(0, 2.5, 0));
        p.setMetadata("nofall", new FixedMetadataValue(this, true));

        e.setCancelled(true);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (p.hasMetadata("nofall")) {
                e.setCancelled(true);
                p.removeMetadata("nofall", this);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItemDrop().getItemStack();
        if (p.getName().equals("DarknerArk69") && item.getType() == Material.FIRE_CHARGE &&
            item.hasItemMeta() && detonatorName.equals(item.getItemMeta().getDisplayName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        ItemStack item = e.getCurrentItem();
        if (p.getName().equals("DarknerArk69") && item != null && item.getType() == Material.FIRE_CHARGE &&
            item.hasItemMeta() && detonatorName.equals(item.getItemMeta().getDisplayName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        if (killer != null && killer.getName().equals("DarknerArk69")) {
            e.setDeathMessage(victim.getName() + " got detonated by DarknerArk69.");
        }
    }
}
