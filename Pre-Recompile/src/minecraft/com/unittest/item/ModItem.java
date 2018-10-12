package com.unittest.item;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;

public class ModItem extends Item {
	public static void registerItems() {
		registerItemBlock(Blocks.UNPOWERED_INPUT_BLOCK);
		registerItemBlock(Blocks.UNPOWERED_OUTPUT_BLOCK);
	}
}