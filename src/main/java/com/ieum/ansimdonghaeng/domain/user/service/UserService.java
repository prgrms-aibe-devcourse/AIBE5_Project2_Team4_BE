package com.ieum.ansimdonghaeng.domain.user.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.user.dto.request.UserProfileUpdateRequest;
import com.ieum.ansimdonghaeng.domain.user.dto.response.PublicUserProfileResponse;
import com.ieum.ansimdonghaeng.domain.user.dto.response.UserProfileResponse;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getMyProfile(Long userId) {
        return toProfileResponse(getActiveUserById(userId));
    }

    @Transactional
    public UserProfileResponse updateMyProfile(Long userId, UserProfileUpdateRequest request) {
        User user = getActiveUserById(userId);
        user.updateProfile(request.name(), request.phone(), request.intro());
        return toProfileResponse(user);
    }

    public PublicUserProfileResponse getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .filter(savedUser -> Boolean.TRUE.equals(savedUser.getActiveYn()))
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "User was not found."));

        return new PublicUserProfileResponse(
                user.getId(),
                user.getName(),
                user.getIntro(),
                user.getRoleCode()
        );
    }

    private User getActiveUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "User was not found."));

        if (Boolean.FALSE.equals(user.getActiveYn())) {
            throw new CustomException(ErrorCode.USER_INACTIVE);
        }

        return user;
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getIntro(),
                user.getRoleCode(),
                Boolean.TRUE.equals(user.getActiveYn())
        );
    }
}
