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

def get_all_servers(tmdb_id, kind, season=None, episode=None):
    if kind == 'movie':
        return [
            {"name": "Vidsrc Pro", "url": f"https://vidsrc.pro/embed/movie/{tmdb_id}"},
            {"name": "Vidsrc.to", "url": f"https://vidsrc.to/embed/movie/{tmdb_id}"},
            {"name": "MultiEmbed", "url": f"https://multiembed.mov/directstream.php?video_id={tmdb_id}&tmdb=1"},
            {"name": "EmbedSu", "url": f"https://embed.su/embed/movie/{tmdb_id}"}
        ]
    else:
        return [
            {"name": "Vidsrc Pro", "url": f"https://vidsrc.pro/embed/tv/{tmdb_id}/{season}/{episode}"},
            {"name": "Vidsrc.to", "url": f"https://vidsrc.to/embed/tv/{tmdb_id}/{season}/{episode}"},
            {"name": "MultiEmbed", "url": f"https://multiembed.mov/directstream.php?video_id={tmdb_id}&tmdb=1&s={season}&e={episode}"},
            {"name": "EmbedSu", "url": f"https://embed.su/embed/tv/{tmdb_id}/{season}/{episode}"}
        ]

def get_tv_details(tmdb_id):
    # جلب تفاصيل المسلسل لتقليل الطلبات
    data = tmdb(f'/tv/{tmdb_id}', {'language': 'en-US'})
    seasons_list = []
    for s in data.get('seasons', []):
        s_num = s.get('season_number')
        if s_num == 0 or not s.get('episode_count'): continue
        # لجلب الحلقات بشكل مجمع
        ep_data = tmdb(f'/tv/{tmdb_id}/season/{s_num}', {'language': 'en-US'})
        episodes = [{
            'name': f"حلقة {e.get('episode_number')}",
            'servers': get_all_servers(tmdb_id, 'tv', s_num, e.get('episode_number'))
        } for e in ep_data.get('episodes', [])]
        seasons_list.append({'season': s_num, 'episodes': episodes})
    return seasons_list

def clean_item(x, kind):
    tmdb_id = x.get('id')
    base = {
        'id': f"{kind}-{tmdb_id}",
        'tmdb_id': tmdb_id,
        'title': x.get('title') or x.get('name'),
        'overview': x.get('overview', ''),
        'poster': IMG + (x.get('poster_path') or ''),
        'kind': kind,
        'updated_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())
    }
    if kind == 'movie':
        base['servers'] = get_all_servers(tmdb_id, 'movie')
    else:
        base['seasons'] = get_tv_details(tmdb_id)
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
    path.write_text(json.dumps(existing, ensure_ascii=False, indent=2), encoding='utf-8')

def main():
    DATA.mkdir(exist_ok=True)
    
    # زيادة العدد إلى 100 لكل فئة
    # ملاحظة: سحب 100 مسلسل قد يستغرق وقتاً طويلاً، لذا تأكد من ضبط timeout في Action
    
    movies = [clean_item(x, 'movie') for x in tmdb('/discover/movie', {'sort_by': 'popularity.desc', 'vote_count.gte': '100'}).get('results', [])[:100]]
    merge_and_save(DATA / 'movies.json', movies)
    
    kdrama = [clean_item(x, 'kdrama') for x in tmdb('/discover/tv', {'with_origin_country': 'KR', 'with_original_language': 'ko'}).get('results', [])[:100]]
    merge_and_save(DATA / 'kdrama.json', kdrama)
    
    anime = [clean_item(x, 'anime') for x in tmdb('/discover/tv', {'with_origin_country': 'JP', 'with_genres': '16'}).get('results', [])[:100]]
    merge_and_save(DATA / 'anime.json', anime)

if __name__ == '__main__':
    main()
