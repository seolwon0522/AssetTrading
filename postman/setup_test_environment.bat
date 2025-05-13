@echo off
echo AssetTrading 테스트 환경 설정 중...

echo Spring Boot 애플리케이션 실행 상태 확인 중...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080' -Method 'HEAD' -TimeoutSec 5; if ($response.StatusCode -eq 200) { Write-Host 'Spring Boot 애플리케이션이 실행 중입니다!' -ForegroundColor Green } else { Write-Host 'Spring Boot 애플리케이션이 예상치 못한 상태 코드를 반환했습니다:' $response.StatusCode -ForegroundColor Yellow } } catch { Write-Host 'Spring Boot 애플리케이션이 실행 중이지 않습니다. 테스트 전에 애플리케이션을 시작해주세요.' -ForegroundColor Red }"

echo.
echo 사용 방법:
echo 1. Postman 컬렉션 가져오기: AssetTrading_Products_API.json
echo 2. Postman 환경 가져오기: AssetTrading_Environment.json
echo 3. 환경 드롭다운에서 "AssetTrading 환경" 선택하기
echo 4. TESTING_GUIDE.md의 단계를 따라 진행하세요
echo.

echo Postman 컬렉션 폴더 열기...
start "" "%~dp0"

echo.
echo 테스트 준비 완료!
pause 