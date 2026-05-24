package io.github.naimjeg.damagenexus.core;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;

public record DamageOperation(String source, DamagePhase phase, String type, float value) {}