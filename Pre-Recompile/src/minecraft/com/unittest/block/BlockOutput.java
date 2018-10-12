package com.unittest.block;

import java.util.Random;
import com.unittest.tileentity.TileEntityIOBase;
import com.unittest.tileentity.TileEntityOutput;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockOutput extends BlockIOBase {
	protected BlockOutput(boolean powered) {
		super(powered);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityOutput();
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(Blocks.UNPOWERED_OUTPUT_BLOCK);
	}

	/**
	 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
	 * block, etc.
	 */
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		((TileEntityIOBase)worldIn.getTileEntity(pos)).setPowerState(worldIn.isBlockPowered(pos));
	}
}