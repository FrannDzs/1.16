package cofh.core.tileentity;

import net.minecraft.block.Blocks;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

import static cofh.lib.util.references.CoreReferences.GLOW_AIR_TILE;

public class GlowAirTile extends TileEntity implements ITickableTileEntity {

    protected int duration = 200;

    public GlowAirTile() {

        super(GLOW_AIR_TILE);
    }

    @Override
    public void tick() {

        if (world == null) {
            return;
        }
        if (--duration <= 0) {
            this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
            this.world.removeTileEntity(this.pos);
            this.remove();
        }
    }

    public int getDuration() {

        return duration;
    }

    public void setDuration(int duration) {

        this.duration = duration;
    }

}
