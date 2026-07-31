/*
 * Copyright © 2022-2026, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class Config {
	public static Boolean LOWER_WATER_LEVEL;
	public static Boolean SPLASH_PARTICLES_EFFECT;
	public static @Nullable Sound SPLASH_SOUND_EFFECT = null;
	public static float SPLASH_SOUND_EFFECT_VOLUME = 1.0f;
	public static float SPLASH_SOUND_EFFECT_PITCH = 1.0f;
	public static Boolean TRANSFORM_PARTICLES_EFFECT;
	public static @Nullable Sound TRANSFORM_SOUND_EFFECT = null;
	public static float TRANSFORM_SOUND_EFFECT_VOLUME = 1.0f;
	public static float TRANSFORM_SOUND_EFFECT_PITCH = 1.0f;
	public static Boolean REQUIRE_PERMISSION;

	private static @Nullable Sound parseSound(JavaPlugin plugin, String path) {
		NamespacedKey key = NamespacedKey.fromString(plugin.getConfig().getString(path, ""));
		if (key == null) return null;

		@Nullable Sound sound = Registry.SOUNDS.get(key);
		if (sound == null) return null;

		return sound;
	}

	public static void reload(JavaPlugin plugin) {
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration cnf = plugin.getConfig();

		LOWER_WATER_LEVEL = cnf.getBoolean("cauldrons.lower-water-level", true);

		SPLASH_PARTICLES_EFFECT = cnf.getBoolean("effects.splash.particles", true);

		SPLASH_SOUND_EFFECT = parseSound(plugin, "effects.splash.sound.name");
		SPLASH_SOUND_EFFECT_VOLUME = (float) cnf.getDouble("effects.splash.sound.volume");
		SPLASH_SOUND_EFFECT_PITCH = (float) cnf.getDouble("effects.splash.sound.pitch");

		TRANSFORM_PARTICLES_EFFECT = cnf.getBoolean("effects.transform.particles", true);

		TRANSFORM_SOUND_EFFECT = parseSound(plugin, "effects.transform.sound.name");
		TRANSFORM_SOUND_EFFECT_VOLUME = (float) cnf.getDouble("effects.transform.sound.volume", 1.0d);
		TRANSFORM_SOUND_EFFECT_PITCH = (float) cnf.getDouble("effects.transform.sound.pitch", 1.0d);

		REQUIRE_PERMISSION = cnf.getBoolean("cauldrons.require-permission-node", true);
	}
}
