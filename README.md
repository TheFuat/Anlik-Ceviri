# Anlık Çeviri

Kelime ve cümleleri 20 dil arasında çeviren Android uygulaması. Varsayılan dil çifti İngilizce → Türkçe.
Android uygulamasında çeviri, Google ML Kit ile telefonun içinde yapılır: ücretsiz, sınırsız ve anahtar gerektirmez. Her dil için ilk kullanımda yaklaşık 30 MB'lık dil paketi bir kez indirilir, sonra internetsiz çalışır. ML Kit'in desteklemediği bir dil seçilirse anahtarsız Google, o da olmazsa MyMemory kullanılır.

## APK'yı indirme

Her yüklemede GitHub Actions APK'yı otomatik derler: **Actions → APK derle → son çalıştırma → Artifacts → AnlikCeviri-apk**.

## Yerelde derleme

Android Studio ile klasörü aç, ardından **Build → Build APK(s)**.
