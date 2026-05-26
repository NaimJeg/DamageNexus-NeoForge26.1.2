package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;

import java.util.List;

public record DamageRuleListComponent(List<DamageRuleDefinition> entries) {

    public static final DamageRuleListComponent EMPTY =
            new DamageRuleListComponent(List.of());

    public static final Codec<DamageRuleListComponent> CODEC =
            DamageRuleDefinition.CODEC
                    .listOf()
                    .fieldOf("entries")
                    .xmap(DamageRuleListComponent::new, DamageRuleListComponent::entries)
                    .codec();

    public DamageRuleListComponent {
        entries = List.copyOf(entries);
    }
}