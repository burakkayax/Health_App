# PR24 — Full App Audit

## Kısa özet

PR24 kapsamında uygulamaya yeni özellik eklenmedi ve mevcut bug'lar düzeltilmedi. Kod tabanı, veri kaybı, yanlış hesaplama, crash, UI/UX akış bozukluğu, test eksikliği, performans/batarya ve profesyonel kalite riskleri açısından denetlendi.

Genel sonuç: uygulama ana geliştirme hattında derleniyor ve JVM/unit testleri geçiyor; ancak import/settings atomikliği, CI'da çalışmayan kritik instrumented testler, date-window tutarsızlıkları, step service foreground zamanlaması ve state-save hata yüzeyleri sonraki düzeltme PR'larında öncelikli ele alınmalı.

Kod değişikliği yapılmadı. Bu PR yalnızca audit dokümanları ve CHANGELOG girdisi içerir.

## İncelenen modüller

| Modül / Alan | İncelenen örnek dosyalar |
|---|---|
| Project structure | `settings.gradle.kts`, root/app/data/domain/benchmark Gradle dosyaları |
| CI workflow | `.github/workflows/android-ci.yml` |
| Room database | `HealthDatabase.kt`, `HealthDatabaseMigrations.kt`, DAO/entity dosyaları |
| DataStore settings | `SettingsRepositoryImpl.kt`, settings key ve model akışları |
| Import/export/delete-all | `HealthDataManagementRepositoryImpl.kt`, `JsonHealthDataImporter.kt`, `HealthDataExportRepositoryImpl.kt`, import/export use case'leri |
| Onboarding | `OnboardingViewModel.kt`, `OnboardingUiState.kt`, `OnboardingDashboardConfig.kt`, `SmartGoalSuggestions.kt`, `OnboardingSteps.kt`, onboarding tests |
| Today dashboard | `TodayViewModel.kt`, `TodayUiMapper.kt`, `TodayScreen.kt`, `DashboardRepositoryImpl.kt` |
| Nutrition/custom food | `MealEditorViewModel.kt`, `NutritionCalculations.kt`, `CustomFoodEditorViewModel.kt`, `CustomFoodRepositoryImpl.kt` |
| Sleep/stability | `SleepCalculations.kt`, `SleepStabilityCalculations.kt`, `SleepDetailScreen.kt`, sleep tests |
| Trends/date windows | `MetricDateWindows.kt`, `DateWindowCalculations.kt`, `TrendsRepositoryImpl.kt`, `TrendsUiStateMapperTest.kt` |
| Profile/settings | `ProfileViewModel.kt`, `ProfileGoalsViewModel.kt`, profile cards and settings UI |
| Step tracking | `StepCounterService.kt`, step service decision/write policy tests, manifest service declarations |
| Water reminders | `WaterReminderSchedule.kt`, `WaterReminderScheduler.kt`, `WaterReminderWorker.kt`, reminder tests |
| Navigation/state | `AppNavigation.kt`, `MainShell.kt`, detail route/ViewModel date state |
| Compose UI/forms | Today sheets, meal/custom-food editors, profile/settings forms |
| Benchmark/performance | `benchmark` module, baseline profile and macrobenchmark tests |

## Genel sağlık puanı

| Alan | Puan | Not |
|---|---:|---|
| Build/compile sağlığı | 90/100 | İstenen Gradle zinciri geçti. Lint warning borcu var. |
| Domain hesaplama testleri | 82/100 | Sleep stability ve date-window bazı domain testleri güçlü; Trends/date sözleşmeleri hala parçalı. |
| Data/import güvenliği | 58/100 | Import Room transaction + DataStore ayrımı partial import riski taşıyor. Data module JVM test kapsamı yok. |
| Onboarding/settings tutarlılığı | 70/100 | Son PR cleanup'ları yerinde; completeOnboarding atomiklik ve step preference UX riski var. |
| UI/UX/forms | 66/100 | Ana akışlar çalışır görünüyor; silent validation ve error feedback açıkları var. |
| Navigation/state restore | 64/100 | Shell-level selectedDate basit akışta yeterli; detail route argümanı eksikliği ileride kırılgan. |
| Performance/battery | 68/100 | Service throttling ve baseline profile var; foreground timing, recomposition ve benchmark raporlama eksik. |

Genel puan: **71/100**. Uygulama derlenebilir ve çekirdek akışlar testlenmiş durumda; PR25 serisinin ilk işi veri bütünlüğü, tarih hesapları, settings/state atomikliği ve CI test kapsamı olmalı.

## Çalıştırılan komutlar

| Komut | Sonuç | Not |
|---|---|---|
| `./gradlew spotlessCheck detekt lintDebug :app:compileDebugKotlin :domain:test :data:test :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug :app:assembleRelease` | Geçti | Exit code 0, yaklaşık 33.2 sn. |
| `./gradlew spotlessCheck` | Geçti | Kombine komut içinde çalıştı. |
| `./gradlew detekt` | Geçti | Kombine komut içinde çalıştı. |
| `./gradlew lintDebug` | Geçti | Rapor: 0 error, 142 warning, 1 hint. |
| `./gradlew :app:compileDebugKotlin` | Geçti | Kombine komut içinde çalıştı. |
| `./gradlew :domain:test` | Geçti | 5 suite, 57 test, 0 failure. |
| `./gradlew :data:test` | Geçti | Task geçti; `data/src/test` bulunmadığı için fiili test kapsamı yok. |
| `./gradlew :app:testDebugUnitTest` | Geçti | 25 suite, 219 test, 0 failure. |
| `./gradlew :app:compileDebugAndroidTestKotlin` | Geçti | Instrumented testler derlendi, cihazda çalıştırılmadı. |
| `./gradlew :app:assembleDebug` | Geçti | `app-debug.apk` üretildi. |
| `./gradlew :app:assembleRelease` | Geçti | `app-release-unsigned.apk` üretildi. |
| `./gradlew :app:connectedDebugAndroidTest` | Çalıştırılmadı | SDK `adb` cihaz listesinde bağlı emulator/cihaz yoktu. |
| `./gradlew :benchmark:connectedCheck` | Çalıştırılmadı | Bağlı emulator/cihaz olmadığı için benchmark ortamı uygun değildi. |

CI gözlemi: `.github/workflows/android-ci.yml` format, detekt, lint, compile, unit test, androidTest compile ve assemble adımlarını çalıştırıyor. `connectedDebugAndroidTest` ve benchmark connected testleri CI'da çalışmıyor.

## En kritik 10 bulgu

| ID | Şiddet | Alan | Dosya/Fonksiyon | Problem | Etki | Hedef PR |
|---|---|---|---|---|---|---|
| AUD-001 | P0 | Import/Settings | `HealthDataManagementRepositoryImpl.importHealthData` | Room import transaction'ı commit olduktan sonra DataStore settings yazımı başarısız olursa partial import kalıyor. | Kullanıcı verileri import edilmiş ama profil/hedef/reminder/theme state'i eski veya yarım kalmış olabilir. | PR25.1 |
| AUD-002 | P1 | CI/Tests | `.github/workflows/android-ci.yml` | Kritik androidTest'ler CI'da sadece derleniyor, çalıştırılmıyor. | Import, migration ve Compose regressions merge öncesi yakalanmayabilir. | PR25.1 |
| AUD-003 | P1 | Step service | `StepCounterService.onStartCommand` | Foreground service, `startForegroundService` sonrası DataStore okumasını bekleyip sonra `startForeground` çağırıyor. | Yavaş başlangıçta foreground-service timeout/crash riski. | PR25.4 |
| AUD-004 | P1 | Step settings | `StepCounterService.start`, `PermissionEffects` | Permission yoksa servis başlamıyor/duruyor ama setting her zaman disabled state'e geri çekilmiyor. | Profile toggle ve gerçek servis durumu ayrışabilir. | PR25.3 |
| AUD-005 | P1 | Onboarding | `OnboardingViewModel.finishOnboarding` | Onboarding step preference state'i tutuluyor ama tamamlamada her zaman `stepTrackingEnabled=false` yazılıyor. | UI kullanıcının seçiminin uygulanacağı izlenimini verebilir. | PR25.3 |
| AUD-006 | P1 | Onboarding/Settings | `SettingsRepositoryImpl.completeOnboarding` | Profile, goals, body measurement, supplements ve onboarding flag sıralı yazılıyor; atomik değil. | Hata halinde onboarding yarım state bırakabilir. | PR25.4 |
| AUD-007 | P1 | Trends/Sleep | `MetricDateWindows`, `TrendsRepositoryImpl`, `SleepDetailScreen` | Monthly tanımı month-to-date, rolling 30 ve month-start query olarak üç farklı yerde farklı. | Sleep/detail/trends değerleri aynı tarih için tutarsız olabilir. | PR25.2 |
| AUD-008 | P1 | Trends | `TrendCalculations.averageByLoggedDays` | Ortalama değerler yalnızca kayıtlı günlere göre, adherence/goal ise tüm period günlerine göre hesaplanıyor. | Kullanıcı weekly/monthly average değerlerini olduğundan iyi okuyabilir. | PR25.2 |
| AUD-009 | P1 | Profile goals | `ProfileGoalsViewModel.saveGoalsAndMeasurement` | Goals DataStore yazımı ve measurement Room yazımı ardışık; hata UI'da görünmüyor. | Kısmi kayıt ve sessiz başarısızlık riski. | PR25.4 |
| AUD-010 | P1 | Custom food | `CustomFoodEditorViewModel`, `CustomFoodEditorState` | Import/export optional nutrient alanlarını koruyor; edit UI state'i fiber/sugar/sodium taşımıyor. | Imported custom food editlenince optional nutrition verisi kaybolabilir. | PR25.1 |

## Detaylı bulgular

### AUD-001

- ID: AUD-001
- Şiddet: P0
- Alan: Import / Settings
- Dosya/fonksiyon: `data/src/main/java/com/burak/healthapp/data/export/HealthDataManagementRepositoryImpl.kt`, `importHealthData`
- Problem: Import akışı health tablolarını `database.withTransaction` içinde yazıyor, transaction commit olduktan sonra `settingsRepository.updateProfile` ve `settingsRepository.updateGoalSettings` çağrıları yapılıyor. Bu ikinci aşama başarısız olursa import sonucu `SettingsFailure` dönüyor ama Room verileri kalıcı kalıyor.
- Etki: Aynı import dosyası için veri kayıtları gelmiş, hedefler/profil/reminder/dashboard settings gelmemiş olabilir. Kullanıcı "import failed" gördüğü halde veri setinin bir kısmı değişmiş olur.
- Nasıl doğrulanır: `HealthDataManagementRepositoryInstrumentedTest.importHealthData_withSettingsFailureResultsInPartialImport` senaryosunu cihazda çalıştır; settings fake failure sonrası Room tablolarının değiştiğini gözle.
- Önerilen düzeltme: PR25.1'de import'u açıkça iki fazlı ve idempotent yap; settings failure durumunu kullanıcıya "records imported, settings failed" gibi doğru raporla veya settings snapshot/rollback stratejisi ekle. Transaction sınırını dokümante et ve JVM testlenebilir core importer hazırlığı yap.
- Hedef PR: PR25.1

### AUD-002

- ID: AUD-002
- Şiddet: P1
- Alan: Tests / CI
- Dosya/fonksiyon: `.github/workflows/android-ci.yml`, Android test adımı
- Problem: CI `:app:compileDebugAndroidTestKotlin` çalıştırıyor ama `:app:connectedDebugAndroidTest` çalıştırmıyor. Import repository, Room migration ve Compose UI testleri yalnızca compile güvenliği sağlıyor.
- Etki: Import idempotency, partial import, migration ve kritik UI regressions merge öncesi yakalanmayabilir.
- Nasıl doğrulanır: Workflow dosyasında connected test adımı olmadığını kontrol et; `app/src/androidTest` altındaki testleri listele.
- Önerilen düzeltme: PR25.1'de kritik import/migration senaryolarını JVM testlenebilir hale getir veya CI'a emulator-backed connected suite ekle. Uzun Compose testlerini ayrı job olarak planla.
- Hedef PR: PR25.1

### AUD-003

- ID: AUD-003
- Şiddet: P1
- Alan: Step Tracking / Foreground Service
- Dosya/fonksiyon: `app/src/main/java/com/burak/healthapp/core/step/StepCounterService.kt`, `onStartCommand`, `startForegroundCompat`
- Problem: `StepCounterService.start` `ContextCompat.startForegroundService` çağırıyor; servis içinde önce permission ve DataStore `settings.first()` okunuyor, sonra `startForegroundCompat()` çağrılıyor.
- Etki: Android'in foreground service zaman sınırında DataStore/Hilt/coroutine gecikmesi crash yaratabilir.
- Nasıl doğrulanır: Permission verilmiş ama DataStore okuması geciktirilmiş test/fake ile `startForegroundCompat()` çağrısının service start'tan sonra geciktiğini doğrula.
- Önerilen düzeltme: PR25.4'te minimal notification ile foreground'u çok erken başlat, sonra settings/sensor kararını uygula; permission yoksa state reconciliation ve stop path'i testle.
- Hedef PR: PR25.4

### AUD-004

- ID: AUD-004
- Şiddet: P1
- Alan: Step Tracking / Settings
- Dosya/fonksiyon: `StepCounterService.start`, `StepCounterService.onStartCommand`, `PermissionEffects`
- Problem: Permission yoksa servis start çağrısı sessizce dönebiliyor veya servis kendini durduruyor; ancak `stepTrackingEnabled` ayarı her path'te `false` yapılmıyor.
- Etki: Profile UI "enabled" gösterebilir, ama foreground service/sensor aktif değildir. Kullanıcı tracking'in çalıştığını sanabilir.
- Nasıl doğrulanır: Step tracking enabled iken ACTIVITY_RECOGNITION permission revoke et; app resume/startup sonrası profile switch ve service durumunu karşılaştır.
- Önerilen düzeltme: PR25.3'te permission revoke/no-sensor kararını settings ile uzlaştır; UI'da disabled reason göster ve service start kararını unit testle.
- Hedef PR: PR25.3

### AUD-005

- ID: AUD-005
- Şiddet: P1
- Alan: Onboarding
- Dosya/fonksiyon: `app/src/main/java/com/burak/healthapp/feature/onboarding/OnboardingViewModel.kt`, `finishOnboarding`, `skipWithDefaults`
- Problem: `stepTrackingPreferred` SavedStateHandle ile saklanıyor ve UI'dan güncelleniyor; ancak onboarding tamamlandığında `stepTrackingEnabled=false` yazılıyor. Bu güvenli davranış testlenmiş, fakat UI metni kullanıcıya seçimin hemen uygulanacağı izlenimi verebilir.
- Etki: Onboarding sonrası Today/Profile state'i kullanıcı beklentisiyle uyuşmayabilir.
- Nasıl doğrulanır: Onboarding'de step tracking tercih et, tamamla, profile/settings tarafında toggle'ın false kaldığını kontrol et.
- Önerilen düzeltme: PR25.3'te onboarding copy/CTA ve permission akışını netleştir; "Profile'da izinle açılacak" sözleşmesini testle.
- Hedef PR: PR25.3

### AUD-006

- ID: AUD-006
- Şiddet: P1
- Alan: Onboarding / Settings
- Dosya/fonksiyon: `data/src/main/java/com/burak/healthapp/data/repository/SettingsRepositoryImpl.kt`, `completeOnboarding`
- Problem: Profile, goal settings, initial measurement, supplements, reminder, step tracking ve `onboardingComplete` flag'i farklı DataStore/Room çağrılarıyla sırayla yazılıyor.
- Etki: Bir ara çağrı başarısız olursa onboarding incomplete kalırken bazı hedefler veya supplement template'leri yazılmış olabilir.
- Nasıl doğrulanır: Fake repository/DAO ile `replaceSupplementTemplates` veya final DataStore edit'inde exception fırlat; retry öncesi DataStore/Room state'ini kontrol et.
- Önerilen düzeltme: PR25.4'te completeOnboarding'i tek orchestrated result modeline al; hata halinde kullanıcıya retry-safe state göster, partial writes için regression test ekle.
- Hedef PR: PR25.4

### AUD-007

- ID: AUD-007
- Şiddet: P1
- Alan: Sleep / Trends / Date windows
- Dosya/fonksiyon: `MetricDateWindows.kt`, `TrendsRepositoryImpl`, `SleepDetailScreen`
- Problem: Monthly period bazı yerlerde month-to-date, bazı yerlerde rolling 30 days, sleep detail query tarafında month-start şeklinde uygulanıyor.
- Etki: Aynı seçili tarih için Trends, detail ring ve loaded data farklı gün kümelerini temsil edebilir. Ay başında geçmiş ay günleri boşmuş gibi görünebilir.
- Nasıl doğrulanır: 2026-05-05 anchor ile April sonu sleep records ekle; Sleep monthly detail, Trends monthly ve shared date-window helper sonuçlarını karşılaştır.
- Önerilen düzeltme: PR25.2'de period sözleşmesini tek helper üzerinden tanımla; weekly/monthly için inclusive boundaries ve query windows testlerini ekle.
- Hedef PR: PR25.2

### AUD-008

- ID: AUD-008
- Şiddet: P1
- Alan: Trends / Calculations
- Dosya/fonksiyon: `domain/src/main/java/com/burak/healthapp/domain/analytics/TrendCalculations.kt`, `averageByLoggedDays`
- Problem: Hydration, sleep, steps ve nutrition average değerleri yalnızca kayıtlı günlere göre hesaplanıyor; period completeness/adherence ise tüm günleri kullanıyor.
- Etki: Haftalık/aylık average, eksik kayıtlı dönemlerde olduğundan iyi görünebilir. Bu özellikle dashboard/trends karşılaştırmasını yanıltır.
- Nasıl doğrulanır: 7 günlük periodda sadece 1 gün 3000 ml su kaydı ile weekly average metnini ve goal/adherence göstergesini kontrol et.
- Önerilen düzeltme: PR25.2'de "logged-day average" ile "period-day average" ayrımını ürün kararına bağla; UI label ve mapper testlerini güncelle.
- Hedef PR: PR25.2

### AUD-009

- ID: AUD-009
- Şiddet: P1
- Alan: Profile / Goals
- Dosya/fonksiyon: `app/src/main/java/com/burak/healthapp/feature/profile/ProfileGoalsViewModel.kt`, `saveGoalsAndMeasurement`
- Problem: Goal settings ve body measurement ayrı persistence katmanlarına ardışık yazılıyor, exception yakalama/UI error state'i yok.
- Etki: Kullanıcı kaydettiğini sanabilir; hedefler değişmiş ama ölçüm kaydı yazılmamış veya tersi olabilir.
- Nasıl doğrulanır: `dashboardRepository.saveBodyMeasurement` fake'ini failure yap; `settingsRepository.updateGoalSettings` sonrası UI callback ve state'i kontrol et.
- Önerilen düzeltme: PR25.4'te save result/error state ekle; partial save davranışını testle ve kullanıcıya retry imkanı ver.
- Hedef PR: PR25.4

### AUD-010

- ID: AUD-010
- Şiddet: P1
- Alan: Meal / Nutrition / Custom Food
- Dosya/fonksiyon: `CustomFoodEditorViewModel.kt`, `CustomFoodEditorState`, `CustomFood`
- Problem: Domain/export modeli `fiberGrams`, `sugarGrams`, `sodiumMg` alanlarını destekliyor ve import/export koruyor. Edit UI state'i bu alanları taşımıyor; edit save yeni `CustomFood` oluştururken alanlar null kalabilir.
- Etki: Import edilmiş veya ileride detaylı kaydedilmiş custom food editlenince optional nutrient bilgisi kaybolabilir.
- Nasıl doğrulanır: Fiber/sugar/sodium içeren custom food import et, editor ile sadece name değiştirip kaydet, DB/export sonrası optional alanların korunup korunmadığını kontrol et.
- Önerilen düzeltme: PR25.1'de edit state'e hidden preserved fields ekle veya repository patch/update modeli kullan; roundtrip test ekle.
- Hedef PR: PR25.1

### AUD-011

- ID: AUD-011
- Şiddet: P2
- Alan: Data tests
- Dosya/fonksiyon: `data` module test source set
- Problem: `:data:test` geçiyor ama `data/src/test` bulunmadığı için data repository/import/export davranışları JVM seviyesinde testlenmiyor.
- Etki: Data katmanındaki idempotency ve mapping regressions yalnızca app/androidTest tarafına kalıyor.
- Nasıl doğrulanır: `git ls-files "*data/src/test*"` çıktısının boş olduğunu ve Gradle XML rapor üretmediğini kontrol et.
- Önerilen düzeltme: PR25.1'de importer/normalizer/merge key/data mapper için JVM testleri ekle.
- Hedef PR: PR25.1

### AUD-012

- ID: AUD-012
- Şiddet: P2
- Alan: Room migration
- Dosya/fonksiyon: `HealthDatabaseMigrations.kt`, `HealthDatabaseMigrationTest`
- Problem: Migration chain 1->7 var; test yalnızca 4->5 migration'ı kapsıyor ve CI'da connected olarak çalışmıyor.
- Etki: 5->6 caffeine ve 6->7 custom food schema değişiklikleri upgrade cihazlarında regress olabilir.
- Nasıl doğrulanır: Migration test imports ve test methodlarını kontrol et; connected testin CI'da çalışmadığını doğrula.
- Önerilen düzeltme: PR25.1'de 1->latest ve her migration step için test ekle; mümkünse Robolectric/JVM veya emulator CI job planla.
- Hedef PR: PR25.1

### AUD-013

- ID: AUD-013
- Şiddet: P2
- Alan: Today forms / Repository writes
- Dosya/fonksiyon: `TodayViewModel`, Today sheet save callbacks
- Problem: Today form save/delete aksiyonları repository çağrılarını fire-and-forget coroutine ile yapıyor; hata state'i kullanıcıya dönmüyor.
- Etki: Room/DataStore failure durumunda sheet kapanabilir veya input kaybolabilir; kullanıcı başarısızlığı görmez.
- Nasıl doğrulanır: Repository fake exception fırlatacak şekilde ViewModel testle; UI state'te error/snackbar olmadığını kontrol et.
- Önerilen düzeltme: PR25.4'te save/delete result state ve one-shot error event ekle; PR28'de form UX'ini sadeleştir.
- Hedef PR: PR25.4

### AUD-014

- ID: AUD-014
- Şiddet: P2
- Alan: Caffeine form
- Dosya/fonksiyon: `app/src/main/java/com/burak/healthapp/feature/today/CaffeineSheet.kt`
- Problem: Invalid caffeine mg input parse edilemez veya <=0 ise save callback sessizce return ediyor.
- Etki: Kullanıcı butona bastığında neden kayıt olmadığını anlamaz.
- Nasıl doğrulanır: Caffeine sheet'te boş/0/abc değerle Save'e bas; field error veya snackbar oluşmadığını kontrol et.
- Önerilen düzeltme: PR28'de field-level validation ve disabled save state ekle.
- Hedef PR: PR28

### AUD-015

- ID: AUD-015
- Şiddet: P2
- Alan: Water reminder settings
- Dosya/fonksiyon: `WaterReminderPreferenceCard`, `SettingsRepositoryImpl.updateWaterReminderSettings`
- Problem: Profile reminder interval UI herhangi pozitif değeri kabul ediyor; repository minimuma coerce ediyor. UI bu dönüşümü kullanıcıya anlatmıyor.
- Etki: Kullanıcı 1 dakika seçtiğini sanabilir, ama kayıt 15 dakikaya döner.
- Nasıl doğrulanır: Profile reminder interval'i minimumun altına ayarla; saved settings ve UI text'i karşılaştır.
- Önerilen düzeltme: PR25.3/PR28'de onboarding/profile validation sözleşmesini eşitle ve field error göster.
- Hedef PR: PR25.3

### AUD-016

- ID: AUD-016
- Şiddet: P2
- Alan: Today dashboard customization
- Dosya/fonksiyon: `TodayScreen.DashboardCustomizationSheet`
- Problem: Sheet local listesi `remember(cards)` ile kuruluyor; switch toggle immediate local update yapmadan repository flow güncellemesini bekliyor.
- Etki: Persistence gecikmesinde switch görsel olarak geri sekebilir veya kullanıcı stale state görebilir.
- Nasıl doğrulanır: DashboardRepository save geciktirilmiş fake ile Compose testte switch'e bas; checked state'in flow emission'a kadar değişmediğini kontrol et.
- Önerilen düzeltme: PR25.3'te optimistic local state veya loading/disabled state kullan; reset/reorder davranışını testle.
- Hedef PR: PR25.3

### AUD-017

- ID: AUD-017
- Şiddet: P2
- Alan: Navigation / State restore
- Dosya/fonksiyon: `AppNavigation.kt`, `MainShell.kt`, detail route'lar
- Problem: Detail ekranlar `selectedDate` değerini shell-level `rememberSaveable` state'ten alıyor; route argümanı yok.
- Etki: Process death, future deep link veya direct route senaryolarında detail ekran beklenen tarihi taşımayabilir.
- Nasıl doğrulanır: Detail route açıkken process recreation simüle et; route path'inde tarih bilgisi olmadığı için state kaynağının shell'e bağlı olduğunu doğrula.
- Önerilen düzeltme: PR26'da tarih argümanlı detail routes ve back-stack state sözleşmesi oluştur.
- Hedef PR: PR26

### AUD-018

- ID: AUD-018
- Şiddet: P2
- Alan: Localization / UI text
- Dosya/fonksiyon: `app/src/main/res/values/strings.xml`, `app/src/main/res/values-en/strings.xml`
- Problem: Basit resource key karşılaştırmasında `values-en` içinde olmayan 145 string key bulundu. Onboarding key'lerinde açık yok, ancak Today/Profile/detail/import key'leri İngilizce resource setinde eksik.
- Etki: İngilizce locale'de fallback davranışı tutarsız olabilir; profesyonel kalite ve accessibility testleri zayıflar.
- Nasıl doğrulanır: `values` ve `values-en` string key setlerini karşılaştır; `missing_en_count=145`, `missing_onboarding_count=0`.
- Önerilen düzeltme: PR27'de resource parity test/script ekle ve eksik İngilizce metinleri tamamla.
- Hedef PR: PR27

### AUD-019

- ID: AUD-019
- Şiddet: P2
- Alan: Today mapper / Localization
- Dosya/fonksiyon: `TodayUiMapper.kt`
- Problem: Today summary/card textlerinin bir bölümü mapper içinde doğrudan display string olarak üretiliyor; resource parity ve locale testleriyle birlikte yönetilmesi zorlaşıyor.
- Etki: Dil değişimi, snapshot test ve accessibility copy denetimi parçalı kalır.
- Nasıl doğrulanır: `TodayUiMapper.kt` içinde user-facing label/headline üretimlerini ara ve resource kullanımını karşılaştır.
- Önerilen düzeltme: PR27'de mapper display contract'ını resource id/format argümanı veya merkezi UI copy helper ile sadeleştir.
- Hedef PR: PR27

### AUD-020

- ID: AUD-020
- Şiddet: P2
- Alan: Meal editor validation
- Dosya/fonksiyon: `MealEditorViewModel.attachValidation`, `MealEditorSheet`
- Problem: Macro numeric validation çoğunlukla tek `macroError` alanına bağlanıyor; hangi macro field'ın hatalı olduğu net değil.
- Etki: Kullanıcı form hatasını çözmek için gereksiz deneme yapar.
- Nasıl doğrulanır: Protein/carbs/fat alanlarından birine invalid giriş ver; hata mesajının field-specific olup olmadığını kontrol et.
- Önerilen düzeltme: PR28'de field-level error modeline geç ve Compose UI test ekle.
- Hedef PR: PR28

### AUD-021

- ID: AUD-021
- Şiddet: P2
- Alan: Water Reminder / Notifications
- Dosya/fonksiyon: `WaterReminderWorker`, `WaterReminderScheduler`, permission effects
- Problem: Notification permission dışarıdan revoke edilirse worker permission check ile notification göstermeyebilir; ancak settings enabled ve periodic work devam edebilir.
- Etki: Kullanıcı reminder açık sanabilir ama bildirim almaz; gereksiz scheduled work devam eder.
- Nasıl doğrulanır: Reminder enabled iken POST_NOTIFICATIONS permission revoke et; WorkManager job ve profile UI state'ini kontrol et.
- Önerilen düzeltme: PR25.3'te app resume permission reconciliation, UI disabled reason ve scheduler cancel testleri ekle.
- Hedef PR: PR25.3

### AUD-022

- ID: AUD-022
- Şiddet: P2
- Alan: Water Reminder schedule UX
- Dosya/fonksiyon: `WaterReminderSchedule`, `WaterReminderPreferenceCard`
- Problem: Schedule calculation overnight window'ı destekliyor görünüyor; profile UI copy/validation bu davranışı açık hale getirmiyor.
- Etki: 22:00-02:00 gibi pencerelerde kullanıcı beklenen reminder saatlerini anlamayabilir.
- Nasıl doğrulanır: Overnight start/end time ile next reminder snapshot testini ve UI özet metnini karşılaştır.
- Önerilen düzeltme: PR28'de form açıklaması ve testleri ekle; PR25.3'te scheduler sözleşmesini sabitle.
- Hedef PR: PR28

### AUD-023

- ID: AUD-023
- Şiddet: P2
- Alan: Performance / Today
- Dosya/fonksiyon: `DashboardRepositoryImpl.observeToday`, `TodayViewModel`
- Problem: Today snapshot çok sayıda Flow ve Room kaynağını combine ediyor; her emission tüm UI snapshot mapping'ini tetikliyor.
- Etki: Veri hacmi arttığında recomposition ve query maliyeti artabilir; mevcut benchmark bu ekranı düzenli raporlamıyor.
- Nasıl doğrulanır: Büyük meal/hydration/supplement veri setiyle Today recomposition count ve trace al.
- Önerilen düzeltme: PR29'da benchmark/reporting ekle; gerekirse PR27'de UI state parçalama ve derived mapping iyileştirmesi yap.
- Hedef PR: PR29

### AUD-024

- ID: AUD-024
- Şiddet: P3
- Alan: Lint / Polish
- Dosya/fonksiyon: `app/build/reports/lint-results-debug.txt`
- Problem: `lintDebug` 0 error ile geçti ama 142 warning ve 1 hint raporladı. Uyarılar çoğunlukla typography ve version catalog temizlikleri.
- Etki: CI kırmıyor; ancak profesyonel kalite ve rapor okunabilirliği düşüyor.
- Nasıl doğrulanır: `./gradlew lintDebug` sonrası lint text/html raporunu aç.
- Önerilen düzeltme: PR29'da lint warning borcunu sınıflandırıp güvenli olanları temizle.
- Hedef PR: PR29

### AUD-025

- ID: AUD-025
- Şiddet: P3
- Alan: Benchmark / CI reporting
- Dosya/fonksiyon: `benchmark/build.gradle.kts`, `benchmark/src/main/java/...`
- Problem: Baseline profile ve macrobenchmark testleri var, ancak CI bunları çalıştırmıyor veya raporlamıyor.
- Etki: Performance regressions düzenli olarak görünür değil.
- Nasıl doğrulanır: CI workflow'da `:benchmark:connectedCheck` adımı olmadığını kontrol et.
- Önerilen düzeltme: PR29'da benchmark koşullarını ayrı job/manual workflow olarak tanımla ve rapor artifact'larını yükle.
- Hedef PR: PR29

### AUD-026

- ID: AUD-026
- Şiddet: P3
- Alan: Gradle cleanup
- Dosya/fonksiyon: `app/build.gradle.kts`
- Problem: Lint `UseTomlInstead` uyarıları Compose dependency satırları için version catalog kullanımını öneriyor.
- Etki: Düşük riskli bakım borcu.
- Nasıl doğrulanır: Lint report'ta `UseTomlInstead` uyarılarını kontrol et.
- Önerilen düzeltme: PR29'da dependency alias'larını version catalog'a taşı.
- Hedef PR: PR29

### AUD-027

- ID: AUD-027
- Şiddet: P2
- Alan: UI/UX / Accessibility
- Dosya/fonksiyon: Onboarding, Today sheets, Profile forms
- Problem: Font scale 1.3/1.5, küçük ekran ve screenshot regression testleri kritik formlar için sistematik değil.
- Etki: Uzun Türkçe/İngilizce metinler, form alanları ve bottom sheet CTA'ları küçük ekranda taşabilir.
- Nasıl doğrulanır: 320dp genişlik, font scale 1.5 ve dark mode ile onboarding/today/profile form screenshot testi çalıştır.
- Önerilen düzeltme: PR27/PR28'de Compose UI/screenshot smoke suite ekle ve taşan ekranları polish et.
- Hedef PR: PR27

### AUD-028

- ID: AUD-028
- Şiddet: P2
- Alan: Import/export roundtrip
- Dosya/fonksiyon: `JsonHealthDataImporter`, `HealthDataExportRepositoryImpl`, `HealthDataManagementRepositoryImpl`
- Problem: Schema v1/v2/v3 desteği ve custom-food idempotency kodda mevcut; ancak full export -> import -> export roundtrip JVM testi yok.
- Etki: Gelecek schema değişiklikleri duplicate/idempotency davranışını sessizce bozabilir.
- Nasıl doğrulanır: Aynı export'u iki kez import eden full fixture testini JVM'de çalıştırmaya çalış; mevcut kapsamın androidTest'e bağlı olduğunu gör.
- Önerilen düzeltme: PR25.1'de serializer/import planner/merge key pure-Kotlin testlerini ekle.
- Hedef PR: PR25.1

## Risk matrisi

| Risk türü | Bulgular | Öncelik | Önerilen sıra |
|---|---|---|---|
| Veri kaybı / partial persistence | AUD-001, AUD-006, AUD-009, AUD-010 | Çok yüksek | PR25.1, PR25.4 |
| Yanlış hesaplama / yanlış yönlendirme | AUD-007, AUD-008 | Yüksek | PR25.2 |
| Crash / service lifecycle | AUD-003 | Yüksek | PR25.4 |
| Settings/UI state tutarsızlığı | AUD-004, AUD-005, AUD-015, AUD-016, AUD-021 | Orta-yüksek | PR25.3 |
| Test ve CI kör noktaları | AUD-002, AUD-011, AUD-012, AUD-028 | Yüksek | PR25.1 |
| Forms/UX/accessibility | AUD-013, AUD-014, AUD-020, AUD-022, AUD-027 | Orta | PR27, PR28 |
| Performance/battery/quality | AUD-023, AUD-024, AUD-025, AUD-026 | Orta-düşük | PR29 |

## Sonraki PR önerileri

| PR | Ana hedef | Öncelikli audit ID'leri |
|---|---|---|
| PR25.1 | Import/export/delete-all consistency fixes | AUD-001, AUD-002, AUD-010, AUD-011, AUD-012, AUD-028 |
| PR25.2 | Date/time and weekly/monthly calculation fixes | AUD-007, AUD-008 |
| PR25.3 | Dashboard visibility and settings consistency fixes | AUD-004, AUD-005, AUD-015, AUD-016, AUD-021 |
| PR25.4 | State restore, ViewModel error and save-state fixes | AUD-003, AUD-006, AUD-009, AUD-013 |
| PR26 | Navigation, Menus & Detail Page Restructure | AUD-017 |
| PR27 | UI/UX Usability Polish | AUD-018, AUD-019, AUD-027 |
| PR28 | Forms, Sheets & Input Flow Simplification | AUD-014, AUD-020, AUD-022 |
| PR29 | Visual Consistency & Interaction Polish / performance hygiene | AUD-023, AUD-024, AUD-025, AUD-026 |

