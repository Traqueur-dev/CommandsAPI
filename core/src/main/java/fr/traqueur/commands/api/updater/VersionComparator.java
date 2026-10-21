package fr.traqueur.commands.api.updater;

import java.util.Locale;

/**
 * Compares two versions of the library, such as {@code 5.3.1} and {@code 5.4.0-SNAPSHOT}.
 * <p>
 * Numeric parts are compared as numbers, so {@code 5.10.0} is newer than {@code 5.9.0}. At
 * equal numbers, a {@code -SNAPSHOT} (or any other qualifier) is older than the release. A
 * version that does not start with a number, such as a build named after a commit, cannot be
 * compared, and the result is {@link #UNKNOWN}.
 * </p>
 *
 * @since 5.4.0
 */
final class VersionComparator {

    /**
     * Returned when one of the versions cannot be compared.
     */
    static final int UNKNOWN = Integer.MIN_VALUE;

    private VersionComparator() {
    }

    /**
     * Compares two versions.
     *
     * @param current The current version.
     * @param latest  The latest published version.
     * @return a negative number when current is older, 0 when equal, a positive number when
     * current is newer, or {@link #UNKNOWN} when they cannot be compared.
     */
    static int compare(String current, String latest) {
        Parsed a = parse(current);
        Parsed b = parse(latest);
        if (a == null || b == null) {
            return UNKNOWN;
        }
        int length = Math.max(a.numbers.length, b.numbers.length);
        for (int i = 0; i < length; i++) {
            long x = i < a.numbers.length ? a.numbers[i] : 0;
            long y = i < b.numbers.length ? b.numbers[i] : 0;
            if (x != y) {
                return x < y ? -1 : 1;
            }
        }
        if (a.qualified == b.qualified) {
            return 0;
        }
        return a.qualified ? -1 : 1;
    }

    private static Parsed parse(String version) {
        if (version == null) {
            return null;
        }
        String trimmed = version.trim().toLowerCase(Locale.ROOT);
        int dash = trimmed.indexOf('-');
        String numeric = dash < 0 ? trimmed : trimmed.substring(0, dash);
        if (numeric.isEmpty()) {
            return null;
        }
        String[] parts = numeric.split("\\.");
        long[] numbers = new long[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                numbers[i] = Long.parseLong(parts[i]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return new Parsed(numbers, dash >= 0);
    }

    private record Parsed(long[] numbers, boolean qualified) {
    }
}
