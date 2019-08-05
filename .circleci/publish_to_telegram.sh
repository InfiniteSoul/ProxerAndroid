#!/usr/bin/env bash
set -euo pipefail

if [[ -v CIRCLE_COMPARE_URL ]]; then
	COMMIT_RANGE=$(echo "${CIRCLE_COMPARE_URL}" | cut -d/ -f7)
	CAPTION=$(git log --pretty=format:"- %s" "${COMMIT_RANGE}" --reverse) || true
else
	CAPTION=$(git log -1 --pretty=format:"- %s") || true
fi

if [[ -v CAPTION ]]; then
    CAPTION=$(echo "$CAPTION" | cut -c -1024)
else
	CAPTION="No changelog available"
fi

if [[ -v TELEGRAM_CHAT_ID ]] && [[ -v TELEGRAM_BOT_ID ]]; then
    curl -s -S \
        -F chat_id="$TELEGRAM_CHAT_ID" \
        -F caption="$CAPTION" \
        -F document=@$(find ./build/outputs/apk/logRelease/ -type f -name "*.apk") \
        https://api.telegram.org/bot${TELEGRAM_BOT_ID}/sendDocument >/dev/null
else
    echo "TELEGRAM_CHAT_ID or TELEGRAM_BOT_ID not set."
    exit -1
fi
