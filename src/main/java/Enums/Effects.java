package Enums;

import java.util.EnumSet;
import java.util.Set;

public enum Effects {
    NOEFFECT(0),
    AFTERBURNER(1),
    ASTEROIDFIELD(2),
    GASCLOUD(4),
    SUPERFOOD(8),
    SHIELD(16);

    public final Integer value;

    private Effects(Integer value) {
        this.value = value;
    }

    public EnumSet<Effects> getEffects() {
        EnumSet effects = EnumSet.noneOf(Effects.class);
        for (var effect : Effects.values()) {
            if ((value & effect.value) != 0) {
                effects.add(effect);
            }
        }
        return effects;
    }

    public static EnumSet<Effects> getEffects(int value) {
        EnumSet effects = EnumSet.noneOf(Effects.class);
        for (var effect : Effects.values()) {
            if ((value & effect.value) != 0) {
                effects.add(effect);
            }
        }
        return effects;
    }

    public Integer getValue(Set<Effects> effectsSet) {
        Integer val = 0;
        for (var e : effectsSet) {
            val += e.value;
        }
        return val;
    }
}
