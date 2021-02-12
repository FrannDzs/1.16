package cofh.lib.util.constants;

public class NBTTags {

    private NBTTags() {

    }

    public static final String TAG_ACCESSIBLE = "Accessible";
    public static final String TAG_ACTIVE = "Active";
    public static final String TAG_ACTIVE_TRACK = "WasActive";
    public static final String TAG_AMOUNT = "Amount";
    public static final String TAG_AMOUNT_IN = "AmountIn";
    public static final String TAG_AMOUNT_OUT = "AmountOut";
    public static final String TAG_ARROW_DATA = "ArrowData";
    public static final String TAG_ARROWS = "Arrows";
    public static final String TAG_AUGMENTS = "Augments";
    public static final String TAG_AUGMENT_DATA = "AugmentData";
    public static final String TAG_BLOCK_ENTITY = "BlockEntityTag";
    public static final String TAG_BLOCK_STATE = "BlockStateTag";
    public static final String TAG_BOOST_AMP = "BoostAmp";
    public static final String TAG_BOOST_CYCLES = "BoostCycles";
    public static final String TAG_BOOST_DUR = "BoostDur";
    public static final String TAG_BOOST_MAX = "BoostMax";
    public static final String TAG_BOOST_MULT = "BoostMult";
    public static final String TAG_CART_DATA = "CartData";
    public static final String TAG_CAPACITY = "Capacity";
    public static final String TAG_COLORS = "Colors";
    public static final String TAG_COOLANT = "Coolant";
    public static final String TAG_COOLANT_MAX = "CoolantMax";
    public static final String TAG_CREATIVE = "Creative";
    public static final String TAG_ENCHANTMENTS = "Enchantments";
    public static final String TAG_ENTITY = "EntityTag";
    public static final String TAG_ENERGY = "Energy";
    public static final String TAG_ENERGY_MAX = "EnergyMax";
    public static final String TAG_ENERGY_RECV = "EnergyRecv";
    public static final String TAG_ENERGY_SEND = "EnergySend";
    public static final String TAG_FACING = "Facing";
    public static final String TAG_FILTER = "Filter";
    public static final String TAG_FILTER_OPT_LIST = "AllowList";
    public static final String TAG_FILTER_OPT_NBT = "CheckNBT";
    public static final String TAG_FILTER_OPT_TAG = "CheckTag";
    public static final String TAG_FILTER_TYPE = "FilterType";
    public static final String TAG_FLUID = "Fluid";
    public static final String TAG_FLUID_NAME = "FluidName";
    public static final String TAG_FUEL = "Fuel";
    public static final String TAG_FUEL_MAX = "FuelMax";
    public static final String TAG_FUSE = "Fuse";
    public static final String TAG_INDEX = "Index";
    public static final String TAG_INSTANT = "Instant";
    public static final String TAG_ITEM_INV = "ItemInv";
    public static final String TAG_MODE = "Mode";
    public static final String TAG_NAME = "Name";
    public static final String TAG_ORIGIN = "Origin";
    public static final String TAG_POTION = "Potion";
    public static final String TAG_PRIMED = "Primed";
    public static final String TAG_PROCESS = "Proc";
    public static final String TAG_PROCESS_MAX = "ProcMax";
    public static final String TAG_PROCESS_TICK = "ProcTick";
    public static final String TAG_RENDER_FLUID = "RenderFluid";
    public static final String TAG_SECURE = "Secure";
    public static final String TAG_SETTINGS = "Settings";
    public static final String TAG_SKULL_OWNER = "SkullOwner";
    public static final String TAG_SIDES = "Sides";
    public static final String TAG_SLOT = "Slot";
    public static final String TAG_STORED_ENCHANTMENTS = "StoredEnchantments";
    public static final String TAG_TANK = "Tank";
    public static final String TAG_TANK_INV = "TankInv";
    public static final String TAG_TIMER = "Timer";
    public static final String TAG_TRACK_IN = "TrackIn";
    public static final String TAG_TRACK_OUT = "TrackOut";
    public static final String TAG_TYPE = "Type";
    public static final String TAG_UUID = "UUID";
    public static final String TAG_XP = "Xp";
    public static final String TAG_XP_MAX = "XpMax";
    public static final String TAG_XP_TIMER = "XpTimer";

    public static final String TAG_SECURITY = "Security";
    public static final String TAG_SEC_ACCESS = "SecAccess";
    public static final String TAG_SEC_OWNER_NAME = "SecName";
    public static final String TAG_SEC_OWNER_UUID = "SecUUID";

    public static final String TAG_RS_CONTROL = "RSControl";
    public static final String TAG_RS_MODE = "RSMode";
    public static final String TAG_RS_POWER = "RSPower";
    public static final String TAG_RS_THRESHOLD = "RSThreshold";

    public static final String TAG_XFER = "Xfer";
    public static final String TAG_XFER_IN = "XferIn";
    public static final String TAG_XFER_OUT = "XferOut";

    // Compatibility
    public static final String TAG_DEMAGNETIZE_COMPAT = "AllowMachineRemoteMovement";
    public static final String TAG_CONVEYOR_COMPAT = "PreventRemoteMovement";

    // region AUGMENTS
    public static final String TAG_PROPERTIES = "Properties";

    // Types
    public static final String TAG_AUGMENT_TYPE_UPGRADE = "UpgradeAug";
    public static final String TAG_AUGMENT_TYPE_RF = "RFAug";
    public static final String TAG_AUGMENT_TYPE_FLUID = "FluidAug";
    public static final String TAG_AUGMENT_TYPE_MACHINE = "MachineAug";
    public static final String TAG_AUGMENT_TYPE_DYNAMO = "DynamoAug";
    public static final String TAG_AUGMENT_TYPE_AREA_EFFECT = "AreaAug";
    public static final String TAG_AUGMENT_TYPE_FILTER = "FilterAug";
    public static final String TAG_AUGMENT_TYPE_POTION = "PotionAug";

    // General
    public static final String TAG_AUGMENT_BASE_MOD = "BaseMod";

    public static final String TAG_AUGMENT_RF_CREATIVE = "RFCreative";
    public static final String TAG_AUGMENT_RF_STORAGE = "RFMax";
    public static final String TAG_AUGMENT_RF_XFER = "RFXfer";

    public static final String TAG_AUGMENT_FLUID_CREATIVE = "FluidCreative";
    public static final String TAG_AUGMENT_FLUID_STORAGE = "FluidMax";

    public static final String TAG_AUGMENT_ITEM_CREATIVE = "ItemCreative";
    public static final String TAG_AUGMENT_ITEM_STORAGE = "ItemMax";

    public static final String TAG_AUGMENT_AREA_DEPTH = "AreaDepth";
    public static final String TAG_AUGMENT_AREA_RADIUS = "AreaRadius";
    public static final String TAG_AUGMENT_AREA_REACH = "AreaReach";

    public static final String TAG_AUGMENT_FEATURE_CYCLE_PROCESS = "EnableCycleProcess";
    public static final String TAG_AUGMENT_FEATURE_RS_CONTROL = "EnableRSControl";
    public static final String TAG_AUGMENT_FEATURE_SIDE_CONFIG = "EnableReconfig";
    public static final String TAG_AUGMENT_FEATURE_XP_STORAGE = "EnableXpStorage";

    public static final String TAG_AUGMENT_POTION_AMPLIFIER = "PotionAmp";
    public static final String TAG_AUGMENT_POTION_DURATION = "PotionDur";

    // Dynamo-Specific
    public static final String TAG_AUGMENT_DYNAMO_COIL = "DynamoCoil";
    public static final String TAG_AUGMENT_DYNAMO_ENERGY = "DynamoEnergy";
    public static final String TAG_AUGMENT_DYNAMO_POWER = "DynamoPower";

    // Machine-Specific
    public static final String TAG_AUGMENT_MACHINE_MIN_OUTPUT = "MachineMin";
    public static final String TAG_AUGMENT_MACHINE_PRIMARY = "MachinePri";
    public static final String TAG_AUGMENT_MACHINE_SECONDARY = "MachineSec";

    public static final String TAG_AUGMENT_MACHINE_CATALYST = "MachineCat";
    public static final String TAG_AUGMENT_MACHINE_ENERGY = "MachineEnergy";
    public static final String TAG_AUGMENT_MACHINE_POWER = "MachinePower";
    public static final String TAG_AUGMENT_MACHINE_SPEED = "MachineSpeed";
    public static final String TAG_AUGMENT_MACHINE_XP = "MachineXp";
    // endregion
}
