package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.entities.Modality;
import com.universidad_nur.notasnurv3_api.entities.SystemSetting;
import com.universidad_nur.notasnurv3_api.repositories.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    @Transactional(readOnly = true)
    public List<SystemSetting> getAllSettings() {
        return systemSettingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public String getSettingValue(String key, String defaultValue) {
        Optional<SystemSetting> setting = systemSettingRepository.findBySettingKey(key);
        return setting.map(SystemSetting::getSettingValue).orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public int getIntValue(String key, int defaultValue) {
        String valueStr = getSettingValue(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Transactional(readOnly = true)
    public int getAbsenceLimit(Modality modality) {
        return switch (modality) {
            case FACE_TO_FACE -> resolveIntSetting("MAX_ABSENCES_PRESENTIAL", "ABSENCE_LIMIT_FACE_TO_FACE", 5);
            case BLENDED -> resolveIntSetting("MAX_ABSENCES_BLENDED", "ABSENCE_LIMIT_BLENDED", 3);
            case ONLINE -> resolveIntSetting("MAX_ABSENCES_ONLINE", "ABSENCE_LIMIT_ONLINE", 999);
        };
    }

    private int resolveIntSetting(String primaryKey, String legacyKey, int defaultValue) {
        return systemSettingRepository.findBySettingKey(primaryKey)
                .or(() -> systemSettingRepository.findBySettingKey(legacyKey))
                .map(SystemSetting::getSettingValue)
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    @Transactional
    public SystemSetting updateSetting(String key, String value, String description) {
        Optional<SystemSetting> existingOpt = systemSettingRepository.findBySettingKey(key);
        SystemSetting setting;
        if (existingOpt.isPresent()) {
            setting = existingOpt.get();
            setting.setSettingValue(value);
            if (description != null) {
                setting.setDescription(description);
            }
        } else {
            setting = SystemSetting.builder()
                    .settingKey(key)
                    .settingValue(value)
                    .description(description)
                    .build();
        }
        return systemSettingRepository.save(setting);
    }

    @Transactional(readOnly = true)
    public RoundingMode getRoundingMode() {
        String roundingType = getSettingValue("NUR_ROUNDING_MODE", "HALF_UP");
        return switch (roundingType.toUpperCase()) {
            case "HALF_UP" -> RoundingMode.HALF_UP;
            case "HALF_DOWN" -> RoundingMode.HALF_DOWN;
            case "HALF_EVEN" -> RoundingMode.HALF_EVEN;
            case "UP" -> RoundingMode.UP;
            case "DOWN" -> RoundingMode.DOWN;
            case "CEILING" -> RoundingMode.CEILING;
            case "FLOOR" -> RoundingMode.FLOOR;
            default -> RoundingMode.HALF_UP; // Default NUR standard
        };
    }
}
