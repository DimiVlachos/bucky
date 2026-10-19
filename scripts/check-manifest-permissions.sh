#!/usr/bin/env bash
# Fails if a merged app manifest requests a permission Bucky promises never to use (PLAN §4.14).
set -euo pipefail

forbidden='android\.permission\.(INTERNET|QUERY_ALL_PACKAGES)'
root="${1:-app/build/intermediates/merged_manifests}"

manifests=$(find "$root" -path '*/process*Manifest/AndroidManifest.xml' 2>/dev/null || true)
if [ -z "$manifests" ]; then
  echo "No merged manifests under $root. Build the app first (assembleFossDebug assemblePlayDebug)." >&2
  exit 2
fi

status=0
for manifest in $manifests; do
  if grep -En "$forbidden" "$manifest"; then
    echo "FORBIDDEN PERMISSION in $manifest" >&2
    status=1
  else
    echo "ok: $manifest"
  fi
done
exit $status
