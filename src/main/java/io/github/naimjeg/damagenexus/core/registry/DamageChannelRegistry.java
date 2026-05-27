package io.github.naimjeg.damagenexus.core.registry;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.slf4j.Logger;

import java.util.*;

public class DamageChannelRegistry extends SimpleJsonResourceReloadListener<DamageChannelRegistry.ChannelDefinition> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ChannelData UNTYPED_DATA = new ChannelData(
            new DamageChannel(DamageChannel.UNTYPED_ID, 0),
            List.of(),
            null,
            true,
            Integer.MIN_VALUE
    );
    private static volatile ChannelData[] CHANNELS_BY_INDEX = new ChannelData[] {
            UNTYPED_DATA
    };

    private static volatile ChannelData[] MATCH_ORDER = new ChannelData[] {
            UNTYPED_DATA
    };

    private static volatile Map<Identifier, ChannelData> CHANNELS_BY_ID = Map.of(
            DamageChannel.UNTYPED_ID,
            UNTYPED_DATA
    );

    public static DamageChannel getPhysical() {
        return getChannelOrUntyped(DamageChannel.PHYSICAL_ID);
    }

    public static DamageChannel getFire() {
        return getChannelOrUntyped(DamageChannel.FIRE_ID);
    }

    public static DamageChannel getMagic() {
        return getChannelOrUntyped(DamageChannel.MAGIC_ID);
    }

    public record ChannelDefinition(
            Identifier channel,
            List<TagKey<DamageType>> triggerTags,
            Optional<Identifier> resistanceAttribute,
            boolean affectedByArmor,
            int priority
    ) {
        public static final Codec<ChannelDefinition> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Identifier.CODEC
                                .fieldOf("channel")
                                .forGetter(ChannelDefinition::channel),

                        TagKey.codec(Registries.DAMAGE_TYPE)
                                .listOf()
                                .optionalFieldOf("trigger_tags", List.of())
                                .forGetter(ChannelDefinition::triggerTags),

                        Identifier.CODEC
                                .optionalFieldOf("resistance_attribute")
                                .forGetter(ChannelDefinition::resistanceAttribute),

                        Codec.BOOL
                                .optionalFieldOf("affected_by_armor", true)
                                .forGetter(ChannelDefinition::affectedByArmor),

                        Codec.INT
                                .optionalFieldOf("priority", 0)
                                .forGetter(ChannelDefinition::priority)
                ).apply(instance, ChannelDefinition::new));
    }

    public DamageChannelRegistry() {
        super(ChannelDefinition.CODEC, FileToIdConverter.json("damagenexus_channels"));
    }

    @Override
    protected void apply(
            Map<Identifier, ChannelDefinition> prepared,
            ResourceManager manager,
            ProfilerFiller profiler
    ) {
        Map<Identifier, ChannelData> nextById = new HashMap<>();
        List<ChannelData> nextByIndex = new ArrayList<>();

        nextById.put(DamageChannel.UNTYPED_ID, UNTYPED_DATA);
        nextByIndex.add(UNTYPED_DATA);

        prepared.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getValue().channel().toString()))
                .forEach(entry -> {
                    Identifier fileId = entry.getKey();
                    ChannelDefinition def = entry.getValue();

                    try {
                        Identifier channelId = def.channel();

                        if (channelId.equals(DamageChannel.UNTYPED_ID)) {
                            LOGGER.warn(
                                    "Ignoring datapack definition for reserved channel {} from {}",
                                    channelId,
                                    fileId
                            );
                            return;
                        }

                        if (nextById.containsKey(channelId)) {
                            LOGGER.warn(
                                    "Duplicate DamageNexus channel definition {} from {}, skipping.",
                                    channelId,
                                    fileId
                            );
                            return;
                        }

                        Holder<Attribute> resistanceAttr = null;

                        if (def.resistanceAttribute().isPresent()) {
                            Identifier attrId = def.resistanceAttribute().get();

                            resistanceAttr = BuiltInRegistries.ATTRIBUTE
                                    .get(attrId)
                                    .orElse(null);

                            if (resistanceAttr == null) {
                                LOGGER.warn(
                                        "DamageNexus channel {} references missing resistance attribute {}",
                                        channelId,
                                        attrId
                                );
                            }
                        }

                        int denseIndex = nextByIndex.size();

                        DamageChannel channel = new DamageChannel(channelId, denseIndex);

                        ChannelData data = new ChannelData(
                                channel,
                                List.copyOf(def.triggerTags()),
                                resistanceAttr,
                                def.affectedByArmor(),
                                def.priority()
                        );

                        nextById.put(channelId, data);
                        nextByIndex.add(data);

                        LOGGER.debug(
                                "Loaded DamageNexus channel {} with dense index {}",
                                channelId,
                                denseIndex
                        );

                    } catch (Exception e) {
                        LOGGER.error("Failed to process DamageNexus channel data: {}", fileId, e);
                    }
                });

        ChannelData[] indexedArray = nextByIndex.toArray(ChannelData[]::new);

        ChannelData[] matchArray = nextByIndex.stream()
                .filter(data -> !data.channel.id().equals(DamageChannel.UNTYPED_ID))
                .sorted(
                        Comparator
                                .comparingInt(ChannelData::priority)
                                .reversed()
                                .thenComparing(data -> data.channel.id().toString())
                )
                .toArray(ChannelData[]::new);

        CHANNELS_BY_ID = Map.copyOf(nextById);
        CHANNELS_BY_INDEX = indexedArray;
        MATCH_ORDER = matchArray;

        LOGGER.info(
                "Successfully loaded {} DamageNexus channels.",
                CHANNELS_BY_INDEX.length
        );
    }

    public static int channelCount() {
        return CHANNELS_BY_INDEX.length;
    }

    public static DamageChannel getUntyped() {
        return CHANNELS_BY_INDEX[0].channel;
    }

    public static DamageChannel getChannelOrUntyped(Identifier id) {
        ChannelData data = CHANNELS_BY_ID.get(id);
        return data != null ? data.channel : getUntyped();
    }

    public static boolean containsChannel(Identifier id) {
        return CHANNELS_BY_ID.containsKey(id);
    }

    public static DamageChannel resolve(DamageChannel channel) {
        if (channel == null) {
            return getUntyped();
        }

        ChannelData[] byIndex = CHANNELS_BY_INDEX;

        int index = channel.index();

        if (index >= 0 && index < byIndex.length) {
            ChannelData data = byIndex[index];

            if (data.channel.id().equals(channel.id())) {
                return data.channel;
            }
        }

        ChannelData byId = CHANNELS_BY_ID.get(channel.id());
        return byId != null ? byId.channel : getUntyped();
    }

    public static DamageChannel determineInitialChannel(DamageSource source) {
        for (ChannelData data : MATCH_ORDER) {
            for (TagKey<DamageType> tag : data.triggerTags) {
                if (source.is(tag)) {
                    return data.channel;
                }
            }
        }

        return getUntyped();
    }

    public static Holder<Attribute> getResistanceAttribute(DamageChannel rawChannel) {
        DamageChannel channel = resolve(rawChannel);

        int index = channel.index();

        ChannelData[] byIndex = CHANNELS_BY_INDEX;

        if (index < 0 || index >= byIndex.length) {
            return null;
        }

        return byIndex[index].resistanceAttribute;
    }

    public static ChannelData getData(DamageChannel rawChannel) {
        DamageChannel channel = resolve(rawChannel);

        int index = channel.index();

        ChannelData[] byIndex = CHANNELS_BY_INDEX;

        if (index < 0 || index >= byIndex.length) {
            return UNTYPED_DATA;
        }

        return byIndex[index];
    }

    public record ChannelData(
            DamageChannel channel,
            List<TagKey<DamageType>> triggerTags,
            Holder<Attribute> resistanceAttribute,
            boolean affectedByArmor,
            int priority
    ) {}
}