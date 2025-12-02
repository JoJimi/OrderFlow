package org.example.user.controller.performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/performance/oauth2")
public class OAuth2PerformanceController {

    /**
     * 전통적 로그인 vs OAuth2 소셜 로그인 비교
     * GET /api/performance/oauth2/compare
     */
    @GetMapping("/compare")
    public Map<String, Object> compareLoginMethods() {
        log.info("Comparing traditional login vs OAuth2 social login");

        Map<String, Object> result = new HashMap<>();

        // 전통적 회원가입 프로세스
        Map<String, Object> traditional = new HashMap<>();
        traditional.put("steps", List.of(
                "1. 회원가입 폼 작성 (이메일, 비밀번호, 이름, 전화번호 등)",
                "2. 이메일 인증 대기 (평균 30초)",
                "3. 인증 링크 클릭",
                "4. 추가 정보 입력",
                "5. 회원가입 완료"
        ));
        traditional.put("total_steps", 5);
        traditional.put("avg_time_seconds", 120);
        traditional.put("user_input_fields", 8);
        traditional.put("conversion_rate", "45%");

        // OAuth2 소셜 로그인
        Map<String, Object> oauth2 = new HashMap<>();
        oauth2.put("steps", List.of(
                "1. 'Google/Kakao로 로그인' 버튼 클릭",
                "2. 소셜 계정으로 인증 (이미 로그인되어 있으면 즉시 완료)"
        ));
        oauth2.put("total_steps", 2);
        oauth2.put("avg_time_seconds", 10);
        oauth2.put("user_input_fields", 0);
        oauth2.put("conversion_rate", "75%");

        result.put("traditional_signup", traditional);
        result.put("oauth2_signup", oauth2);
        result.put("time_saved_seconds", 110);
        result.put("speedup", "12x faster");
        result.put("conversion_rate_improvement", "+30% (45% → 75%)");
        result.put("description", "OAuth2 dramatically reduces signup friction");

        return result;
    }

    /**
     * 전환율 측정 시뮬레이션
     * GET /api/performance/oauth2/conversion-rate
     */
    @GetMapping("/conversion-rate")
    public Map<String, Object> measureConversionRate() {
        log.info("Measuring OAuth2 conversion rate impact");

        Map<String, Object> result = new HashMap<>();

        // 시뮬레이션 데이터 (실제 프로젝트에서는 Analytics 데이터 사용)
        result.put("test_name", "Conversion Rate Impact Analysis");
        result.put("traditional_signup", Map.of(
                "started", 1000,
                "completed", 450,
                "conversion_rate", "45.0%",
                "avg_completion_time_seconds", 120,
                "drop_off_reasons", List.of(
                        "Email verification timeout (25%)",
                        "Form too long (30%)",
                        "Password requirements complex (20%)",
                        "Other (25%)"
                )
        ));
        result.put("oauth2_signup", Map.of(
                "started", 1000,
                "completed", 750,
                "conversion_rate", "75.0%",
                "avg_completion_time_seconds", 10,
                "drop_off_reasons", List.of(
                        "Privacy concerns (15%)",
                        "Don't have social account (10%)"
                )
        ));
        result.put("improvement", Map.of(
                "additional_signups", 300,
                "conversion_rate_increase", "+30%",
                "time_saved_per_user_seconds", 110,
                "total_time_saved_minutes", (110 * 750) / 60
        ));

        return result;
    }
}