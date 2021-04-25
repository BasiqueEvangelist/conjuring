package com.glisco.conjuring.blocks.soul_weaver;

import com.glisco.conjuring.ConjuringCommon;
import com.glisco.conjuring.items.ConjuringScepter;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class SoulWeaverBlock extends BlockWithEntity {

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(2, 0, 2, 14, 2, 14),
            Block.createCuboidShape(4, 2, 4, 12, 4, 12),
            Block.createCuboidShape(10, 4, 4, 12, 9, 6),
            Block.createCuboidShape(10, 4, 10, 12, 9, 12),
            Block.createCuboidShape(4, 14, 4, 12, 16, 12),
            Block.createCuboidShape(5, 9, 5, 11, 14, 11),
            Block.createCuboidShape(4, 9, 11, 12, 12, 12),
            Block.createCuboidShape(4, 9, 4, 12, 12, 5),
            Block.createCuboidShape(11, 9, 5, 12, 12, 11),
            Block.createCuboidShape(4, 9, 5, 5, 12, 11),
            Block.createCuboidShape(4, 4, 4, 6, 9, 6),
            Block.createCuboidShape(4, 4, 10, 6, 9, 12)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    public SoulWeaverBlock() {
        super(Settings.copy(Blocks.BLACKSTONE).nonOpaque());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new SoulWeaverBlockEntity();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        SoulWeaverBlockEntity weaver = (SoulWeaverBlockEntity) world.getBlockEntity(pos);

        if (weaver.isRunning()) return ActionResult.PASS;

        final ItemStack playerStack = player.getStackInHand(hand);

        if (playerStack.getItem().equals(ConjuringCommon.CONJURATION_ESSENCE) && !weaver.isLit()) {
            weaver.setLit(true);
            playerStack.decrement(1);
            if (playerStack.isEmpty()) player.setStackInHand(hand, ItemStack.EMPTY);
            return ActionResult.SUCCESS;
        }

        if (playerStack.getItem() instanceof ConjuringScepter) {
            weaver.tryStartRitual();
            return ActionResult.SUCCESS;
        }

        ItemStack weaverItem = weaver.getItem();

        if (weaverItem == null) {
            if (playerStack.isEmpty()) return ActionResult.PASS;

            ItemStack playerItem = playerStack.copy();
            playerItem.setCount(1);

            weaver.setItem(playerItem);

            playerStack.decrement(1);
            if (playerStack.isEmpty()) player.setStackInHand(hand, ItemStack.EMPTY);
        } else {
            ItemStack playerItemSingleton = playerStack.copy();
            playerItemSingleton.setCount(1);

            if (playerStack.isEmpty()) {
                player.setStackInHand(hand, weaverItem);
            } else if (ItemStack.areEqual(playerItemSingleton, weaverItem) && playerStack.getCount() + 1 <= playerStack.getMaxCount()) {
                playerStack.increment(1);
            } else {
                ItemScatterer.spawn(world, pos.getX(), pos.getY() + 1f, pos.getZ(), weaverItem);
            }
            weaver.setItem(null);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SoulWeaverBlockEntity) {
                SoulWeaverBlockEntity weaverEntity = (SoulWeaverBlockEntity) blockEntity;

                if (weaverEntity.getItem() != null) {
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), weaverEntity.getItem());
                }

            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}