package com.ieum.ansimdonghaeng.domain.code.service;

import com.ieum.ansimdonghaeng.domain.code.dto.response.CodeLookupResponse;
import com.ieum.ansimdonghaeng.domain.code.repository.AvailableTimeSlotCodeRepository;
import com.ieum.ansimdonghaeng.domain.code.repository.ProjectTypeCodeRepository;
import com.ieum.ansimdonghaeng.domain.code.repository.RegionCodeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeLookupService {

    private final ProjectTypeCodeRepository projectTypeCodeRepository;
    private final RegionCodeRepository regionCodeRepository;
    private final AvailableTimeSlotCodeRepository availableTimeSlotCodeRepository;

    public List<CodeLookupResponse> getProjectTypes() {
        return projectTypeCodeRepository.findAllByActiveYnTrueOrderBySortOrderAscCodeAsc().stream()
                .map(code -> new CodeLookupResponse(
                        code.getCode(),
                        code.getName(),
                        code.getSortOrder(),
                        null,
                        null,
                        null,
                        null
                ))
                .toList();
    }

    public List<CodeLookupResponse> getRegions() {
        return regionCodeRepository.findAllByActiveYnTrueOrderByRegionLevelAscNameAscCodeAsc().stream()
                .map(code -> new CodeLookupResponse(
                        code.getCode(),
                        code.getName(),
                        null,
                        code.getParentRegionCode(),
                        code.getRegionLevel(),
                        null,
                        null
                ))
                .toList();
    }

    public List<CodeLookupResponse> getAvailableTimeSlots() {
        return availableTimeSlotCodeRepository.findAllByActiveYnTrueOrderBySortOrderAscCodeAsc().stream()
                .map(code -> new CodeLookupResponse(
                        code.getCode(),
                        code.getName(),
                        code.getSortOrder(),
                        null,
                        null,
                        code.getStartMinute(),
                        code.getEndMinute()
                ))
                .toList();
    }
}
