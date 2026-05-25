package io.github.naimjeg.damagenexus.api.affix;

import com.mojang.serialization.Codec;

import java.util.List;

public record AffixListComponent(List<AffixEntry> entries) {

    public static final AffixListComponent EMPTY =
            new AffixListComponent(List.of());

    public static final Codec<AffixListComponent> CODEC =
            AffixEntry.CODEC
                    .listOf()
                    .fieldOf("entries")
                    .xmap(AffixListComponent::new, AffixListComponent::entries)
                    .codec();

    public AffixListComponent {
        entries = List.copyOf(entries);
    }
}