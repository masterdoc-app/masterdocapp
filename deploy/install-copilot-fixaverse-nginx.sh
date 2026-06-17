#!/usr/bin/env bash
set -euo pipefail

DEPLOY_PATH="${DEPLOY_PATH:-/opt/masterdoc-copilot}"
CERTBOT_EMAIL="${CERTBOT_EMAIL:-admin@fixaverse.ru}"
SITE_HOST="${SITE_HOST:-copilot.fixaverse.ru}"
SITE="/etc/nginx/sites-available/${SITE_HOST}"
WEB_ROOT="/var/www/${SITE_HOST}"
LEGACY_WEB_ROOT="/var/www/copilot.masterdoc.pro"

mkdir -p /var/www/certbot "${WEB_ROOT}"

# Reuse existing static tree from legacy copilot deploy when present.
if [[ ! -e "${WEB_ROOT}/index.html" && -d "${LEGACY_WEB_ROOT}" ]]; then
  rsync -a "${LEGACY_WEB_ROOT}/" "${WEB_ROOT}/"
fi

if [[ -f "/etc/letsencrypt/live/${SITE_HOST}/fullchain.pem" ]]; then
  cp "${DEPLOY_PATH}/copilot.fixaverse.ru.nginx.conf" "${SITE}"
else
  cp "${DEPLOY_PATH}/copilot.fixaverse.ru.nginx.http.conf" "${SITE}"
fi

ln -sf "${SITE}" "/etc/nginx/sites-enabled/${SITE_HOST}"
nginx -t
systemctl reload nginx

if [[ ! -f "/etc/letsencrypt/live/${SITE_HOST}/fullchain.pem" ]]; then
  certbot certonly --webroot -w /var/www/certbot \
    -d "${SITE_HOST}" \
    --non-interactive --agree-tos --email "${CERTBOT_EMAIL}"
  cp "${DEPLOY_PATH}/copilot.fixaverse.ru.nginx.conf" "${SITE}"
  nginx -t
  systemctl reload nginx
fi

echo "OK: https://${SITE_HOST}"
