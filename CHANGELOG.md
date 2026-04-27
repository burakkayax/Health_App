# Changelog

Bu dosya projedeki değişiklikleri ve önemli teknik güncellemeleri takip eder.

## Unreleased - 2026-04-27

### Eklendi

- Aylık metrik halka grid hücrelerine TalkBack açıklamaları, bugün vurgusu ve değer etiketi desteği eklendi.
- Hydration haftalık bar chart için kompakt litre etiketi formatter'ı ve unit testi eklendi.

### Değiştirildi

- Su, adım ve uyku detay grafiklerinin yüksekliği küçük ekranlarda daha esnek olacak şekilde güncellendi.
- Kart header aksiyon butonları 48dp minimum dokunma hedefini koruyacak şekilde iyileştirildi.

## Unreleased - 2026-04-27

### Eklendi

- Adım takibi için kullanıcı kontrollü `stepTrackingEnabled` ayarı eklendi; varsayılan kapalıdır.
- Profil hedefleri ekranına adım takibini etkinleştirme toggle'ı ve sensör/izin durumuna göre açıklama eklendi.
- Adım sayar foreground bildirimi için “Durdur” aksiyonu eklendi.
- Sensor event yazımlarını 60 saniye veya +50 adım farkına göre sınırlayan `StepSensorWritePolicy` eklendi.
- Adım takip ayarı ve sensor write policy için unit testler eklendi.

### Değiştirildi

- Uygulama açılışında adım servisi artık otomatik izin istemez ve yalnız kullanıcı adım takibini açtıysa başlar.

## Unreleased - 2026-04-27

### Eklendi

- Öğün, su, uyku, kilo, egzersiz, sigara, takviye dozu ve hedef ayarları için Android bağımsız validator katmanı eklendi.
- Today sheet akışlarında geçersiz input için kullanıcıya hata metni gösteren form davranışı eklendi.
- Profil hedefleri kayıt akışında sayı/saat parse hatalarında sessiz fallback yerine hata gösterimi eklendi.
- Validator sınır değerleri için unit testler eklendi.

## Unreleased - 2026-04-27

### Eklendi

- Profil > Veri Yönetimi bölümüne JSON içe aktarma ve tüm sağlık verilerini silme aksiyonları eklendi.
- JSON import için schema validation, desteklenen şema kontrolü ve kayıt sayısı önizleme dialog'u eklendi.
- Import işlemi Room transaction içinde uygulanacak şekilde veri yönetimi repository/use case katmanı eklendi.
- Tüm sağlık kayıtlarını silme işlemi onay dialog'u ve transaction tabanlı silme akışıyla eklendi.
- JSON import validation unit testleri eklendi.

### Korundu

- Profil adı, tema, onboarding ve hedef ayarları delete-all akışında korunur.
- Import işlemi kullanıcı onayı olmadan veritabanına yazmaz.

## Unreleased - 2026-04-27

### Eklendi

- Profil ekranına Veri Yönetimi bölümü eklendi.
- Android Storage Access Framework üzerinden kullanıcı kontrollü JSON dışa aktarma eklendi.
- `schemaVersion = 1` kullanan versiyonlu sağlık verisi export modeli eklendi.
- Export JSON üretimi için use case/repository/exporter katmanı ve unit testler eklendi.

### Korundu

- Import, tüm verileri silme, Health Connect, cloud sync veya Hilt/multi-module değişikliği eklenmedi.
- Export işlemi kullanıcı dosya konumu seçmeden başlamayacak şekilde sınırlandırıldı.

## Unreleased - 2026-04-27

### Değiştirildi

- Android otomatik backup politikası local-first sağlık verisi yaklaşımıyla uyumlu olacak şekilde sıkılaştırıldı.
- `health.db`, `health.db-shm`, `health.db-wal` ve `health_preferences` DataStore dosyası backup/data extraction kurallarında açıkça dışarıda bırakıldı.
- README veri saklama ve gizlilik bölümü gerçek uygulama davranışını yansıtacak şekilde güncellendi.

### Korundu

- Export/import, Health Connect, veritabanı şifreleme veya backend senkronizasyonu eklenmedi.
- Local Room/DataStore çalışma davranışı değiştirilmedi.


## Unreleased - 2026-04-26

### Değiştirildi

- Proje mimarisi tek modül içinde daha temiz bir yapıya taşındı.
- Paket yapısı `core`, `data`, `domain` ve `feature` katmanları etrafında yeniden düzenlendi.
- Uygulama shell, navigation, top bar, bottom bar ve permission side-effect kodları ayrı dosyalara bölündü.
- `HealthApplication` sadeleştirildi; uygulama başlangıç sorumluluğu dışındaki kodlar ilgili altyapı dosyalarına taşındı.
- Manuel dependency container ayrı bir `AppContainer` yapısına ayrıldı.
- Repository interface’leri domain katmanına, repository implementasyonları data katmanına taşındı.
- Büyük repository dosyası daha küçük ve sorumluluğu net dosyalara bölündü.
- Room entity, DAO, mapper, database ve migration kodları daha düzenli paketlere ayrıldı.
- Domain modelleri konu bazlı dosyalara ayrıldı.
- Domain hesaplama fonksiyonları beslenme, uyku, kilo, adım, trend ve tarih hesaplamaları gibi ayrı dosyalara bölündü.
- Today ekranı route, screen, ViewModel, UI state, mapper, action, kart component’leri ve bottom sheet dosyaları olarak yeniden yapılandırıldı.
- Feature’a özel UI state modelleri ilgili feature paketlerine taşındı.
- Kullanıcıya gösterilen metinler lokalizasyona hazır olacak şekilde string resource yapısına taşındı.
- ViewModel kaynaklı validasyon ve hata mesajları için lokalizasyona uygun metin yönetimi altyapısı eklendi.
- Varsayılan sağlık hedefleri ve tekrar eden iş kuralı sabitleri merkezi config yapısına taşındı.
- Notification, reminder ve step counter altyapısı ilgili core paketlerine ayrıldı.
- README dosyası yeni mimariyi, özellikleri, ekran görüntülerini ve geliştirme komutlarını açıklayacak şekilde güncellendi.

### Korundu

- Uygulama ID’si ve namespace aynı bırakıldı: `com.burak.healthapp`.
- Room veritabanı adı, mevcut migration’lar ve schema geçmişi korundu.
- Mevcut kullanıcı akışları ve uygulama davranışı korunacak şekilde refactor yapıldı.
- Sağlık takip özellikleri, onboarding, dashboard, profil, trend ve detay ekranları korunarak yeniden organize edildi.

### Doğrulama

- `./gradlew :app:compileDebugKotlin`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:compileDebugAndroidTestKotlin`
- `./gradlew :app:assembleDebug`



## Unreleased - 2026-04-26

### Eklendi

- Ana sayfaya günlük hedefe bağlı adım sayar kartı eklendi.
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
- Room migration `3 -> 4` eklendi ve `app/schemas/.../4.json` üretildi.

### Test

- Adım sensörü baz/reset hesabı için birim test eklendi.
- Adım trend ortalaması ve grafik noktaları için birim test eklendi.
- Su kaydı silme davranışı için repository testi eklendi.
- Adım hedefi ve su hatırlatıcısı ayarlarının kalıcılığı test edildi.
- Ana sayfa adım kartı ve profil adım/hatırlatma alanları için Compose testleri güncellendi.

### Doğrulama

- `.\gradlew.bat :app:compileDebugKotlin`
- `.\gradlew.bat :app:testDebugUnitTest`
- `.\gradlew.bat :app:compileDebugAndroidTestKotlin`
- `.\gradlew.bat :app:assembleDebug`
- `git diff --check`
