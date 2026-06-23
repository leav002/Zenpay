package com.zenpay.domain.user.service;

import com.zenpay.domain.user.dto.LoginRequest;
import com.zenpay.domain.user.dto.SignupRequest;
import com.zenpay.domain.user.dto.TokenResponse;
import com.zenpay.domain.user.dto.UserResponse;
import com.zenpay.domain.user.entity.User;
import com.zenpay.domain.user.repository.UserRepository;
import com.zenpay.global.exception.BusinessException;
import com.zenpay.global.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_PREFIX = "refresh:";

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.conflict("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw BusinessException.conflict("이미 사용 중인 전화번호입니다.");
        }

        User user = User.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getPhone()
        );
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> BusinessException.badRequest("이메일 또는 비밀번호가 틀렸습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw BusinessException.badRequest("이메일 또는 비밀번호가 틀렸습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Redis에 Refresh Token 저장 (7일)
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + user.getId(),
                refreshToken,
                7, TimeUnit.DAYS
        );

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse reissue(String refreshToken) {
        if (!jwtUtil.isValid(refreshToken) ||
                !"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            throw BusinessException.badRequest("유효하지 않은 Refresh Token입니다.");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        String saved = redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);

        if (!refreshToken.equals(saved)) {
            throw BusinessException.badRequest("만료되었거나 이미 사용된 토큰입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("유저를 찾을 수 없습니다."));

        // RTR: 새 토큰 발급하면서 기존 Refresh Token 교체
        String newAccess = jwtUtil.generateAccessToken(userId, user.getEmail());
        String newRefresh = jwtUtil.generateRefreshToken(userId);

        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + userId,
                newRefresh,
                7, TimeUnit.DAYS
        );

        return new TokenResponse(newAccess, newRefresh);
    }

    public void logout(Long userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("유저를 찾을 수 없습니다."));
        return UserResponse.from(user);
    }
}