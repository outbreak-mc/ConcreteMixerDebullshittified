/*
 * Copyright © 2022-2026, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer.hooks.impl;

import community.leaf.survival.concretemixer.ConcreteMixerPlugin;
import community.leaf.survival.concretemixer.hooks.CauldronAccessHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CauldronLevelChangeEvent;

public class UniversalCauldronAccessHook implements CauldronAccessHook, Listener {
	private final ConcreteMixerPlugin plugin;

	// Universal if plugins cancel cauldron level changes...
	public UniversalCauldronAccessHook(ConcreteMixerPlugin plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void reload() {
		// do nothing...
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleCauldronTestEvent(CauldronLevelChangeEvent e) {
		if (e instanceof LevelChangeTestEvent test)
			test.complete();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isCauldronAccessibleToPlayer(Player player, Block cauldron) {
		LevelChangeTestEvent event = new LevelChangeTestEvent(player, cauldron);
		plugin.getServer().getPluginManager().callEvent(event);
		return event.allowed;
	}

	private static class LevelChangeTestEvent extends CauldronLevelChangeEvent {
		private boolean allowed = false;

		LevelChangeTestEvent(Player player, Block cauldron) {
			super(cauldron, player, ChangeReason.BOTTLE_FILL, cauldron.getState());
		}

		void complete() {
			this.allowed = !isCancelled();
			setCancelled(true);
		}
	}
}
