const fs = require('node:fs');
const path = require('node:path');

const sourceDir = path.resolve(__dirname, '..', 'dist', 'frontend', 'browser');
const targetDir = path.resolve(__dirname, '..', '..', 'src', 'main', 'resources', 'static', 'dashboard');

if (!fs.existsSync(sourceDir)) {
  console.error(`Build output not found: ${sourceDir}`);
  process.exit(1);
}

fs.rmSync(targetDir, { recursive: true, force: true });
fs.mkdirSync(targetDir, { recursive: true });
fs.cpSync(sourceDir, targetDir, { recursive: true });

console.log(`Angular assets copied to ${targetDir}`);
