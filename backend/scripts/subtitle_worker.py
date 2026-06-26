#!/usr/bin/env python3
import argparse, json, os, re, subprocess, tempfile, time, urllib.request
from pathlib import Path

DATA = Path('data')


def read_json(path, default):
    if not path.exists(): return default
    return json.loads(path.read_text(encoding='utf-8') or json.dumps(default))


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding='utf-8')


def download(url, path):
    with urllib.request.urlopen(url, timeout=120) as r:
        path.write_bytes(r.read())


def srt_time(seconds):
    seconds = max(0, float(seconds))
    ms = int(round((seconds - int(seconds)) * 1000))
    s = int(seconds) % 60
    m = (int(seconds) // 60) % 60
    h = int(seconds) // 3600
    return f'{h:02d}:{m:02d}:{s:02d},{ms:03d}'


def parse_srt(text):
    blocks = re.split(r'\n\s*\n', text.strip())
    segs = []
    for b in blocks:
        lines = [x.strip('\ufeff') for x in b.splitlines() if x.strip()]
        if len(lines) >= 3 and '-->' in lines[1]:
            start, end = [x.strip() for x in lines[1].split('-->')[:2]]
            segs.append({'start': start, 'end': end, 'text': ' '.join(lines[2:])})
    return segs


def build_srt(segs, texts=None):
    out = []
    for i, seg in enumerate(segs, 1):
        text = texts[i-1] if texts else seg['text']
        out.append(f"{i}\n{seg['start']} --> {seg['end']}\n{text}\n")
    return '\n'.join(out)


def transcribe_with_groq(audio_path):
    # Uses curl to avoid third-party dependencies.
    key = os.environ.get('GROQ_API_KEY', '').strip()
    if not key:
        raise RuntimeError('GROQ_API_KEY is missing')
    cmd = [
        'curl','-sS','https://api.groq.com/openai/v1/audio/transcriptions',
        '-H', f'Authorization: Bearer {key}',
        '-F', 'model=whisper-large-v3-turbo',
        '-F', 'response_format=verbose_json',
        '-F', 'timestamp_granularities[]=segment',
        '-F', f'file=@{audio_path}'
    ]
    raw = subprocess.check_output(cmd, text=True)
    data = json.loads(raw)
    segs = []
    for x in data.get('segments', []):
        segs.append({'start': srt_time(x.get('start', 0)), 'end': srt_time(x.get('end', 0)), 'text': x.get('text', '').strip()})
    return [s for s in segs if s['text']]


def translate_openrouter(lines, target='Arabic'):
    key = os.environ.get('OPENROUTER_API_KEY', '').strip()
    if not key:
        raise RuntimeError('OPENROUTER_API_KEY is missing')
    prompt = 'Translate each numbered subtitle line to ' + target + '. Keep the same numbering. Return JSON array of strings only.\n' + json.dumps(lines, ensure_ascii=False)
    body = json.dumps({
        'model': os.environ.get('TRANSLATION_MODEL', 'google/gemini-2.0-flash-001'),
        'messages': [{'role':'user','content': prompt}],
        'temperature': 0.2
    })
    import urllib.request
    req = urllib.request.Request('https://openrouter.ai/api/v1/chat/completions', data=body.encode('utf-8'), headers={'Authorization': f'Bearer {key}', 'Content-Type': 'application/json'}, method='POST')
    with urllib.request.urlopen(req, timeout=120) as r:
        data = json.loads(r.read().decode('utf-8'))
    content = data['choices'][0]['message']['content'].strip()
    m = re.search(r'\[.*\]', content, re.S)
    if not m: raise RuntimeError('OpenRouter did not return JSON array')
    arr = json.loads(m.group(0))
    return [str(x) for x in arr]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--media-id', required=True)
    ap.add_argument('--source-language', default='auto')
    ap.add_argument('--source-srt-url', default='')
    ap.add_argument('--video-url', default='')
    args = ap.parse_args()
    media_id = args.media_id.strip()
    out_dir = DATA / 'subtitles' / media_id
    out_dir.mkdir(parents=True, exist_ok=True)
    index_path = DATA / 'subtitles_index.json'
    index = read_json(index_path, {})

    try:
        if args.source_srt_url:
            with urllib.request.urlopen(args.source_srt_url, timeout=60) as r:
                source_text = r.read().decode('utf-8', errors='replace')
            segs = parse_srt(source_text)
        elif args.video_url:
            with tempfile.TemporaryDirectory() as td:
                media = Path(td) / 'source.bin'
                audio = Path(td) / 'audio.mp3'
                download(args.video_url, media)
                subprocess.check_call(['ffmpeg','-y','-i',str(media),'-vn','-ac','1','-ar','16000',str(audio)])
                segs = transcribe_with_groq(audio)
        else:
            index[media_id] = {'id': media_id, 'status': 'needs_source', 'message': 'Provide source_srt_url or legal video_url to generate real SRT.', 'updated_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())}
            write_json(index_path, index)
            print('No source provided; marked needs_source')
            return
        if not segs: raise RuntimeError('No subtitle segments were created')
        en_srt = build_srt(segs)
        (out_dir / 'en.srt').write_text(en_srt, encoding='utf-8')
        ar_lines = translate_openrouter([s['text'] for s in segs], 'Arabic')
        (out_dir / 'ar.srt').write_text(build_srt(segs, ar_lines), encoding='utf-8')
        write_json(out_dir / 'meta.json', {'id': media_id, 'source_language': args.source_language, 'segments': len(segs), 'updated_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())})
        index[media_id] = {'id': media_id, 'status': 'ready', 'en': f'data/subtitles/{media_id}/en.srt', 'ar': f'data/subtitles/{media_id}/ar.srt', 'segments': len(segs), 'updated_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())}
        write_json(index_path, index)
        print(f'Created subtitles for {media_id}: {len(segs)} segments')
    except Exception as e:
        index[media_id] = {'id': media_id, 'status': 'failed', 'error': str(e), 'updated_at': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())}
        write_json(index_path, index)
        raise

if __name__ == '__main__':
    main()
