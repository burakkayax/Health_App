# Changelog

## PR23.15 - Meal History & Nutrition Detail Polish

### Eklendi
- Öğün geçmişi ekranına günlük özet kartı eklendi (toplam kalori, makro dağılımı, öğün/besin sayısı).
- DailySummaryCard composable ile toplam değerler HealthPrimary renkte gösteriliyor.

### Değiştirildi
- Öğün geçmişi ekranındaki tüm hardcoded Türkçe metinler string resource'larına taşındı.
- Besin satırlarındaki makro gösterimi formatlı string resource ile düzenlendi.
- Silme butonu contentDescription olarak string resource kullanacak şekilde güncellendi.
- Hem Türkçe hem İngilizce dil desteği eklendi.

## PR23.14 - Nutrition Entry Flow v2

### Eklendi
- MealTotalSummary veri modeli eklendi (toplam kalori, protein, karb, yağ, besin sayısı).
- Öğün düzenleyicide "Besin Ekle" butonundan sonra gerçek zamanlı toplam makro çubuğu eklendi.
- Toplam bar yalnızca en az bir besin girişi olduğunda gösteriliyor.

### Değiştirildi
- MealEditorViewModel her draft değişikliğinde toplam özeti yeniden hesaplıyor.
- MealEditorSheet'e MealTotalSummaryBar composable eklendi.

## PR23.13.2 - Custom Food Data Management Integration

### Eklendi
- Export şeması v2'den v3'e yükseltildi; ExportedCustomFood veri modeli eklendi.
- Özel besinler dışa aktarma dosyasına dahil ediliyor.
- İçe aktarma sırasında özel besinler content-key dedup mantığıyla birleştiriliyor.
- İçe aktarma önizlemesinde özel besin sayısı gösteriliyor.
- Tüm verileri sil işlemi özel besinleri de temizliyor.

### Değiştirildi
- JsonHealthDataImporter artık schemaVersion 1, 2 ve 3'ü destekliyor.
- ExportedCustomFood için validateNotBlank ve validatePositiveFloat doğrulama eklendi.
- İçe aktarma merge mantığı: aynı content-key varsa ve import verisi daha yeniyse güncelleme yapılıyor.
- Import preview dialog'a özel besin satırı eklendi.
- "Tüm verileri sil" uyarı mesajına özel besin eklendi.

### Test
- JsonHealthDataImporterTest'e 5 yeni test eklendi: v2 backward compat, blank name reject, negative calories reject, zero serving reject, valid custom food accept.

## PR23.13.1 - Custom Food Library Stabilization

### Düzeltildi
- Özel besin ekleme, düzenleme ve silme sonrası besin arama sonuçlarının otomatik güncellenmesi sağlandı (Flow-based observe).
- Yeni özel besin ekleme formunun eski state ile açılma riski giderildi (resetForAdd).
- Özel besin formundaki negatif ve geçersiz makro değerlerinin sessizce 0'a çevrilmesi engellendi.
- Özel besin araması Türkçe karakter duyarsız çalışacak şekilde iyileştirildi (TurkishSearchNormalizer).
- Hazır besin araması da aynı ortak normalizer ile çalışacak şekilde yeniden düzenlendi.

### Değiştirildi
- Özel besin validation hataları resource/enum tabanlı (CustomFoodFieldError) hale getirildi.
- Özel besin formuna favori Switch kontrolü eklendi.
- Kaydetme ve silme işlemleri isSaving/isDeleting loading state ile daha güvenli hale getirildi.
- Kaydetme/silme hata durumlarında form kapanmıyor, hata mesajı gösteriliyor.
- Arama sonuçlarında favori özel besinler "Özel · Favori" etiketi ile gösteriliyor.

### Not
- Özel besinler hâlâ lokal saklanır. Export/import entegrasyonu sonraki veri yönetimi PR'ına bırakılmıştır.
- Database version (7) değişmedi; yeni migration eklenmedi.

## PR23.13 - Custom Food Library v1

### Eklendi
- Kullanıcının kendi özel besinlerini lokal olarak kaydedebilmesi eklendi.
- Özel besinler, hazır besinlerle birlikte öğün ekleme aramasında gösterilmeye başladı.
- Özel besin oluşturma, düzenleme, silme ve favori bilgisi eklendi.

### Değiştirildi
- Öğün ekleme araması, hazır ve özel besinleri tek akışta destekleyecek şekilde genişletildi.
- Besin arama ViewModel ve Route, preset ve custom food'u destekleyen MealFoodSearchViewModel / MealFoodSearchRoute olarak yeniden adlandırıldı.

### Not
- Özel besinler cihazda lokal olarak saklanır. Geçmiş öğün kayıtları snapshot olarak korunur.
- Custom food export/import kapsamına sonraki veri yönetimi PR'ında alınacak.

## PR23.12.3 - Meal Food Search UI Final Polish

### Düzeltildi
- Hazır besin arama alt ekranının küçük ekran, klavye ve sonuç listesi davranışı iyileştirildi.
- Hazır besin arama geri butonuna erişilebilirlik açıklaması eklendi.

### Değiştirildi
- Hazır besin arama route/content isimlendirmesi sheet mimarisini daha doğru yansıtacak şekilde sadeleştirildi.
- Food search regression testleri güçlendirildi.

## PR23.12.2 - Meal Food Search Sheet Redesign

### Düzeltildi
- "Besin ara" ekranının Today ekranı üstüne çıplak çizilmesine neden olan sheet state hatası düzeltildi.
- Hazır besin arama artık Öğün ekle bottom sheet'i içinde alt ekran olarak çalışır.
- Besin seçimi sonrası doğru meal draft doldurulup kullanıcı Öğün ekle ekranına geri döner.

### Değiştirildi
- Öğün ekle bottom sheet'i, MealEditor ve FoodSearch modlarını tek sheet içinde yönetecek şekilde sadeleştirildi.
- Hazır besin arama ekranına geri davranışı ve sheet içi yerleşim netleştirildi.

## PR23.12.1 - Nutrition Presets Integration Hardening and UI Polish

### Düzeltildi
- Hazır besin arama akışındaki çift bottom sheet riski giderildi.
- Hazır besin auto-fill değerlerinde yuvarlama tek yardımcı fonksiyon üzerinden tutarlı hale getirildi.
- CHANGELOG içindeki Türkçe karakter bozulmaları düzeltildi.

### Değiştirildi
- Hazır besin arama kategorileri yatay kaydırılabilir ve seçili durumu görülebilir hale getirildi.
- Hazır besin sonuç kartları kategori ve kısa veri kalitesi etiketiyle daha okunabilir hale getirildi.
- Hazır besin arama hata/loading durumları daha güvenli hale getirildi.

### Eklendi
- Nutrition preset arama, hesaplama ve dataset doğrulama testleri güçlendirildi.

## PR23.12 - Nutrition Presets v1

### Eklendi
- `Besin ara` (Search food) özelliği eklenerek hazır gıdaların kalori ve makro değerlerinin otomatik doldurulması sağlandı.
- NutritionPreset domain modelleri, repository ve Hilt entegrasyonu eklendi.
- Asset dizinine JSON tabanlı 188 yaygın hazır besin seti eklendi.
- Hazır besin verileri için Python tabanlı JSON schema validation scripti eklendi.

### Değiştirildi
- MealEditorSheet'e `Besin ara` butonu eklendi.
- TodayScreen içerisine `NutritionPresetSearch` bottom sheet yönetimi eklendi.


## PR23.11 - Loading, Skeleton and Empty State UX Polish

### Değiştirildi

- Uygulama açılışındaki root "Yükleniyor" metni kaldırıldı.
- Today, Trends ve detay ekranları için initial loading deneyimi skeleton yapılarla iyileştirildi.
- Veri olmayan ekranlarda ghost empty state bileşenleri eklendi.

### Eklendi

- Ortak skeleton/ghost UI bileşenleri eklendi.
- Import/export işlemleri için açıklamalı loading state metinleri güçlendirildi.

## PR23.10 - Benchmark Connected Test Stabilization

### Değiştirildi

- Connected benchmark navigation akışları daha stabil selector/testTag kullanımına yaklaştırıldı.
- Baseline profile ve startup benchmark path'leri connected cihaz/emulator koşullarına göre gözden geçirildi.

### Eklendi

- Connected benchmark çalıştırma koşulları README/performance audit dokümantasyonunda netleştirildi.

## PR23.9 - Trends Tab Redesign v1

### Değiştirildi

- Eğilimler sekmesi haftalık/aylık dönem özeti, hedef uyumu, öne çıkan değişimler, kısa içgörüler ve veri yeterliliği uyarılarıyla yeniden yapılandırıldı.

### Eklendi

- Eğilimler ekranına kısa veri temelli içgörü kartları eklendi.
- Eğilimler ekranına hedef uyumu ve veri kalitesi bölümleri eklendi.

## PR23.8 - Stability, Import/Export and Error Handling Hardening

### Eklendi

- Import/export akışları için typed hata modeli ve daha açıklayıcı kullanıcı mesajları eklendi.
- Import preview aşamasına alan bazlı validation kontrolleri eklendi.
- Büyük dosya ve bozuk tarih/saat formatları için daha güvenli hata yönetimi eklendi.

### Değiştirildi

- Import/export loading ve hata state yönetimi güçlendirildi.

## PR23.7 - Battery, Background Work and Sensor Optimization

### Değiştirildi

- Adım sayar foreground service başlatma akışı, kullanıcı tercihi kapalıyken sensör dinlemeyecek şekilde güçlendirildi.
- StepCounterService sensor listener lifecycle ve pending sensor flush davranışı daha güvenli hale getirildi.
- Su hatırlatıcı worker akışı, bildirim gösterilemeyecek durumlarda gereksiz Today snapshot okumasından kaçınacak şekilde iyileştirildi.

### Korundu

- Performans/recomposition optimizasyonları PR23.6 kapsamında tutuldu.
- Import/export validation ve hata modeli iyileştirmeleri PR23.8'e bırakıldı.

## PR23.6 - Performance and Recomposition Optimization

### Değiştirildi

- Today dashboard türetilmiş kart listesi hesaplamaları daha az recomposition maliyeti oluşturacak şekilde optimize edildi.
- Detail ekran state builder'larında tekrar eden tarih, gruplama ve formatlama işleri azaltıldı.
- Lazy list/grid key kullanımları gözden geçirilerek render kararlılığı iyileştirildi.

### Eklendi

- PR23.6 sonrası performans takip notları performance audit dokümanına eklendi.

### Korundu

- Pil/background work optimizasyonları PR23.7'ye bırakıldı.
- Import/export hata modeli ve validation iyileştirmeleri PR23.8'e bırakıldı.

## PR23.5 - Performance, Battery and Stability Baseline Audit

### Eklendi

- Performans, pil ve kararlılık risklerini belgeleyen baseline audit dokümanı eklendi.
- Debug-only performans ölçüm noktaları eklendi.
- Today, detail ekranları, Flow/Room akışları, background işler ve import/export kararlılığı için optimizasyon planı çıkarıldı.

### Değiştirildi

- README performans audit dokümanına yönlendirecek şekilde güncellendi.

## PR23.4.1 - Smoking Card Status Color Adjustment

### Değiştirildi

- Ana ekrandaki Sigara kartının durum dairesi renk mantığı sadeleştirildi.
- 0 sigara yeşil, limit altındaki sigara kullanımı sarı, limite ulaşan veya limiti aşan değerler kırmızı gösterilir.

## PR23.4 - Dashboard Metric Polish & Number Formatting

### Değiştirildi

- Ana ekrandaki Sigara kartında halka progress yerine dolu durum dairesi kullanılmaya başlandı.
- Büyük metrik sayıları locale-aware binlik ayırıcıyla gösterilecek şekilde iyileştirildi.
- Sigara ve kafein gibi limit odaklı metriklerde limit aşımı görsel anlamı netleştirildi.

### Düzeltildi

- Kafein aylık görünümünde limit aşımının başarı gibi algılanabilecek renkle gösterilmesi engellendi.
- Sigara metriklerinde progress/başarı anlamı oluşturabilecek görsel kullanım azaltıldı.

## PR23.3 - Smoking and Exercise Detail Screens

### Eklendi

- Sigara detay sayfası eklendi.
- Egzersiz detay sayfası eklendi.
- Sigara ve egzersiz için haftalık/aylık grafikler, özet kartları ve geçmiş listeleri eklendi.

### Değiştirildi

- Ana ekrandaki Sigara ve Egzersiz kartlarından detay sayfalarına geçiş eklendi.

## PR23.2 - Caffeine and Hydration Detail Standardization

### Eklendi

- Kafein detay ekranına haftalık ve aylık sekmeli grafik yapısı eklendi.

### Değiştirildi

- Kafein ve su detay ekranlarında grafikler üst bölüme taşındı.
- Kafein ve su progress bar görsel dili ortaklaştırıldı.
- Haftalık grafiklerde gün etiketleri daha okunabilir hale getirildi.

## PR23.1 - Metric Input, Formatting and UI Safety Hotfix

### Düzeltildi

- Ondalıklı metrik girişlerinde virgül desteği eklendi.
- Kafein saatlerinin saniye/nanosaniye ile görünmesi düzeltildi.
- Kafein kayıtlarının kart gövdesine dokununca yanlışlıkla silinmesi engellendi.

### Değiştirildi

- Ortalama açıklamaları daha net hale getirildi.
- Haftalık grafik gün etiketleri Türkçe/İngilizce kaynaklara taşındı.

## PR23 - Adaptive Layout & Large Screen Support

### Eklendi

- Window size class tabanlı compact, medium ve expanded layout desteği eklendi.
- Geniş ekranlarda NavigationRail desteği eklendi.
- Today dashboard, Profile ve Detail ekranları geniş ekranlarda daha verimli alan kullanacak şekilde iyileştirildi.

### Değiştirildi

- Compact telefon düzeni korunurken tablet/landscape ekranlarda çok kolonlu/adaptive düzenler kullanılmaya başlandı.
- Dashboard customization sheet geniş ekranlarda daha dengeli genişlik ve scroll davranışıyla güncellendi.

## PR22 - Domain/UI Text Separation & Localization Cleanup

### Değiştirildi

- Domain modellerinden Android resource ve kullanıcı metni bağımlılıkları temiz tutuldu.
- Trends ekranındaki ViewModel kaynaklı kullanıcı metinleri `UiText` ve string resource yapısına taşındı.
- İngilizce string kaynakları ve Android per-app language hazırlığı eklendi.
- UI label mapping Compose/UI katmanında tutulacak şekilde domain/resource ayrımı netleştirildi.

## PR21 - Multi-Module Architecture

### Değiştirildi

- Proje `:domain`, `:data` ve `:core:ui` modülleriyle ilk multi-module mimariye taşındı.
- Domain modelleri, repository interface'leri, use case'ler, validation ve hesaplama kodları app modülünden ayrıldı.
- Room/DataStore/repository implementasyonları data modülüne taşındı.
- Ortak Compose bileşenleri, tema, navigation destination tanımları ve UI text helper'ları core:ui modülüne taşındı.

## PR20 - Hilt Migration

### Değiştirildi

- Manual dependency container yapısı Hilt tabanlı dependency injection sistemine taşındı.
- ViewModel oluşturma akışları `@HiltViewModel` ve constructor injection ile sadeleştirildi.
- Repository, database, DataStore ve use case bağımlılıkları Hilt module'ları üzerinden sağlanır hale getirildi.

## PR19.1 - Benchmark Signed Target Build Fix

### Düzeltildi

- Connected macrobenchmark testinin unsigned release APK kurmaya çalışması engellendi.
- Benchmark için release'e yakın, debug key ile imzalanmış ayrı target build type eklendi.

## PR19 - Baseline Profile & Macrobenchmark

### Eklendi

- Baseline Profile ve Macrobenchmark altyapısı eklendi.
- Startup, Today scroll ve dashboard customization performans senaryoları tanımlandı.
- README benchmark komutlarıyla güncellendi.

## PR18 - Release Optimization

### Değiştirildi

- Release build için R8 minification ve resource shrinking etkinleştirildi.
- CI release build doğrulaması eklendi.
- README release build komutlarıyla güncellendi.

## PR17 - Caffeine Tracking

### Eklendi

- Düşük eforlu kafein takibi eklendi.
- İçecek türü ve bardak boyuna göre tahmini kafein hesabı eklendi.
- Kafein kartı ve kafein detay ekranı eklendi.
- Kafein verileri export/import şemasına dahil edildi.
- Dashboard customization sistemine Kafein kartı eklendi.

## PR16.4 - Profile and Localization Micro Polish

### Değiştirildi

- Step detail ekranındaki kullanıcı metinleri string resource yapısına taşındı.
- Profil ekranındaki büyük componentler daha küçük dosyalara ayrıldı.
- Dashboard customization ve takviyeler kartı test kapsamı güçlendirildi.

## PR16.3 - Live Drag Reorder for Dashboard Customization

### Değiştirildi

- Kartları Özelleştir menüsünde sıralama davranışı canlı drag reorder etkileşimine taşındı.
- Sürüklenen kart komşu kart sınırlarını geçtikçe liste anlık olarak yeniden sıralanır.
- Final sıra yalnızca drag tamamlandığında kalıcı ayarlara yazılır.

## PR16.2 - Dashboard Customization Scroll & Supplements Layout Polish

### Düzeltildi
- Kartları Özelleştir menüsünde ekran yüksekliğini aşan kartlara erişilememesi düzeltildi.
- Sigara, Takviyeler ve Adım kartlarının özelleştirme listesinde erişilebilir olması sağlandı.
- Drag handle erişilebilirlik açıklaması düzeltildi.

### Değiştirildi
- Takviyeler kartı 1 veya 2 takviye olduğunda içerikleri kart içinde daha dengeli ve ortalı gösterecek şekilde iyileştirildi.

## PR16.1 - Dashboard Customization Hotfix & Polish

### Düzeltildi

- Beslenme öğün ekleme ekranındaki "Akşam" Mojibake metni düzeltildi; MealType label'ları string resource üzerinden yönetilecek şekilde güncellendi.
- Kartları Özelleştir menüsünde Sigara ve Takviyeler kartlarının eksik config'ten dolayı kaybolma sorunu giderildi; sanitize mantığı eksik kartları default'tan tamamlar.

### Değiştirildi

- Kart sıralama UX'i yukarı/aşağı butonlarından sürükle-bırak (drag handle) etkileşimine taşındı; basılı tutma ile haptic feedback ve ölçek animasyonu eklendi.
- "Adım Sayar" / "Adımsayar" kullanıcı metinleri uygulama genelinde "Adım" / "Adım Takibi" olarak sadeleştirildi.
- CHANGELOG başlık formatı tarih bazlı tekrarlardan PR numaralı başlıklara dönüştürüldü.

### Eklendi

- Öğün tipi (Kahvaltı, Öğle, Akşam, Ara Öğün) string resource'ları eklendi.
- Sürükleme handle'ı erişilebilirlik açıklaması eklendi.
- Dashboard config unit testleri genişletildi: SMOKING/SUPPLEMENTS varlık kontrolü, reorder logic, hidden item reorder ve edge case testleri eklendi.

## PR16 - Customizable Dashboard

### Eklendi

- Today dashboard kartları için DataStore tabanlı göster/gizle ve sıralama ayarı eklendi.
- Today ekranına kart özelleştirme bottom sheet'i, varsayılana dön ve tüm kartlar gizliyken empty state eklendi.
- Dashboard config sanitization, repository persistence ve Compose customization testleri eklendi.

## PR15 - Compose Recomposition & State Optimization

### Değiştirildi

- Today, Trends, Meal History, Profile ve Step Detail state akışlarında gereksiz aynı state yayınları azaltıldı.
- Bazı UI mapping işlemleri Default dispatcher üzerine alındı.
- Meal history listesine stable section key eklendi ve ring grid/detail state modellerine Compose immutable ipuçları eklendi.

## PR14 - Water Permission UX & Step Tracking Preferences

### Eklendi

- Profil sekmesine adım takibi ve su hatırlatıcı tercih kartları eklendi.
- Adım detay ekranına adım takibi kapalıyken gösterilen etkinleştirme kartı eklendi.
- Profil ve adım detay tercih kartları için Compose test kapsamı eklendi.

### Değiştirildi

- Hedefleri Düzenle ekranı yalnız hedef/ölçü değerlerini düzenleyecek şekilde sadeleştirildi.
- Su hatırlatıcı bildirim izni artık uygulama açılışında otomatik istenmez; yalnız Profil sekmesindeki kullanıcı aksiyonuyla istenir.

## PR13 - Tests & Data Management Coverage

### Eklendi

- Validation, import/export, ViewModel import/delete state ve adım/su hatırlatıcı saf fonksiyon testleri genişletildi.
- Health data import/delete akışı için in-memory Room instrumentation testleri eklendi.
- Import duplicate engelleme, tarih bazlı replace/upsert, takviye template ID mapping ve delete-all davranışı test kapsamına alındı.

### Değiştirildi

- README testler bölümü kritik unit, ViewModel, import/export ve Room migration/data management test kapsamını açıklayacak şekilde güncellendi.

## PR12 - Room Indexes & Migration 4→5

### Eklendi

- Room database version 5'e yükseltildi ve `MIGRATION_4_5` eklendi.
- Tarih bazlı sağlık kayıtları ve takviye doz ilişkileri için Room indexleri eklendi.
- v4 → v5 migration için instrumentation migration testi eklendi.

### Değiştirildi

- Eski veride aynı tarihli tekil kayıt çakışmaları varsa migration, mevcut repository davranışıyla uyumlu şekilde en güncel kaydı koruyacak şekilde normalize eder.

## PR11 - Static Analysis & CI Quality Gates

### Eklendi

- Spotless + ktlint, Detekt ve `lintDebug` tabanlı statik analiz kalite kapısı eklendi.
- Android CI workflow'u format kontrolü, static analysis, lint, compile, unit test, androidTest Kotlin derleme ve debug assemble adımlarını çalıştıracak şekilde güncellendi.
- CI için unit test, lint ve Detekt rapor artifact upload adımları eklendi.

### Değiştirildi

- Kotlin kaynakları Spotless ile tek seferlik format normalizasyonundan geçirildi.
- README geliştirme komutları bölümüne format, Detekt ve lint komutları eklendi.

## PR10 - Water Reminder Polish

### Eklendi

- Su hatırlatıcı bildirimlerine "250 ml ekle" ve "Bugün hatırlatma" aksiyonları eklendi.
- Bugün susturma durumu DataStore içinde tarih bazlı saklanacak şekilde eklendi.
- Su hatırlatıcı initial delay ve gece aşan saat aralığı hesapları için unit testler eklendi.

### Değiştirildi

- WaterReminderScheduler ilk çalışmayı seçili zaman penceresindeki bir sonraki uygun hatırlatma slotuna hizalar.
- WaterReminderWorker hedef tamamlandıysa veya bugün susturulduysa bildirim üretmez.
- Profil hedefleri ekranındaki su hatırlatıcı toggle'ı Android 13+ bildirim iznini kullanıcı aksiyonuyla ister.

## PR9 - Icon & Brand Polish

### Değiştirildi

- Launcher adaptive icon seti sade kalp + progress ring markasına göre yenilendi.
- Bildirim small iconları launcher foreground yerine ayrı 24dp tek renk ikonlarla güncellendi.

## PR8 - UI Accessibility & Chart Polish

### Eklendi

- Aylık metrik halka grid hücrelerine TalkBack açıklamaları, bugün vurgusu ve değer etiketi desteği eklendi.
- Hydration haftalık bar chart için kompakt litre etiketi formatter'ı ve unit testi eklendi.

### Değiştirildi

- Su, adım ve uyku detay grafiklerinin yüksekliği küçük ekranlarda daha esnek olacak şekilde güncellendi.
- Kart header aksiyon butonları 48dp minimum dokunma hedefini koruyacak şekilde iyileştirildi.

## PR7 - Step Tracking User Control

### Eklendi

- Adım takibi için kullanıcı kontrollü `stepTrackingEnabled` ayarı eklendi; varsayılan kapalıdır.
- Profil hedefleri ekranına adım takibini etkinleştirme toggle'ı ve sensör/izin durumuna göre açıklama eklendi.
- Adım sayar foreground bildirimi için "Durdur" aksiyonu eklendi.
- Sensor event yazımlarını 60 saniye veya +50 adım farkına göre sınırlayan `StepSensorWritePolicy` eklendi.
- Adım takip ayarı ve sensor write policy için unit testler eklendi.

### Değiştirildi

- Uygulama açılışında adım servisi artık otomatik izin istemez ve yalnız kullanıcı adım takibini açtıysa başlar.

## PR6 - Validation & Form Error Handling

### Eklendi

- Öğün, su, uyku, kilo, egzersiz, sigara, takviye dozu ve hedef ayarları için Android bağımsız validator katmanı eklendi.
- Today sheet akışlarında geçersiz input için kullanıcıya hata metni gösteren form davranışı eklendi.
- Profil hedefleri kayıt akışında sayı/saat parse hatalarında sessiz fallback yerine hata gösterimi eklendi.
- Validator sınır değerleri için unit testler eklendi.

## PR5 - JSON Import & Delete

### Eklendi

- Profil > Veri Yönetimi bölümüne JSON içe aktarma ve tüm sağlık verilerini silme aksiyonları eklendi.
- JSON import için schema validation, desteklenen şema kontrolü ve kayıt sayısı önizleme dialog'u eklendi.
- Import işlemi Room transaction içinde uygulanacak şekilde veri yönetimi repository/use case katmanı eklendi.
- Tüm sağlık kayıtlarını silme işlemi onay dialog'u ve transaction tabanlı silme akışıyla eklendi.
- JSON import validation unit testleri eklendi.

### Korundu

- Profil adı, tema, onboarding ve hedef ayarları delete-all akışında korunur.
- Import işlemi kullanıcı onayı olmadan veritabanına yazmaz.

## PR4B - JSON Export

### Eklendi

- Profil ekranına Veri Yönetimi bölümü eklendi.
- Android Storage Access Framework üzerinden kullanıcı kontrollü JSON dışa aktarma eklendi.
- `schemaVersion = 1` kullanan versiyonlu sağlık verisi export modeli eklendi.
- Export JSON üretimi için use case/repository/exporter katmanı ve unit testler eklendi.

### Korundu

- Import, tüm verileri silme, Health Connect, cloud sync veya Hilt/multi-module değişikliği eklenmedi.
- Export işlemi kullanıcı dosya konumu seçmeden başlamayacak şekilde sınırlandırıldı.

## PR4A - Backup Policy & Data Privacy

### Değiştirildi

- Android otomatik backup politikası local-first sağlık verisi yaklaşımıyla uyumlu olacak şekilde sıkılaştırıldı.
- `health.db`, `health.db-shm`, `health.db-wal` ve `health_preferences` DataStore dosyası backup/data extraction kurallarında açıkça dışarıda bırakıldı.
- README veri saklama ve gizlilik bölümü gerçek uygulama davranışını yansıtacak şekilde güncellendi.

### Korundu

- Export/import, Health Connect, veritabanı şifreleme veya backend senkronizasyonu eklenmedi.
- Local Room/DataStore çalışma davranışı değiştirilmedi.

## PR3 - Architecture Refactor

### Değiştirildi

- Proje mimarisi tek modül içinde daha temiz bir yapıya taşındı.
- Paket yapısı `core`, `data`, `domain` ve `feature` katmanları etrafında yeniden düzenlendi.
- Uygulama shell, navigation, top bar, bottom bar ve permission side-effect kodları ayrı dosyalara bölündü.
- `HealthApplication` sadeleştirildi; uygulama başlangıç sorumluluğu dışındaki kodlar ilgili altyapı dosyalarına taşındı.
- Manuel dependency container ayrı bir `AppContainer` yapısına ayrıldı.
- Repository interface'leri domain katmanına, repository implementasyonları data katmanına taşındı.
- Büyük repository dosyası daha küçük ve sorumluluğu net dosyalara bölündü.
- Room entity, DAO, mapper, database ve migration kodları daha düzenli paketlere ayrıldı.
- Domain modelleri konu bazlı dosyalara ayrıldı.
- Domain hesaplama fonksiyonları beslenme, uyku, kilo, adım, trend ve tarih hesaplamaları gibi ayrı dosyalara bölündü.
- Today ekranı route, screen, ViewModel, UI state, mapper, action, kart component'leri ve bottom sheet dosyaları olarak yeniden yapılandırıldı.
- Feature'a özel UI state modelleri ilgili feature paketlerine taşındı.
- Kullanıcıya gösterilen metinler lokalizasyona hazır olacak şekilde string resource yapısına taşındı.
- ViewModel kaynaklı validasyon ve hata mesajları için lokalizasyona uygun metin yönetimi altyapısı eklendi.
- Varsayılan sağlık hedefleri ve tekrar eden iş kuralı sabitleri merkezi config yapısına taşındı.
- Notification, reminder ve step counter altyapısı ilgili core paketlerine ayrıldı.
- README dosyası yeni mimariyi, özellikleri, ekran görüntülerini ve geliştirme komutlarını açıklayacak şekilde güncellendi.

### Korundu

- Uygulama ID'si ve namespace aynı bırakıldı: `com.burak.healthapp`.
- Room veritabanı adı, mevcut migration'lar ve schema geçmişi korundu.
- Mevcut kullanıcı akışları ve uygulama davranışı korunacak şekilde refactor yapıldı.
- Sağlık takip özellikleri, onboarding, dashboard, profil, trend ve detay ekranları korunarak yeniden organize edildi.

## PR2 - Step & Water Tracking

### Eklendi

- Ana sayfaya günlük hedefe bağlı adım kartı eklendi.
- Adım kartından açılan haftalık ve aylık adım detay ekranı eklendi.
- Eğilimler ekranına ortalama adım ve adım trend grafiği eklendi.
- Profil hedeflerine günlük adım hedefi alanı eklendi.
- Profil hedeflerine su hatırlatıcısı ayarları eklendi: açık/kapalı, başlangıç saati, bitiş saati ve hatırlatma sıklığı.
- Android `TYPE_STEP_COUNTER` sensörüyle çalışan foreground adım sayma servisi eklendi.
- WorkManager tabanlı su içme hatırlatıcısı eklendi.
- Bildirim kanalları ve adım/su bildirim altyapısı eklendi.
- Room veritabanı v4 şeması ve `step_entries` tablosu eklendi.

### Değiştirildi

- Uyku kaydı aynı gün için yeniden kaydedildiğinde yeni satır açmak yerine mevcut günlük kayıt güncellenir.
- Su ekleme akışı 0 veya negatif değerleri kaydetmeyecek şekilde sıkılaştırıldı.
- Profil özetinde adım hedefi ve su hatırlatıcısı durumu gösterilir.
- Eğilimler ekranındaki boş veri kontrolü adım verisini de dikkate alır.

### Silme / Geri Alma

- Su kayıtları ana sayfadan tek tek silinebilir.
- Günlük uyku, egzersiz ve sigara kayıtları ana sayfadan temizlenebilir.
- Günlük takviye dozları takviye bazında silinebilir.
- Mevcut öğün ve kilo silme davranışları korunur.

### İzinler ve Altyapı

- `ACTIVITY_RECOGNITION`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE` ve `FOREGROUND_SERVICE_HEALTH` izinleri eklendi.
- `androidx.work:work-runtime-ktx` bağımlılığı eklendi.
- Room migration `3 → 4` eklendi ve `app/schemas/.../4.json` üretildi.

### Test

- Adım sensörü baz/reset hesabı için birim test eklendi.
- Adım trend ortalaması ve grafik noktaları için birim test eklendi.
- Su kaydı silme davranışı için repository testi eklendi.
- Adım hedefi ve su hatırlatıcısı ayarlarının kalıcılığı test edildi.
- Ana sayfa adım kartı ve profil adım/hatırlatma alanları için Compose testleri güncellendi.
