package io.github.naimjeg.damagenexus.api.rule.builder;

import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DamageRuleBuilder {

    private final Identifier id;
    private DamageRuleRole role;
    private DamagePhase phase = DamagePhase.BASE_MODIFICATION;
    private int priority = 500;
    private DamageRuleDisplay display = DamageRuleDisplay.EMPTY;
    private final List<DamageRuleCondition> conditions = new ArrayList<>();
    private final List<DamageRuleOperation> operations = new ArrayList<>();
    private DamageRuleStacking stacking = DamageRuleStacking.STACK;
    private Optional<Identifier> stackingGroup = Optional.empty();
    private Optional<String> traceLabel = Optional.empty();

    private DamageRuleBuilder(
            Identifier id,
            DamageRuleRole role
    ) {
        this.id = id;
        this.role = role;
    }

    public static DamageRuleBuilder offensive(Identifier id) {
        return new DamageRuleBuilder(id, DamageRuleRole.OFFENSIVE);
    }

    public static DamageRuleBuilder defensive(Identifier id) {
        return new DamageRuleBuilder(id, DamageRuleRole.DEFENSIVE);
    }

    public static DamageRuleBuilder any(Identifier id) {
        return new DamageRuleBuilder(id, DamageRuleRole.ANY);
    }

    public DamageRuleBuilder role(DamageRuleRole role) {
        this.role = role;
        return this;
    }

    public DamageRuleBuilder phase(DamagePhase phase) {
        this.phase = phase;
        return this;
    }

    public DamageRuleBuilder baseModification() {
        return phase(DamagePhase.BASE_MODIFICATION);
    }

    public DamageRuleBuilder typeScaling() {
        return phase(DamagePhase.TYPE_SCALING);
    }

    public DamageRuleBuilder criticalHit() {
        return phase(DamagePhase.CRITICAL_HIT);
    }

    public DamageRuleBuilder conditionalMultiplier() {
        return phase(DamagePhase.CONDITIONAL_MULTI);
    }

    public DamageRuleBuilder globalAdjustment() {
        return phase(DamagePhase.GLOBAL_ADJUSTMENT);
    }

    public DamageRuleBuilder mitigationSetup() {
        return phase(DamagePhase.MITIGATION_SETUP);
    }

    public DamageRuleBuilder finalOverride() {
        return phase(DamagePhase.FINAL_OVERRIDE);
    }

    public DamageRuleBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public DamageRuleBuilder display(
            String name,
            String description
    ) {
        this.display = new DamageRuleDisplay(
                Optional.ofNullable(name),
                Optional.ofNullable(description)
        );
        return this;
    }

    public DamageRuleBuilder name(String name) {
        this.display = new DamageRuleDisplay(
                Optional.ofNullable(name),
                this.display.description()
        );
        return this;
    }

    public DamageRuleBuilder description(String description) {
        this.display = new DamageRuleDisplay(
                this.display.name(),
                Optional.ofNullable(description)
        );
        return this;
    }

    public DamageRuleBuilder when(DamageRuleCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public DamageRuleBuilder always() {
        return when(DamageNexusConditions.always());
    }

    public DamageRuleBuilder critical() {
        return when(DamageNexusConditions.critical());
    }

    public DamageRuleBuilder targetOnFire() {
        return when(DamageNexusConditions.targetOnFire());
    }

    public DamageRuleBuilder operation(DamageRuleOperation operation) {
        this.operations.add(operation);
        return this;
    }

    public DamageRuleBuilder addBaseDamage(
            Identifier channel,
            float value
    ) {
        return operation(DamageNexusOperations.addBaseDamage(channel, value));
    }

    public DamageRuleBuilder addBaseDamage(
            Identifier channel,
            DamageApplicationBucket applicationBucket,
            float value
    ) {
        return operation(DamageNexusOperations.addBaseDamage(
                channel,
                applicationBucket,
                value
        ));
    }

    public DamageRuleBuilder addChannelPreMultiplier(
            Identifier channel,
            float value
    ) {
        return operation(DamageNexusOperations.addChannelPreMultiplier(channel, value));
    }

    public DamageRuleBuilder addChannelPreMultiplier(
            Identifier channel,
            Identifier preMultiplierBucket,
            float value
    ) {
        return operation(DamageNexusOperations.addChannelPreMultiplier(
                channel,
                preMultiplierBucket,
                value
        ));
    }

    public DamageRuleBuilder addChannelPostMultiplier(
            Identifier channel,
            float value
    ) {
        return operation(DamageNexusOperations.addChannelPostMultiplier(channel, value));
    }

    public DamageRuleBuilder addGlobalPreMultiplier(float value) {
        return operation(DamageNexusOperations.addGlobalPreMultiplier(value));
    }

    public DamageRuleBuilder addGlobalPreMultiplier(
            Identifier preMultiplierBucket,
            float value
    ) {
        return operation(DamageNexusOperations.addGlobalPreMultiplier(
                preMultiplierBucket,
                value
        ));
    }

    public DamageRuleBuilder addGlobalPostMultiplier(float value) {
        return operation(DamageNexusOperations.addGlobalPostMultiplier(value));
    }

    public DamageRuleBuilder convertDamage(
            Identifier from,
            Identifier to,
            float ratio
    ) {
        return operation(DamageNexusOperations.convertDamage(from, to, ratio));
    }

    public DamageRuleBuilder gainExtraDamage(
            Identifier basedOn,
            Identifier to,
            float ratio
    ) {
        return operation(DamageNexusOperations.gainExtraDamage(basedOn, to, ratio));
    }

    public DamageRuleBuilder addTemporaryResistance(
            Identifier channel,
            float value
    ) {
        return operation(DamageNexusOperations.addTemporaryResistance(channel, value));
    }

    public DamageRuleBuilder addChannelMitigation(
            Identifier channel,
            float value
    ) {
        return operation(DamageNexusOperations.addChannelMitigation(channel, value));
    }

    public DamageRuleBuilder overrideFinalDamage(float value) {
        return operation(DamageNexusOperations.overrideFinalDamage(value));
    }

    public DamageRuleBuilder stacking(DamageRuleStacking stacking) {
        this.stacking = stacking;
        return this;
    }

    public DamageRuleBuilder stack() {
        return stacking(DamageRuleStacking.STACK);
    }

    public DamageRuleBuilder uniqueSource() {
        return stacking(DamageRuleStacking.UNIQUE_SOURCE);
    }

    public DamageRuleBuilder highestValue() {
        return stacking(DamageRuleStacking.HIGHEST_VALUE);
    }

    public DamageRuleBuilder lowestValue() {
        return stacking(DamageRuleStacking.LOWEST_VALUE);
    }

    public DamageRuleBuilder replace() {
        return stacking(DamageRuleStacking.REPLACE);
    }

    public DamageRuleBuilder stackingGroup(Identifier group) {
        this.stackingGroup = Optional.ofNullable(group);
        return this;
    }

    public DamageRuleBuilder trace(String traceLabel) {
        this.traceLabel = Optional.ofNullable(traceLabel);
        return this;
    }

    public DamageRuleDefinition build() {
        if (operations.isEmpty()) {
            throw new IllegalStateException(
                    "DamageRuleBuilder cannot build rule without operations: " + id
            );
        }

        for (DamageRuleOperation operation : operations) {
            if (!operation.supportsPhase(phase)) {
                throw new IllegalStateException(
                        "Operation " + operation.type()
                                + " does not support phase " + phase
                                + " in rule " + id
                );
            }
        }

        return new DamageRuleDefinition(
                id,
                role,
                phase,
                priority,
                display,
                List.copyOf(conditions),
                List.copyOf(operations),
                stacking,
                stackingGroup,
                traceLabel
        );
    }
}