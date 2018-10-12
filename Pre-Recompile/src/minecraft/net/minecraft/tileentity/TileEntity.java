package net.minecraft.tileentity;

import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.unittest.tileentity.TileEntityInput;
import com.unittest.tileentity.TileEntityOutput;
import net.minecraft.block.Block;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public abstract class TileEntity {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final RegistryNamespaced<ResourceLocation, Class<? extends TileEntity>> REGISTRY = new RegistryNamespaced<>();

	static {
		register("furnace", TileEntityFurnace.class);
		register("chest", TileEntityChest.class);
		register("ender_chest", TileEntityEnderChest.class);
		register("jukebox", BlockJukebox.TileEntityJukebox.class);
		register("dispenser", TileEntityDispenser.class);
		register("dropper", TileEntityDropper.class);
		register("sign", TileEntitySign.class);
		register("mob_spawner", TileEntityMobSpawner.class);
		register("noteblock", TileEntityNote.class);
		register("piston", TileEntityPiston.class);
		register("brewing_stand", TileEntityBrewingStand.class);
		register("enchanting_table", TileEntityEnchantmentTable.class);
		register("end_portal", TileEntityEndPortal.class);
		register("beacon", TileEntityBeacon.class);
		register("skull", TileEntitySkull.class);
		register("daylight_detector", TileEntityDaylightDetector.class);
		register("hopper", TileEntityHopper.class);
		register("comparator", TileEntityComparator.class);
		register("flower_pot", TileEntityFlowerPot.class);
		register("banner", TileEntityBanner.class);
		register("structure_block", TileEntityStructure.class);
		register("end_gateway", TileEntityEndGateway.class);
		register("command_block", TileEntityCommandBlock.class);
		register("shulker_box", TileEntityShulkerBox.class);
		register("bed", TileEntityBed.class);

		register("input", TileEntityInput.class);
		register("output", TileEntityOutput.class);
	}
	/** the instance of the world the tile entity is in. */
	protected World world;
	protected BlockPos pos = BlockPos.ORIGIN;
	protected boolean tileEntityInvalid;

	private int blockMetadata = -1;

	/** the Block type that this TileEntity is contained within */
	protected Block blockType;

	@Nullable
	public static TileEntity create(World worldIn, NBTTagCompound compound) {
		TileEntity tileentity = null;
		String s = compound.getString("id");

		try {
			Class<? extends TileEntity> oclass = REGISTRY.getObject(new ResourceLocation(s));

			if(oclass != null)
				tileentity = oclass.newInstance();
		} catch(Throwable throwable1) {
			LOGGER.error("Failed to create block entity {}", s, throwable1);
		}

		if(tileentity != null)
			try {
				tileentity.setWorldCreate(worldIn);
				tileentity.readFromNBT(compound);
			} catch(Throwable throwable) {
				LOGGER.error("Failed to load data for block entity {}", s, throwable);
				tileentity = null;
			}
		else
			LOGGER.warn("Skipping BlockEntity with id {}", s);

		return tileentity;
	}

	@Nullable
	public static ResourceLocation getKey(Class<? extends TileEntity> clazz) {
		return REGISTRY.getNameForObject(clazz);
	}

	private static void register(String id, Class<? extends TileEntity> clazz) {
		REGISTRY.putObject(new ResourceLocation(id), clazz);
	}

	public void addInfoToCrashReport(CrashReportCategory reportCategory) {
		reportCategory.addDetail("Name", () -> TileEntity.REGISTRY.getNameForObject(TileEntity.this.getClass()) + " // " + TileEntity.this.getClass().getCanonicalName());

		if(world != null) {
			CrashReportCategory.addBlockInfo(reportCategory, pos, getBlockType(), getBlockMetadata());
			reportCategory.addDetail("Actual block type", () -> {
				int i = Block.getIdFromBlock(world.getBlockState(pos).getBlock());

				try {
					return String.format("ID #%d (%s // %s)", i, Block.getBlockById(i).getUnlocalizedName(), Block.getBlockById(i).getClass().getCanonicalName());
				} catch(Throwable var3) {
					return "ID #" + i;
				}
			});
			reportCategory.addDetail("Actual block data value", () -> {
				IBlockState iblockstate = world.getBlockState(pos);
				int i = iblockstate.getBlock().getMetaFromState(iblockstate);

				if(i < 0)
					return "Unknown? (Got " + i + ")";
				else {
					String s = String.format("%4s", Integer.toBinaryString(i)).replace(" ", "0");
					return String.format("%1$d / 0x%1$X / 0b%2$s", i, s);
				}
			});
		}
	}

	public int getBlockMetadata() {
		if(blockMetadata == -1) {
			IBlockState iblockstate = world.getBlockState(pos);
			blockMetadata = iblockstate.getBlock().getMetaFromState(iblockstate);
		}

		return blockMetadata;
	}

	/**
	 * Gets the block type at the location of this entity (client-only).
	 */
	public Block getBlockType() {
		if(blockType == null && world != null)
			blockType = world.getBlockState(pos).getBlock();

		return blockType;
	}

	@Nullable

	/**
	 * Get the formatted ChatComponent that will be used for the sender's username in chat
	 */
	public ITextComponent getDisplayName() {
		return null;
	}

	/**
	 * Returns the square of the distance between this entity and the passed in coordinates.
	 */
	public double getDistanceSq(double x, double y, double z) {
		double d0 = pos.getX() + 0.5D - x;
		double d1 = pos.getY() + 0.5D - y;
		double d2 = pos.getZ() + 0.5D - z;
		return d0 * d0 + d1 * d1 + d2 * d2;
	}

	public double getMaxRenderDistanceSquared() {
		return 4096.0D;
	}

	public BlockPos getPos() {
		return pos;
	}

	@Nullable

	/**
	 * Retrieves packet to send to the client whenever this Tile Entity is resynced via World.notifyBlockUpdate. For
	 * modded TE's, this packet comes back to you clientside in {@link #onDataPacket}
	 */
	public SPacketUpdateTileEntity getUpdatePacket() {
		return null;
	}

	/**
	 * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
	 * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
	 */
	public NBTTagCompound getUpdateTag() {
		return writeInternal(new NBTTagCompound());
	}

	/**
	 * Returns the worldObj for this tileEntity.
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Returns true if the worldObj isn't null.
	 */
	public boolean hasWorld() {
		return world != null;
	}

	/**
	 * invalidates a tile entity
	 */
	public void invalidate() {
		tileEntityInvalid = true;
	}

	public boolean isInvalid() {
		return tileEntityInvalid;
	}

	/**
	 * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
	 * hasn't changed and skip it.
	 */
	public void markDirty() {
		if(world != null) {
			IBlockState iblockstate = world.getBlockState(pos);
			blockMetadata = iblockstate.getBlock().getMetaFromState(iblockstate);
			world.markChunkDirty(pos, this);

			if(getBlockType() != Blocks.AIR)
				world.updateComparatorOutputLevel(pos, getBlockType());
		}
	}

	public void mirror(Mirror mirrorIn) {}

	public boolean onlyOpsCanSetNbt() {
		return false;
	}

	public void readFromNBT(NBTTagCompound compound) {
		pos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
	}

	/**
	 * See {@link Block#eventReceived} for more information. This must return true serverside before it is called
	 * clientside.
	 */
	public boolean receiveClientEvent(int id, int type) {
		return false;
	}

	public void rotate(Rotation rotationIn) {}

	public void setPos(BlockPos posIn) {
		pos = posIn.toImmutable();
	}

	/**
	 * Sets the worldObj for this tileEntity.
	 */
	public void setWorld(World worldIn) {
		world = worldIn;
	}

	protected void setWorldCreate(World worldIn) {}

	public void updateContainingBlockInfo() {
		blockType = null;
		blockMetadata = -1;
	}

	/**
	 * validates a tile entity
	 */
	public void validate() {
		tileEntityInvalid = false;
	}

	private NBTTagCompound writeInternal(NBTTagCompound compound) {
		ResourceLocation resourcelocation = REGISTRY.getNameForObject(this.getClass());

		if(resourcelocation == null)
			throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
		else {
			compound.setString("id", resourcelocation.toString());
			compound.setInteger("x", pos.getX());
			compound.setInteger("y", pos.getY());
			compound.setInteger("z", pos.getZ());
			return compound;
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		return writeInternal(compound);
	}
}
