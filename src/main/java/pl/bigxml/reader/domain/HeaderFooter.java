package pl.bigxml.reader.domain;

import lombok.Getter;

@Getter
public class HeaderFooter {
    private final StringBuilder header = new StringBuilder();
    private final StringBuilder footer = new StringBuilder();
}
