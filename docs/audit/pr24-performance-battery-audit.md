# PR24 — Performance & Battery Audit

Bu ek doküman performans, batarya, background work ve ölçüm altyapısı risklerini listeler. PR24 kapsamında herhangi bir optimizasyon yapılmadı.

## Bulgular

| ID | Şiddet | Alan | Dosya/Fonksiyon | Problem | Etki | Hedef PR |
|---|---|---|---|---|---|---|
| AUD-003 | P1 | Foreground service | `StepCounterService` | `startForeground` çağrısı DataStore okumasından sonra geliyor. | Timeout/crash ve kötü başlangıç deneyimi. | PR25.4 |
| AUD-021 | P2 | WorkManager/notifications | `WaterReminderWorker`, `WaterReminderScheduler` | Permission revoke sonrası periodic work/settings reconcile eksik olabilir. | Reminder açık görünür ama bildirim gelmez; gereksiz iş kalır. | PR25.3 |
| AUD-023 | P2 | Today performance | `DashboardRepositoryImpl.observeToday`, `TodayViewModel` | Çok sayıda Flow emission'ı tüm Today snapshot mapping'ini tetikliyor. | Veri arttıkça recomposition/query maliyeti. | PR29 |
| AUD-025 | P3 | Benchmark | `benchmark` module | Macrobenchmark/baseline profile CI'da raporlanmıyor. | Performance regressions görünmez. | PR29 |
| AUD-024 | P3 | Lint/quality | lint report | 142 warning kaldı. | Gürültülü kalite sinyali. | PR29 |

## Performans/batarya akışları

| Akış | Mevcut durum | Risk | Öneri |
|---|---|---|---|
| Today screen data aggregation | Repository birçok DAO/settings Flow'unu combine edip ViewModel'de UI snapshot'a map ediyor. | Sık veri yazımında geniş recomposition. | PR29'da recomposition count ve trace ölç; gerekirse UI state parçala. |
| Meal/custom food search | Turkish normalizer ve custom/preset food birleşimi mevcut. | Büyük custom food listesinde search latency ölçülmemiş. | PR29'da fixture benchmark veya lightweight search unit perf ölçümü. |
| Charts/detail lists | Detail ekranlarda period day listeleri ve chart/ring hesapları UI tarafında yapılabiliyor. | Büyük listelerde frame drop riski. | PR29'da selected detail screens için macrobenchmark veya trace. |
| Step foreground service | Sensor reset/write policy testlenmiş; service lifecycle timing zayıf. | Timeout crash ve battery/user trust riski. | PR25.4'te early foreground + safe stop. |
| Step sensor writes | Reset/baseline logic var. | Gün değişimi/sensor reset edge cases testli ama lifecycle permission drift eklenmeli. | PR25.3/PR25.4 testleri. |
| Water reminders | WorkManager ve permission check var. | Permission revoke veya disabled state sonrası scheduled work kalabilir. | PR25.3 scheduler reconciliation. |
| Baseline profile | Benchmark module mevcut. | CI/manual reporting yoksa etkisi izlenmez. | PR29 manual workflow/artifacts. |
| Lint warning budget | Lint çalışıyor. | Warning sayısı kalite sinyalini zayıflatır. | PR29 warning budget/cleanup. |

## PR24 verification sonucu

| Komut | Sonuç | Performans/batarya açısından not |
|---|---|---|
| `:app:assembleDebug` | Geçti | Debug APK üretildi; runtime performans ölçümü değildir. |
| `:app:assembleRelease` | Geçti | Release unsigned APK üretildi; baseline profile etkisi ölçülmedi. |
| `:benchmark:connectedCheck` | Çalıştırılmadı | Bağlı emulator/cihaz yoktu. |
| `:app:connectedDebugAndroidTest` | Çalıştırılmadı | Bağlı emulator/cihaz yoktu. |

## PR29 ölçüm önerisi

| Ölçüm | Hedef | Kabul sinyali |
|---|---|---|
| Startup macrobenchmark | App cold start | Önceki baseline'a göre regresyon yok. |
| Today large-data trace | 90 gün meal/hydration/supplement/steps fixture | Frame drop ve excessive recomposition yok. |
| Detail chart scroll/render | Sleep/hydration/exercise monthly views | Jank veya boş frame yok. |
| Baseline profile generation | Critical startup/navigation path | Artifact üretiliyor ve CI/manual workflow'a yükleniyor. |
| WorkManager audit | Reminder enabled/disabled/permission revoke | Gereksiz periodic work kalmıyor. |

