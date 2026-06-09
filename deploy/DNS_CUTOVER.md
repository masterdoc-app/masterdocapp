# DNS: copilot.masterdoc.pro на VPS

Выполнить **один раз** после первого успешного деплоя workflow `Deploy Web to VPS`.

## Целевое состояние

- `copilot.masterdoc.pro` → **A** `91.207.75.72`

## Порядок действий

1. Убедиться, что на VPS поднят nginx и статика в `/var/www/copilot.masterdoc.pro` (workflow `deploy-copilot-web.yml` зелёный).
2. Проверить с сервера до переключения DNS:
   ```bash
   curl -fsS http://91.207.75.72/ -H 'Host: copilot.masterdoc.pro' | head -c 200
   ```
3. В DNS-панели **REG.RU** для домена `masterdoc.pro`:
   - добавить A-запись `copilot` → `91.207.75.72`;
   - если был `mvp-web` (CNAME на GitHub Pages) — удалить, он больше не нужен.
4. Дождаться распространения DNS (TTL ~1–6 ч; можно снизить TTL заранее).
5. Проверить:
   ```bash
   dig +short copilot.masterdoc.pro A
   ./scripts/smoke-copilot-web.sh https://copilot.masterdoc.pro
   ```
6. В GitHub repo `masterdoc-app/masterdocapp`:
   - **Settings → Pages** — убрать custom domain `mvp-web.masterdoc.pro`, если ещё привязан.

## Откат

Удалить A-запись `copilot` или вернуть предыдущую DNS-конфигурацию.
