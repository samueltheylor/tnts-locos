"""Sube una version del mod a Modrinth (API v2, formato multipart con campo
`data`). Uso tras compilar el jar:

    python tools/upload_modrinth.py 1.9.5 tnts_forge/build/libs/tnts-1.9.5.jar "changelog..."

El token se lee de `.modrinth_token` (gitignoreado) o de la variable MODRINTH_TOKEN.
Requiere: curl (para el multipart con archivos grandes).

Idempotente: si ya existe una version con el mismo numero, la borra antes de
subir (evita duplicados si el script se corta a mitad).
"""
import json
import os
import subprocess
import sys

PROJECT_ID = "EPKqrXGK"  # tnts-locos
BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # tnts_locos


def main():
    if len(sys.argv) < 3:
        print("uso: python tools/upload_modrinth.py <version> <jar> [changelog]")
        sys.exit(1)
    version = sys.argv[1]
    jar = os.path.join(BASE, sys.argv[2]) if not os.path.isabs(sys.argv[2]) else sys.argv[2]
    changelog = sys.argv[3] if len(sys.argv) > 3 else f"v{version}"

    token = os.environ.get("MODRINTH_TOKEN") or open(
        os.path.join(BASE, ".modrinth_token")).read().strip()

    data = {
        "name": f"v{version}",
        "version_number": version,
        "changelog": changelog,
        "game_versions": ["1.20.1"],
        "version_types": ["release"],
        "release_channel": "release",
        "loaders": ["forge"],
        "featured": False,
        "project_id": PROJECT_ID,
        "dependencies": [],
        "file_parts": ["jar"],
        "primary_file": "jar",
    }
    data_path = os.path.join(os.environ.get("TEMP", "/tmp"), "mr_data.json")
    with open(data_path, "w") as f:
        json.dump(data, f)

    import urllib.request

    def api(path, method="GET"):
        req = urllib.request.Request(
            f"https://api.modrinth.com/v2{path}",
            method=method,
            headers={"Authorization": token,
                     "User-Agent": "samueltheylor/tnts-locos (dev)"},
        )
        with urllib.request.urlopen(req) as r:
            body = r.read()
            return json.loads(body.decode("utf-8")) if body else None

    # idempotente: borra versiones previas con el mismo numero
    for old in api(f"/project/{PROJECT_ID}/version") or []:
        if old.get("version_number") == version:
            api(f"/version/{old['id']}", method="DELETE")
            print(f"borrada version previa {old['id']}")

    cmd = [
        "curl", "-s", "-X", "POST", "https://api.modrinth.com/v2/version",
        "-H", f"Authorization: {token}",
        "-H", "User-Agent: samueltheylor/tnts-locos (dev)",
        "-F", f"data=<{data_path};type=application/json",
        "-F", f"jar=@{jar}",
    ]
    proc = subprocess.run(cmd, capture_output=True, text=True, errors="replace")
    out = (proc.stdout or "").strip()
    if not out:
        print("ERROR: respuesta vacia de la API (stdout de curl vacio)")
        print("stderr:", (proc.stderr or "")[:500])
        sys.exit(1)
    resp = json.loads(out)
    if "id" in resp:
        print(f"OK version {resp['version_number']} subida: https://modrinth.com/mod/{PROJECT_ID}/version/{resp['id']}")
    else:
        print("ERROR:", resp)
        sys.exit(1)


if __name__ == "__main__":
    main()
