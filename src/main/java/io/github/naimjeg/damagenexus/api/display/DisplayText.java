package io.github.naimjeg.damagenexus.api.display;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record DisplayText(
        Optional<String> translate,
        Optional<String> text,
        List<String> args,
        Optional<String> fallback
) {
    private static final Codec<DisplayText> OBJECT_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING
                            .optionalFieldOf("translate")
                            .forGetter(DisplayText::translate),

                    Codec.STRING
                            .optionalFieldOf("text")
                            .forGetter(DisplayText::text),

                    Codec.STRING
                            .listOf()
                            .optionalFieldOf("args", List.of())
                            .forGetter(DisplayText::args),

                    Codec.STRING
                            .optionalFieldOf("fallback")
                            .forGetter(DisplayText::fallback)
            ).apply(instance, DisplayText::new));

    /**
     * Backward compatible:
     *
     * Old JSON:
     *   "name": "Sharp Edge"
     *
     * New JSON:
     *   "name": { "translate": "affix.damagenexus.sharp_edge" }
     */
    public static final Codec<DisplayText> CODEC =
            Codec.either(Codec.STRING, OBJECT_CODEC)
                    .xmap(
                            either -> either.map(
                                    DisplayText::literal,
                                    Function.identity()
                            ),
                            display -> display.translate().isPresent()
                                    ? Either.right(display)
                                    : Either.left(display.text()
                                    .or(() -> display.fallback())
                                    .orElse(""))
                    );

    public static final DisplayText EMPTY =
            new DisplayText(
                    Optional.empty(),
                    Optional.empty(),
                    List.of(),
                    Optional.empty()
            );

    public DisplayText {
        translate = normalize(translate);
        text = normalize(text);
        fallback = normalize(fallback);
        args = args == null ? List.of() : List.copyOf(args);
    }

    public static DisplayText literal(String text) {
        return new DisplayText(
                Optional.empty(),
                Optional.ofNullable(text),
                List.of(),
                Optional.empty()
        );
    }

    public static DisplayText translatable(String key, String... args) {
        return new DisplayText(
                Optional.ofNullable(key),
                Optional.empty(),
                args == null ? List.of() : Arrays.asList(args),
                Optional.empty()
        );
    }

    public static DisplayText translatableWithFallback(
            String key,
            String fallback,
            String... args
    ) {
        return new DisplayText(
                Optional.ofNullable(key),
                Optional.empty(),
                args == null ? List.of() : Arrays.asList(args),
                Optional.ofNullable(fallback)
        );
    }

    public boolean isBlank() {
        return translate.isEmpty()
                && text.map(String::isBlank).orElse(true)
                && fallback.map(String::isBlank).orElse(true);
    }

    public String debugString() {
        if (translate.isPresent()) {
            return translate.get();
        }

        return text.or(() -> fallback).orElse("");
    }

    private static Optional<String> normalize(Optional<String> input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }

        String value = input.get();

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(value);
    }
}