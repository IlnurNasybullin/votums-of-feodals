package io.github.ilnurnasybullin.votums.of.feodals.core.fief;

import java.util.Objects;

public record Fief(String name, int value) {

    public final static int MIN_VALUE = 1;
    public final static int MAX_VALUE = 10;

    public static Fief of(String name, int value) {
        checkValue(value);

        return new Fief(name, value);
    }

    private static void checkValue(int value) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new IllegalArgumentException(String.format(
                    "Value must be in range [%d, %d], current value is %d",
                    MIN_VALUE, MAX_VALUE, value
            ));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        var other = (Fief) obj;

        return Objects.equals(name(), other.name());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static class Builder {

        private String name;
        private int value;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder value(int value) {
            this.value = value;
            return this;
        }

        public Fief build() {
            return Fief.of(name, value);
        }
    }
}
