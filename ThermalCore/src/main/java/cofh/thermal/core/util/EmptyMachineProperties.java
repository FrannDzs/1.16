package cofh.thermal.core.util;

import net.minecraft.nbt.CompoundNBT;

public class EmptyMachineProperties extends MachineProperties {

    public static final EmptyMachineProperties INSTANCE = new EmptyMachineProperties();

    public void resetAttributes() {

    }

    public void setAttributesFromAugment(CompoundNBT augmentData) {

    }

    public void finalizeAttributes() {

    }

}
