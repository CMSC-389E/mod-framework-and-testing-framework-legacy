package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockRedstoneDiode extends BlockHorizontal {
	protected static final AxisAlignedBB REDSTONE_DIODE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);

	/** Tells whether the repeater is powered or not */
	protected final boolean isRepeaterPowered;

	protected BlockRedstoneDiode(boolean powered) {
		super(Material.CIRCUITS);
		isRepeaterPowered = powered;
	}

	public static boolean isDiode(IBlockState state) {
		return Blocks.UNPOWERED_REPEATER.isSameDiode(state) || Blocks.UNPOWERED_COMPARATOR.isSameDiode(state);
	}

	protected int calculateInputStrength(World worldIn, BlockPos pos, IBlockState state) {
		EnumFacing enumfacing = state.getValue(FACING);
		BlockPos blockpos = pos.offset(enumfacing);
		int i = worldIn.getRedstonePower(blockpos, enumfacing);

		if(i >= 15)
			return i;
		else {
			IBlockState iblockstate = worldIn.getBlockState(blockpos);
			return Math.max(i, iblockstate.getBlock() == Blocks.REDSTONE_WIRE ? iblockstate.getValue(BlockRedstoneWire.POWER).intValue() : 0);
		}
	}

	public boolean canBlockStay(World worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos.down()).isTopSolid();
	}

	/**
	 * Checks if this block can be placed exactly at the given position.
	 */
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos.down()).isTopSolid() ? super.canPlaceBlockAt(worldIn, pos) : false;
	}

	/**
	 * Can this block provide power. Only wire currently seems to have this change based on its state.
	 */
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	public IBlockState duplicateState(Block block, IBlockState state) {
		return block.getDefaultState().withProperty(FACING, state.getValue(FACING)).withProperty(BlockRedstoneRepeater.DELAY, state.getValue(BlockRedstoneRepeater.DELAY)).withProperty(BlockRedstoneRepeater.LOCKED, state.getValue(BlockRedstoneRepeater.LOCKED));
	}

	protected int getActiveSignal(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
		return 15;
	}

	/**
	 * Get the geometry of the queried face at the given position and state. This is used to decide whether things like
	 * buttons are allowed to be placed on the face, or how glass panes connect to the face, among other things.
	 * <p>
	 * Common values are {@code SOLID}, which is the default, and {@code UNDEFINED}, which represents something that
	 * does not fit the other descriptions and will generally cause other things not to connect to the face.

	 * @return an approximation of the form of the given face
	 */
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	/**
	 * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
	 * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
	 */
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return REDSTONE_DIODE_AABB;
	}

	protected abstract int getDelay(IBlockState state);

	protected abstract IBlockState getPoweredState(IBlockState unpoweredState);

	protected int getPowerOnSide(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		IBlockState iblockstate = worldIn.getBlockState(pos);
		Block block = iblockstate.getBlock();

		if(isAlternateInput(iblockstate)) {
			if(block == Blocks.REDSTONE_BLOCK)
				return 15;
			else
				return block == Blocks.REDSTONE_WIRE ? iblockstate.getValue(BlockRedstoneWire.POWER).intValue() : worldIn.getStrongPower(pos, side);
		} else
			return 0;
	}

	protected int getPowerOnSides(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
		EnumFacing enumfacing = state.getValue(FACING);
		EnumFacing enumfacing1 = enumfacing.rotateY();
		EnumFacing enumfacing2 = enumfacing.rotateYCCW();
		return Math.max(getPowerOnSide(worldIn, pos.offset(enumfacing1), enumfacing1), getPowerOnSide(worldIn, pos.offset(enumfacing2), enumfacing2));
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	 * IBlockstate
	 */
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return blockState.getWeakPower(blockAccess, pos, side);
	}

	protected int getTickDelay(IBlockState state) {
		return getDelay(state);
	}

	protected abstract IBlockState getUnpoweredState(IBlockState poweredState);

	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if(!isPowered(blockState))
			return 0;
		else
			return blockState.getValue(FACING) == side ? getActiveSignal(blockAccess, pos, blockState) : 0;
	}

	protected boolean isAlternateInput(IBlockState state) {
		return state.canProvidePower();
	}

	public boolean isAssociatedBlock(Block other) {
		return isSameDiode(other.getDefaultState());
	}

	public boolean isFacingTowardsRepeater(World worldIn, BlockPos pos, IBlockState state) {
		EnumFacing enumfacing = state.getValue(FACING).getOpposite();
		BlockPos blockpos = pos.offset(enumfacing);

		if(isDiode(worldIn.getBlockState(blockpos)))
			return worldIn.getBlockState(blockpos).getValue(FACING) != enumfacing;
		else
			return false;
	}

	public boolean isFullCube(IBlockState state) {
		return false;
	}

	public boolean isLocked(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
		return false;
	}

	/**
	 * Used to determine ambient occlusion and culling when rebuilding chunks for render
	 */
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	protected boolean isPowered(IBlockState state) {
		return isRepeaterPowered;
	}

	public boolean isSameDiode(IBlockState state) {
		Block block = state.getBlock();
		return block == getPoweredState(getDefaultState()).getBlock() || block == getUnpoweredState(getDefaultState()).getBlock();
	}

	/**
	 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
	 * block, etc.
	 */
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if(canBlockStay(worldIn, pos))
			updateState(worldIn, pos, state);
		else {
			dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);

			for(EnumFacing enumfacing : EnumFacing.values())
				worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, false);
		}
	}

	protected void notifyNeighbors(World worldIn, BlockPos pos, IBlockState state) {
		EnumFacing enumfacing = state.getValue(FACING);
		BlockPos blockpos = pos.offset(enumfacing.getOpposite());
		worldIn.neighborChanged(blockpos, this, pos);
		worldIn.notifyNeighborsOfStateExcept(blockpos, this, enumfacing);
	}

	/**
	 * Called after the block is set in the Chunk data, but before the Tile Entity is set
	 */
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		notifyNeighbors(worldIn, pos, state);
	}

	/**
	 * Called after a player destroys this Block - the posiiton pos may no longer hold the state indicated.
	 */
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
		if(isRepeaterPowered)
			for(EnumFacing enumfacing : EnumFacing.values())
				worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, false);

		super.onBlockDestroyedByPlayer(worldIn, pos, state);
	}

	/**
	 * Called by ItemBlocks after a block is set in the world, to allow post-place logic
	 */
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if(shouldBePowered(worldIn, pos, state))
			worldIn.scheduleUpdate(pos, this, 1);
	}

	/**
	 * Called randomly when setTickRandomly is set to true (used by e.g. crops to grow, etc.)
	 */
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {}

	protected boolean shouldBePowered(World worldIn, BlockPos pos, IBlockState state) {
		return calculateInputStrength(worldIn, pos, state) > 0;
	}

	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return side.getAxis() != EnumFacing.Axis.Y;
	}

	protected void updateState(World worldIn, BlockPos pos, IBlockState state) {
		if(!isLocked(worldIn, pos, state)) {
			boolean flag = shouldBePowered(worldIn, pos, state);

			if(isRepeaterPowered != flag && !worldIn.isBlockTickPending(pos, this))
				if(getDelay(state) <= 2)
					worldIn.setBlockState(pos, duplicateState(flag ? Blocks.POWERED_REPEATER : Blocks.UNPOWERED_REPEATER, state));
				else {
					int i = -1;

					if(isFacingTowardsRepeater(worldIn, pos, state))
						i = -3;
					else if(isRepeaterPowered)
						i = -2;

					worldIn.updateBlockTick(pos, this, getDelay(state), i);
				}
		}
	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if(!isLocked(worldIn, pos, state)) {
			boolean flag = shouldBePowered(worldIn, pos, state);

			if(isRepeaterPowered && !flag)
				worldIn.setBlockState(pos, getUnpoweredState(state), 2);
			else if(!isRepeaterPowered) {
				worldIn.setBlockState(pos, getPoweredState(state), 2);

				if(!flag)
					worldIn.updateBlockTick(pos, getPoweredState(state).getBlock(), getTickDelay(state), -1);
			}
		}
	}
}
