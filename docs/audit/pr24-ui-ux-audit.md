# PR24 — UI/UX Audit

Bu ek doküman, PR24 full audit bulgularının UI/UX ve accessibility tarafını ayrıntılandırır. Yeni UI redesign önermez; sonraki PR'larda hangi mevcut akışların netleştirileceğini listeler.

## Önceliklendirilmiş UI/UX bulguları

| ID | Şiddet | Alan | Dosya/Fonksiyon | Problem | Etki | Hedef PR |
|---|---|---|---|---|---|---|
| AUD-005 | P1 | Onboarding | `OnboardingViewModel.finishOnboarding` | Step tracking tercihi tamamlamada false kaydediliyor; UI bunu net anlatmayabilir. | Kullanıcı tracking'in açıldığını sanabilir. | PR25.3 |
| AUD-014 | P2 | Caffeine sheet | `CaffeineSheet` | Invalid input sessiz return ediyor. | Kullanıcı neden kayıt olmadığını anlamaz. | PR28 |
| AUD-015 | P2 | Water reminder | `WaterReminderPreferenceCard` | Minimum interval profile'da sessiz coerce ediliyor. | Kaydedilen değer kullanıcı beklentisinden farklı olabilir. | PR25.3 |
| AUD-016 | P2 | Dashboard customization | `DashboardCustomizationSheet` | Switch state repository emission beklediği için stale/bounce görünebilir. | Ayar değişti mi değişmedi mi belirsizleşir. | PR25.3 |
| AUD-018 | P2 | Localization | `values`, `values-en` | İngilizce resource setinde 145 key eksik. | Locale kalitesi ve accessibility copy denetimi zayıflar. | PR27 |
| AUD-019 | P2 | Today copy | `TodayUiMapper` | Display text üretimi mapper içinde parçalı. | Localization ve snapshot testleri zorlaşır. | PR27 |
| AUD-020 | P2 | Meal editor | `MealEditorViewModel`, `MealEditorSheet` | Macro validation field-specific değil. | Kullanıcı hangi alanı düzelteceğini anlamayabilir. | PR28 |
| AUD-022 | P2 | Reminder UX | `WaterReminderSchedule`, profile reminder card | Overnight reminder penceresi UI'da açık değil. | Kullanıcı beklenen bildirim saatlerini yanlış okuyabilir. | PR28 |
| AUD-027 | P2 | Accessibility | Onboarding/Today/Profile forms | Font scale ve küçük ekran screenshot kapsamı sistematik değil. | Taşma ve touch target regressions yakalanmayabilir. | PR27 |

## Ekran bazlı notlar

| Ekran / Akış | Gözlem | Risk | Önerilen sonraki PR |
|---|---|---|---|
| Onboarding | Smart goal ve selection state için test kapsamı iyi. Step tracking tercihi güvenlik nedeniyle false kaydediliyor. | Copy/expectation mismatch | PR25.3 |
| Today dashboard | Kart visibility ve reset/order davranışı var. Customization sheet persistence latency'ye hassas. | Stale toggle state | PR25.3 |
| Meal editor | Toplam ve macro validation mevcut; field-level macro error zayıf. | Form çözümleme yükü | PR28 |
| Custom food editor | Required/numeric validation var; optional nutrient preservation UI state'i eksik. | Veri kaybı + form güveni | PR25.1 / PR28 |
| Caffeine sheet | Hızlı kayıt basit; invalid input sessiz. | Kullanıcı feedback eksik | PR28 |
| Water reminder profile | Parse/coerce güvenli; min interval feedback eksik. | Kullanıcı ayarı yanlış anlar | PR25.3 / PR28 |
| Profile data management | Export/import/delete-all akışı görünür; partial import davranışı kullanıcıya daha net anlatılmalı. | Import failure algısı yanlış | PR25.1 |
| Detail screens | Period UI'ları mevcut; selectedDate route sözleşmesi zayıf. | Process death/direct route kırılgan | PR26 |
| Sleep/Trends | Sleep stability dili genel olarak dikkatli; monthly period mismatch UX'e yanlış metrik olarak yansıyabilir. | Yanlış yönlendirme | PR25.2 |

## Accessibility ve localization

| Kontrol | Sonuç | Aksiyon |
|---|---|---|
| Onboarding string key parity | `missing_onboarding_count=0` | Onboarding tarafı PR23 cleanup sonrası iyi durumda. |
| Genel `values-en` parity | `missing_en_count=145` | PR27'de tamamlanmalı ve parity test eklenmeli. |
| Content descriptions | Yeni Today/detail delete/action key'lerinin bir kısmı English resource'ta eksik | PR27'de accessibility copy parity yapılmalı. |
| Font scale 1.3/1.5 | Sistematik screenshot test yok | PR27'de smoke suite eklenmeli. |
| Small screen sheets | Manuel risk var; sheet CTA/keyboard overlap testleri eksik | PR28'de form testleriyle kapatılmalı. |
| Touch target | Kodda genel Material/Compose component kullanımı iyi; özel icon/action satırları screenshot ile doğrulanmalı | PR27/PR29 |
| Dark mode contrast | Theme altyapısı var; contrast snapshot/automated check yok | PR27/PR29 |
| Medical claim language | Sleep stability "stability/variation" dili kontrollü görünüyor; future insight-like text için guardrail yok | PR27 |

## Form/sheet audit checklist

| Form | Keyboard/IME | Validation | Save/delete feedback | Küçük ekran riski | Hedef PR |
|---|---|---|---|---|---|
| Meal editor | Numeric alanlar var | Macro error field-specific değil | Save failure surface eksik | Orta | PR28 |
| Custom food editor | Numeric alanlar var | Required/numeric testli; optional fields korunmuyor | Save failure surface sınırlı | Orta | PR25.1 / PR28 |
| Water sheet | Basit numeric | Profile/onboarding davranışı farklı | Save failure surface eksik | Düşük-orta | PR28 |
| Caffeine sheet | Numeric | Invalid sessiz | Feedback yok | Düşük-orta | PR28 |
| Smoking sheet | Basit input | Edge validation ayrıca testlenmeli | Save failure surface eksik | Düşük | PR28 |
| Exercise sheet | Duration/intensity input | Field errors gözden geçirilmeli | Save failure surface eksik | Orta | PR28 |
| Weight sheet | Numeric | BMI/measurement edge testleri artırılmalı | Save failure surface eksik | Orta | PR25.4 / PR28 |
| Supplement dose sheet | Template/dose mapping mevcut | Delete/save feedback testleri eksik | Save/delete failure surface eksik | Orta | PR28 |
| Onboarding smart goal fields | Validation regression testleri iyi | UI small screen/font scale testi eksik | Save failure state var ama repository partial yazım var | Orta | PR25.4 / PR27 |

## PR27/PR28 için kabul edilebilir UI çalışması sınırı

- PR27: Copy, resource parity, empty/loading/error clarity, screenshot/accessibility smoke tests.
- PR28: Field-level validation, keyboard/IME consistency, save/cancel/delete feedback, sheet ergonomics.
- PR24 bulguları navigation restructure veya full redesign gerektirmez; bunlar PR26/PR29 kapsamına ayrılmalı.

