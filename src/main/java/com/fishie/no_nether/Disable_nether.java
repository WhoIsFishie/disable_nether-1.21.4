package com.fishie.no_nether;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Disable_nether implements ModInitializer {
	public static final String MOD_ID = "disable_nether";

	private static final RegistryKey<DamageType> GAME_DESIGN = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of("nonetherportal", "game_design"));

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			BlockState blockState = world.getBlockState(hitResult.getBlockPos());
			// Cancel interaction with obsidian while holding flint and steel
			if (blockState.isOf(Blocks.OBSIDIAN) &&
					player.getStackInHand(hand).isOf(net.minecraft.item.Items.FLINT_AND_STEEL)) {
				player.sendMessage(net.minecraft.text.Text.literal("§cNether is locked."),
						true);

				return ActionResult.FAIL;
			}

			return ActionResult.PASS;
		});

		// Register server tick event
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			// Check all worlds for players
			for (ServerWorld world : server.getWorlds()) {
				for (ServerPlayerEntity player : world.getPlayers()) {
					// Check if player is in nether
					if (player.getWorld().getRegistryKey() == World.NETHER) {
						player.damage(world, world.getDamageSources().create(GAME_DESIGN), Float.MAX_VALUE);
					}

					// Check if player is touching nether portal block
					BlockState blockAtPlayerFeet = world.getBlockState(player.getBlockPos());
					BlockState blockAtPlayerBody = world.getBlockState(player.getBlockPos().up());

					if (blockAtPlayerFeet.isOf(Blocks.NETHER_PORTAL) || blockAtPlayerBody.isOf(Blocks.NETHER_PORTAL)) {
						player.damage(world, world.getDamageSources().create(GAME_DESIGN), Float.MAX_VALUE);
					}
				}
			}
		});
	}
}