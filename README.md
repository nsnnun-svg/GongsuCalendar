# 공수계산노트 (Gongsu Calendar)

일용/공수 기반 근무 기록을 월별 캘린더로 관리하는 안드로이드 앱입니다.
스크린샷으로 주신 "공수계산노트" 앱을 참고하여 아래 기능을 구현했습니다.

## 주요 기능

- **월별 캘린더**: 날짜를 탭하면 공수(0.5/1.0/1.5/2.0/휴)와 단가를 입력하는 다이얼로그가 열립니다.
- **공수/단가 스테퍼**: +/- 버튼으로 값 증감 (증감폭은 설정에서 조정 가능)
- **메모, 그룹, 정산여부**: 하루 기록마다 메모 입력, 그룹 지정, 정산 체크 가능
- **하단 합계바**: 이번 달 합계 금액과 공수 건수(휴일 수 포함) 표시
- **정산/세금 계산**: 이번 달 합계를 기준으로
  - 일용근무 (소득세 + 지방소득세 + 고용보험)
  - 4대보험 (지역보험/건강보험/노인장기요양보험/고용보험/산재보험)
  - 사업소득 (3.3% 등)
  세 가지 방식 중 선택해 실수령액을 계산합니다.
- **그룹필터**: 그룹 추가/삭제, 특정 그룹만 필터링해서 캘린더에 표시
- **좌측 드로어 메뉴**:
  - 데이터 백업/복구 (JSON 파일로 내보내기/가져오기, Storage Access Framework 사용)
  - 기본값/UI 설정 (기본 단가·공수, 버튼 증감값, 표시 옵션 토글)
  - 세율조정 (모든 세율 항목을 직접 수정 가능)
  - 일괄변경 (메모/공수/단가/정산여부를 현재 달 전체에 일괄 적용)
  - 공유하기 (이번 달 합계를 텍스트로 공유)

## 기술 스택

- Kotlin, AndroidX, Material Components
- Room (SQLite ORM) — 근무 기록/그룹 데이터 저장
- SharedPreferences — 기본값, UI 옵션, 세율 저장
- ViewBinding 없이 findViewById 기반의 단순한 View 구조 (의존성 최소화)
- 최소 지원 버전: Android 7.0 (API 24) / 타겟: API 35

## 빌드 방법

1. Android Studio(최신 버전 권장)에서 `File > Open`으로 이 프로젝트 최상위 폴더(`GongsuCalendar`)를 엽니다.
2. Gradle Wrapper 실행 파일(`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`)이 포함되어 있지 않으므로,
   최초 열 때 Android Studio가 "Gradle wrapper가 없습니다 — 생성하시겠습니까?" 라고 물으면 **예**를 선택하세요.
   (또는 터미널에서 Gradle이 설치되어 있다면 프로젝트 루트에서 `gradle wrapper` 명령을 한 번 실행하세요.)
3. Gradle Sync가 끝나면 실제 기기 또는 에뮬레이터에서 `Run ▶` 을 눌러 실행합니다.

## 프로젝트 구조

```
app/src/main/java/com/gongsu/calendar/
 ├─ MainActivity.kt              # 캘린더 메인 화면 + 드로어 메뉴
 ├─ data/                        # Room 엔티티/DAO/DB
 │   ├─ WorkEntry.kt
 │   ├─ GroupEntity.kt
 │   ├─ WorkEntryDao.kt
 │   ├─ GroupDao.kt
 │   └─ AppDatabase.kt
 ├─ prefs/AppPrefs.kt            # 기본값 / UI 옵션 / 세율 저장소
 ├─ util/
 │   ├─ DateUtils.kt
 │   └─ TaxCalculator.kt         # 세율 계산 로직
 ├─ backup/BackupManager.kt      # JSON 백업/복구
 └─ ui/
     ├─ CalendarAdapter.kt       # 월 그리드 RecyclerView 어댑터
     ├─ EntryDialogFragment.kt   # 하루 기록 입력 다이얼로그
     ├─ GroupFilterDialogFragment.kt
     ├─ UiSettingsActivity.kt
     ├─ TaxSettingsActivity.kt
     └─ SettlementActivity.kt
```

## 참고 / 향후 개선 아이디어

- 세율 계산은 스크린샷 속 설정값(소득세 2.7%, 지방소득세 10.0%, 4대보험 세율, 사업소득 3.3% 등)을
  그대로 반영할 수 있도록 "세율조정" 화면에서 자유롭게 수정 가능하게 구현했습니다.
  실제 세법상의 정확한 일용근로소득세 계산식(1일 15만원 비과세 등)까지는 반영하지 않았으므로,
  필요하면 `TaxCalculator.kt` 의 `DAILY_WORK` 분기를 원하는 공식으로 교체하면 됩니다.
- 연간 캘린더 보기, 목록 보기(≡ 아이콘), 알림설정 등은 자리만 마련해두었고 "준비중" 토스트로 처리했습니다.
  필요하시면 이어서 구현해 드릴 수 있습니다.
