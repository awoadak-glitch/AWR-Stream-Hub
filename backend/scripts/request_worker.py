#!/usr/bin/env python3
import argparse, json, os, time, urllib.parse, urllib.request
from pathlib import Path

DATA = Path('data')
IMG = 'https://image.tmdb.org/t/p/w780'


def tmdb(path, params):
    key = os.environ.get('TMDB_API_KEY', '').strip()
    if not key:
        raise SystemExit('TMDB_API_KEY secret is missing')
    params = dict(params); params['api_key'] = key
    url = 'https://api.themoviedb.org/3' + path + '?' + urllib.parse.urlencode(params)
    with urllib.request.urlopen(url, timeout=30) as r:
        return json.loads(r.read().decode('utf-8'))


def load(path):
    if not path.exists(): return []
    return json.loads(path.read_text(encoding='utf-8') or '[]')


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding='utf-8')


def clean(x, kind):
    title = x.get('title') or x.get('name') or x.get('original_title') or x.get('original_name') or 'Untitled'
    date = x.get('release_date') or x.get('first_air_date') or ''
    poster = x.get('poster_path') or ''
    backdrop = x.get('backdrop_path') or ''
    return {
        'id': f"{kind}-{x.get('id', abs(hash(title)))}",
        'tmdb_id': x.get('id'),
        'title': title,
        'original': x.get('original_title') or x.get('original_name') or title,
        'kind': kind,
        'language': x.get('original_language') or 'auto',
        'year': int(date[:4]) if date[:4].isdigit() else None,
        'rating': round(float(x.get('vote_average') or 0), 1),
        'poster': IMG + poster if poster else '',
        'backdrop': IMG + backdrop if backdrop else '',
        'overview': x.get('overview') or '',
        'genres': [],
        'subtitle_status': 'missing',
        'requested_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())
    }


def search(title, kind):
    if kind == 'movie':
        data = tmdb('/search/movie', {'query': title, 'language': 'en-US', 'include_adult': 'false', 'page': 1})
        return [clean(x, 'movie') for x in data.get('results', [])[:5]]
    data = tmdb('/search/tv', {'query': title, 'language': 'en-US', 'include_adult': 'false', 'page': 1})
    final_kind = 'anime' if kind == 'anime' else 'kdrama'
    results = []
    for x in data.get('results', []):
        lang = x.get('original_language')
        country = (x.get('origin_country') or [''])[0]
        if final_kind == 'anime' and not (lang == 'ja' or country == 'JP'):
            continue
        if final_kind == 'kdrama' and not (lang == 'ko' or country == 'KR'):
            continue
        results.append(clean(x, final_kind))
    return results[:5]


def append_unique(path, items):
    existing = load(path)
    seen = {x.get('id') for x in existing}
    for item in items:
        if item['id'] not in seen:
            existing.insert(0, item); seen.add(item['id'])
    write(path, existing[:200])


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--title', required=True)
    ap.add_argument('--kind', default='movie')
    args = ap.parse_args()
    kind = args.kind.lower().strip()
    if kind not in {'movie','anime','kdrama'}: kind = 'movie'
    results = search(args.title, kind)
    path = DATA / {'movie':'movies.json','anime':'anime.json','kdrama':'kdrama.json'}[kind]
    append_unique(path, results)
    requests = load(DATA / 'requests.json')
    requests.insert(0, {'title': args.title, 'kind': kind, 'status': 'found' if results else 'not_found', 'count': len(results), 'created_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())})
    write(DATA / 'requests.json', requests[:200])
    print(f'Processed request {args.title}: {len(results)} result(s)')

if __name__ == '__main__':
    main()
