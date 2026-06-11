package io.github.naimjeg.damagenexus.core.pipeline;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.context.DamageRuleContext;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DamageNexusPipelineTest {

    @Test
    void tiedPriorityOrderingIsDeterministic() {
        TestProcessor internalA = new TestProcessor(DamagePhase.BASE_MODIFICATION, 100);
        TestProcessor internalB = new TestProcessor(DamagePhase.BASE_MODIFICATION, 100);
        TestProcessor externalA = new TestProcessor(DamagePhase.BASE_MODIFICATION, 100);
        TestProcessor externalB = new TestProcessor(DamagePhase.BASE_MODIFICATION, 100);

        DamageNexusPipeline.PipelineSnapshot snapshot =
                DamageNexusPipeline.buildPipelineSnapshot(
                        List.of(internalA, internalB),
                        List.of(externalA, externalB),
                        7
                );

        List<DamageNexusPipeline.PipelineEntry> processors =
                snapshot.processors(DamagePhase.BASE_MODIFICATION);

        assertSame(internalA, processors.get(0).processor());
        assertSame(internalB, processors.get(1).processor());
        assertSame(externalA, processors.get(2).processor());
        assertSame(externalB, processors.get(3).processor());
    }

    @Test
    void priorityStillRunsDescendingBeforeTieBreakers() {
        TestProcessor lower = new TestProcessor(DamagePhase.BASE_MODIFICATION, 100);
        TestProcessor higher = new TestProcessor(DamagePhase.BASE_MODIFICATION, 200);

        DamageNexusPipeline.PipelineSnapshot snapshot =
                DamageNexusPipeline.buildPipelineSnapshot(
                        List.of(lower, higher),
                        List.of(),
                        0
                );

        List<DamageNexusPipeline.PipelineEntry> processors =
                snapshot.processors(DamagePhase.BASE_MODIFICATION);

        assertSame(higher, processors.get(0).processor());
        assertSame(lower, processors.get(1).processor());
    }

    @Test
    void snapshotListsAndMapAreImmutable() {
        TestProcessor processor = new TestProcessor(DamagePhase.BASE_MODIFICATION, 100);

        DamageNexusPipeline.PipelineSnapshot snapshot =
                DamageNexusPipeline.buildPipelineSnapshot(
                        List.of(processor),
                        List.of(),
                        0
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.processors(DamagePhase.BASE_MODIFICATION).add(
                        new DamageNexusPipeline.PipelineEntry(
                                processor,
                                false,
                                null,
                                99,
                                100
                        )
                )
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.phaseProcessors().put(
                        DamagePhase.FINAL_OVERRIDE,
                        List.of()
                )
        );
    }

    @Test
    void rebuiltSnapshotDoesNotMutatePreviousSnapshot() {
        TestProcessor first = new TestProcessor(DamagePhase.BASE_MODIFICATION, 100);
        TestProcessor second = new TestProcessor(DamagePhase.BASE_MODIFICATION, 100);

        DamageNexusPipeline.PipelineSnapshot before =
                DamageNexusPipeline.buildPipelineSnapshot(
                        List.of(),
                        List.of(first),
                        1
                );

        DamageNexusPipeline.PipelineSnapshot after =
                DamageNexusPipeline.buildPipelineSnapshot(
                        List.of(),
                        List.of(second),
                        2
                );

        assertEquals(1, before.externalVersion());
        assertEquals(2, after.externalVersion());
        assertSame(first, before.processors(DamagePhase.BASE_MODIFICATION).getFirst().processor());
        assertSame(second, after.processors(DamagePhase.BASE_MODIFICATION).getFirst().processor());
    }

    private record TestProcessor(
            DamagePhase phase,
            int priority
    ) implements DamagePhaseProcessor {

        @Override
        public void apply(DamageRuleContext ctx) {
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }
}
