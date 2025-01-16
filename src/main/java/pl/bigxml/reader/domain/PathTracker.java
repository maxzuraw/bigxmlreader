package pl.bigxml.reader.domain;

import org.apache.logging.log4j.util.Strings;

import java.util.LinkedList;
import java.util.List;


public class PathTracker {

    private final List<String> track;

    public PathTracker() {
        track = new LinkedList<>();
    }

    public void addNextElement(String elementName) {
        track.add(elementName);
    }

    public String getLastElement() {
        return track.get(track.size() - 1);
    }

    public void removeLastElement() {
        if (track.isEmpty()) {
            throw new IllegalStateException("Cannot remove from an empty track list");
        }
        track.remove(track.size() - 1);
    }

    public String getFullTrack() {
        return Strings.join(track, '.');
    }
}
