package com.unittest.block;

import java.util.Random;
import com.unittest.tileentity.TileEntityIOBase;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockIOBase extends ModBlock implements ITileEntityProvider {
	public final boolean isPowered;

	protected BlockIOBase(boolean powered) {
		super(Material.WOOD);
		setCreativeTab(CreativeTabs.REDSTONE).setResistance(6000000.0F);
		isPowered = powered;
		hasTileEntity = true;
		if(isPowered)
			setLightLevel(1.0F);
	}

	@Override
	public abstract Item getItemDropped(IBlockState state, Random rand, int fortune);

	public boolean isPowered() {
		return isPowered;
	}

	/**
	 * Called when the block is right clicked by a player.
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!worldIn.isRemote) {
			
			TileEntity tileEntity = worldIn.getTileEntity(pos);
			if(tileEntity instanceof TileEntityIOBase) {
				TileEntityIOBase entity = (TileEntityIOBase)tileEntity;
				if (playerIn.isSneaking()) {
					entity.decrementTag();
				}
				else {
					entity.incrementTag();
				}

			}
		}
		return true;
	}

	/**
	 * Called after a player destroys this Block - the posiiton pos may no longer hold the state indicated.
	 */
	@Override
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
		worldIn.removeTileEntity(pos);
	}
}