package io.github.naimjeg.damagenexus.api.item;

import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixDefinition;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixSelectionResolver;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixValidator;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryDefinition;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntrySelectionResolver;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryValidator;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class DamageNexusItemApi {

    private static final String SOURCE_SET_ENTRIES = "item_api/set_entries";
    private static final String SOURCE_SET_AFFIXES = "item_api/set_affixes";
    private static final String SOURCE_ADD_ENTRY = "item_api/add_entry";
    private static final String SOURCE_ADD_AFFIX = "item_api/add_affix";

    private DamageNexusItemApi() {
    }

    public static DamageNexusItemEntries get(ItemStack stack) {
        if (isUnavailable(stack)) {
            return DamageNexusItemEntries.EMPTY;
        }

        List<DamageEntryDefinition> entries = stack.getOrDefault(
                ModDataComponents.DAMAGE_ENTRIES.get(),
                List.of()
        );

        List<DamageAffixDefinition> affixes = stack.getOrDefault(
                ModDataComponents.DAMAGE_AFFIXES.get(),
                List.of()
        );

        return new DamageNexusItemEntries(entries, affixes);
    }

    public static boolean set(
            ItemStack stack,
            DamageNexusItemEntries value
    ) {
        if (isUnavailable(stack)) {
            return false;
        }

        DamageNexusItemEntries normalized =
                value == null ? DamageNexusItemEntries.EMPTY : value;

        setRawEntries(stack, normalized.entries());
        setRawAffixes(stack, normalized.affixes());

        return true;
    }

    public static boolean clear(ItemStack stack) {
        if (isUnavailable(stack)) {
            return false;
        }

        boolean changed =
                stack.has(ModDataComponents.DAMAGE_ENTRIES.get())
                        || stack.has(ModDataComponents.DAMAGE_AFFIXES.get());

        stack.remove(ModDataComponents.DAMAGE_ENTRIES.get());
        stack.remove(ModDataComponents.DAMAGE_AFFIXES.get());

        return changed;
    }

    public static boolean hasAny(ItemStack stack) {
        return !get(stack).isEmpty();
    }

    public static List<DamageEntryDefinition> getEntries(ItemStack stack) {
        return get(stack).entries();
    }

    public static List<DamageEntryDefinition> getResolvedEntries(
            ItemStack stack
    ) {
        return DamageEntrySelectionResolver.resolve(getEntries(stack));
    }

    public static boolean setEntries(
            ItemStack stack,
            List<DamageEntryDefinition> entries
    ) {
        if (isUnavailable(stack)) {
            return false;
        }

        List<DamageEntryDefinition> valid =
                DamageEntryValidator.filterValid(
                        entries,
                        SOURCE_SET_ENTRIES
                );

        setRawEntries(stack, valid);
        return true;
    }

    public static boolean addEntry(
            ItemStack stack,
            DamageEntryDefinition entry
    ) {
        return addEntry(stack, entry, SOURCE_ADD_ENTRY);
    }

    public static boolean addEntry(
            ItemStack stack,
            DamageEntryDefinition entry,
            String source
    ) {
        if (isUnavailable(stack) || entry == null) {
            return false;
        }

        String effectiveSource =
                source == null || source.isBlank()
                        ? SOURCE_ADD_ENTRY
                        : source;

        List<DamageEntryDefinition> valid =
                DamageEntryValidator.filterValid(
                        List.of(entry),
                        effectiveSource
                );

        if (valid.isEmpty()) {
            return false;
        }

        List<DamageEntryDefinition> next =
                new ArrayList<>(getEntries(stack));

        next.addAll(valid);

        return setEntries(stack, next);
    }

    public static boolean removeEntry(
            ItemStack stack,
            Identifier entryId
    ) {
        Objects.requireNonNull(entryId, "entryId must not be null");

        return removeEntries(
                stack,
                entry -> entry.id().equals(entryId)
        ) > 0;
    }

    public static int removeEntries(
            ItemStack stack,
            Predicate<DamageEntryDefinition> predicate
    ) {
        if (isUnavailable(stack) || predicate == null) {
            return 0;
        }

        List<DamageEntryDefinition> current = getEntries(stack);

        if (current.isEmpty()) {
            return 0;
        }

        List<DamageEntryDefinition> kept = new ArrayList<>();
        int removed = 0;

        for (DamageEntryDefinition entry : current) {
            if (predicate.test(entry)) {
                removed++;
            } else {
                kept.add(entry);
            }
        }

        if (removed == 0) {
            return 0;
        }

        setRawEntries(stack, kept);
        return removed;
    }

    public static boolean hasEntry(
            ItemStack stack,
            Identifier entryId
    ) {
        Objects.requireNonNull(entryId, "entryId must not be null");

        for (DamageEntryDefinition entry : getEntries(stack)) {
            if (entry.id().equals(entryId)) {
                return true;
            }
        }

        return false;
    }

    public static List<DamageAffixDefinition> getAffixes(ItemStack stack) {
        return get(stack).affixes();
    }

    public static List<DamageAffixDefinition> getResolvedAffixes(
            ItemStack stack
    ) {
        return DamageAffixSelectionResolver.resolve(getAffixes(stack));
    }

    public static boolean setAffixes(
            ItemStack stack,
            List<DamageAffixDefinition> affixes
    ) {
        if (isUnavailable(stack)) {
            return false;
        }

        List<DamageAffixDefinition> valid =
                DamageAffixValidator.filterValid(
                        affixes,
                        SOURCE_SET_AFFIXES
                );

        setRawAffixes(stack, valid);
        return true;
    }

    public static boolean addAffix(
            ItemStack stack,
            DamageAffixDefinition affix
    ) {
        return addAffix(stack, affix, SOURCE_ADD_AFFIX);
    }

    public static boolean addAffix(
            ItemStack stack,
            DamageAffixDefinition affix,
            String source
    ) {
        if (isUnavailable(stack) || affix == null) {
            return false;
        }

        String effectiveSource =
                source == null || source.isBlank()
                        ? SOURCE_ADD_AFFIX
                        : source;

        List<DamageAffixDefinition> valid =
                DamageAffixValidator.filterValid(
                        List.of(affix),
                        effectiveSource
                );

        if (valid.isEmpty()) {
            return false;
        }

        List<DamageAffixDefinition> next =
                new ArrayList<>(getAffixes(stack));

        next.addAll(valid);

        return setAffixes(stack, next);
    }

    public static boolean removeAffix(
            ItemStack stack,
            Identifier affixId
    ) {
        Objects.requireNonNull(affixId, "affixId must not be null");

        return removeAffixes(
                stack,
                affix -> affix.id().equals(affixId)
        ) > 0;
    }

    public static int removeAffixes(
            ItemStack stack,
            Predicate<DamageAffixDefinition> predicate
    ) {
        if (isUnavailable(stack) || predicate == null) {
            return 0;
        }

        List<DamageAffixDefinition> current = getAffixes(stack);

        if (current.isEmpty()) {
            return 0;
        }

        List<DamageAffixDefinition> kept = new ArrayList<>();
        int removed = 0;

        for (DamageAffixDefinition affix : current) {
            if (predicate.test(affix)) {
                removed++;
            } else {
                kept.add(affix);
            }
        }

        if (removed == 0) {
            return 0;
        }

        setRawAffixes(stack, kept);
        return removed;
    }

    public static boolean hasAffix(
            ItemStack stack,
            Identifier affixId
    ) {
        Objects.requireNonNull(affixId, "affixId must not be null");

        for (DamageAffixDefinition affix : getAffixes(stack)) {
            if (affix.id().equals(affixId)) {
                return true;
            }
        }

        return false;
    }

    public static int removeEntriesFromNamespace(
            ItemStack stack,
            String namespace
    ) {
        if (namespace == null || namespace.isBlank()) {
            return 0;
        }

        return removeEntries(
                stack,
                entry -> namespace.equals(entry.id().getNamespace())
        );
    }

    public static int removeAffixesFromNamespace(
            ItemStack stack,
            String namespace
    ) {
        if (namespace == null || namespace.isBlank()) {
            return 0;
        }

        return removeAffixes(
                stack,
                affix -> namespace.equals(affix.id().getNamespace())
        );
    }

    private static void setRawEntries(
            ItemStack stack,
            List<DamageEntryDefinition> entries
    ) {
        if (entries == null || entries.isEmpty()) {
            stack.remove(ModDataComponents.DAMAGE_ENTRIES.get());
            return;
        }

        stack.set(
                ModDataComponents.DAMAGE_ENTRIES.get(),
                List.copyOf(entries)
        );
    }

    private static void setRawAffixes(
            ItemStack stack,
            List<DamageAffixDefinition> affixes
    ) {
        if (affixes == null || affixes.isEmpty()) {
            stack.remove(ModDataComponents.DAMAGE_AFFIXES.get());
            return;
        }

        stack.set(
                ModDataComponents.DAMAGE_AFFIXES.get(),
                List.copyOf(affixes)
        );
    }

    private static boolean isUnavailable(ItemStack stack) {
        return stack == null || stack.isEmpty();
    }
}