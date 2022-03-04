package org.example.er2petriflow.er;

import java.io.InputStream;

public class TestHelper {

    public static InputStream getTestFile(String fileName) {
        return ImporterTests.class.getResourceAsStream("/" + fileName + ".erdplus");
    }
}
