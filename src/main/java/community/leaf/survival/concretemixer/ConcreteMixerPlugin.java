/*
 * Copyright © 2022-2026, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import community.leaf.survival.concretemixer.hooks.HookHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public class ConcreteMixerPlugin extends JavaPlugin {
	private @Nullable EffectHandler effects;
	private @Nullable HookHandler hooks;
	private @Nullable PermissionHandler permissions;

	private static <T> T initialized(@Nullable T thing) {
		if (thing != null) {
			return thing;
		}
		throw new IllegalStateException("Not initialized.");
	}

	@Override
	public void onEnable() {
		Config.reload(this);
		this.effects = new EffectHandler();
		this.hooks = new HookHandler(this);
		this.permissions = new PermissionHandler(this);

		getServer().getPluginManager().registerEvents(new CauldronPowderDropListener(this), this);

		if (Bukkit.getPluginManager().getPlugin("CommandAPI") != null) {
			try {
				Class.forName("community.leaf.survival.concretemixer.ConcreteMixerCommand")
					.getDeclaredConstructor(ConcreteMixerPlugin.class)
					.newInstance(this)
				;
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
			         ClassNotFoundException e) {
				getLogger().warning("Failed to enable `/concretemixer reload` command: " + e.getMessage());
			}
		} else {
			getLogger().warning("Unable to initialize `/concretemixer reload` command because CommandAPI is not installed.");
		}
	}

	public EffectHandler effects() {
		return initialized(effects);
	}

	public HookHandler hooks() {
		return initialized(hooks);
	}

	public PermissionHandler permissions() {
		return initialized(permissions);
	}
}
