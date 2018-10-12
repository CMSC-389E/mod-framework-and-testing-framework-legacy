package com.unittest.tileentity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import modblocks.CommandLoad;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;

public abstract class TileEntityIOBase extends TileEntity implements ITickable {
	private static long lastTickTime;
	public static boolean loaded = false;
	private boolean isPowered;
	protected int index;

	public TileEntityIOBase() {
		if(!loaded) {
			loaded = true;
			try {
				BufferedReader tests = new BufferedReader(new FileReader("tests.txt"));
				tests.readLine();
				String[] things = tests.readLine().split("\t");
				List<String> ins = new LinkedList<>();
				List<String> outs = new LinkedList<>();
				for(String label : things)
					if(label.charAt(0) == 'i')
						ins.add(label);
					else if(label.charAt(0) == 'o')
						outs.add(label);

				CommandLoad.inputs = ins.toArray(new String[0]);
				CommandLoad.outputs = outs.toArray(new String[0]);
				tests.close();
			} catch(IOException e) {
				e.printStackTrace();
			}

		}
		index = 0;
	}

	public void decrementTag() {
		if(CommandLoad.inputs == null || CommandLoad.inputs.length == 0)
			return;
		setIndex(index == 0 ? getCommandLoadArray().length - 1 : --index % getCommandLoadArray().length);
	}

	public abstract IBlockState getBlockState(boolean isPowered);

	public abstract String[] getCommandLoadArray();

	public String getTag() {
		if(CommandLoad.inputs == null || CommandLoad.inputs.length == 0)
			return "EMPTY";
		return getCommandLoadArray()[index % getCommandLoadArray().length];
	}

	@Override
	/**
	 * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
	 * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
	 */
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	public void incrementTag() {
		if(CommandLoad.inputs == null || CommandLoad.inputs.length == 0)
			return;
		setIndex(++index % getCommandLoadArray().length);
	}

	public boolean isPowered() {
		return isPowered;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		index = compound.getInteger("index");
		isPowered = compound.getBoolean("isPowered");
	}

	private void sendTagInChat() {
		Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString("Tag: " + getTag()), true);
	}

	public void setIndex(int index) {
		this.index = index;
		getWorld().notifyBlockUpdate(pos, getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
		markDirty();
	}

	public void setPowerState(boolean powered) {
		isPowered = powered;
		if(!getWorld().isRemote) {
			IBlockState oldState = getWorld().getBlockState(getPos());
			getWorld().setBlockState(getPos(), getBlockState(isPowered()), 3);
			getWorld().notifyBlockUpdate(pos, oldState, getWorld().getBlockState(getPos()), 3);
		}
		markDirty();
	}

	@Override
	public String toString() {
		return getTag();
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	@Override
	public void update() {
		if(!getWorld().isRemote && Minecraft.getMinecraft().player != null) {
			long time = Minecraft.getSystemTime();
			if(time - lastTickTime > Minecraft.getMinecraft().getLimitFramerate()) {
				lastTickTime = Minecraft.getSystemTime();
				RayTraceResult result = Minecraft.getMinecraft().objectMouseOver;
				if(result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
					BlockPos pos = result.getBlockPos();
					TileEntity e = getWorld().getTileEntity(pos);
					if(e instanceof TileEntityIOBase)
						((TileEntityIOBase)e).sendTagInChat();
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("index", index);
		compound.setBoolean("isPowered", isPowered());
		return compound;
	}
}