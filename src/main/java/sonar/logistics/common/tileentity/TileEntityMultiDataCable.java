package sonar.logistics.common.tileentity;

import java.util.List;

import net.minecraftforge.common.util.ForgeDirection;
import sonar.core.common.tileentity.TileEntitySonar;
import sonar.core.utils.BlockCoords;
import sonar.logistics.api.connecting.IDataCable;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.registries.CableRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityMultiDataCable extends TileEntitySonar implements IDataCable {

	public int registryID = -1;

	@Override
	public boolean isBlocked(ForgeDirection dir) {
		return false;
	}

	public int canRenderConnection(ForgeDirection dir) {
		return CableHelper.canRenderConnection(this, dir);
	}

	public boolean maxRender() {
		return true;
	}

	public void onLoaded() {
		super.onLoaded();
		if (!this.worldObj.isRemote)
			CableHelper.addCable(this);
	}

	public void invalidate() {
		CableHelper.removeCable(this);
		super.invalidate();
	}

	@SideOnly(Side.CLIENT)
	public List<String> getWailaInfo(List<String> currenttip) {
		return currenttip;
	}

	@Override
	public BlockCoords getCoords() {
		return new BlockCoords(this);
	}

	@Override
	public int registryID() {
		return registryID;
	}

	@Override
	public void setRegistryID(int id) {
		this.registryID = id;
	}

	@Override
	public boolean unlimitedChannels() {
		return true;
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return true;
	}

}
