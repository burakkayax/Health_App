# PR24 — Test Gap Map

## CI gerçekliği

| Test/verification tipi | Durum | PR24 gözlemi |
|---|---|---|
| Format | CI ve lokal geçiyor | `spotlessCheck` kombine komutta geçti. |
| Static analysis | CI ve lokal geçiyor | `detekt` geçti. |
| Lint | CI ve lokal geçiyor | `lintDebug` geçti, 142 warning kaldı. |
| Domain JVM unit | CI ve lokal geçiyor | 57 test geçti. |
| App JVM unit | CI ve lokal geçiyor | 219 test geçti. |
| Data JVM unit | Task geçiyor | `data/src/test` yok; fiili data test kapsamı yok. |
| Android instrumented | CI'da yalnız compile | `:app:compileDebugAndroidTestKotlin` geçiyor, connected run yok. |
| Compose UI | CI'da yalnız compile | `app/src/androidTest` testleri cihazda koşmuyor. |
| Macrobenchmark | CI'da yok | Bağlı cihaz olmadığı için PR24'te de çalıştırılmadı. |

## Test boşlukları

| Alan | Mevcut Test | Eksik Test | Risk | Önerilen Test Türü | Hedef PR |
|---|---|---|---|---|---|
| Import partial settings failure | Instrumented test var ama CI'da çalışmıyor | Settings failure sonrası partial import contract'ı JVM'de yok | Import sonucu kullanıcıya yanlış raporlanabilir | Repository unit / JVM unit | PR25.1 |
| Custom food idempotency | AndroidTest ve importer unit parçaları var | Same export twice import full roundtrip JVM testi yok | Duplicate veya stale merge regression | JVM unit / Repository unit | PR25.1 |
| Custom food optional nutrients | Export/import alanları korunuyor | Edit sonrası fiber/sugar/sodium preservation testi yok | Optional nutrient veri kaybı | ViewModel unit / Repository unit | PR25.1 |
| Schema v1/v2/v3 compatibility | Importer unit testleri var | Full export->import->export fixture testi yok | Backward compatibility kırılabilir | JVM unit | PR25.1 |
| Delete all | Repository path incelendi | Settings korunumu ve tüm health tabloları için JVM/integration assertion yok | Silme eksik veya fazla olabilir | Repository unit / Android instrumented | PR25.1 |
| Room migrations | 4->5 test var | 1->latest, 5->6, 6->7 testleri yok; CI connected run yok | Upgrade cihazlarında crash/data kaybı | Android instrumented / Robolectric mümkünse | PR25.1 |
| Data module repositories | Yok | `data/src/test` source set kapsamı yok | Data mapping ve Room-independent logic kör | JVM unit | PR25.1 |
| Monthly date windows | Bazı helper/unit testleri var | Trends/detail/monthly contract tek fixture ile testlenmiyor | Yanlış weekly/monthly değerler | JVM unit | PR25.2 |
| Sleep monthly early-month | Sleep stability domain testleri güçlü | Sleep detail query vs ring day window ay başı testi yok | Ay başında geçmiş ay günleri yanlış empty görünebilir | ViewModel unit / Compose UI | PR25.2 |
| Logged-day vs period-day average | Domain helper testleri sınırlı | Missing-day average label/mapper contract testi yok | Kullanıcı yanlış ortalama okuyabilir | JVM unit / ViewModel unit | PR25.2 |
| Dashboard visibility | ViewModel/config testleri var | Customization sheet optimistic toggle/reorder/reset Compose testi eksik | UI state bounce/stale görünüm | Compose UI | PR25.3 |
| Step permission reconciliation | Step decision tests var | Permission revoke sonrası setting/UI/service reconciliation testi eksik | Toggle açık ama servis kapalı kalabilir | ViewModel unit / Android instrumented | PR25.3 |
| Water reminder permission | Schedule tests var | POST_NOTIFICATIONS revoke sonrası scheduler cancel/UI disabled reason testi yok | Reminder açık görünür ama bildirim gelmez | ViewModel unit / Android instrumented | PR25.3 |
| Water reminder min interval | Onboarding validation var | Profile form min interval field error testi yok | Silent coercion | Compose UI / ViewModel unit | PR25.3 |
| Onboarding partial complete | ViewModel regression testleri var | Repository-level failure injection ve partial write testi yok | Onboarding incomplete + partial settings | Repository unit / ViewModel unit | PR25.4 |
| Profile goals save | Sınırlı | Goals success + measurement failure state testi yok | Partial save sessiz kalabilir | ViewModel unit | PR25.4 |
| Today sheet save failure | Yok veya sınırlı | Room exception sonrası snackbar/error state testi yok | Kullanıcı kaydın başarısız olduğunu görmez | ViewModel unit / Compose UI | PR25.4 |
| Foreground service timing | Start decision tests var | `startForeground` early-call lifecycle test yok | Android service timeout crash | Android instrumented / Robolectric | PR25.4 |
| Navigation route state | Yok | Detail route date arg/process death testi yok | Direct route/deep link yanlış tarih | Navigation test / Compose UI | PR26 |
| Localization parity | Manuel karşılaştırma yapıldı | values/values-en key parity CI testi yok | İngilizce locale eksik/fallback | JVM script test / lint custom check | PR27 |
| Small screen/font scale | Bazı Compose tests var | Onboarding, Today sheets, Profile forms için 320dp/fontScale 1.5 screenshot yok | Taşma ve erişilebilirlik regression | Screenshot test / Compose UI | PR27 |
| Form validation specificity | Unit testler parçalı | Meal macro field-specific, caffeine invalid, custom food edge UI testleri eksik | Kullanıcı hatayı çözemez | Compose UI / ViewModel unit | PR28 |
| Macrobenchmark | Benchmark module var | CI/manual workflow raporlama yok | Performance regression görünmez | Macrobenchmark | PR29 |
| Lint warning budget | Lint çalışıyor | Warning budget veya baseline policy yok | Warning borcu büyüyebilir | CI policy / static analysis | PR29 |

## Öncelikli test taşıma önerisi

| Sıra | Test hedefi | Neden |
|---:|---|---|
| 1 | Import planner/merge key/idempotency JVM tests | PR25.1'in veri kaybı riskini en hızlı düşürür. |
| 2 | Room migration full-chain connected/Robolectric tests | Upgrade crash/data kaybını yakalar. |
| 3 | Date-window contract tests | PR25.2 hesaplama düzeltmelerini güvenli kılar. |
| 4 | Step permission + foreground service lifecycle tests | Crash ve misleading tracking state riskini kapatır. |
| 5 | Forms save failure ViewModel tests | Sessiz başarısızlıkları kullanıcıya görünür hale getiren PR25.4'ü güvenceye alır. |

