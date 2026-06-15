package me.stky.relaytd.api.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true) // Apply to all entities
//@Converter() // require @Convert(converter = StringDatabaseNormConverter.class) on each field
public class StringDatabaseNormConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        String trimmed = attribute.trim();
        return "".equalsIgnoreCase(trimmed) ? null : trimmed;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : dbData.trim();
    }

}