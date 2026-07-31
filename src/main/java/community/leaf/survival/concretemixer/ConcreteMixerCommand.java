/*
 * Copyright © 2022-2026, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import dev.jorel.commandapi.CommandAPICommand;

public class ConcreteMixerCommand {
	public ConcreteMixerCommand(ConcreteMixerPlugin plugin) {
		new CommandAPICommand("concretemixer")
			.withPermission("concretemixer.admin")
			.withSubcommands(
				new CommandAPICommand("reload")
					.executes((sender, args) -> {
						Config.reload(plugin);
						plugin.hooks().reload();
						sender.sendMessage("Configuration reloaded");
					})
			)
			.register();
	}
}
