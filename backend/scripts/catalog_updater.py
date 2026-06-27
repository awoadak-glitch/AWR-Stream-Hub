#!/usr/bin/env python3
import json, os, time, urllib.parse, urllib.request
from pathlib import Path

DATA = Path('data')
IMG = 'https://image.tmdb.org/t/p/w780'

def tmdb(path, params):
    key = os.environ.get('TMDB_API_KEY', '').strip()
    if not key: raise SystemExit('TMDB_API_KEY missing')
    params['api_key'] = key
    url = 'https://api.themoviedb.org/3' + path + '?' + urllib.parse.urlencode(params)
    with urllib.request.urlopen(url, timeout=30) as r:
        return json.loads(r.read().decode('utf-8'))

# دالة جلب معرف IMDb للمسلسل/الفيلم
def get_imdb_id(tmdb_id, kind):
    endpoint = f'/movie/{tmdb_id}/external_ids' if kind == 'movie' else f'/tv/{tmdb_id}/external_ids'
    data = tmdb(endpoint, {})
    return data.get('imdb_id')

def clean_item(x, kind):
    tmdb_id = x.get('id')
    imdb_id = get_imdb_id(tmdb_id, 'movie' if kind == 'movie' else 'tv')
    
    base = {
        'id': f"{kind}-{tmdb_id}",
        'tmdb_id': tmdb_id,
        'imdb_id': imdb_id,
        'title': x.get('title') or x.get('name'),
        'overview': x.get('overview', ''),
        'poster': IMG + (x.get('poster_path') or ''),
        'kind': kind,
        # استخدام صيغة الرابط التي طلبتها
        'watch_url': f"https://www.playimdb.com/title/{imdb_id}/" if imdb_id else "",
        'updated_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())
    }
    return base

def merge_and_save(path, new_items):
    existing = []
    if path.exists():
        try:
            content = path.read_text(encoding='utf-8').strip()
            if content: existing = json.loads(content)
        except: existing = []
    
    seen = {i['id'] for i in existing}
    for item in new_items:
        if item['id'] not in seen:
            existing.insert(0, item)
            seen.add(item['id'])
    path.write_text(json.dumps(existing, ensure_ascii=False, indent=2), encoding='utf-8')

def main():
    DATA.mkdir(exist_ok=True)
    
    # سحب 100 عنصر لكل فئة (استخدام الصفحة 1 و 2 لضمان التنوع)
    for page in range(1, 3):
        # أفلام
        movies = [clean_item(x, 'movie') for x in tmdb('/discover/movie', {'sort_by': 'popularity.desc', 'vote_count.gte': '100', 'page': page}).get('results', [])]
        merge_and_save(DATA / 'movies.json', movies)
        
        # كيدراما
        kdrama = [clean_item(x, 'kdrama') for x in tmdb('/discover/tv', {'with_origin_country': 'KR', 'with_original_language': 'ko', 'page': page}).get('results', [])]
        merge_and_save(DATA / 'kdrama.json', kdrama)
        
        # أنمي
        anime = [clean_item(x, 'anime') for x in tmdb('/discover/tv', {'with_origin_country': 'JP', 'with_genres': '16', 'page': page}).get('results', [])]
        merge_and_save(DATA / 'anime.json', anime)

    print("تم التحديث بنجاح باستخدام معرفات IMDb.")

if __name__ == '__main__':
    main()
