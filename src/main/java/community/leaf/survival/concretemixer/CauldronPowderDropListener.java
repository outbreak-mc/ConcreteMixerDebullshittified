/*
 * Copyright © 2022-2026, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CauldronPowderDropListener implements Listener {
	private final Map<UUID, Integer> transformationTasksByItemUuid = new HashMap<>();

	private final ConcreteMixerPlugin plugin;

	public CauldronPowderDropListener(ConcreteMixerPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (event.isCancelled()) return;

		Player player = event.getPlayer();
		if (!plugin.permissions().allowsConvertingConcretePowder(player)) {
			return;
		}

		Item item = event.getItemDrop();
		if (Concrete.ofPowder(item.getItemStack().getType()).isEmpty()) {
			return;
		}

		transformConcretePowder(item);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemMerge(ItemMergeEvent event) {
		if (event.isCancelled()) return;

		Item aggregate = event.getTarget();
		Item piece = event.getEntity();

		if (cancelExistingTransformation(aggregate) || cancelExistingTransformation(piece)) {
			// Merge thrower information too.
			if (aggregate.getThrower() == null) {
				aggregate.setThrower(piece.getThrower());
			}

			transformConcretePowder(aggregate);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onCauldronLevelChange(CauldronLevelChangeEvent event) {
		if (event.isCancelled()) return;

		if (!(event.getEntity() instanceof Player player))
			return;

		if (!plugin.permissions().allowsConvertingConcretePowder(player))
			return;

		Block block = event.getBlock();
		BlockData pre = block.getBlockData();
		BlockData post = event.getNewState().getBlockData();

		if (pre.getMaterial() != Material.CAULDRON || post.getMaterial() != Material.WATER_CAULDRON) {
			return;
		}

		block.getWorld().getNearbyEntities(block.getBoundingBox()).stream()
			.map(entity -> ((Item) entity))
			.filter(item -> Concrete.ofPowder(item.getItemStack().getType()).isPresent())
			.filter(item -> !transformationTasksByItemUuid.containsKey(item.getUniqueId()))
			.limit(64L)
			.forEach(this::transformConcretePowder);
	}

	private boolean cancelExistingTransformation(Item item) {
		@Nullable Integer taskId = transformationTasksByItemUuid.remove(item.getUniqueId());

		if (taskId != null) {
			Bukkit.getScheduler().cancelTask(taskId);
			return true;
		}

		return false;
	}

	private void cancel(Item item, Integer id) {
		transformationTasksByItemUuid.remove(item.getUniqueId());
		Bukkit.getScheduler().cancelTask(id);
	}

	private @Nullable Entity entity(@Nullable UUID uuid) {
		return (uuid == null) ? null : plugin.getServer().getEntity(uuid);
	}

	private void transformConcretePowder(Item item) {
		BukkitTask task = new SomeTaskIdk(item, this).runTaskTimer(plugin, 2L, 2L);

		transformationTasksByItemUuid.put(item.getUniqueId(), task.getTaskId());
	}

	class SomeTaskIdk extends BukkitRunnable {
		Item item;
		CauldronPowderDropListener listener;
		IterationCounter iterations = new IterationCounter();

		SomeTaskIdk(Item item, CauldronPowderDropListener listener) {
			this.item = item;
			this.listener = listener;
		}

		@Override
		public void run() {
			Block cauldron = item.getLocation().getBlock();
			Material material = item.getItemStack().getType();

			// Outside the cauldron, dropping in ... (or not)
			if (cauldron.getType() != Material.WATER_CAULDRON) {
				iterations.outside++;

				// Took too long to drop in, might not even be a cauldron nearby for all we know
				if (iterations.outside > 20) {
					listener.cancel(item, getTaskId());
				}

				return;
			}

			// Inside the cauldron
			iterations.inside++;
			boolean lowerWaterLevel = Config.LOWER_WATER_LEVEL;

			// Check if player is allowed to use this specific cauldron
			// (only if water level gets lowered, since that could be considered griefing)
			if (lowerWaterLevel && entity(item.getThrower()) instanceof Player player) {
				if (!plugin.permissions().canAccessCauldron(player, cauldron)) {
					listener.cancel(item, getTaskId());
					return;
				}
			}

			if (iterations.inside == 1) {
				item.setPickupDelay(40);
				plugin.effects().cauldronSplashSound(item.getLocation());
			}

			if (iterations.inside < 15) {
				plugin.effects().cauldronSplashParticles(cauldron);
				return;
			}

			// Done with this task... it's finally time to transform the powder!
			listener.cancel(item, getTaskId());

			@Nullable Concrete concrete = Concrete.ofPowder(material).orElse(null);
			if (concrete == null) {
				return;
			}

			ItemStack stack = item.getItemStack();
			stack.setType(concrete.concrete());
			item.setItemStack(stack);

			item.setVelocity(new Vector(0, 0.3, 0));
			item.setPickupDelay(10);

			plugin.effects().concreteTransform(cauldron);

			if (!lowerWaterLevel) {
				return;
			}
			if (!(cauldron.getBlockData() instanceof Levelled levelled)) {
				return;
			}

			int level = levelled.getLevel() - 1;

			if (level <= 0) {
				cauldron.setType(Material.CAULDRON);
			} else {
				levelled.setLevel(level);
				cauldron.setBlockData(levelled);
			}
		}

		class IterationCounter {
			int outside = 0;
			int inside = 0;
		}
	}
}
