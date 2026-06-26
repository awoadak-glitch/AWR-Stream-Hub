#!/usr/bin/env python3
import argparse, json, os, time, urllib.parse, urllib.request
from pathlib import Path

DATA = Path('data')
IMG = 'https://image.tmdb.org/t/p/w780'

# دالة توليد روابط المشاهدة الذكية
def get_watch_urls(tmdb_id, kind):
    vidsrc_kind = "movie" if kind == "movie" else "tv"
    return [
        {"name": "Vidsrc (Dood/OK)", "url": f"https://vidsrc.cc/v2/embed/{vidsrc_kind}/{tmdb_id}"},
        {"name": "Multiembed (Direct)", "url": f"https://multiembed.mov/directstream.php?video_id={tmdb_id}&tmdb=1"}
    ]

def tmdb(path, params):
    key = os.environ.get('TMDB_API_KEY', '').strip()
    if not key: raise SystemExit('TMDB_API_KEY secret is missing')
    params = dict(params); params['api_key'] = key
    url = 'https://api.themoviedb.org/3' + path + '?' + urllib.parse.urlencode(params)
    with urllib.request.urlopen(url, timeout=30) as r:
        return json.loads(r.read().decode('utf-8'))

def genre_map(kind):
    endpoint = '/genre/movie/list' if kind == 'movie' else '/genre/tv/list'
    try: return {g['id']: g['name'] for g in tmdb(endpoint, {'language': 'en-US'}).get('genres', [])}
    except: return {}

def clean_item(x, kind, genres):
    tmdb_id = x.get('id')
    title = x.get('title') or x.get('name') or 'Untitled'
    date = x.get('release_date') or x.get('first_air_date') or ''
    return {
        'id': f"{kind}-{tmdb_id}",
        'tmdb_id': tmdb_id,
        'title': title,
        'kind': kind,
        'year': int(date[:4]) if date[:4].isdigit() else None,
        'poster': IMG + (x.get('poster_path') or ''),
        'watch_urls': get_watch_urls(tmdb_id, kind),
        'updated_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())
    }

# دالة لسحب 500 عنصر جديد في كل تشغيلة
def fetch_500_new(endpoint, kind, params, genres):
    out = []
    page = 1
    while len(out) < 500:
        params['page'] = page
        data = tmdb(endpoint, params)
        results = data.get('results', [])
        if not results: break
        for x in results:
            if len(out) >= 500: break
            out.append(clean_item(x, kind, genres))
        page += 1
    return out

# دالة دمج البيانات (تحافظ على القديم وتضيف الجديد فقط)
def merge_and_save(path, new_items):
    existing = json.loads(path.read_text(encoding='utf-8')) if path.exists() else []
    seen = {i['id'] for i in existing}
    added_count = 0
    for item in new_items:
        if item['id'] not in seen:
            existing.insert(0, item) # الأحدث في البداية
            seen.add(item['id'])
            added_count += 1
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(existing, ensure_ascii=False, indent=2), encoding='utf-8')
    return added_count

def main():
    DATA.mkdir(exist_ok=True)
    genres_m = genre_map('movie')
    genres_t = genre_map('tv')
    
    # سحب ودمج 500 عنصر لكل فئة
    m_count = merge_and_save(DATA / 'movies.json', fetch_500_new('/discover/movie', 'movie', {'sort_by': 'popularity.desc', 'vote_count.gte': '100'}, genres_m))
    k_count = merge_and_save(DATA / 'kdrama.json', fetch_500_new('/discover/tv', 'kdrama', {'with_origin_country': 'KR', 'with_original_language': 'ko'}, genres_t))
    a_count = merge_and_save(DATA / 'anime.json', fetch_500_new('/discover/tv', 'anime', {'with_origin_country': 'JP', 'with_genres': '16'}, genres_t))
    
    # تحديث ملف الـ latest
    with open(DATA / 'latest.json', 'w', encoding='utf-8') as f:
        json.dump({'updated_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime()), 
                   'added_movies': m_count, 'added_kdrama': k_count, 'added_anime': a_count}, f, indent=2)
    
    print(f'تمت إضافة {m_count} أفلام، {k_count} كيدراما، و {a_count} أنمي جديد.')

if __name__ == '__main__':
    main()
