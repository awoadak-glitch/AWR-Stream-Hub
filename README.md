# AWR Stream Hub — Direct GitHub Connected

هذه النسخة مربوطة مباشرة بريبوك:

- `awoadak-glitch/AWR-Stream-Hub`
- تقرأ البيانات من:
  - `data/movies.json`
  - `data/anime.json`
  - `data/kdrama.json`
- لا تحتوي أعمال وهمية داخل التطبيق.
- إذا كانت ملفات JSON فارغة سيعرض التطبيق رسالة فارغة فقط.

## أهم ميزة

داخل التطبيق افتح تبويب **Settings** وضع:

```text
Owner: awoadak-glitch
Repo: AWR-Stream-Hub
Branch: main
GitHub Token: token الخاص بك
```

بعدها:

- زر **Request now via GitHub Workflow** يشغل `priority-requests.yml`.
- زر **Fetch / Generate AR + EN SRT** يشغل `subtitle-demand.yml`.
- زر **Refresh** يعيد قراءة GitHub Raw.

## صلاحيات GitHub Token

استخدم Fine-grained token على نفس الريبو فقط:

```text
Repository: AWR-Stream-Hub
Actions: Read and Write
Contents: Read and Write
Metadata: Read
```

## أسرار GitHub المطلوبة للـ workflows

ضعها في:

```text
Settings → Secrets and variables → Actions
```

```text
TMDB_API_KEY
GROQ_API_KEY
OPENROUTER_API_KEY
```

## ملاحظات الترجمة

إنشاء SRT حقيقي يحتاج مصدر قانوني:

- `source_srt_url` أو
- `video_url`

إذا ضغطت طلب ترجمة بدون مصدر، سيحفظ workflow الحالة `needs_source` في `data/subtitles_index.json` ولا ينشئ ترجمة وهمية.
