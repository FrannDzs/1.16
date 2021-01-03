package cofh.core.block;

import cofh.core.tileentity.TileCoFH;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;

import java.util.function.Supplier;

import static cofh.lib.util.constants.Constants.ACTIVE;

public class TileBlockActive extends TileBlockCoFH {

    public TileBlockActive(Properties builder, Supplier<? extends TileCoFH> supplier) {

        super(builder, supplier);
        this.setDefaultState(this.stateContainer.getBaseState().with(ACTIVE, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {

        super.fillStateContainer(builder);
        builder.add(ACTIVE);
    }

}
