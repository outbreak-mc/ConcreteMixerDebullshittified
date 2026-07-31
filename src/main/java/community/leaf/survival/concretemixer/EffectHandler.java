/*
 * Copyright © 2022-2026, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

import java.util.concurrent.ThreadLocalRandom;

public class EffectHandler {
	private float fluctuate(float middle) {
		return ThreadLocalRandom.current().nextFloat(middle - 0.125F, middle + 0.125F);
	}

	public void cauldronSplashSound(Location location) {
		World world = location.getWorld();
		if (Config.SPLASH_SOUND_EFFECT == null || world == null) return;

		world.playSound(
			location,
			Config.SPLASH_SOUND_EFFECT,
			Config.SPLASH_SOUND_EFFECT_VOLUME,
			fluctuate(Config.SPLASH_SOUND_EFFECT_PITCH)
		);
	}

	public void cauldronSplashParticles(Block cauldron) {
		if (!Config.SPLASH_PARTICLES_EFFECT) return;
		if (cauldron.getType() != Material.WATER_CAULDRON) return;

		double waterHeight = 0.9 - (0.1875 * (3 - ((Levelled) cauldron.getBlockData()).getLevel()));

		cauldron.getWorld().spawnParticle(
			XParticle.SPLASH.get(),
			cauldron.getLocation().getBlockX() + 0.5,
			cauldron.getLocation().getBlockY() + waterHeight,
			cauldron.getLocation().getBlockZ() + 0.5,
			8,
			0.15,
			0.05,
			0.15
		);
	}

	public void concreteTransformParticles(Block cauldron) {
		if (!Config.TRANSFORM_PARTICLES_EFFECT) return;

		cauldron.getWorld().spawnParticle(
			XParticle.POOF.get(),
			cauldron.getLocation().getBlockX() + 0.5,
			cauldron.getLocation().getBlockY() + 1.0,
			cauldron.getLocation().getBlockZ() + 0.5,
			3,
			0.1,
			0.0,
			0.1,
			0.03
		);
	}

	public void concreteTransformSound(Location location) {
		World world = location.getWorld();
		if (Config.TRANSFORM_SOUND_EFFECT == null || world == null) return;

		world.playSound(
			location,
			Config.TRANSFORM_SOUND_EFFECT,
			Config.TRANSFORM_SOUND_EFFECT_VOLUME,
			fluctuate(Config.TRANSFORM_SOUND_EFFECT_PITCH)
		);
	}

	public void concreteTransform(Block cauldron) {
		concreteTransformParticles(cauldron);
		concreteTransformSound(cauldron.getLocation());
	}
}
