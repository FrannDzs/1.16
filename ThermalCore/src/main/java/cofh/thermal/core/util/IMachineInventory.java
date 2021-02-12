package cofh.thermal.core.util;

/**
 * Empty interface which combines base Thermal Inventory functionality along with a MachineProperties getter.
 */
public interface IMachineInventory extends IThermalInventory {

    MachineProperties getMachineProperties();

}
