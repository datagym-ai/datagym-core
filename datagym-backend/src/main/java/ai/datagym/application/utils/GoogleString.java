package ai.datagym.application.utils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Split (by spaces), divide (numeric and non-numeric with min-length 3) and normalize(trim,lowercase) string for
 * easy use when building criteria, matcher, etc. later on.
 */
public class GoogleString {

    private String originalString;

    private Collection<String> parts = new ArrayList<>();
    private Collection<Long> partsNumeric = new ArrayList<>();
    private Collection<String> partsNotNumeric = new ArrayList<>();

    public GoogleString(String input) {
        this.originalString = input;

        if (input == null) {
            return;
        }

        for (String s : input.split(" ")) {
            if (!s.isEmpty()) {
                parts.add(s.toLowerCase());
                try {
                    Long val = Long.valueOf(s);
                    partsNumeric.add(val);
                } catch (NumberFormatException nfe) {
                    if (s.length() > 2) {
                        partsNotNumeric.add(s.toLowerCase());
                    }
                }
            }
        }
    }

    public String getOriginalString() {
        return originalString;
    }

    public Collection<String> getParts() {
        return parts;
    }

    public Collection<Long> getPartsNumeric() {
        return partsNumeric;
    }

    public Collection<String> getPartsNotNumeric() {
        return partsNotNumeric;
    }

    @Override
    public String toString() {
        return "GoogleString{" +
                "originalString='" + originalString + '\'' +
                ", parts=" + parts +
                ", partsNumeric=" + partsNumeric +
                ", partsNotNumeric=" + partsNotNumeric +
                '}';
    }
}