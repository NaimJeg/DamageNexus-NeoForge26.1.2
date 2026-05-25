package io.github.naimjeg.damagenexus.api.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleRole;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleStacking;
import io.github.naimjeg.damagenexus.api.rule.RuleExecutionContext;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record AffixEntry(
        Identifier id,
        DamageRuleRole role,
        DamagePhase phase,
        int priority,
        AffixDisplay display,
        List<AffixCondition> conditions,
        List<AffixEffect> effects,
        DamageRuleStacking stacking,
        Optional<Identifier> stackingGroup,
        Optional<String> traceLabel
) {
    private static final Codec<DamagePhase> PHASE_CODEC =
            Codec.STRING.xmap(
                    name -> DamagePhase.valueOf(name.toUpperCase(Locale.ROOT)),
                    phase -> phase.name().toLowerCase(Locale.ROOT)
            );

    public static final Codec<AffixEntry> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Identifier.CODEC
                            .fieldOf("id")
                            .forGetter(AffixEntry::id),

                    DamageRuleRole.CODEC
                            .optionalFieldOf("role", DamageRuleRole.OFFENSIVE)
                            .forGetter(AffixEntry::role),

                    PHASE_CODEC
                            .fieldOf("phase")
                            .forGetter(AffixEntry::phase),

                    Codec.INT
                            .optionalFieldOf("priority", 500)
                            .forGetter(AffixEntry::priority),

                    AffixDisplay.CODEC
                            .optionalFieldOf("display", AffixDisplay.EMPTY)
                            .forGetter(AffixEntry::display),

                    AffixCondition.CODEC
                            .listOf()
                            .optionalFieldOf("conditions", List.of())
                            .forGetter(AffixEntry::conditions),

                    AffixEffect.CODEC
                            .listOf()
                            .optionalFieldOf("effects", List.of())
                            .forGetter(AffixEntry::effects),

                    DamageRuleStacking.CODEC
                            .optionalFieldOf("stacking", DamageRuleStacking.STACK)
                            .forGetter(AffixEntry::stacking),

                    Identifier.CODEC
                            .optionalFieldOf("stacking_group")
                            .forGetter(AffixEntry::stackingGroup),

                    Codec.STRING
                            .optionalFieldOf("trace_label")
                            .forGetter(AffixEntry::traceLabel)
            ).apply(instance, AffixEntry::new));

    public AffixEntry {
        conditions = List.copyOf(conditions);
        effects = List.copyOf(effects);
    }

    public Identifier source() {
        return id;
    }

    public String traceName() {
        return traceLabel.orElse(id.toString());
    }

    /**
     * 新标准入口：带 RuleExecutionContext。
     */
    public void tryExecute(
            DamageNexusContext ctx,
            DamagePhase runningPhase,
            RuleExecutionContext exec
    ) {
        if (this.phase != runningPhase) {
            return;
        }

        if (!this.role.canRunAs(exec.role())) {
            ctx.debugger.logOperation(
                    id.toString(),
                    runningPhase,
                    "RULE_ROLE_MISMATCH",
                    0.0f
            );
            return;
        }

        for (AffixCondition condition : conditions) {
            if (!condition.test(ctx)) {
                ctx.debugger.logOperation(
                        id.toString(),
                        runningPhase,
                        "RULE_CONDITION_FAILED:" + condition.type(),
                        0.0f
                );
                return;
            }
        }

        ctx.debugger.logOperation(
                id.toString(),
                runningPhase,
                "RULE_TRIGGERED",
                1.0f
        );

        for (AffixEffect effect : effects) {
            effect.apply(ctx);
        }
    }

    /**
     * 旧入口：暂时保留，避免一次性改爆。
     * 默认按 offensive 执行。
     */
    public void tryExecute(DamageNexusContext ctx, DamagePhase runningPhase) {
        if (ctx.attacker == null) {
            return;
        }

        tryExecute(
                ctx,
                runningPhase,
                RuleExecutionContext.weaponAffix(
                        ctx.attacker,
                        ctx.attacker.getMainHandItem(),
                        net.minecraft.world.entity.EquipmentSlot.MAINHAND
                )
        );
    }
}