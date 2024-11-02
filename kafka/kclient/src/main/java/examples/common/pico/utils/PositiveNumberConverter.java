package examples.common.pico.utils;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class PositiveNumberConverter implements ITypeConverter<Integer> {
    @Override
    public Integer convert(String value) {
        int retValue = Integer.valueOf(value);
        if (retValue < 0) {
            throw new TypeConversionException("Invalid format: Must be a positive number");
        }
        return retValue;
    }
}
