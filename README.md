# AWR Stream Hub 🎬

تطبيق أندرويد لمشاهدة الأنمي والأفلام والدراما الكورية، بدون حسابات، مع تخزين محلي للمفضلة والمشاهدة.

## ✨ المميزات

- **واجهة رئيسية** بأسلوب HiTV: Banner متحرك، Trending، Popular Anime، Movies، K-Drama، Continue Watching
- **بدون تسجيل دخول** — كل البيانات (المفضلة، سجل المشاهدة، آخر حلقة) محفوظة محلياً عبر **Room Database**
- **مشغل فيديو مدمج** (ExoPlayer / Media3) يدعم HLS، استكمال المشاهدة، الحلقة التالية، واختيار الترجمة
- **أقسام**: Anime, Movies, K-Drama, Search, Favorites, History

## 🔌 مصادر البيانات (APIs)

| API | الاستخدام |
|---|---|
| [Jikan](https://jikan.moe/) | بيانات الأنمي (MyAnimeList) — البحث، الأعلى تقييماً، الموسم الحالي |
| [Consumet](https://consumet.org/) | روابط التشغيل المباشر للأنمي/الأفلام/الدراما + الترجمات |

> ⚠️ Consumet API عبارة عن خدمة عامة قد تتغير عناوينها أو تتوقف أحياناً. للاستخدام الجاد يُفضّل استضافة نسخة خاصة بك من [consumet/api.consumet.org](https://github.com/consumet/api.consumet.org) على Render/Railway مجاناً، وتغيير الرابط الأساسي في:
> `app/src/main/java/com/awr/streamhub/data/remote/NetworkModule.kt`

## 🏗️ البنية التقنية

```
app/src/main/java/com/awr/streamhub/
├── data/
│   ├── local/          # Room Database (Favorites + Watch History)
│   ├── models/         # Domain models (MediaItem, Episode...)
│   ├── remote/          # Retrofit services (Jikan + Consumet) + DTOs
│   └── repository/      # MediaRepository يجمع كل المصادر
├── ui/
│   ├── components/      # عناصر UI قابلة لإعادة الاستخدام
│   ├── screens/          # Home, Catalog, Search, Detail, Player, Favorites, History
│   └── theme/            # الألوان والثيم
├── viewmodel/            # MainViewModel (StateFlow لكل الشاشات)
└── MainActivity.kt        # نقطة الدخول + التنقل بين الشاشات
```

**التقنيات المستخدمة:**
- Jetpack Compose (100% UI)
- Room Database (تخزين محلي)
- Retrofit + OkHttp (شبكة)
- Coil (تحميل الصور)
- Media3 / ExoPlayer (تشغيل الفيديو + HLS)
- Coroutines + StateFlow (إدارة الحالة)

## 🚀 طريقة البناء

### عبر GitHub Actions (تلقائي)
كل push على `main` يبني APK تلقائياً. حمّله من تبويب **Actions** → آخر run → **Artifacts**.

### محلياً (Android Studio)
1. افتح المجلد في Android Studio (Hedgehog أو أحدث)
2. دع Gradle يزامن المشروع
3. شغّل على جهاز/محاكي (minSdk 24)

### عبر الطرفية
```bash
./gradlew assembleDebug
# الناتج: app/build/outputs/apk/debug/app-debug.apk
```

## 🧩 ميزة الترجمة بالذكاء الاصطناعي (المرحلة القادمة)

زر "🤖 AI Translate Subtitles" موجود في شاشة التفاصيل كنقطة بداية. المسار المخطط له:

```
الصوت → Grok ASR → OpenAI Translation → ملف SRT → تشغيل مع الفيديو
```

هذا يتطلب باك-إند خارجي (server.js / Python worker) لأن مفاتيح الـ API لا توضع داخل تطبيق الأندرويد مباشرة. يمكن ربط الزر بنداء API لباك-إند خاص بك يرجّع رابط ملف SRT جاهز ليُحمَّل في المشغل.

## 📌 ملاحظات مهمة

- بعض روابط Consumet (خاصة لمصادر الأنمي/الدراما) تنتهي صلاحيتها بسرعة أو تتطلب headers خاصة (Referer) — قد تحتاج تعديل `ConsumetApiService` حسب حالة الخدمة وقت الاستخدام.
- التطبيق لا يخزّن أو يستضيف أي ملفات فيديو؛ هو فقط واجهة تستهلك روابط من مصادر خارجية.
- تأكد من قراءة سياسات الاستخدام الخاصة بـ Consumet/Jikan قبل النشر العام.

## 📄 الترخيص

مشروع شخصي/تعليمي — استخدمه وعدّله بحرية.
