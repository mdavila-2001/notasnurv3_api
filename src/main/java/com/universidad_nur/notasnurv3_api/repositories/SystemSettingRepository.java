package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Integer> {
    Optional<SystemSetting> findBySettingKey(String settingKey);
}
