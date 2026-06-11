package io.github.naimjeg.damagenexus.core.registry;

import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DamageChannelRegistryTest {

    private static final Identifier ALPHA =
            Identifier.fromNamespaceAndPath("test", "alpha");

    private static final Identifier BETA =
            Identifier.fromNamespaceAndPath("test", "beta");

    @AfterEach
    void resetRegistry() {
        DamageChannelRegistry.resetStateForTesting();
    }

    @Test
    void staleChannelWithSameIdResolvesToCurrentIndexAfterReload() {
        DamageChannelRegistry.replaceStateForTesting(definitions(BETA));
        DamageChannel staleBeta =
                DamageChannelRegistry.getChannelOrUntyped(BETA);

        assertEquals(1, staleBeta.index());
        assertTrue(DamageChannelRegistry.isCurrentRuntimeChannel(staleBeta));

        DamageChannelRegistry.replaceStateForTesting(definitions(ALPHA, BETA));

        DamageChannel resolved =
                DamageChannelRegistry.resolve(staleBeta);

        assertEquals(BETA, resolved.id());
        assertEquals(2, resolved.index());
        assertFalse(DamageChannelRegistry.isCurrentRuntimeChannel(staleBeta));
        assertTrue(DamageChannelRegistry.isKnownRuntimeChannel(staleBeta));
    }

    private static Map<Identifier, DamageChannelRegistry.ChannelDefinition> definitions(
            Identifier... ids
    ) {
        Map<Identifier, DamageChannelRegistry.ChannelDefinition> definitions =
                new LinkedHashMap<>();

        for (Identifier id : ids) {
            definitions.put(
                    id,
                    new DamageChannelRegistry.ChannelDefinition(
                            id,
                            List.of(),
                            Optional.empty(),
                            true,
                            0
                    )
            );
        }

        return definitions;
    }
}
