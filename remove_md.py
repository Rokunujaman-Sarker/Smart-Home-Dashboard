import os
import sys
from pathlib import Path

root = Path(r"C:\Users\User\Desktop\Smart Home Dashboard")
removed = []
for path in root.rglob('*.md'):
    try:
        path.unlink()
        removed.append(str(path))
    except Exception as e:
        print(f"Failed to delete {path}: {e}")

print(f"Deleted {len(removed)} .md files")
for p in removed:
    print(p)

