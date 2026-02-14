import sys
from pathlib import Path
script_path = Path('src/main/resources/static/js/dashboard.js')
with script_path.open('a', encoding='utf-8') as out:
    out.write(sys.stdin.read())
