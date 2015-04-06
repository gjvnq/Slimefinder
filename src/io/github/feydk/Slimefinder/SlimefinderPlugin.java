package io.github.feydk.Slimefinder;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import java.util.Random;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class SlimefinderPlugin extends JavaPlugin implements Listener
{
	private String wandBlock = "SLIME_BALL";
	private Material wandMaterial = null;

	@Override
	public void onEnable()
	{
		reloadConfig();
		wandBlock = getConfig().getString("wand-block", "SLIME_BALL");
		wandMaterial = Material.getMaterial(wandBlock);
		getConfig().options().copyDefaults(true);
		saveConfig();

		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable()
	{
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[])
	{
		Player player = null;

		if(sender instanceof Player)
			player = (Player)sender;

		if(player == null)
		{
			sender.sendMessage("Player expected");
			return true;
		}

		if (args.length > 1)
			return false;

		Chunk chunk = player.getWorld().getChunkAt(player.getLocation());
		int x = chunk.getX();
		int z = chunk.getZ();

		boolean isSlimy = isSlimeChunk(player, x, z);

		revealResult(isSlimy, player);

		return true;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	void onPlayerInteract(PlayerInteractEvent event)
	{
		Action action = event.getAction();

		if(action == Action.LEFT_CLICK_AIR || action == Action.PHYSICAL)
			return;

		Player player = event.getPlayer();
		ItemStack itemInHand = player.getItemInHand();
		Material materialInHand = itemInHand.getType();
		Block clickedBlock = event.getClickedBlock();
		Material clickedBlockType = null;

		if(clickedBlock != null)
			clickedBlockType = clickedBlock.getType();
		else
			clickedBlockType = Material.AIR;

		if(action == Action.LEFT_CLICK_BLOCK && clickedBlock != null && materialInHand == wandMaterial)
		{
			Chunk chunk = clickedBlock.getChunk();
			int x = chunk.getX();
			int z = chunk.getZ();

			boolean isSlimy = isSlimeChunk(player, x, z);

			revealResult(isSlimy, player);

			event.setCancelled(true);
		}
	}

	private void revealResult(boolean isSlimy, Player player)
	{
		if(isSlimy)
			player.sendMessage(" " + ChatColor.GREEN + "✔" + ChatColor.AQUA + " This " + ChatColor.UNDERLINE + "is" + ChatColor.RESET + ChatColor.AQUA + " a slime chunk." + ChatColor.RESET + "");
		else
			player.sendMessage(" " + ChatColor.RED + "✘" + ChatColor.AQUA + " This is " + ChatColor.UNDERLINE + "not" + ChatColor.RESET + ChatColor.AQUA + " a slime chunk." + ChatColor.RESET + "");
	}

	private boolean isSlimeChunk(Player player, int x, int z)
	{
		long seed = player.getWorld().getSeed();

		Random rnd = new Random(seed + (long) (x * x * 0x4c1906) + (long) (x * 0x5ac0db) + (long) (z * z) * 0x4307a7L + (long) (z * 0x5f24f) ^ 0x3ad8025f);

		return rnd.nextInt(10) == 0;
	}
}