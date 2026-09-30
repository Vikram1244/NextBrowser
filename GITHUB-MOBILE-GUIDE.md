# Next Browser — GitHub Actions से मोबाइल पर APK बनाना

## जरूरी बात
GitHub Actions को Gradle Wrapper (`gradlew`, `gradle/wrapper/...`) चाहिए।
अगर आपके ZIP में ये wrapper files नहीं हैं, तो GitHub में import करने से पहले
किसी Gradle-capable environment में wrapper generate करना पड़ेगा।

सबसे आसान विकल्प: GitHub पर repository बनाकर इस project को upload करें,
फिर GitHub Actions में workflow देखें। अगर build `gradlew not found` पर रुके,
तो repository में Gradle Wrapper जोड़ना होगा।

## GitHub मोबाइल steps
1. github.com खोलें और sign in करें।
2. New repository बनाएं, उदाहरण: `NextBrowser`.
3. इस ZIP को extract करके उसकी files repository में upload करें।
4. `.github/workflows/build-apk.yml` भी upload हुआ है यह सुनिश्चित करें।
5. GitHub में **Actions** tab खोलें।
6. **Build Next Browser APK** workflow चुनें।
7. **Run workflow** दबाएं।
8. Build पूरा होने के बाद workflow run खोलें।
9. नीचे **Artifacts** में `NextBrowser-debug-apk` डाउनलोड करें।
10. ZIP extract करके `app-debug.apk` install करें।

## अगर Actions tab में workflow नहीं दिखता
Repository में `.github/workflows/build-apk.yml` path verify करें और push/commit करें।

## Production release
यह debug APK है। Play Store के लिए बाद में release keystore, signing,
versioning, privacy policy, data safety declaration और Play policy review चाहिए।
