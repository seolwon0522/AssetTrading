package com.example.AssetTrading.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.AssetTrading.Service.BusinessNumCheckService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/business-verification")
@RequiredArgsConstructor
public class BusinessVerificationController {

    private final BusinessNumCheckService businessNumCheckService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 사업자등록번호 진위확인 API
     * 국세청 사업자등록정보 진위확인 API와 연동하여 사업자등록번호의 유효성을 검증합니다.
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyBusiness(@RequestBody Map<String, Object> request) {
        try {
            // 요청 파라미터 추출 (User 엔티티의 필드명을 기준으로 매핑)
            String businessNum = request.get("businessNum") != null ? request.get("businessNum").toString() : "";
            String userName = request.get("userName") != null ? request.get("userName").toString() : "";
            String startDate = request.get("startDate") != null ? request.get("startDate").toString() : "";
            String companyName = request.get("companyName") != null ? request.get("companyName").toString() : "";
            
            // 사업자등록번호 진위확인 서비스 호출
            boolean isValid = businessNumCheckService.checkBusinessNum(
                businessNum,
                userName,
                startDate,
                companyName
            );
            
            // 응답 데이터 구성
            Map<String, Object> data = new HashMap<>();
            data.put("b_no", businessNum.replaceAll("-", ""));
            data.put("valid", isValid ? "01" : "02");
            data.put("valid_msg", isValid ? "유효한 사업자등록번호입니다." : "유효하지 않은 사업자등록번호입니다.");
            
            if (isValid) {
                data.put("status", "01");
                data.put("status_msg", "계속사업자");
            } else {
                data.put("status", "02");
                data.put("status_msg", "확인할 수 없습니다");
            }
            
            // 국세청 API 응답 형식과 동일하게 구성 (문자열로 통일)
            Map<String, Object> response = new HashMap<>();
            response.put("status_code", "OK");
            response.put("request_cnt", "1");
            response.put("valid_cnt", isValid ? "1" : "0");
            response.put("data", new Object[]{data});
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 오류 응답 구성
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status_code", "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 사업자등록번호 상태 조회 API
     * 간단한 형식 검증만 수행하는 간이 API입니다.
     * 실제 국세청 API 호출은 /verify 엔드포인트를 사용하세요.
     */
    @GetMapping("/status/{businessNum}")
    public ResponseEntity<Map<String, Object>> checkBusinessStatus(@PathVariable String businessNum) {
        // 사업자등록번호에서 하이픈 제거
        businessNum = businessNum != null ? businessNum.replaceAll("-", "") : "";
        
        Map<String, Object> response = new HashMap<>();
        response.put("businessNum", businessNum);
        
        try {
            // 간단한 형식 검증 (실제 구현에서는 국세청 API를 호출해야 함)
            boolean isValidFormat = businessNum != null && !businessNum.isEmpty() && businessNum.length() == 10;
            
            response.put("isValidFormat", isValidFormat);
            response.put("message", isValidFormat ? 
                "사업자등록번호 형식이 유효합니다. 자세한 정보는 /verify API를 사용하세요." : 
                "유효하지 않은 사업자등록번호 형식입니다.");
                
        } catch (Exception e) {
            response.put("isValidFormat", false);
            response.put("message", "사업자등록번호 확인 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 