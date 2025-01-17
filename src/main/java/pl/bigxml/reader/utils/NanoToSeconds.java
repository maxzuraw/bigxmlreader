package pl.bigxml.reader.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NanoToSeconds {

    public static double toSeconds(long nanoseconds) {
        return nanoseconds / 1_000_000_000.0;
    }
}
