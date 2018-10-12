package com.unittest.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class ModBlock extends Block {
	protected ModBlock(Material materialIn) {
		super(materialIn);
	}

	public static void registerBlocks() {
		registerBlock(257, "unpowered_input_block", new BlockInput(false).setUnlocalizedName("inputBlock"));
		registerBlock(256, "powered_input_block", new BlockInput(true).setUnlocalizedName("inputBlock"));
		registerBlock(259, "unpowered_output_block", new BlockOutput(false).setUnlocalizedName("outputBlock"));
		registerBlock(258, "powered_output_block", new BlockOutput(true).setUnlocalizedName("outputBlock"));
	}
}