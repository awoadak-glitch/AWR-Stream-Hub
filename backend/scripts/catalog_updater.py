#!/usr/bin/env python3
import json, os, time, urllib.parse, urllib.request
from pathlib import Path

DATA = Path('data')
IMG = 'https://image.tmdb.org/t/p/w780'

def tmdb(path, params):
    key = os.environ.get('TMDB_API_KEY', '').strip()
    params['api_key'] = key
    url = 'https://api.themoviedb.org/3' + path + '?' + urllib.parse.urlencode(params)
    with urllib.request.urlopen(url, timeout=30) as r:
        return json.loads(r.read().decode('utf-8'))

# دالة لتوليد رابط مشاهدة لكل حلقة
def get_episode_url(tmdb_id, season, episode):
    # استخدام معرف vidsrc الذي يدعم تحديد الموسم والحلقة
    return f"https://vidsrc.cc/v2/embed/tv/{tmdb_id}/{season}/{episode}"

def get_tv_details(tmdb_id):
    # جلب تفاصيل المسلسل لمعرفة المواسم
    data = tmdb(f'/tv/{tmdb_id}', {'language': 'en-US'})
    seasons = []
    for season in data.get('seasons', []):
        s_num = season.get('season_number')
        if s_num == 0: continue # تخطي الـ specials
        
        # جلب حلقات الموسم
        episodes_data = tmdb(f'/tv/{tmdb_id}/season/{s_num}', {'language': 'en-US'})
        episodes = []
        for ep in episodes_data.get('episodes', []):
            episodes.append({
                'name': f"حلقة {ep.get('episode_number')}",
                'url': get_episode_url(tmdb_id, s_num, ep.get('episode_number'))
            })
        seasons.append({'season': s_num, 'episodes': episodes})
    return seasons

def clean_series(x, kind):
    tmdb_id = x.get('id')
    return {
        'id': f"{kind}-{tmdb_id}",
        'tmdb_id': tmdb_id,
        'title': x.get('name'),
        'kind': kind,
        'poster': IMG + (x.get('poster_path') or ''),
        'seasons': get_tv_details(tmdb_id), # هنا سحب كامل للمواسم والحلقات
        'updated_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())
    }

# تعديل دالة fetch للتعامل مع المسلسلات
def fetch_series(endpoint, kind, params):
    out = []
    data = tmdb(endpoint, params)
    for x in data.get('results', [])[:10]: # جلب 10 مسلسلات جديدة في كل تشغيلة لتجنب ضغط الـ API
        out.append(clean_series(x, kind))
    return out

def merge_and_save(path, new_items):
    existing = json.loads(path.read_text(encoding='utf-8')) if path.exists() else []
    seen = {i['id'] for i in existing}
    for item in new_items:
        if item['id'] not in seen:
            existing.insert(0, item)
            seen.add(item['id'])
    path.write_text(json.dumps(existing, ensure_ascii=False, indent=2), encoding='utf-8')

def main():
    DATA.mkdir(exist_ok=True)
    # سحب الكيدراما والأنمي
    merge_and_save(DATA / 'kdrama.json', fetch_series('/discover/tv', 'kdrama', {'with_origin_country': 'KR', 'with_original_language': 'ko'}))
    merge_and_save(DATA / 'anime.json', fetch_series('/discover/tv', 'anime', {'with_origin_country': 'JP', 'with_genres': '16'}))
    print("تم تحديث المسلسلات مع كامل الحلقات.")

if __name__ == '__main__':
    main()
