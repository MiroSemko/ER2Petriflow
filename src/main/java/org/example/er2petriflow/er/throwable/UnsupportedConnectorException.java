package org.example.er2petriflow.er.throwable;

import lombok.Getter;
import org.example.er2petriflow.er.json.Connector;

public class UnsupportedConnectorException extends IllegalArgumentException {

    @Getter
    private final Connector connector;

    public UnsupportedConnectorException(String s, Connector connector) {
        super(s);
        this.connector = connector;
    }
}
