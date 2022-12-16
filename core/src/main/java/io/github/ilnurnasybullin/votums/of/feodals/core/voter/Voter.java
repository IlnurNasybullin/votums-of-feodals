package io.github.ilnurnasybullin.votums.of.feodals.core.voter;

import java.util.Objects;

public record Voter(String name, Status status) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;

        var other = (Voter) obj;
        return Objects.equals(name(), other.name());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
