#!/usr/bin/env python3
"""Insert lang entries for all new TNTs into gen_resources.py."""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_massive_tnts import NEW_TNTS

GEN = os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_resources.py")

with open(GEN, "r", encoding="utf-8") as f:
    content = f.read()

# NEW_TNTS structure: (name, effect, palette, radius, breaks, fire, recipe, count, lang_es, lang_en, lang_fr, lang_de, lang_pt)
# Indices: 0=name, 8=lang_es, 9=lang_en, 10=lang_fr, 11=lang_de, 12=lang_pt

# Generate entries for each language
lang_entries = {"es": [], "en": [], "fr": [], "de": [], "pt": []}
for name, *rest in NEW_TNTS:
    lang_es = rest[7]
    lang_en = rest[8]
    lang_fr = rest[9]
    lang_de = rest[10]
    lang_pt = rest[11]
    lang_entries["es"].append(f'    "block.tnts.{name}": "{lang_es}",')
    lang_entries["en"].append(f'    "block.tnts.{name}": "{lang_en}",')
    lang_entries["fr"].append(f'    "block.tnts.{name}": "{lang_fr}",')
    lang_entries["de"].append(f'    "block.tnts.{name}": "{lang_de}",')
    lang_entries["pt"].append(f'    "block.tnts.{name}": "{lang_pt}",')

# Insert into each LANG_* dict
for lang_key, entries in lang_entries.items():
    block = "\n".join(entries)
    # Find the closing brace of the LANG_XX dict
    marker = f'LANG_{lang_key.upper()} = {{'
    pos = content.find(marker)
    if pos < 0:
        print(f"WARNING: {marker} not found, skipping")
        continue
    # Find the matching closing brace
    brace_count = 0
    i = content.index('{', pos)
    start = i
    for j in range(i, len(content)):
        if content[j] == '{':
            brace_count += 1
        elif content[j] == '}':
            brace_count -= 1
            if brace_count == 0:
                # Insert before closing brace
                content = content[:j] + block + "\n" + content[j:]
                print(f"  Added {len(entries)} entries to LANG_{lang_key.upper()}")
                break
    # Re-find for next iteration (positions shifted)

with open(GEN, "w", encoding="utf-8") as f:
    f.write(content)

print(f"Done! Added lang entries for {len(NEW_TNTS)} TNTs in 5 languages")
