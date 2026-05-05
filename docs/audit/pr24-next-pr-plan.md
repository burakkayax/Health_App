# PR24 — Next PR Plan

## PR25.1 — Import/export/delete-all consistency fixes

### Amaç

Import/export/delete-all akışlarında veri bütünlüğünü ve idempotency güvenini artırmak. PR25.1 yeni özellik eklememeli; var olan data management davranışını daha tutarlı, testlenebilir ve kullanıcıya doğru raporlanır hale getirmeli.

### Kapsam

| Başlık | Kapsam |
|---|---|
| Partial import contract | Room success + DataStore failure durumunu explicit result veya retry-safe modelle yönet. |
| Custom food preservation | Edit akışında `fiberGrams`, `sugarGrams`, `sodiumMg` korunmalı. |
| Import planner tests | Merge key, older/newer `updatedAt`, same export twice import idempotency JVM testleri. |
| Migration coverage | 1->latest, 5->6, 6->7 migration testleri. |
| Delete all coverage | Tüm health tabloları siliniyor mu, settings korunuyor mu testle. |
| CI coverage | Kritik androidTest'leri JVM'e taşı veya emulator job planla. |

### Öncelikli audit ID'leri

AUD-001, AUD-002, AUD-010, AUD-011, AUD-012, AUD-028

### Test planı

| Test | Tür |
|---|---|
| Import success, settings failure, retry | Repository/JVM + integration |
| Same export twice import no duplicate | JVM unit |
| Custom food optional nutrient edit preservation | ViewModel unit / Repository unit |
| Export -> import -> export fixture equality | JVM unit |
| Delete all table coverage | Repository/instrumented |
| Room migration 1->latest and step-by-step | Android instrumented / Robolectric if feasible |

### Kabul kriterleri

- Settings failure sonrası import davranışı kullanıcıya ve tests'e açıkça yansır.
- Same export twice import duplicate üretmez.
- Older/newer custom food `updatedAt` davranışı korunur.
- Custom food optional nutrient alanları edit sonrası kaybolmaz.
- Delete all health tabloları için testli kapsam vardır.
- Kritik import/migration testlerinin en az bir bölümü CI'da gerçekten çalışır.

## PR25.2 — Date/time and weekly/monthly calculation fixes

### Amaç

Weekly/monthly period tanımlarını ve average hesaplama sözleşmesini tekleştirerek dashboard, detail ve trends ekranlarının aynı veriye aynı anlamı vermesini sağlamak.

### Kapsam

| Başlık | Kapsam |
|---|---|
| Monthly contract | month-to-date mi rolling 30 mu ürün kararı ver; tüm helper/repository/UI buna uysun. |
| Inclusive boundaries | Start/end LocalDate boundaries için test seti oluştur. |
| Sleep detail query | Monthly query ve ring/display day listesi aynı window'ı kullansın. |
| Logged-day average | UI label ya da calculation contract netleşsin. |
| Time edge cases | Midnight-adjacent sleep, DST-independent LocalTime/LocalDate fixtures. |

### Öncelikli audit ID'leri

AUD-007, AUD-008

### Test planı

| Test | Tür |
|---|---|
| Weekly/monthly window helper golden fixtures | JVM unit |
| Ay başı monthly detail query/ring consistency | ViewModel unit |
| 0/1/enough sleep records stability state | Domain unit |
| Missing-day average behavior | Domain + mapper unit |
| Trends vs Today selectedDate consistency | ViewModel/repository unit |

### Kabul kriterleri

- Monthly period tanımı tek dokümante helper üzerinden gelir.
- Sleep detail ve Trends aynı date range'i kullanır.
- Missing-day average davranışı bilinçli ürün kararıyla label/test kazanır.
- Date boundary tests CI'da çalışır.

## PR25.3 — Dashboard visibility and settings consistency fixes

### Amaç

Onboarding/Profile/Today settings state'lerini kullanıcı beklentisiyle uyumlu hale getirmek; dashboard visibility, step tracking ve reminder permission drift'lerini kapatmak.

### Kapsam

| Başlık | Kapsam |
|---|---|
| Step tracking permission | Permission revoke/no sensor durumunda settings ve UI reconcile edilsin. |
| Onboarding step preference | UI copy ve saved setting sözleşmesi netleşsin. |
| Dashboard customization | Switch/reorder/reset optimistic veya loading state ile tutarlı olsun. |
| Water reminder validation | Profile ve onboarding min interval/time parse davranışı eşitlensin. |
| Notification permission | Permission revoked olduğunda scheduler/UI state güncellensin. |

### Öncelikli audit ID'leri

AUD-004, AUD-005, AUD-015, AUD-016, AUD-021

### Test planı

| Test | Tür |
|---|---|
| Permission revoke -> step setting false/disabled reason | ViewModel unit / instrumented |
| Onboarding step preference completion contract | ViewModel unit / Compose UI |
| Dashboard customization delayed repository fake | Compose UI |
| Reminder min interval invalid profile input | ViewModel/Compose UI |
| Notification permission revoke cancels or disables reminder | ViewModel/instrumented |

### Kabul kriterleri

- Step tracking UI gerçek service/permission durumuyla tutarlı olur.
- Onboarding step tracking kullanıcının beklentisini yanlış kurmaz.
- Dashboard card visibility save gecikmesinde görsel olarak stale/bounce olmaz.
- Reminder settings invalid/coerced state'i kullanıcıya görünürdür.

## PR25.4 — State restore, ViewModel error and save-state fixes

### Amaç

Kritik save/delete/onboarding/service başlangıç akışlarında hata state'lerini görünür ve retry-safe yapmak.

### Kapsam

| Başlık | Kapsam |
|---|---|
| Onboarding complete | Partial write failure path ve retry state. |
| Profile goals save | Goals + measurement save result/error state. |
| Today sheets | Save/delete failure snackbar veya field-level error. |
| Step foreground service | Early `startForeground` ve safe stop path. |
| Saved state | Critical ViewModel restore senaryoları için regression tests. |

### Öncelikli audit ID'leri

AUD-003, AUD-006, AUD-009, AUD-013

### Test planı

| Test | Tür |
|---|---|
| Onboarding repository failure injection | ViewModel/repository unit |
| Profile measurement failure after goals success | ViewModel unit |
| Today sheet save exception keeps user-informed state | ViewModel/Compose UI |
| Foreground service early notification path | Android instrumented / Robolectric |
| SavedStateHandle invalid/partial state restore | ViewModel unit |

### Kabul kriterleri

- Kritik save path'lerinde sessiz failure kalmaz.
- Kullanıcı input'u failure durumunda kaybolmaz veya açık retry sunulur.
- Step service lifecycle timeout riski azaltılır.

## PR26 — Navigation, Menus & Detail Page Restructure

### Amaç

Detail ekranların route/state sözleşmesini güçlendirmek ve future deep link/direct route desteğini kolaylaştırmak.

### Kapsam

| Başlık | Kapsam |
|---|---|
| Date route args | Detail ekranlar gerekli tarihi route argümanı veya typed nav state ile taşısın. |
| Back behavior | Bottom sheet/dialog açıkken back davranışı testlensin. |
| Menus/detail | Detail CTA ve menu entry noktaları merkezi hale gelsin. |
| Process death | Route + SavedStateHandle restore smoke tests. |

### Öncelikli audit ID'leri

AUD-017

### Test planı

Navigation unit/smoke tests, Compose UI back-stack tests, route argument parser tests.

### Kabul kriterleri

- Detail ekran seçili tarihi route state'ten tekrar kurabilir.
- Process death/direct route davranışı testlidir.
- Today/Profile navigation davranışı bozulmaz.

## PR27 — UI/UX Usability Polish

### Amaç

Kullanıcı-facing metin, localization, empty/loading/error state ve accessibility görünürlük açıklarını kapatmak.

### Kapsam

| Başlık | Kapsam |
|---|---|
| Localization parity | `values` ve `values-en` key eşliği sağlanır. |
| UI copy centralization | Today mapper hard-coded display textleri sadeleşir. |
| Empty/loading/error states | Kritik kart ve detail ekranlarda yönlendirici metinler netleşir. |
| Font scale/small screen | 320dp + fontScale 1.5 smoke coverage. |
| Medical claim language | Sleep/trends copy sağlık tavsiyesi gibi görünmez. |

### Öncelikli audit ID'leri

AUD-018, AUD-019, AUD-027

### Test planı

Resource parity test, Compose UI/screenshot smoke tests, dark mode/font scale checks.

### Kabul kriterleri

- İngilizce resource seti eksik key bırakmaz.
- Küçük ekran/font scale kritik ekranlarda taşma üretmez.
- Sleep/trends metinleri kesin tıbbi iddia içermez.

## PR28 — Forms, Sheets & Input Flow Simplification

### Amaç

Form validasyonunu, keyboard/IME davranışını ve save/cancel/delete UX'ini kullanıcı için daha öngörülebilir yapmak.

### Kapsam

| Başlık | Kapsam |
|---|---|
| Caffeine/water sheets | Silent invalid return yerine field error. |
| Meal editor | Macro field-specific errors. |
| Custom food editor | Validation ve preservation state görünürlüğü. |
| Supplement/weight/exercise sheets | Keyboard type, IME action, cancel/back consistency. |
| Delete confirmations | Yanlışlıkla save/delete tetiklenmesini azaltma. |

### Öncelikli audit ID'leri

AUD-014, AUD-020, AUD-022

### Test planı

ViewModel validation tests, Compose UI keyboard/field error tests, small-screen sheet tests.

### Kabul kriterleri

- Invalid input hiçbir kritik formda sessiz kalmaz.
- Hata mesajı doğru field veya form bölgesinde görünür.
- Back/cancel/save davranışı testlenmiş ve tutarlıdır.

## PR29 — Visual Consistency & Interaction Polish

### Amaç

Görsel tutarlılık, lint warning borcu, benchmark raporlama ve performance/battery gözlemlenebilirliğini iyileştirmek.

### Kapsam

| Başlık | Kapsam |
|---|---|
| Lint warning cleanup | Typography/version catalog uyarıları sınıflandırılır ve temizlenir. |
| Benchmark reporting | Baseline profile/macrobenchmark için manual veya scheduled workflow. |
| Today recomposition | Büyük veri setiyle recomposition/trace ölçümü. |
| Chart/list performance | Lazy list/chart rendering smoke ölçümleri. |
| Visual consistency | Padding, touch target, dark mode contrast polish. |

### Öncelikli audit ID'leri

AUD-023, AUD-024, AUD-025, AUD-026

### Test planı

Macrobenchmark, baseline profile generation check, lint warning budget, selected screen screenshot/contrast checks.

### Kabul kriterleri

- Performance regressions için tekrarlanabilir ölçüm yolu vardır.
- Lint warning raporu daha temiz ve yönetilebilir olur.
- Görsel polish kodun davranışsal riskini artırmadan yapılır.

