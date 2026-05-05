# PR24 — Critical Bugs

Bu dosya yalnızca P0 ve P1 bulguları içerir. PR24 bu bug'ları düzeltmez; sonraki PR'lar için doğrulanabilir iş listesi çıkarır.

| ID | Şiddet | Alan | Kısa problem | Hedef PR |
|---|---|---|---|---|
| AUD-001 | P0 | Import/Settings | Settings failure sonrası partial import kalıyor. | PR25.1 |
| AUD-002 | P1 | CI/Tests | Kritik androidTest'ler CI'da çalışmıyor. | PR25.1 |
| AUD-003 | P1 | Step service | Foreground service `startForeground` çağrısı gecikebilir. | PR25.4 |
| AUD-004 | P1 | Step settings | Permission revoke/no permission durumunda toggle ve servis state'i ayrışabilir. | PR25.3 |
| AUD-005 | P1 | Onboarding | Step tracking tercihi UI'da var ama tamamlamada false kaydediliyor. | PR25.3 |
| AUD-006 | P1 | Onboarding/Settings | `completeOnboarding` atomik değil. | PR25.4 |
| AUD-007 | P1 | Date windows | Monthly period tanımı ekranlar/repository arasında tutarsız. | PR25.2 |
| AUD-008 | P1 | Trends | Ortalama değerler logged-day bazlı olduğu için yanıltıcı olabilir. | PR25.2 |
| AUD-009 | P1 | Profile goals | Goals + measurement save partial ve sessiz başarısız olabilir. | PR25.4 |
| AUD-010 | P1 | Custom food | Optional nutrient alanları edit sırasında kaybolabilir. | PR25.1 |

## AUD-001 — Import settings failure sonrası partial import

- ID: AUD-001
- Şiddet: P0
- Dosya/fonksiyon: `data/src/main/java/com/burak/healthapp/data/export/HealthDataManagementRepositoryImpl.kt`, `importHealthData`
- Repro / verification steps:
  1. Room tarafında meal/hydration/custom food içeren geçerli export JSON hazırla.
  2. `settingsRepository.updateProfile` veya `updateGoalSettings` çağrısını failure döndürecek fake ile çalıştır.
  3. `importHealthData` sonucunda `SettingsFailure` döndüğünü doğrula.
  4. Room tablolarında import edilen kayıtların kaldığını kontrol et.
- Expected behavior: Import sonucu başarısızsa kullanıcıya partial success açıkça raporlanmalı veya health/settings state'i tutarlı kalmalı.
- Actual behavior: Room transaction commit oluyor; settings yazımı sonra başarısız olursa kayıtlar kalıyor.
- Suggested fix: Import planını iki fazlı/idempotent yap; settings failure için explicit partial result veya rollback/snapshot stratejisi uygula.
- Test recommendation: JVM import planner testi + instrumented integration testi. Aynı fixture için success, settings failure ve retry senaryoları.
- Target PR: PR25.1

## AUD-002 — Kritik androidTest'ler CI'da çalışmıyor

- ID: AUD-002
- Şiddet: P1
- Dosya/fonksiyon: `.github/workflows/android-ci.yml`
- Repro / verification steps:
  1. Workflow dosyasında Android test adımını kontrol et.
  2. Adımın `:app:compileDebugAndroidTestKotlin` olduğunu doğrula.
  3. `app/src/androidTest` altındaki import/migration/Compose testlerinin connected cihaz olmadan koşmadığını kontrol et.
- Expected behavior: Import idempotency, Room migration ve kritik UI testleri merge öncesi gerçekten koşmalı veya JVM karşılığı olmalı.
- Actual behavior: CI sadece androidTest source set'ini compile ediyor.
- Suggested fix: Kritik data testlerini JVM'e taşı; kalan integration testleri için emulator job ekle.
- Test recommendation: `HealthDataManagementRepositoryInstrumentedTest` içindeki idempotency/failure case'lerin pure Kotlin import planner testleriyle desteklenmesi.
- Target PR: PR25.1

## AUD-003 — Step foreground service zamanlama riski

- ID: AUD-003
- Şiddet: P1
- Dosya/fonksiyon: `app/src/main/java/com/burak/healthapp/core/step/StepCounterService.kt`, `onStartCommand`, `startForegroundCompat`
- Repro / verification steps:
  1. Step permission verilmiş senaryo kur.
  2. DataStore `settings.first()` okumasını geciktiren fake kullan.
  3. Service start'tan sonra `startForegroundCompat()` çağrısının geciktiğini izle.
- Expected behavior: `startForegroundService` sonrası foreground notification çok erken kurulmalı.
- Actual behavior: Service önce settings okuyor ve tracking enabled kararını bekliyor.
- Suggested fix: Minimal notification ile foreground'u erken başlat; settings/sensor uygun değilse controlled stop path'e geç.
- Test recommendation: Service start decision unit testleri + Robolectric/instrumented lifecycle smoke testi.
- Target PR: PR25.4

## AUD-004 — Step permission ile settings drift'i

- ID: AUD-004
- Şiddet: P1
- Dosya/fonksiyon: `StepCounterService.start`, `StepCounterService.onStartCommand`, `PermissionEffects`
- Repro / verification steps:
  1. Profile'da step tracking enabled yap.
  2. Android settings'ten ACTIVITY_RECOGNITION permission revoke et.
  3. App'i resume/startup yaptır.
  4. Profile toggle, foreground service ve dashboard step state'ini karşılaştır.
- Expected behavior: Permission yoksa UI ve settings bunu açıkça yansıtmalı.
- Actual behavior: Start path'i permission yoksa dönebiliyor; settings her zaman false'a reconcile edilmiyor.
- Suggested fix: Permission revoke/no sensor durumunda settings ve UI state'i merkezi decision sonucu ile eşitle.
- Test recommendation: `StepServiceStartDecision` ve `ProfileViewModel` permission regression testleri.
- Target PR: PR25.3

## AUD-005 — Onboarding step tracking tercihi uygulanmıyor gibi görünebilir

- ID: AUD-005
- Şiddet: P1
- Dosya/fonksiyon: `app/src/main/java/com/burak/healthapp/feature/onboarding/OnboardingViewModel.kt`, `finishOnboarding`
- Repro / verification steps:
  1. Onboarding'de step tracking preference'i açık seç.
  2. Onboarding'i tamamla.
  3. Profile step tracking setting'inin false kaldığını doğrula.
- Expected behavior: UI, seçimin hemen tracking açmadığını ve izin/profile adımı gerektiğini net anlatmalı.
- Actual behavior: State saklanıyor ama tamamlamada `stepTrackingEnabled=false` yazılıyor.
- Suggested fix: Onboarding copy ve state contract'ını netleştir; permission isteyen gerçek enable akışını Profile'a bırak.
- Test recommendation: Onboarding ViewModel testlerine preference copy/summary contract veya UI test ekle.
- Target PR: PR25.3

## AUD-006 — Onboarding complete atomik değil

- ID: AUD-006
- Şiddet: P1
- Dosya/fonksiyon: `data/src/main/java/com/burak/healthapp/data/repository/SettingsRepositoryImpl.kt`, `completeOnboarding`
- Repro / verification steps:
  1. `completeOnboarding` içinde supplement veya final DataStore edit aşamasında fake failure üret.
  2. Hata sonrası profile/goals/measurement/supplement state'ini incele.
  3. Onboarding flag'in false kalırken bazı kayıtların yazıldığını doğrula.
- Expected behavior: Retry-safe ve kullanıcıya açık bir failure state olmalı.
- Actual behavior: Sıralı writes partial state bırakabilir.
- Suggested fix: Orchestrated result modeli, daha net rollback/cleanup veya idempotent retry stratejisi.
- Test recommendation: Failure injection ViewModel/repository unit testleri.
- Target PR: PR25.4

## AUD-007 — Monthly date-window tutarsızlığı

- ID: AUD-007
- Şiddet: P1
- Dosya/fonksiyon: `MetricDateWindows.kt`, `TrendsRepositoryImpl`, `SleepDetailScreen`
- Repro / verification steps:
  1. Anchor date olarak 2026-05-05 seç.
  2. 2026-04 sonu ve 2026-05 başı için sleep/hydration/step kayıtları ekle.
  3. Sleep monthly detail, Trends monthly ve shared monthly helper çıktılarını karşılaştır.
- Expected behavior: Monthly period tanımı tüm ekranlarda aynı olmalı.
- Actual behavior: month-to-date, rolling 30 ve month-start query davranışları karışıyor.
- Suggested fix: Weekly/monthly window contract'ını tek helper ve tek test setiyle sabitle.
- Test recommendation: Inclusive/exclusive boundary, ay başı, leap day ve timezone-independent LocalDate testleri.
- Target PR: PR25.2

## AUD-008 — Logged-day average kullanıcıyı yanıltabilir

- ID: AUD-008
- Şiddet: P1
- Dosya/fonksiyon: `domain/src/main/java/com/burak/healthapp/domain/analytics/TrendCalculations.kt`, `averageByLoggedDays`
- Repro / verification steps:
  1. 7 günlük periodda yalnız 1 gün veri gir.
  2. Weekly average ve goal/adherence metriklerini karşılaştır.
  3. Average değerinin tek kayıtlı gün üzerinden hesaplandığını doğrula.
- Expected behavior: UI average tanımını açıkça belirtmeli veya period-day average kullanmalı.
- Actual behavior: Logged-day average yüksek görünebilir.
- Suggested fix: Product kararına göre iki metrik ayrımı yap; label ve tests ile netleştir.
- Test recommendation: Missing-day fixtures ile Trends mapper/unit testleri.
- Target PR: PR25.2

## AUD-009 — Profile goals save partial/silent failure

- ID: AUD-009
- Şiddet: P1
- Dosya/fonksiyon: `app/src/main/java/com/burak/healthapp/feature/profile/ProfileGoalsViewModel.kt`, `saveGoalsAndMeasurement`
- Repro / verification steps:
  1. `settingsRepository.updateGoalSettings` success, `dashboardRepository.saveBodyMeasurement` failure fake'i kullan.
  2. Save callback ve UI state'i gözle.
  3. Goals yazılmış ama measurement yazılmamış state'i doğrula.
- Expected behavior: Save failure kullanıcıya gösterilmeli ve retry imkanı olmalı.
- Actual behavior: Exception path için açık UI error state'i yok.
- Suggested fix: Save result state, loading/disabled state ve snackbar/field error contract ekle.
- Test recommendation: ViewModel unit testleri ve profile form UI testleri.
- Target PR: PR25.4

## AUD-010 — Custom food optional nutrients edit sırasında kaybolabilir

- ID: AUD-010
- Şiddet: P1
- Dosya/fonksiyon: `CustomFoodEditorViewModel.kt`, `CustomFoodEditorState`, `domain/model/nutrition/CustomFoodModels.kt`
- Repro / verification steps:
  1. `fiberGrams`, `sugarGrams`, `sodiumMg` dolu custom food import et.
  2. Custom food editor ile sadece ad/marka değiştir.
  3. DB/export sonrası optional alanların null olup olmadığını kontrol et.
- Expected behavior: Kullanıcı edit sırasında görmese bile mevcut optional nutrient alanları korunmalı.
- Actual behavior: Editor state/save modeli bu alanları taşımıyor.
- Suggested fix: Editor state'e preserved optional fields ekle veya repository'de partial update modeli kullan.
- Test recommendation: Custom food edit roundtrip JVM/ViewModel testi.
- Target PR: PR25.1

