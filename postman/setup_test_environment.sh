#!/bin/bash

echo "AssetTrading 테스트 환경 설정 중..."

echo "Spring Boot 애플리케이션 실행 상태 확인 중..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080 | grep -q "200"; then
  echo -e "\033[0;32mSpring Boot 애플리케이션이 실행 중입니다!\033[0m"
else
  echo -e "\033[0;31mSpring Boot 애플리케이션이 실행 중이지 않습니다. 테스트 전에 애플리케이션을 시작해주세요.\033[0m"
fi

echo
echo "사용 방법:"
echo "1. Postman 컬렉션 가져오기: AssetTrading_Products_API.json"
echo "2. Postman 환경 가져오기: AssetTrading_Environment.json"
echo "3. 환경 드롭다운에서 \"AssetTrading 환경\" 선택하기"
echo "4. TESTING_GUIDE.md의 단계를 따라 진행하세요"
echo

echo "Postman 컬렉션 폴더 열기..."
if [[ "$OSTYPE" == "darwin"* ]]; then
  # macOS
  open .
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
  # Linux
  if command -v xdg-open > /dev/null; then
    xdg-open .
  elif command -v gnome-open > /dev/null; then
    gnome-open .
  else
    echo "폴더를 자동으로 열 수 없습니다. postman 디렉토리로 수동으로 이동해주세요."
  fi
fi

echo
echo "테스트 준비 완료!"
read -p "계속하려면 Enter 키를 누르세요..." 