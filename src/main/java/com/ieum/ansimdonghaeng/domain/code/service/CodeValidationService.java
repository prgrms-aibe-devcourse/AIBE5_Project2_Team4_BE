package com.ieum.ansimdonghaeng.domain.code.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.code.repository.AvailableTimeSlotCodeRepository;
import com.ieum.ansimdonghaeng.domain.code.repository.ProjectTypeCodeRepository;
import com.ieum.ansimdonghaeng.domain.code.repository.RegionCodeRepository;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeValidationService {

    private final ProjectTypeCodeRepository projectTypeCodeRepository;
    private final RegionCodeRepository regionCodeRepository;
    private final AvailableTimeSlotCodeRepository availableTimeSlotCodeRepository;

    public void validateProjectTypeCode(String code, String fieldName) {
        validateCode(code, fieldName, projectTypeCodeRepository::existsByCodeAndActiveYnTrue);
    }

    public void validateRegionCode(String code, String fieldName) {
        validateCode(code, fieldName, regionCodeRepository::existsByCodeAndActiveYnTrue);
    }

    public void validateProjectTypeCodes(Set<String> codes, String fieldName) {
        validateCodes(codes, fieldName, projectTypeCodeRepository::existsByCodeAndActiveYnTrue);
    }

    public void validateRegionCodes(Set<String> codes, String fieldName) {
        validateCodes(codes, fieldName, regionCodeRepository::existsByCodeAndActiveYnTrue);
    }

    public void validateAvailableTimeSlotCodes(Set<String> codes, String fieldName) {
        validateCodes(codes, fieldName, availableTimeSlotCodeRepository::existsByCodeAndActiveYnTrue);
    }

    private void validateCode(String code, String fieldName, Predicate<? super String> existsPredicate) {
        if (!StringUtils.hasText(code)) {
            return;
        }
        if (!existsPredicate.test(code)) {
            throw invalidCode(fieldName, Set.of(code));
        }
    }

    private void validateCodes(Collection<String> codes,
                               String fieldName,
                               Predicate<? super String> existsPredicate) {
        if (codes == null || codes.isEmpty()) {
            return;
        }
        Set<String> invalidCodes = new LinkedHashSet<>();
        for (String code : codes) {
            if (!StringUtils.hasText(code) || !existsPredicate.test(code)) {
                invalidCodes.add(code);
            }
        }
        if (!invalidCodes.isEmpty()) {
            throw invalidCode(fieldName, invalidCodes);
        }
    }

    private CustomException invalidCode(String fieldName, Collection<String> invalidCodes) {
        return new CustomException(
                ErrorCode.INVALID_INPUT_VALUE,
                fieldName + ": unsupported code(s) " + invalidCodes
        );
    }
}
