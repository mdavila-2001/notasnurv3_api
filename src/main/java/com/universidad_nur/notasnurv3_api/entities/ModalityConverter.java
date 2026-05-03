package com.universidad_nur.notasnurv3_api.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ModalityConverter implements AttributeConverter<Modality, String> {

    @Override
    public String convertToDatabaseColumn(Modality attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getApiValue();
    }

    @Override
    public Modality convertToEntityAttribute(String dbData) {
        return Modality.fromValue(dbData);
    }
}
