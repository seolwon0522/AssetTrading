package com.example.AssetTrading.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessNumCheckService {

    @Value("${business.api.url}")
    private String apiUrl;

    @Value("${business.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 국세청 사업자등록정보 진위확인 API를 호출하여 사업자등록번호의 유효성을 검증합니다.
     * 
     * @param businessNum 사업자등록번호 (필수)
     * @param userName 대표자명
     * @param startDate 개업일자 (YYYYMMDD 형식)
     * @param companyName 상호명
     * @return 사업자등록번호 유효성 여부 (true: 유효, false: 유효하지 않음)
     */
    public boolean checkBusinessNum(String businessNum, String userName, String startDate, String companyName) {
        try {
            // 1. 입력값 전처리
            // 사업자등록번호에서 하이픈 제거
            businessNum = businessNum != null ? businessNum.replaceAll("-", "") : "";
            
            // 날짜 형식 변환 (YYYY-MM-DD -> YYYYMMDD)
            if (startDate != null && startDate.contains("-")) {
                startDate = startDate.replaceAll("-", "");
            }
            
            // 디버깅을 위한 입력값 로깅
            System.out.println("사업자번호: " + businessNum);
            System.out.println("대표자명: " + userName);
            System.out.println("개업일자: " + startDate);
            System.out.println("상호명: " + companyName);
            
            // 2. API URL 구성 (serviceKey는 URL 인코딩된 형태로 전달)
            URI uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("serviceKey", apiKey)
                .build(true)
                .toUri();
            
            System.out.println("API 요청 URL: " + uri.toString());
            
            // 3. API 요청 바디 구성 (국세청 API 명세서 형식에 맞춤)
            Map<String, Object> business = new HashMap<>();
            business.put("b_no", businessNum);      // 사업자등록번호 (필수값)
            
            // 선택적 파라미터는 값이 있을 때만 추가
            if (startDate != null && !startDate.isEmpty()) {
                business.put("start_dt", startDate);    // 개업일자 (YYYYMMDD)
            }
            
            if (userName != null && !userName.isEmpty()) {
                business.put("p_nm", userName);         // 대표자성명
            }
            
            if (companyName != null && !companyName.isEmpty()) {
                business.put("b_nm", companyName);      // 상호
            }
            
            // businesses 배열 형태로 요청 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("businesses", List.of(business));
            
            // 4. HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            
            // 5. HTTP 요청 엔티티 생성
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // 요청 바디 로깅
            System.out.println("API 요청 BODY: " + objectMapper.writeValueAsString(requestBody));
            
            // 6. API 호출 및 응답 수신
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                uri, 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );
            
            String response = responseEntity.getBody();
            System.out.println("API 응답: " + response);
            
            // 7. 응답 처리
            if (response != null) {
                Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
                
                // API 요청 결과 코드 확인 (Integer 또는 String 타입 모두 처리)
                Object requestResultObj = responseMap.get("request_cnt");
                Object statusCodeObj = responseMap.get("status_code");
                
                String statusCode = statusCodeObj != null ? statusCodeObj.toString() : "";
                
                if (!"OK".equals(statusCode)) {
                    System.out.println("API 오류: " + statusCode + " - " + responseMap.get("message"));
                    return false;
                }
                
                // 데이터 처리
                List<?> dataList = (List<?>) responseMap.get("data");
                if (dataList != null && !dataList.isEmpty()) {
                    Map<?, ?> data = (Map<?, ?>) dataList.get(0);
                    
                    // 응답 필드 로깅
                    for (Map.Entry<?, ?> entry : data.entrySet()) {
                        System.out.println(entry.getKey() + ": " + entry.getValue());
                    }
                    
                    // 진위확인 결과 확인 (String 또는 다른 타입으로 반환될 수 있음)
                    // b_stt_cd: "계속사업자", "휴업자", "폐업자" 등의 상태 코드
                    // tax_type: 과세유형 (부가가치세 일반과세자, 간이과세자 등)
                    // valid: "01"(유효한 사업자번호), "02"(유효하지 않은 사업자번호)
                    Object validObj = data.get("valid");
                    Object bSttCdObj = data.get("b_stt_cd");
                    
                    String valid = validObj != null ? validObj.toString() : "";
                    String bSttCd = bSttCdObj != null ? bSttCdObj.toString() : "";
                    
                    if ("01".equals(valid)) {
                        System.out.println("사업자등록번호 검증 성공: 유효한 사업자");
                        System.out.println("사업자 상태: " + bSttCd);
                        return true;
                    } else {
                        System.out.println("사업자등록번호 검증 실패: 유효하지 않은 사업자정보");
                        System.out.println("상태 코드: " + data.get("status"));
                        System.out.println("상태 메시지: " + data.get("status_msg"));
                        return false;
                    }
                }
            }
            
            System.out.println("사업자등록번호 검증 실패: 응답 데이터 없음");
            return false;
        } catch (Exception e) {
            System.out.println("사업자등록번호 검증 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
