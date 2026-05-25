#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# gerar-pdf-fase3.sh — Gera entrega-fase3-grupo14soat.pdf a partir do HTML
# Grupo 14SOAT | FIAP 2025/2026
# ─────────────────────────────────────────────────────────────────────────────

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HTML_FILE="$SCRIPT_DIR/presentation-fase3.html"
PDF_FILE="$SCRIPT_DIR/entrega-fase3-grupo14soat.pdf"

echo "────────────────────────────────────────────"
echo " Mecânica API — Gerador de PDF de Entrega"
echo " Fase 3 — Operação Corporativa"
echo " Grupo 14SOAT | FIAP 2025/2026"
echo "────────────────────────────────────────────"
echo ""

# ── 1. Chrome Headless (macOS) ──────────────────────────────────────────────
CHROME_PATHS=(
  "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
  "/Applications/Chromium.app/Contents/MacOS/Chromium"
  "/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary"
)

for CHROME in "${CHROME_PATHS[@]}"; do
  if [ -f "$CHROME" ]; then
    echo "[1/1] Gerando PDF via Chrome Headless..."
    "$CHROME" \
      --headless=new \
      --disable-gpu \
      --no-sandbox \
      --print-to-pdf="$PDF_FILE" \
      --print-to-pdf-no-header \
      --no-margins \
      --run-all-compositor-stages-before-draw \
      --virtual-time-budget=5000 \
      "file://$HTML_FILE" 2>/dev/null
    echo ""
    echo "✅ PDF gerado com sucesso!"
    echo "   → $PDF_FILE"
    echo ""
    echo "Para visualizar:"
    echo "   open \"$PDF_FILE\""
    exit 0
  fi
done

# ── 2. Node.js + Puppeteer (fallback) ───────────────────────────────────────
if command -v node &>/dev/null && node -e "require('puppeteer')" 2>/dev/null; then
  echo "[1/1] Chrome não encontrado. Usando Node.js + Puppeteer..."
  node - <<'NODEJS'
const puppeteer = require('puppeteer');
const path = require('path');
const htmlFile = path.join(__dirname, 'presentation-fase3.html');
const pdfFile  = path.join(__dirname, 'entrega-fase3-grupo14soat.pdf');

(async () => {
  const browser = await puppeteer.launch({ headless: 'new', args: ['--no-sandbox'] });
  const page = await browser.newPage();
  await page.goto('file://' + htmlFile, { waitUntil: 'networkidle0' });
  await page.pdf({
    path: pdfFile,
    format: 'A4',
    printBackground: true,
    margin: { top: 0, right: 0, bottom: 0, left: 0 }
  });
  await browser.close();
  console.log('✅ PDF gerado: ' + pdfFile);
})();
NODEJS
  exit 0
fi

# ── 3. Instrução Manual ─────────────────────────────────────────────────────
echo "⚠️  Chrome / Chromium não encontrado no sistema."
echo ""
echo "Opções para gerar o PDF:"
echo ""
echo "  A) Instalar Google Chrome: https://www.google.com/chrome/"
echo "  B) Instalar Puppeteer: npm install -g puppeteer"
echo "  C) Abrir presentation-fase3.html no browser e usar 'Imprimir → Salvar como PDF'"
echo ""
echo "Caminho do HTML: $HTML_FILE"
exit 1
