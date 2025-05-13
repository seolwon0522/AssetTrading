# AssetTrading API 테스트 가이드 (Postman)

이 디렉토리는 AssetTrading API를 테스트하기 위한 Postman 컬렉션과 환경 설정을 포함하고 있습니다.

## 내용물

- `AssetTrading_Products_API.json` - 상품 엔드포인트 테스트용 Postman 컬렉션
- `AssetTrading_Environment.json` - Postman 컬렉션용 환경 변수
- `TESTING_GUIDE.md` - 상품 엔드포인트 테스트를 위한 단계별 가이드
- `setup_test_environment.bat` - 테스트 환경 설정을 돕는 Windows 스크립트
- `setup_test_environment.sh` - 테스트 환경 설정을 돕는 Linux/Mac 스크립트

## 빠른 시작

### Windows 사용자

1. `setup_test_environment.bat`를 더블클릭하여 설정 스크립트 실행
2. 터미널 창의 지침을 따르세요

### Linux/Mac 사용자

1. 이 디렉토리에서 터미널 열기
2. `chmod +x setup_test_environment.sh` 명령어로 스크립트 실행 권한 부여
3. `./setup_test_environment.sh` 명령어로 스크립트 실행
4. 터미널 창의 지침을 따르세요

## 수동 설정

1. Spring Boot 애플리케이션이 `localhost:8080`에서 실행 중인지 확인
2. Postman 열기
3. 컬렉션(`AssetTrading_Products_API.json`)과 환경(`AssetTrading_Environment.json`) 가져오기
4. 환경 드롭다운에서 "AssetTrading 환경" 선택
5. `TESTING_GUIDE.md`의 단계를 따르세요

## 사용 가능한 엔드포인트

컬렉션에는 SellProductController의 모든 엔드포인트가 포함되어 있습니다:

- 상품 등록
- 상품 검색 (페이징 처리 유무)
- 상품 상세 정보
- 상품 정보 업데이트
- 상품 상태 관리
- 카테고리 및 태그 필터링
- 가격 범위 필터링
- 추천 상품
- 판매자별 상품
- 노출 가능 상품
- 인기 상품
- 상품 삭제

## 환경 변수

환경 설정에는 다음과 같은 변수들이 포함되어 있습니다:

- `baseUrl` - API 기본 URL (기본값: `http://localhost:8080`)
- `productId` - 테스트 중인 상품 ID (기본값: `1`)
- `sellerUserIdx` - 판매자 ID (기본값: `1`)
- `keyword` - 검색 키워드 (기본값: `노트북`)
- `category` - 상품 카테고리 (기본값: `전자제품`)
- `tag` - 상품 태그 (기본값: `게이밍`)
- `minPrice` - 범위 필터링을 위한 최소 가격 (기본값: `500000`)
- `maxPrice` - 범위 필터링을 위한 최대 가격 (기본값: `2000000`) 