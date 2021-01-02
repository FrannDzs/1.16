package cofh.thermal.cultivation.item;

import cofh.core.item.FluidContainerItem;
import cofh.core.item.IAugmentableItem;
import cofh.core.item.IMultiModeItem;
import cofh.core.util.ChatHelper;
import cofh.core.util.ProxyUtils;
import cofh.core.util.RayTracer;
import cofh.core.util.Utils;
import cofh.core.util.helpers.AugmentDataHelper;
import cofh.thermal.core.common.ThermalConfig;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

import static cofh.core.util.constants.Constants.BUCKET_VOLUME;
import static cofh.core.util.constants.Constants.RGB_DURABILITY_WATER;
import static cofh.core.util.constants.NBTTags.*;
import static cofh.core.util.helpers.AugmentableHelper.*;
import static cofh.core.util.helpers.FluidHelper.IS_WATER;
import static cofh.core.util.helpers.FluidHelper.isWater;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class WateringCanItem extends FluidContainerItem implements IAugmentableItem, IMultiModeItem {

    protected static final int MB_PER_USE = 50;

    protected IntSupplier numSlots = () -> ThermalConfig.toolAugments;
    protected Predicate<ItemStack> augValidator = (e) -> true;

    protected static boolean allowFakePlayers = false;
    protected static boolean removeSourceBlocks = true;

    protected static final Set<Block> EFFECTIVE_BLOCKS = new ObjectOpenHashSet<>();

    static {
        EFFECTIVE_BLOCKS.add(Blocks.MYCELIUM);
        EFFECTIVE_BLOCKS.add(Blocks.CHORUS_FLOWER);
    }

    public WateringCanItem(Properties builder, int fluidCapacity) {

        super(builder, fluidCapacity, IS_WATER);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("filled"), (stack, world, entity) -> getFluidAmount(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getFluidAmount(stack) > 0 && hasActiveTag(stack) ? 1F : 0F);
    }

    public WateringCanItem setNumSlots(IntSupplier numSlots) {

        this.numSlots = numSlots;
        return this;
    }

    public WateringCanItem setAugValidator(Predicate<ItemStack> augValidator) {

        this.augValidator = augValidator;
        return this;
    }

    @Override
    protected void tooltipDelegate(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        int radius = getMode(stack) * 2 + 1;
        if (radius <= 1) {
            tooltip.add(new TranslationTextComponent("info.cofh.single_block").mergeStyle(TextFormatting.ITALIC));
        } else {
            tooltip.add(new TranslationTextComponent("info.cofh.area").appendString(": " + radius + "x" + radius).mergeStyle(TextFormatting.ITALIC));
        }
        if (getNumModes(stack) > 1) {
            addIncrementModeChangeTooltip(stack, worldIn, tooltip, flagIn);
        }
        super.tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {

        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged) && (slotChanged || getFluidAmount(oldStack) > 0 != getFluidAmount(newStack) > 0 || getFluidAmount(newStack) > 0 && hasActiveTag(oldStack) != hasActiveTag(newStack));
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {

        return RGB_DURABILITY_WATER;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {

        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();

        if (player == null || Utils.isFakePlayer(player) && !allowFakePlayers) {
            return ActionResultType.FAIL;
        }
        if (player.isSecondaryUseActive()) {
            BlockRayTraceResult traceResult = RayTracer.retrace(player, RayTraceContext.FluidMode.SOURCE_ONLY);
            if (traceResult.getType() != RayTraceResult.Type.MISS) {
                if (isWater(world.getBlockState(traceResult.getPos()))) {
                    return ActionResultType.PASS;
                }
            }
        }
        ItemStack stack = player.getHeldItem(context.getHand());
        BlockPos offsetPos = world.getBlockState(pos).isSolid() ? pos.offset(context.getFace()) : pos;

        if (getFluidAmount(stack) < getWaterPerUse(stack)) {
            return ActionResultType.FAIL;
        }
        setActive(stack, player);

        int radius = getMode(stack);
        int x = offsetPos.getX();
        int y = offsetPos.getY();
        int z = offsetPos.getZ();

        for (int i = x - radius; i <= x + radius; ++i) {
            for (int k = z - radius; k <= z + radius; ++k) {
                Utils.spawnParticles(world, ParticleTypes.FALLING_WATER, i + world.rand.nextDouble(), y - 1 + world.rand.nextDouble(), k + world.rand.nextDouble(), 1, 0, 0, 0, 0);
            }
        }
        Iterable<BlockPos> area = BlockPos.getAllInBoxMutable(offsetPos.add(-radius, -2, -radius), offsetPos.add(radius, 1, radius));
        for (BlockPos scan : area) {
            BlockState state = world.getBlockState(scan);
            if (state.getBlock() instanceof FarmlandBlock) {
                if (state.get(FarmlandBlock.MOISTURE) < 7) {
                    world.setBlockState(scan, state.with(FarmlandBlock.MOISTURE, 7));
                }
            }
        }
        if (Utils.isServerWorld(world)) {
            if (world.rand.nextFloat() < Math.max(getEffectiveness(stack), 0.05)) {
                for (BlockPos scan : area) {
                    BlockState plantState = world.getBlockState(scan);
                    Block plant = plantState.getBlock();
                    if (plant instanceof IGrowable || plant instanceof IPlantable || EFFECTIVE_BLOCKS.contains(plant)) {
                        if (plant.ticksRandomly(plantState)) {
                            plant.randomTick(plantState, (ServerWorld) world, scan, world.rand);
                        } else {
                            world.getPendingBlockTicks().scheduleTick(scan, plant, 0);
                        }
                    }
                }
            }
            if (!player.abilities.isCreativeMode) {
                drain(stack, getWaterPerUse(stack) * (getMode(stack) + 1) * 2, EXECUTE);
            }
        }
        return ActionResultType.FAIL;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {

        BlockRayTraceResult traceResult = RayTracer.retrace(player, RayTraceContext.FluidMode.SOURCE_ONLY);
        ItemStack stack = player.getHeldItem(hand);

        if (traceResult.getType() == RayTraceResult.Type.MISS) {
            return ActionResult.resultPass(stack);
        }
        BlockPos tracePos = traceResult.getPos();

        if (!player.isSecondaryUseActive() || !world.isBlockModifiable(player, tracePos) || Utils.isFakePlayer(player) && !allowFakePlayers) {
            return ActionResult.resultFail(stack);
        }
        if (isWater(world.getBlockState(tracePos)) && getSpace(stack) > 0) {
            if (removeSourceBlocks) {
                world.setBlockState(tracePos, Blocks.AIR.getDefaultState(), 11);
            }
            fill(stack, new FluidStack(Fluids.WATER, BUCKET_VOLUME), EXECUTE);
            player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            return ActionResult.resultSuccess(stack);
        }
        return ActionResult.resultPass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

        if (!hasActiveTag(stack)) {
            return;
        }
        long activeTime = stack.getOrCreateTag().getLong(TAG_ACTIVE);

        if (entityIn.world.getGameTime() > activeTime) {
            stack.getOrCreateTag().remove(TAG_ACTIVE);
        }
    }

    // region HELPERS
    protected void setActive(ItemStack stack, LivingEntity entity) {

        stack.getOrCreateTag().putLong(TAG_ACTIVE, entity.world.getGameTime() + 20);
    }

    protected void setAttributesFromAugment(ItemStack container, CompoundNBT augmentData) {

        CompoundNBT subTag = container.getChildTag(TAG_PROPERTIES);
        if (subTag == null) {
            return;
        }
        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_BASE_MOD);
        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_FLUID_STORAGE);

        setAttributeFromAugmentAdd(subTag, augmentData, TAG_AUGMENT_AREA_RADIUS);
    }

    protected boolean hasActiveTag(ItemStack stack) {

        return stack.getOrCreateTag().contains(TAG_ACTIVE);
    }

    protected float getEffectiveness(ItemStack stack) {

        return 0.30F * getBaseMod(stack) - 0.05F * getMode(stack);
    }

    protected float getBaseMod(ItemStack stack) {

        return getPropertyWithDefault(stack, TAG_AUGMENT_BASE_MOD, 1.0F);
    }

    protected int getRadius(ItemStack stack) {

        return (int) getPropertyWithDefault(stack, TAG_AUGMENT_AREA_RADIUS, 0.0F) + 1;
    }

    protected int getWaterPerUse(ItemStack stack) {

        return MB_PER_USE;
    }
    // endregion

    // region IFluidContainerItem
    @Override
    public int getCapacity(ItemStack container) {

        float base = getPropertyWithDefault(container, TAG_AUGMENT_BASE_MOD, 1.0F);
        float mod = getPropertyWithDefault(container, TAG_AUGMENT_FLUID_STORAGE, 1.0F);
        return getMaxStored(container, Math.round(fluidCapacity * mod * base));
    }
    // endregion

    // region IAugmentableItem
    @Override
    public int getAugmentSlots(ItemStack augmentable) {

        return numSlots.getAsInt();
    }

    @Override
    public boolean validAugment(ItemStack augmentable, ItemStack augment) {

        return augValidator.test(augment);
    }

    @Override
    public void updateAugmentState(ItemStack container, List<ItemStack> augments) {

        container.getOrCreateTag().put(TAG_PROPERTIES, new CompoundNBT());
        for (ItemStack augment : augments) {
            CompoundNBT augmentData = AugmentDataHelper.getAugmentData(augment);
            if (augmentData == null) {
                continue;
            }
            setAttributesFromAugment(container, augmentData);
        }
        int fluidExcess = getFluidAmount(container) - getCapacity(container);
        if (fluidExcess > 0) {
            drain(container, fluidExcess, EXECUTE);
        }
        if (getMode(container) >= getNumModes(container)) {
            setMode(container, getNumModes(container) - 1);
        }
    }
    // endregion

    // region IMultiModeItem
    @Override
    public int getNumModes(ItemStack stack) {

        return 1 + getRadius(stack);
    }

    @Override
    public void onModeChange(PlayerEntity player, ItemStack stack) {

        player.world.playSound(null, player.getPosition(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.PLAYERS, 0.6F, 1.0F - 0.1F * getMode(stack));
        int radius = getMode(stack) * 2 + 1;
        if (radius <= 1) {
            ChatHelper.sendIndexedChatMessageToPlayer(player, new TranslationTextComponent("info.cofh.single_block"));
        } else {
            ChatHelper.sendIndexedChatMessageToPlayer(player, new TranslationTextComponent("info.cofh.area").appendString(": " + radius + "x" + radius));
        }
    }
    // endregion
}
