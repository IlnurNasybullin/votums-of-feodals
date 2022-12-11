package io.github.ilnurnasybullin.votums.of.feodals.core.lord;

import java.util.Objects;

public record Lord(String name, Status status) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;

        var other = (Lord) obj;
        return Objects.equals(name(), other.name());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
