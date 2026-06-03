package io.github.naimjeg.damagenexus.api.display;

import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record DamageContributionDescriptor(
        Identifier id,
        DamageContributionSourceKind sourceKind,
        DamageContributionOperationKind operationKind,
        DamagePhase phase,

        Optional<Identifier> channel,
        Optional<DamageApplicationBucket> applicationBucket,
        Optional<Identifier> preMultiplierBucket,

        float value,

        Optional<Identifier> displayGroup,
        Optional<String> displayName,
        List<String> tooltipLines,

        Optional<String> traceLabel,
        boolean runtimeOnly,
        boolean debugOnly
) {
    public DamageContributionDescriptor {
        if (id == null) {
            throw new IllegalArgumentException("Damage contribution id cannot be null");
        }

        if (sourceKind == null) {
            sourceKind = DamageContributionSourceKind.UNKNOWN;
        }

        if (operationKind == null) {
            throw new IllegalArgumentException("Damage contribution operation kind cannot be null: " + id);
        }

        if (phase == null) {
            throw new IllegalArgumentException("Damage contribution phase cannot be null: " + id);
        }

        channel = channel == null ? Optional.empty() : channel;
        applicationBucket = applicationBucket == null ? Optional.empty() : applicationBucket;
        preMultiplierBucket = preMultiplierBucket == null ? Optional.empty() : preMultiplierBucket;
        displayGroup = displayGroup == null ? Optional.empty() : displayGroup;
        displayName = displayName == null ? Optional.empty() : displayName;
        tooltipLines = tooltipLines == null ? List.of() : List.copyOf(tooltipLines);
        traceLabel = traceLabel == null ? Optional.empty() : traceLabel;
    }

    public static DamageContributionDescriptor vanillaBase(
            Identifier id,
            DamagePhase phase,
            Identifier channel,
            DamageApplicationBucket bucket,
            float value,
            String traceLabel
    ) {
        return new DamageContributionDescriptor(
                id,
                DamageContributionSourceKind.VANILLA_BRIDGE,
                DamageContributionOperationKind.ADD_BASE_DAMAGE,
                phase,
                Optional.ofNullable(channel),
                Optional.ofNullable(bucket),
                Optional.empty(),
                value,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.ofNullable(traceLabel),
                true,
                false
        );
    }

    public static DamageContributionDescriptor vanillaMultiplier(
            Identifier id,
            DamagePhase phase,
            Identifier channel,
            DamageApplicationBucket bucket,
            Identifier preMultiplierBucket,
            float value,
            String traceLabel
    ) {
        return new DamageContributionDescriptor(
                id,
                DamageContributionSourceKind.VANILLA_BRIDGE,
                DamageContributionOperationKind.ADD_APPLICATION_PRE_MULTIPLIER,
                phase,
                Optional.ofNullable(channel),
                Optional.ofNullable(bucket),
                Optional.ofNullable(preMultiplierBucket),
                value,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.ofNullable(traceLabel),
                true,
                false
        );
    }

    public static DamageContributionDescriptor vanillaTemporaryResistance(
            Identifier id,
            DamagePhase phase,
            Identifier channel,
            float value,
            String traceLabel
    ) {
        return new DamageContributionDescriptor(
                id,
                DamageContributionSourceKind.VANILLA_BRIDGE,
                DamageContributionOperationKind.ADD_TEMPORARY_RESISTANCE,
                phase,
                Optional.ofNullable(channel),
                Optional.empty(),
                Optional.empty(),
                value,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.ofNullable(traceLabel),
                true,
                false
        );
    }

    public static DamageContributionDescriptor vanillaMitigation(
            Identifier id,
            DamagePhase phase,
            Identifier channel,
            float value,
            String traceLabel
    ) {
        return new DamageContributionDescriptor(
                id,
                DamageContributionSourceKind.VANILLA_BRIDGE,
                DamageContributionOperationKind.ADD_CHANNEL_MITIGATION,
                phase,
                Optional.ofNullable(channel),
                Optional.empty(),
                Optional.empty(),
                value,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.ofNullable(traceLabel),
                true,
                false
        );
    }

    public static DamageContributionDescriptor vanillaArmorEffectiveness(
            Identifier id,
            DamagePhase phase,
            float value,
            String traceLabel
    ) {
        return new DamageContributionDescriptor(
                id,
                DamageContributionSourceKind.VANILLA_BRIDGE,
                DamageContributionOperationKind.MULTIPLY_ARMOR_EFFECTIVENESS,
                phase,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                value,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.ofNullable(traceLabel),
                true,
                false
        );
    }
}