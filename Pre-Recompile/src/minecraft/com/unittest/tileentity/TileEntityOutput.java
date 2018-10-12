package com.unittest.tileentity;

import modblocks.CommandLoad;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class TileEntityOutput extends TileEntityIOBase {
	public IBlockState getBlockState(boolean isPowered) {
		return isPowered ? Blocks.POWERED_OUTPUT_BLOCK.getDefaultState() : Blocks.UNPOWERED_OUTPUT_BLOCK.getDefaultState();
	}

	public boolean getResult(boolean expected) {
		return expected == isPowered();
	}

	public String[] getCommandLoadArray() {
		return CommandLoad.outputs;
	}
}