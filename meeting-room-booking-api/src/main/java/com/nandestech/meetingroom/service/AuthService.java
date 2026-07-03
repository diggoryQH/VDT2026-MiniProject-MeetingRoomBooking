package com.nandestech.meetingroom.service;

import com.nandestech.meetingroom.dto.AuthResponseData;
import com.nandestech.meetingroom.entity.RefreshToken;
import com.nandestech.meetingroom.entity.User;
import com.nandestech.meetingroom.repository.RefreshTokenRepository;
import com.nandestech.meetingroom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasetoTokenService pasetoTokenService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponseData login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String accessToken = pasetoTokenService.generateAccessToken(username);
        String refreshTokenValue = pasetoTokenService.generateRefreshToken(username);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        refreshTokenRepository.save(refreshToken);

        return AuthResponseData.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .build();
    }

    @Transactional
    public AuthResponseData refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Invalid token");
        }

        refreshTokenRepository.delete(refreshToken);

        String username = refreshToken.getUsername();

        String newAccessToken = pasetoTokenService.generateAccessToken(username);
        String newRefreshTokenValue = pasetoTokenService.generateRefreshToken(username);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUsername(username);
        newRefreshToken.setToken(newRefreshTokenValue);
        newRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        refreshTokenRepository.save(newRefreshToken);

        return AuthResponseData.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .build();
    }
}
