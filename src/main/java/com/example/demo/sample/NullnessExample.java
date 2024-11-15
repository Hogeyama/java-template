package com.example.demo.sample;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class NullnessExample {
    private final String nonNullField;
    private final @Nullable String nullableField;

    public NullnessExample(String nonNullField, @Nullable String nullableField) {
        this.nonNullField = nonNullField;
        this.nullableField = nullableField;
    }

    public String getNonNullField() {
        return nonNullField;
    }

    public @Nullable String getNullableField() {
        return nullableField;
    }

    public String processNullableField() {
        if (nullableField == null) {
            return "Field was null";
        }
        return "Field value: " + nullableField;
    }
}
