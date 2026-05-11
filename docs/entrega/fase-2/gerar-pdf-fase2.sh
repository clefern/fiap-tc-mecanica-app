#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# gerar-pdf-fase2.sh — Gera entrega-fase2-grupo14soat.pdf
# Fase 2 — Infraestrutura e Escalabilidade
# Grupo 14SOAT | FIAP 2025/2026
# ─────────────────────────────────────────────────────────────────────────────

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HTML_FILE="$SCRIPT_DIR/presentation-fase2.html"
PDF_FILE="$SCRIPT_DIR/entrega-fase2-grupo14soat.pdf"

echo "────────────────────────────────────────────────────────"
echo " Mecânica API — PDF de Entrega · Fase 2"
echo " Infraestrutura, CI/CD e Escalabilidade"
echo " Grupo 14SOAT | FIAP 2025/2026"
echo "────────────────────────────────────────────────────────"
echo ""

# ── 1. Node.js + Puppeteer (preferred — full control over headers/footers) ──
if command -v node &>/dev/null; then
  echo "[1/1] Gerando PDF via Puppeteer..."

  # Install puppeteer in a temp dir if not available
  TMPDIR_NPM=$(mktemp -d)
  trap "rm -rf $TMPDIR_NPM" EXIT

  if ! node -e "require('puppeteer')" 2>/dev/null; then
    echo "     Instalando puppeteer (primeira vez)..."
    (cd "$TMPDIR_NPM" && npm init -y > /dev/null 2>&1 && npm install puppeteer > /dev/null 2>&1)
    export NODE_PATH="$TMPDIR_NPM/node_modules"
  fi

  node -e "
    const puppeteer = require('puppeteer');

    (async () => {
      const browser = await puppeteer.launch({
        headless: 'new',
        args: ['--no-sandbox', '--disable-setuid-sandbox']
      });
      const page = await browser.newPage();
      await page.goto('file://${HTML_FILE}', {
        waitUntil: 'networkidle0',
        timeout: 30000
      });
      await page.pdf({
        path: '${PDF_FILE}',
        format: 'A4',
        printBackground: true,
        displayHeaderFooter: false,
        preferCSSPageSize: true
      });
      await browser.close();
    })();
  "

  echo ""
  echo "✅ PDF da Fase 2 gerado com sucesso!"
  echo "   → $PDF_FILE"
  echo ""
  echo "Para visualizar:"
  echo "   open \"$PDF_FILE\""
  exit 0
fi

# ── 2. Chrome Headless (fallback — may show headers/footers) ─────────────────
CHROME_PATHS=(
  "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
  "/Applications/Chromium.app/Contents/MacOS/Chromium"
  "/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary"
)

for CHROME in "${CHROME_PATHS[@]}"; do
  if [ -f "$CHROME" ]; then
    echo "[1/1] Gerando PDF via Chrome Headless (fallback)..."
    "$CHROME" \
      --headless=new \
      --disable-gpu \
      --no-sandbox \
      --print-to-pdf="$PDF_FILE" \
      --print-to-pdf-no-header \
      --run-all-compositor-stages-before-draw \
      --virtual-time-budget=5000 \
      "file://$HTML_FILE" 2>/dev/null
    echo ""
    echo "✅ PDF da Fase 2 gerado com sucesso!"
    echo "   → $PDF_FILE"
    echo ""
    echo "Para visualizar:"
    echo "   open \"$PDF_FILE\""
    exit 0
  fi
done

# ── 3. Instrução Manual ──────────────────────────────────────────────────────
echo "⚠️  Node.js e Chrome/Chromium não encontrados no sistema."
echo ""
echo "Opções para gerar o PDF:"
echo ""
echo "  A) Instalar Node.js e re-executar: bash gerar-pdf-fase2.sh"
echo ""
echo "  B) Abrir manualmente:"
echo "     open \"$HTML_FILE\""
echo "     → Cmd+P → Salvar como PDF → A4, sem margens, fundo gráfico habilitado"
echo ""
