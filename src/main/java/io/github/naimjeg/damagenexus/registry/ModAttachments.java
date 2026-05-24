package io.github.naimjeg.damagenexus.registry;

import io.github.naimjeg.damagenexus.DamageNexus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DamageNexus.MODID);

    public static final Supplier<AttachmentType<Boolean>> PENDING_JUMP_CRIT = ATTACHMENTS.register(
            "pending_jump_crit",
            () -> AttachmentType.builder(() -> false).build()
    );
}