package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import sonar.core.SonarCore;
import sonar.core.integration.fmp.FMPHelper;
import sonar.core.integration.fmp.handlers.TileHandler;
import sonar.core.inventory.ContainerSync;
import sonar.core.inventory.slots.SlotList;
import sonar.core.network.PacketTileSync;
import sonar.core.utils.helpers.NBTHelper.SyncType;
import sonar.logistics.common.handlers.FluidReaderHandler;

public class ContainerFluidReader extends ContainerSync {

	private static final int INV_START = 0, INV_END = INV_START + 26, HOTBAR_START = INV_END + 1, HOTBAR_END = HOTBAR_START + 8;
	public FluidReaderHandler handler;

	public ContainerFluidReader(FluidReaderHandler handler, TileEntity entity, InventoryPlayer inventoryPlayer) {
		super(handler, entity);
		this.handler = handler;
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 41 + j * 18, 174 + i * 18));
			}
		}

		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 41 + i * 18, 232));
		}

	}

	@Override
	public void detectAndSendChanges() {
		for (int i = 0; i < this.inventorySlots.size(); ++i) {
			ItemStack itemstack = ((Slot) this.inventorySlots.get(i)).getStack();
			ItemStack itemstack1 = (ItemStack) this.inventoryItemStacks.get(i);

			if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
				itemstack1 = itemstack == null ? null : itemstack.copy();
				this.inventoryItemStacks.set(i, itemstack1);

				for (int j = 0; j < this.crafters.size(); ++j) {
					((ICrafting) this.crafters.get(j)).sendSlotContents(this, i, itemstack1);
				}
			}
		}
		if (sync != null) {
			if (crafters != null) {
				NBTTagCompound tag = new NBTTagCompound();
				TileHandler handler = FMPHelper.getHandler(tile);
				handler.writeData(tag, SyncType.SPECIAL);
				if (tag.hasNoTags()) {
					return;
				}
				for (Object o : crafters) {
					if (o != null && o instanceof EntityPlayerMP) {
						SonarCore.network.sendTo(new PacketTileSync(tile.xCoord, tile.yCoord, tile.zCoord, tag, SyncType.SPECIAL), (EntityPlayerMP) o);

					}
				}

			}

		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(par2);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (par2 >= INV_START) {
				if (!tile.getWorldObj().isRemote) {
					ItemStack copy = itemstack1.copy();
					if (copy != null && copy.getItem() instanceof IFluidContainerItem) {
						IFluidContainerItem container = (IFluidContainerItem) copy.getItem();
						FluidStack stack = container.getFluid(copy);
						if (stack != null) {
							handler.current = stack;
						}
					} else if (copy != null) {
						FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(copy);
						if (fluid != null) {
							handler.current = fluid;
						}
					}
				}
				if (!this.mergeItemStack(itemstack1.copy(), 0, INV_START, false)) {
					return null;
				}
			} else if (par2 >= INV_START && par2 < HOTBAR_START) {
				if (!this.mergeItemStack(itemstack1, HOTBAR_START, HOTBAR_END + 1, false)) {
					return null;
				}
			} else if (par2 >= HOTBAR_START && par2 < HOTBAR_END + 1) {
				if (!this.mergeItemStack(itemstack1, INV_START, INV_END + 1, false)) {
					return null;
				}
			}
			if (itemstack1.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.stackSize == itemstack.stackSize) {
				return null;
			}

			slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
		}

		return itemstack;
	}

	public ItemStack slotClick(int slotID, int buttonID, int flag, EntityPlayer player) {
		Slot targetSlot = slotID < 0 ? null : (Slot) this.inventorySlots.get(slotID);
		if ((targetSlot instanceof SlotList)) {
			if (buttonID == 2) {
				targetSlot.putStack(null);
			} else {
				targetSlot.putStack(player.inventory.getItemStack() == null ? null : player.inventory.getItemStack().copy());
			}
			return player.inventory.getItemStack();
		}
		return super.slotClick(slotID, buttonID, flag, player);
	}

}