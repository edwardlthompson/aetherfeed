import re
import pathlib
import sys

p = pathlib.Path(sys.argv[1]).read_text(encoding="utf-8")
print("TEXTS:")
for t in re.findall(r'text="([^"]+)"', p):
    print(" -", t)
print("---WEB---")
for m in re.finditer(
    r'class="([^"]*WebView[^"]*)"[^>]*scrollable="([^"]+)"[^>]*bounds="([^"]+)"', p
):
    print(m.group(1), m.group(2), m.group(3))
print("---SCROLLABLE---")
for m in re.finditer(r'class="([^"]+)"[^>]*scrollable="true"[^>]*bounds="([^"]+)"', p):
    print(m.group(1), m.group(2))
print("---TITLE BOUNDS---")
for m in re.finditer(
    r'text="([^"]+)"[^>]*class="android.widget.TextView"[^>]*bounds="([^"]+)"', p
):
    print(m.group(2), m.group(1)[:80])
