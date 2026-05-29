package io.github.naimjeg.damagenexus.core.rule;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleStacking;
import io.github.naimjeg.damagenexus.builtin.rule.condition.AllOfCondition;
import io.github.naimjeg.damagenexus.builtin.rule.condition.AnyOfCondition;
import io.github.naimjeg.damagenexus.builtin.rule.condition.DamageChannelIsCondition;
import io.github.naimjeg.damagenexus.builtin.rule.condition.NotCondition;
import io.github.naimjeg.damagenexus.builtin.rule.operation.*;
import io.github.naimjeg.damagenexus.builtin.rule.provider.DatapackDamageRuleProvider;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.*;

public final class DatapackDamageRuleReloadListener
        extends SimpleJsonResourceReloadListener<DamageRuleDefinition> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public DatapackDamageRuleReloadListener() {
        super(
                DamageRuleDefinition.CODEC,
                FileToIdConverter.json("damagenexus_rules")
        );
    }

    @Override
    protected void apply(
            Map<Identifier, DamageRuleDefinition> prepared,
            ResourceManager manager,
            ProfilerFiller profiler
    ) {
        List<Map.Entry<Identifier, DamageRuleDefinition>> entries =
                new ArrayList<>(prepared.entrySet());

        entries.sort(Comparator.comparing(entry -> entry.getKey().toString()));

        List<DamageRuleDefinition> accepted = new ArrayList<>();
        Map<Identifier, Identifier> firstFileByRuleId = new HashMap<>();

        int rejected = 0;

        for (Map.Entry<Identifier, DamageRuleDefinition> entry : entries) {
            Identifier fileId = entry.getKey();
            DamageRuleDefinition rule = entry.getValue();

            try {
                Identifier previousFile = firstFileByRuleId.putIfAbsent(
                        rule.id(),
                        fileId
                );

                if (previousFile != null) {
                    LOGGER.warn(
                            "[DamageNexus] Duplicate global datapack damage rule id {}. Keeping {}, skipping {}.",
                            rule.id(),
                            previousFile,
                            fileId
                    );

                    rejected++;
                    continue;
                }

                ValidationResult validation = validateRule(fileId, rule);

                if (!validation.accepted()) {
                    rejected++;
                    continue;
                }

                accepted.add(rule);
            } catch (Exception e) {
                rejected++;

                LOGGER.error(
                        "[DamageNexus] Failed to process global datapack damage rule from {}",
                        fileId,
                        e
                );
            }
        }

        accepted.sort((a, b) -> a.id().toString().compareTo(b.id().toString()));

        DatapackDamageRuleProvider.setRules(accepted);

        LOGGER.info(
                "[DamageNexus] Loaded {} global datapack damage rules. rejected={}",
                accepted.size(),
                rejected
        );
    }

    private static ValidationResult validateRule(
            Identifier fileId,
            DamageRuleDefinition rule
    ) {
        boolean accepted = true;

        if (rule.operations().isEmpty()) {
            LOGGER.warn(
                    "[DamageNexus] Global datapack rule {} from {} has no operations. It will load but do nothing.",
                    rule.id(),
                    fileId
            );
        }

        if (requiresStackingGroup(rule.stacking())
                && rule.stackingGroup().isEmpty()) {
            LOGGER.warn(
                    "[DamageNexus] Global datapack rule {} from {} uses stacking={} without stacking_group. The rule id will be used as fallback, so cross-rule stacking may not work as intended.",
                    rule.id(),
                    fileId,
                    rule.stacking()
            );
        }

        for (DamageRuleCondition condition : rule.conditions()) {
            validateCondition(fileId, rule, condition);
        }

        for (DamageRuleOperation operation : rule.operations()) {
            if (!DamageRuleExecutor.isOperationAllowedInPhase(
                    operation,
                    rule.phase()
            )) {
                if (!operation.supportsPhase(rule.phase())) {
                    LOGGER.error(
                            "[DamageNexus] Rejecting global datapack rule {} from {} because operation {} does not support phase {}.",
                            rule.id(),
                            fileId,
                            operation.type(),
                            rule.phase()
                    );

                    return new ValidationResult(false);
                }

                LOGGER.warn(
                        "[DamageNexus] Global datapack rule {} from {} has operation {} that is not allowed in phase {}. It will be skipped at runtime.",
                        rule.id(),
                        fileId,
                        operation.type(),
                        rule.phase()
                );
            }

            if (!validateOperation(fileId, rule, operation)) {
                accepted = false;
            }
        }

        return new ValidationResult(accepted);
    }

    private static boolean validateOperation(
            Identifier fileId,
            DamageRuleDefinition rule,
            DamageRuleOperation operation
    ) {
        boolean accepted = true;

        if (operation instanceof AddBaseDamageOperation addBase) {
            validateChannelId(
                    fileId,
                    rule,
                    "operation=" + operation.type(),
                    addBase.channelId()
            );
        }

        if (operation instanceof AddChannelPreMultiplierOperation addPre) {
            validateChannelId(
                    fileId,
                    rule,
                    "operation=" + operation.type(),
                    addPre.channelId()
            );

            if (!validateBucketId(
                    fileId,
                    rule,
                    operation,
                    addPre.preMultiplierBucketId()
            )) {
                accepted = false;
            }
        }

        if (operation instanceof AddChannelPostMultiplierOperation addPost) {
            validateChannelId(
                    fileId,
                    rule,
                    "operation=" + operation.type(),
                    addPost.channelId()
            );
        }

        if (operation instanceof AddTemporaryResistanceOperation addResistance) {
            validateChannelId(
                    fileId,
                    rule,
                    "operation=" + operation.type(),
                    addResistance.channelId()
            );
        }

        if (operation instanceof AddGlobalPreMultiplierOperation addGlobalPre) {
            if (!validateBucketId(
                    fileId,
                    rule,
                    operation,
                    addGlobalPre.preMultiplierBucketId()
            )) {
                accepted = false;
            }
        }

        return accepted;
    }

    private static void validateCondition(
            Identifier fileId,
            DamageRuleDefinition rule,
            DamageRuleCondition condition
    ) {
        if (condition instanceof DamageChannelIsCondition channelCondition) {
            validateChannelId(
                    fileId,
                    rule,
                    "condition=" + condition.type(),
                    channelCondition.channelId()
            );

            return;
        }

        if (condition instanceof AllOfCondition allOf) {
            for (DamageRuleCondition child : allOf.conditions()) {
                validateCondition(fileId, rule, child);
            }

            return;
        }

        if (condition instanceof AnyOfCondition anyOf) {
            for (DamageRuleCondition child : anyOf.conditions()) {
                validateCondition(fileId, rule, child);
            }

            return;
        }

        if (condition instanceof NotCondition not) {
            validateCondition(fileId, rule, not.condition());
        }
    }

    private static void validateChannelId(
            Identifier fileId,
            DamageRuleDefinition rule,
            String location,
            Identifier channelId
    ) {
        /*
         * Non-fatal:
         * channel definitions are datapack-driven too. If reload ordering ever changes,
         * rejecting here would cause false negatives. Runtime fallback is still untyped.
         */
        if (!DamageChannelRegistry.containsChannel(channelId)) {
            LOGGER.warn(
                    "[DamageNexus] Global datapack rule {} from {} references unknown damage channel {} at {}. Runtime will resolve it as untyped unless the channel is loaded later. \nIf this channel is loaded by another datapack later, you can ignore this message.",
                    rule.id(),
                    fileId,
                    channelId,
                    location
            );
        }
    }

    private static boolean validateBucketId(
            Identifier fileId,
            DamageRuleDefinition rule,
            DamageRuleOperation operation,
            Optional<Identifier> bucketId
    ) {
        if (bucketId.isEmpty()) {
            return true;
        }

        Identifier id = bucketId.get();

        /*
         * Fatal:
         * unknown pre-multiplier bucket id will throw at runtime when the operation applies.
         */
        if (!PreMultiplierBucketRegistry.containsPreMultiplierBucket(id)) {
            LOGGER.error(
                    "[DamageNexus] Rejecting global datapack rule {} from {} because operation {} references unknown pre-multiplier bucket {}.",
                    rule.id(),
                    fileId,
                    operation.type(),
                    id
            );

            return false;
        }

        return true;
    }

    private static boolean requiresStackingGroup(DamageRuleStacking stacking) {
        return switch (stacking) {
            case STACK -> false;
            case UNIQUE_SOURCE,
                 HIGHEST_VALUE,
                 LOWEST_VALUE,
                 REPLACE -> true;
        };
    }

    private record ValidationResult(boolean accepted) {}
}