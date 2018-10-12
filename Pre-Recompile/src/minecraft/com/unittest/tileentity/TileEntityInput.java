package com.unittest.tileentity;

import modblocks.CommandLoad;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class TileEntityInput extends TileEntityIOBase {
	public IBlockState getBlockState(boolean isPowered) {
		return isPowered ? Blocks.POWERED_INPUT_BLOCK.getDefaultState() : Blocks.UNPOWERED_INPUT_BLOCK.getDefaultState();
	}

	public String[] getCommandLoadArray() {
		return CommandLoad.inputs;
	}
}