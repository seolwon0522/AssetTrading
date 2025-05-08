package com.example.AssetTrading.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class BusinessNumCheckService {

    @Value("${allbaro.api.url}")
    private String apiUrl;

    @Value("${allbaro.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean checkBusinessNum(String businessNum, String ownerName, String startDate, String companyName) {
        try {
            // 하드코딩된 인코딩 API 키 직접 사용
            String hardcodedKey = "1eSCQTQUIGYN8kq9hQp8Fh0AaB7nQXDSVPu2ZGekXTEGFKUzErmtLCEeALW2A4h%2FLbeeF4l9Au%2F86O%2BjaPX58A%3D%3D";
            String url = apiUrl + "?serviceKey=" + hardcodedKey;
            
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            java.util.List<String> bNoList = new java.util.ArrayList<>();
            bNoList.add(businessNum);
            body.put("b_no", bNoList);
            
            if (startDate != null && !startDate.isEmpty()) {
                java.util.List<String> startDtList = new java.util.ArrayList<>();
                startDtList.add(startDate);
                body.put("start_dt", startDtList);
            }
            
            if (ownerName != null && !ownerName.isEmpty()) {
                java.util.List<String> pNmList = new java.util.ArrayList<>();
                pNmList.add(ownerName);
                body.put("p_nm", pNmList);
            }
            
            if (companyName != null && !companyName.isEmpty()) {
                java.util.List<String> corpNmList = new java.util.ArrayList<>();
                corpNmList.add(companyName);
                body.put("corp_nm", corpNmList);
            }
            
            java.util.List<String> pNm2List = new java.util.ArrayList<>();
            pNm2List.add("");
            body.put("p_nm2", pNm2List);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));
            
            org.springframework.http.HttpEntity<java.util.Map<String, Object>> requestEntity = 
                new org.springframework.http.HttpEntity<>(body, headers);

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            System.out.println("API 요청 URL: " + url);
            System.out.println("API 요청 BODY: " + objectMapper.writeValueAsString(body));

            org.springframework.http.ResponseEntity<String> responseEntity = 
                restTemplate.exchange(url, org.springframework.http.HttpMethod.POST, requestEntity, String.class);
            
            String response = responseEntity.getBody();
            System.out.println("API 응답: " + response);

            if (response != null) {
                java.util.Map<String, Object> map = objectMapper.readValue(response, java.util.Map.class);
                
                if (map.containsKey("status_code") && !"OK".equals(map.get("status_code"))) {
                    System.out.println("API 오류: " + map.get("status_code") + " - " + map.get("message"));
                    return false;
                }
                
                java.util.List<?> dataList = (java.util.List<?>) map.get("data");
                if (dataList != null && !dataList.isEmpty()) {
                    java.util.Map<?, ?> data = (java.util.Map<?, ?>) dataList.get(0);
                    
                    for (java.util.Map.Entry<?, ?> entry : data.entrySet()) {
                        System.out.println(entry.getKey() + ": " + entry.getValue());
                    }
                    
                    Object bSttCd = data.get("b_stt_cd");
                    Object valid = data.get("valid");
                    
                    if ("01".equals(bSttCd)) {
                        System.out.println("사업자등록번호 검증 성공: 계속사업자");
                        return true;
                    } else if ("Y".equals(valid)) {
                        System.out.println("사업자등록번호 검증 성공: 유효한 사업자");
                        return true;
                    } else {
                        System.out.println("사업자등록번호 검증 실패: 유효하지 않은 사업자정보");
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
