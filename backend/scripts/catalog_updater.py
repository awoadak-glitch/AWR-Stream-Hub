#!/usr/bin/env python3
import argparse, json, os, time, urllib.parse, urllib.request
from pathlib import Path

DATA = Path('data')
IMG = 'https://image.tmdb.org/t/p/w780'


def tmdb(path, params):
    key = os.environ.get('TMDB_API_KEY', '').strip()
    if not key:
        raise SystemExit('TMDB_API_KEY secret is missing')
    params = dict(params)
    params['api_key'] = key
    url = 'https://api.themoviedb.org/3' + path + '?' + urllib.parse.urlencode(params)
    with urllib.request.urlopen(url, timeout=30) as r:
        return json.loads(r.read().decode('utf-8'))


def genre_map(kind):
    endpoint = '/genre/movie/list' if kind == 'movie' else '/genre/tv/list'
    try:
        return {g['id']: g['name'] for g in tmdb(endpoint, {'language': 'en-US'}).get('genres', [])}
    except Exception:
        return {}


def clean_item(x, kind, genres):
    title = x.get('title') or x.get('name') or x.get('original_title') or x.get('original_name') or 'Untitled'
    date = x.get('release_date') or x.get('first_air_date') or ''
    item_id = f"{kind}-{x.get('id', str(abs(hash(title))))}"
    poster = x.get('poster_path') or ''
    backdrop = x.get('backdrop_path') or ''
    return {
        'id': item_id,
        'tmdb_id': x.get('id'),
        'title': title,
        'original': x.get('original_title') or x.get('original_name') or title,
        'kind': kind,
        'language': (x.get('original_language') or 'auto'),
        'year': int(date[:4]) if date[:4].isdigit() else None,
        'rating': round(float(x.get('vote_average') or 0), 1),
        'poster': IMG + poster if poster else '',
        'backdrop': IMG + backdrop if backdrop else '',
        'overview': x.get('overview') or '',
        'genres': [genres.get(g, str(g)) for g in x.get('genre_ids', [])[:4]],
        'subtitle_status': 'missing',
        'updated_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())
    }


def discover_movies(limit):
    genres = genre_map('movie')
    out = []
    page = 1
    while len(out) < limit and page <= 5:
        data = tmdb('/discover/movie', {
            'language': 'en-US', 'sort_by': 'popularity.desc', 'include_adult': 'false',
            'vote_count.gte': '100', 'page': page
        })
        out += [clean_item(x, 'movie', genres) for x in data.get('results', [])]
        page += 1
    return dedupe(out)[:limit]


def discover_kdrama(limit):
    genres = genre_map('tv')
    out = []
    page = 1
    while len(out) < limit and page <= 5:
        data = tmdb('/discover/tv', {
            'language': 'en-US', 'sort_by': 'popularity.desc', 'with_origin_country': 'KR',
            'with_original_language': 'ko', 'vote_count.gte': '50', 'page': page
        })
        out += [clean_item(x, 'kdrama', genres) for x in data.get('results', [])]
        page += 1
    return dedupe(out)[:limit]


def discover_anime(limit):
    genres = genre_map('tv')
    out = []
    page = 1
    while len(out) < limit and page <= 5:
        data = tmdb('/discover/tv', {
            'language': 'en-US', 'sort_by': 'popularity.desc', 'with_origin_country': 'JP',
            'with_original_language': 'ja', 'with_genres': '16', 'vote_count.gte': '20', 'page': page
        })
        out += [clean_item(x, 'anime', genres) for x in data.get('results', [])]
        page += 1
    return dedupe(out)[:limit]


def dedupe(items):
    seen, out = set(), []
    for i in items:
        key = i['id']
        if key not in seen:
            seen.add(key); out.append(i)
    return out


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding='utf-8')


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--limit', type=int, default=100)
    args = ap.parse_args()
    DATA.mkdir(exist_ok=True)
    movies = discover_movies(args.limit)
    kdrama = discover_kdrama(args.limit)
    anime = discover_anime(args.limit)
    write(DATA / 'movies.json', movies)
    write(DATA / 'kdrama.json', kdrama)
    write(DATA / 'anime.json', anime)
    write(DATA / 'latest.json', {
        'updated_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime()),
        'movies_count': len(movies), 'kdrama_count': len(kdrama), 'anime_count': len(anime),
        'files': {'movies': 'data/movies.json', 'kdrama': 'data/kdrama.json', 'anime': 'data/anime.json'}
    })
    print(f'Wrote {len(movies)} movies, {len(kdrama)} kdrama, {len(anime)} anime')

if __name__ == '__main__':
    main()
