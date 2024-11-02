package examples.common.pico.utils;

import examples.common.Format;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class FormatConverter implements ITypeConverter<Format> {
    @Override
    public Format convert(String value) {
        Format format = Format.valueOf(value.toUpperCase());
        if (format == null || !format.isSupported()) {
            throw new TypeConversionException("Unsupported format " + value + ". Supported formats: " + String.join(", ", Format.supportedFormats()));
        }
        return format;
    }
}
