package com.glisco.conjuring.items.soul_alloy_tools;

import com.glisco.conjuring.Conjuring;
import com.glisco.conjuring.entities.SoulHarvesterEntity;
import com.glisco.conjuring.items.ConjuringItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class SoulAlloyScythe extends HoeItem implements SoulAlloyTool {

    private static final BiFunction<Block, BlockState, Boolean> MATURE_PREDICATE = (block, blockState) ->
            block == blockState.getBlock() && blockState.getBlock() instanceof CropBlock crop && crop.isMature(blockState);

    public SoulAlloyScythe() {
        super(SoulAlloyToolMaterial.INSTANCE, 2, -3.0f, new Settings().group(Conjuring.CONJURING_GROUP).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean canAoeDig() {
        return true;
    }

    @Override
    public Predicate<BlockState> getAoeToolOverridePredicate() {
        return blockState -> blockState.getBlock() instanceof PlantBlock;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (SoulAlloyTool.isSecondaryEnabled(context.getStack())) {
            return ActionResult.PASS;
        } else {
            return super.useOnBlock(context);
        }
    }

    public static BiFunction<Block, BlockState, Boolean> getCrawlPredicate(ItemStack stack) {
        return SoulAlloyTool.getModifierLevel(stack, SoulAlloyModifier.IGNORANCE) < 1 ?
                BlockCrawler.IDENTITY_PREDICATE : MATURE_PREDICATE;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        if (!SoulAlloyTool.isSecondaryEnabled(user.getStackInHand(hand))) return TypedActionResult.pass(user.getStackInHand(hand));

        if (!world.isClient()) {
            SoulHarvesterEntity harvester = new SoulHarvesterEntity(world, user);
            harvester.refreshPositionAndAngles(user.getX(), user.getEyeY(), user.getZ(), 0, 0);
            harvester.setVelocity(user, user.getPitch(), user.getYaw(), 0f, 1.5f, 1);

            harvester.setItem(user.getStackInHand(hand));

            int scopeGems = SoulAlloyTool.getModifierLevel(user.getStackInHand(hand), SoulAlloyModifier.SCOPE);
            if (scopeGems > 0) {
                harvester.setMaxBlocks((int) (8 + Math.pow(scopeGems, Conjuring.CONFIG.tools_config.scythe_scope_exponent) * 8));
            }

            world.spawnEntity(harvester);

            user.getItemCooldownManager().set(ConjuringItems.SOUL_ALLOY_SCYTHE, Conjuring.CONFIG.tools_config.scythe_secondary_cooldown);
            user.getStackInHand(hand).damage(Conjuring.CONFIG.tools_config.scythe_secondary_base_durability_cost + Conjuring.CONFIG.tools_config.scythe_secondary_per_scope_durability_cost,
                    user, player -> player.sendToolBreakStatus(hand));
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.addAll(SoulAlloyTool.getTooltip(stack));
    }

}
