package com.venkyraghav.examples.flink.util.picocli;

import java.nio.file.Paths;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class FileExistsConverter implements ITypeConverter<String> {
    @Override
    public String convert(String value) {
        try {
            boolean canRead = Paths.get(value).toFile().canRead();
            if (!canRead) {
                throw new TypeConversionException("File " + value + " is not readable");
            }
        } catch (Exception e) {
            throw new TypeConversionException("File " + value + " .Exception: " + e.getLocalizedMessage());
        }
        return value;
    }
}