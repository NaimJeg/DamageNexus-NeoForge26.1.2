package io.github.naimjeg.damagenexus.core.pipeline;

import io.github.naimjeg.damagenexus.api.DamageNexusTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

public final class DamageSourcePolicy {

    private DamageSourcePolicy() {
    }

    public static boolean shouldManage(DamageSource source) {
        if (source == null) {
            return false;
        }

        if (source.is(DamageNexusTags.DamageTypes.BYPASSES_DAMAGENEXUS)) {
            return false;
        }

        /*
         * Keep hard-vanilla special damage outside DN by default.
         *
         * Examples usually include damage types intended to ignore ordinary
         * survival mechanics, such as out-of-world / kill-style damage.
         *
         * If you want DamageNexus to own even these sources, remove this check
         * and rely only on the custom BYPASSES_DAMAGENEXUS tag.
         */
        return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }
}
