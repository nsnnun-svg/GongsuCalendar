[일당월급계산서 - Android Studio 프로젝트]

1. 이 zip 을 PC에서 압축 해제
2. Android Studio 실행 → Open → 압축 푼 GongsuCalendar 폴더 선택
3. 아래쪽에서 Gradle Sync 가 끝날 때까지 기다리기 (처음엔 몇 분 걸림)
   - "Install missing SDK / Platform 34" 같은 안내가 나오면 Install 클릭
   - "Update Android Gradle Plugin" 안내가 나오면 무시(Don't remind)해도 되고, Update 해도 됨
4. 폰을 USB로 연결(USB 디버깅 ON) → 위쪽 ▶ Run 버튼
   또는 메뉴 Build → Build Bundle(s)/APK(s) → Build APK(s)
   → app/build/outputs/apk/debug/app-debug.apk 를 폰에 설치

* 기존에 설치된 공수달력 앱은 먼저 삭제하고 설치하세요. (서명이 달라 충돌)
* 예전 앱의 데이터는 이 앱으로 옮겨지지 않습니다. (저장 방식이 새로 바뀜)
* 화면/기능
  - 달력에서 날짜 터치 → 공수(0.5/1.0/1.5/2.0/휴), 단가, 정산, 그룹, 메모 입력
  - 하단: 이번 달 합계 / 정산·세금 화면 / 그룹 필터
  - 왼쪽 메뉴: 백업·복구, 기본값/세율 설정, 일괄변경
