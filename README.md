# Health_App

`Health_App`, Android için Kotlin ve Jetpack Compose ile geliştirilmiş modern bir sağlık takip uygulamasıdır.

## Özellikler

- Beslenme, makro, su, uyku, kilo, egzersiz, sigara ve takviye takibi
- AMOLED uyumlu koyu tema ve açık tema
- Tarih seçimine bağlı günlük veri girişi
- Trend grafikleri, kilo detayı ve uyku analizi ekranları
- Yerel veri saklama için Room ve ayarlar için DataStore

## Geliştirme

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:assembleDebug
```

Debug APK oluşturulduktan sonra şu konumda bulunur:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Paket

- Application ID: `com.burak.healthapp`
- Minimum SDK: 26
- Target SDK: 36
