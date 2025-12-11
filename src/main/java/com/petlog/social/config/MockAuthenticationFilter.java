package com.petlog.social.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Collections;

// ⚠️ 개발용 임시 필터입니다. 배포 시 반드시 삭제하세요!
public class MockAuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 1. 가짜 유저 정보 생성 (ID: 1L, Email: test@test.com, Role: USER)
        // 실제 Principal 객체(CustomUserDetails 등)가 있다면 그에 맞춰 생성하는 것이 가장 좋습니다.
        // 여기서는 간단하게 문자열("testUser")이나 ID(1L)를 넣습니다.
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        1L, // Principal: 컨트롤러에서 @AuthenticationPrincipal로 받을 값 (ID 등)
                        null, // Credentials (비밀번호 등, 보통 null)
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // 권한
                );

        // 2. SecurityContext에 강제 주입 (로그인 된 척 하기)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. 다음 필터로 진행
        chain.doFilter(request, response);
    }
}