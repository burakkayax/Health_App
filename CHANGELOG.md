# Changelog

Bu dosya projedeki kullanıcıya dönük değişiklikleri ve önemli teknik güncellemeleri takip eder.


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
