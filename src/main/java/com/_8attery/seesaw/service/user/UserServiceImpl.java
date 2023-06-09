package com._8attery.seesaw.service.user;

import com._8attery.seesaw.domain.user.User;
import com._8attery.seesaw.domain.user.UserRepository;
import com._8attery.seesaw.dto.api.request.NicknameRequestDto;
import com._8attery.seesaw.dto.api.response.UserHistoryResponseDto;
import com._8attery.seesaw.exception.BaseException;
import com._8attery.seesaw.exception.custom.ResourceNotFoundException;
import com._8attery.seesaw.service.util.ServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com._8attery.seesaw.exception.BaseResponseStatus.DATABASE_ERROR;
import static java.time.temporal.ChronoUnit.DAYS;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ServiceUtils serviceUtils;

    @Override
    public User resolveUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 id를 가진 사용자를 찾을 수 없습니다."));
    }

    // 닉네임 수정
    @Override
    @Transactional
    public void updateNickname(Long userId, NicknameRequestDto nicknameRequestDto) throws BaseException {
        try {
            userRepository.updateUserNickname(userId, nicknameRequestDto.getNickName());

        } catch (Exception exception) {
            log.info(exception.getMessage());
            exception.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Override
    public String getUserNickname(Long userId) throws BaseException {
        try {

            return userRepository.findUserNickname(userId);

        } catch (Exception exception) {
            log.info(exception.getMessage());
            exception.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 회원 탈퇴 화면 - 함께 한 일수, 가치 목록 조회 API
    @Override
    public UserHistoryResponseDto getUserHistory(Long userId) throws BaseException {
        try {

            UserHistoryResponseDto res = new UserHistoryResponseDto();

            // 함께 한 일수
            LocalDate createdAt = userRepository.findUserCreatedAt(userId);
            LocalDate today = LocalDate.now();

            Long days = DAYS.between(createdAt, today) + 1;
            res.setDayCount(days);

            // 가치 목록
            List<String> values = userRepository.findValues(userId);
            res.setValueNames(values);

            return res;

        } catch (Exception exception) {
            log.info(exception.getMessage());
            exception.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User retrievedUser = serviceUtils.retrieveUserById(userId);
        userRepository.delete(retrievedUser);
    }

    @Override
    public String getUserEmail(Long userId) {
        return serviceUtils.retrieveUserById(userId).getEmail();
    }
}
