package com.glisco.conjuring.util;

import com.mojang.serialization.Codec;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SerializationAttribute;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class GemTinkeringCriterion extends AbstractCriterion<GemTinkeringCriterion.Conditions> {

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, conditions -> true);
    }

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public static class Conditions implements AbstractCriterion.Conditions {

        public static final Codec<Conditions> CODEC = StructEndecBuilder.of(
                Endec.ofCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC).optionalFieldOf("player", c -> c.playerPredicate, (LootContextPredicate) null),
                Conditions::new
        ).codec(SerializationAttribute.HUMAN_READABLE);

        private final LootContextPredicate playerPredicate;

        public Conditions(LootContextPredicate playerPredicate) {
            this.playerPredicate = playerPredicate;
        }

        @Override
        public Optional<LootContextPredicate> player() {
            return Optional.ofNullable(this.playerPredicate);
        }
    }
}
