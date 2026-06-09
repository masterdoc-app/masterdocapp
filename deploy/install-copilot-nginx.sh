#!/usr/bin/env bash
set -euo pipefail

DEPLOY_PATH="${DEPLOY_PATH:-/opt/masterdoc-copilot}"
CERTBOT_EMAIL="${CERTBOT_EMAIL:-admin@masterdoc.pro}"
SITE="/etc/nginx/sites-available/copilot.masterdoc.pro"
WEB_ROOT="/var/www/copilot.masterdoc.pro"

mkdir -p /var/www/certbot "${WEB_ROOT}"

if [[ -f /etc/letsencrypt/live/copilot.masterdoc.pro/fullchain.pem ]]; then
  cp "${DEPLOY_PATH}/copilot.masterdoc.pro.nginx.conf" "${SITE}"
else
  cp "${DEPLOY_PATH}/copilot.masterdoc.pro.nginx.http.conf" "${SITE}"
fi

ln -sf "${SITE}" /etc/nginx/sites-enabled/copilot.masterdoc.pro
nginx -t
systemctl reload nginx

if [[ ! -f /etc/letsencrypt/live/copilot.masterdoc.pro/fullchain.pem ]]; then
  certbot certonly --webroot -w /var/www/certbot \
    -d copilot.masterdoc.pro \
    --non-interactive --agree-tos --email "${CERTBOT_EMAIL}"
  cp "${DEPLOY_PATH}/copilot.masterdoc.pro.nginx.conf" "${SITE}"
  nginx -t
  systemctl reload nginx
fi
