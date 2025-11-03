// Generates PNG favicon fallbacks and Apple touch icon from the existing SVG favicon.
// Outputs to public/icons so Angular copies them without config changes.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import sharp from 'sharp';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const root = path.resolve(__dirname, '..');
const svgPath = path.join(root, 'src', 'favicon.svg');
const outDir = path.join(root, 'public', 'icons');

const targets = [
  { name: 'favicon-16x16.png', size: 16 },
  { name: 'favicon-32x32.png', size: 32 },
  { name: 'apple-touch-icon.png', size: 180 }
];

async function ensureDir(dir) {
  await fs.promises.mkdir(dir, { recursive: true });
}

async function generate() {
  if (!fs.existsSync(svgPath)) {
    console.error('Source SVG not found:', svgPath);
    process.exit(1);
  }
  await ensureDir(outDir);
  const svgBuffer = await fs.promises.readFile(svgPath);
  for (const t of targets) {
    const outPath = path.join(outDir, t.name);
    await sharp(svgBuffer, { density: 384 })
      .resize(t.size, t.size, { fit: 'cover' })
      .png({ compressionLevel: 9 })
      .toFile(outPath);
    console.log('Generated', path.relative(root, outPath));
  }
}

generate().catch(err => { console.error(err); process.exit(1); });
