#!/usr/bin/env python3
"""Inserts new TNT entries into TntDefaults.java."""
import sys, os, io, contextlib, tempfile

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__))))
import gen_defaults_entries

# Generate entries by running gen_defaults_entries as script
import subprocess
result = subprocess.run([sys.executable, os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_defaults_entries.py")], capture_output=True, text=True)
new_entries = result.stdout

# Read existing TntDefaults.java
tnt_defaults = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "java", "com", "tnts", "config", "TntDefaults.java")
with open(tnt_defaults, "r", encoding="utf-8") as f:
    content = f.read()

marker = 'add("portal_tnt"'
pos = content.find(marker)
if pos < 0:
    print("ERROR: marker not found")
    sys.exit(1)
# Find end of that line
eol = content.find("\n", pos)
insert_point = eol + 1

new_content = content[:insert_point] + \
    "\n        // === TNTs MASIVAS (v1.11.0) ===\n" + \
    new_entries + \
    "\n" + content[insert_point:]

with open(tnt_defaults, "w", encoding="utf-8") as f:
    f.write(new_content)
print(f"OK - inserted {len(new_entries.splitlines())} entries into TntDefaults.java")
