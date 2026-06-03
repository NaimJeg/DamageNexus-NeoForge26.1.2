package io.github.naimjeg.damagenexus.core.trace;

import io.github.naimjeg.damagenexus.api.display.DamageContributionDescriptor;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class DamageContributionCollector {

    private final List<DamageContributionDescriptor> applied = new ArrayList<>();
    private final List<DamageContributionDescriptor> rejected = new ArrayList<>();

    public void recordApplied(DamageContributionDescriptor descriptor) {
        if (descriptor != null) {
            applied.add(descriptor);
        }
    }

    public void recordRejected(DamageContributionDescriptor descriptor) {
        if (descriptor != null) {
            rejected.add(descriptor);
        }
    }

    public void record(
            DamageMutationResult result,
            Supplier<DamageContributionDescriptor> descriptor
    ) {
        if (result == null || descriptor == null) {
            return;
        }

        DamageContributionDescriptor resolved = descriptor.get();

        if (resolved == null) {
            return;
        }

        if (result == DamageMutationResult.APPLIED) {
            applied.add(resolved);
        } else {
            rejected.add(resolved);
        }
    }

    public List<DamageContributionDescriptor> applied() {
        return List.copyOf(applied);
    }

    public List<DamageContributionDescriptor> rejected() {
        return List.copyOf(rejected);
    }

    public boolean isEmpty() {
        return applied.isEmpty() && rejected.isEmpty();
    }
}