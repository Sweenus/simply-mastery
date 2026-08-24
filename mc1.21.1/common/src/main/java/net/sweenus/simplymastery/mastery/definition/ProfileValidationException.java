package net.sweenus.simplymastery.mastery.definition;

import java.util.List;

public final class ProfileValidationException extends IllegalArgumentException {

    private final List<String> errors;

    public ProfileValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
