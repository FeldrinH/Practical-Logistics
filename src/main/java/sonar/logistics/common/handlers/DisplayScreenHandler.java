package sonar.logistics.common.handlers;

import io.netty.buffer.ByteBuf;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import sonar.core.SonarCore;
import sonar.core.integration.fmp.FMPHelper;
import sonar.core.integration.fmp.handlers.TileHandler;
import sonar.core.network.utils.IByteBufTile;
import sonar.core.utils.BlockCoords;
import sonar.core.utils.helpers.NBTHelper.SyncType;
import sonar.logistics.Logistics;
import sonar.logistics.api.Info;
import sonar.logistics.api.StandardInfo;
import sonar.logistics.api.connecting.IInfoEmitter;
import sonar.logistics.api.connecting.IInfoReader;
import sonar.logistics.common.tileentity.TileEntityInventoryReader;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.helpers.InfoHelper;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class DisplayScreenHandler extends TileHandler implements IByteBufTile {

	public Info info;

	public DisplayScreenHandler(boolean isMultipart, TileEntity tile) {
		super(isMultipart, tile);
	}

	@Override
	public void update(TileEntity te) {
		if (!te.getWorldObj().isRemote) {
			this.updateData(te, te, ForgeDirection.getOrientation(FMPHelper.getMeta(te)));
		}
	}

	public void updateData(TileEntity te, TileEntity packetTile, ForgeDirection dir) {
		List<BlockCoords> connections = CableHelper.getConnections(te, dir.getOpposite());
		if (!connections.isEmpty() && connections.get(0) != null) {
			Object target = FMPHelper.getTile(connections.get(0).getTileEntity());
			if (target == null) {
				return;
			} else {
				Info lastInfo = info;
				if (target instanceof IInfoReader) {
					IInfoReader infoReader = (IInfoReader) target;
					if (infoReader.currentInfo() != null && infoReader.getSecondaryInfo() != null) {
						this.info = InfoHelper.combineData(infoReader.currentInfo(), infoReader.getSecondaryInfo());
					} else if (infoReader.currentInfo() != null) {
						this.info = infoReader.currentInfo();
					} else if (this.info != null) {
						this.info.emptyData();
					}
				} else if (target instanceof TileEntityInventoryReader) {
					TileEntityInventoryReader infoNode = (TileEntityInventoryReader) target;
					if (infoNode.currentInfo() != null) {
						this.info = infoNode.currentInfo();
					} else if (this.info != null) {
						this.info.emptyData();
					}
				} else if (target instanceof IInfoEmitter) {
					IInfoEmitter infoNode = (IInfoEmitter) target;
					if (infoNode.currentInfo() != null) {
						this.info = infoNode.currentInfo();
					} else if (this.info != null) {
						this.info.emptyData();
					}

				}

				if (info != null) {
					if (!info.isEqualType(lastInfo) || !info.isDataEqualType(lastInfo)) {
						if (info instanceof StandardInfo && info.isEqualType(lastInfo)) {
							SonarCore.sendPacketAround(packetTile, 64, 0);
						} else {
							SonarCore.sendPacketAround(packetTile, 64, 0);
						}
					}
				} else {
					SonarCore.sendPacketAround(packetTile, 64, 0);
				}
			}
		}
	}

	public Info currentInfo() {
		return info;
	}

	public boolean canConnect(TileEntity te, ForgeDirection dir) {
		return dir.equals(ForgeDirection.getOrientation(FMPHelper.getMeta(te)).getOpposite());
	}

	@SideOnly(Side.CLIENT)
	public List<String> getWailaInfo(List<String> currenttip) {
		if (info != null) {
			currenttip.add("Current Data: " + info.getDisplayableData());
		}
		return currenttip;
	}

	@Override
	public void removed(World world, int x, int y, int z, int meta) {

	}

	@Override
	public void writePacket(ByteBuf buf, int id) {
		if (id == 0) {
			if (info != null) {
				buf.writeBoolean(true);
				InfoHelper.writeInfo(buf, info);
			} else {
				buf.writeBoolean(false);
			}
		}
		if (id == 1) {
			ByteBufUtils.writeUTF8String(buf, info.getData());
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		if (id == 0) {
			if (buf.readBoolean()) {
				info = Logistics.infoTypes.readFromBuf(buf);
			} else {
				info = null;
			}
		}
		if (id == 1) {
			StandardInfo standardInfo = (StandardInfo) info;
			standardInfo.setData(ByteBufUtils.readUTF8String(buf));
		}
	}

	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		if (type == SyncType.SAVE) {
			if (nbt.hasKey("currentInfo")) {
				info = Logistics.infoTypes.readFromNBT(nbt.getCompoundTag("currentInfo"));
			}
		}
	}

	public void writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		if (type == SyncType.SAVE) {
			if (info != null) {
				NBTTagCompound infoTag = new NBTTagCompound();
				Logistics.infoTypes.writeToNBT(infoTag, info);
				nbt.setTag("currentInfo", infoTag);
			}
		}
	}
}
