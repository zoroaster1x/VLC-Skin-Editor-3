package dev.zoroaster1x.vlcskin.edit;

import java.util.ArrayList;
import java.util.List;

/**
 * A command built from independent steps that apply and revert in order.
 */
public final class ValueCommand implements Command {

    /**
     * One reversible setter call.
     */
    public record Step(Runnable apply, Runnable revert) {
    }

    private final String description;
    private final List<Step> steps;

    private ValueCommand(String description, List<Step> steps) {
        this.description = description;
        this.steps = steps;
    }

    @Override
    public void apply() {
        for (Step step : steps) {
            step.apply().run();
        }
    }

    @Override
    public void undo() {
        for (int i = steps.size() - 1; i >= 0; i--) {
            steps.get(i).revert().run();
        }
    }

    @Override
    public String description() {
        return description;
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    public static Builder builder(String description) {
        return new Builder(description);
    }

    /**
     * Collects steps, skipping any pair that is not an actual change.
     */
    public static final class Builder {

        private final String description;
        private final List<Step> steps = new ArrayList<>();

        private Builder(String description) {
            this.description = description;
        }

        public Builder step(Runnable apply, Runnable revert) {
            steps.add(new Step(apply, revert));
            return this;
        }

        /**
         * Adds a step only when oldValue and newValue differ.
         */
        public <T> Builder set(T oldValue, T newValue, java.util.function.Consumer<T> setter) {
            if (oldValue == null ? newValue != null : !oldValue.equals(newValue)) {
                steps.add(new Step(() -> setter.accept(newValue), () -> setter.accept(oldValue)));
            }
            return this;
        }

        public ValueCommand build() {
            return new ValueCommand(description, List.copyOf(steps));
        }
    }
}
