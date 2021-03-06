package sonar.logistics.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import sonar.core.common.block.SonarMaterials;
import sonar.logistics.Logistics;
import sonar.logistics.common.tileentity.TileEntityInventoryReader;
import sonar.logistics.network.LogisticsGui;

public class BlockInventoryReader extends BlockDirectionalConnector {

	public BlockInventoryReader() {
		super(SonarMaterials.machine);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityInventoryReader();
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public void openGui(World world, int x, int y, int z, EntityPlayer player) {
		if (world.isRemote) {
			return;
		}
		TileEntity target = world.getTileEntity(x, y, z);
		if (!world.isRemote && target != null && target instanceof TileEntityInventoryReader) {
			TileEntityInventoryReader node = (TileEntityInventoryReader) target;
			// if (player.getHeldItem() == null)
			player.openGui(Logistics.instance, LogisticsGui.inventoryReader, world, x, y, z);
			/*
			 * else { ItemStack remove = player.getHeldItem().copy(); StoredItemStack stack = node.handler.insertItem(node, new StoredItemStack(remove)); if (stack == null || stack.stored == 0) { remove = null; } else { remove.stackSize = (int) stack.stored; } if (!ItemStack.areItemStacksEqual(remove, player.getHeldItem())) { player.inventory.setInventorySlotContents(player.inventory.currentItem, remove); } else { player.openGui(Logistics.instance, LogisticsGui.inventoryReader, world, x, y, z); }
			 * 
			 * }
			 */

		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block oldblock, int oldMetadata) {
		world.removeTileEntity(x, y, z);
	}

}
