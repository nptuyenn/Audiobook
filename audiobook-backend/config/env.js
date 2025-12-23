import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

console.log('--- Using manual .env loader ---');

// Because this file is in audiobook-backend/config/env.js, the .env file should be in ../
// Use import.meta.url to get the current file's path in an ES module context.
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const envPath = path.resolve(__dirname, '../.env');

try {
  console.log(`Attempting to load environment variables from: ${envPath}`);
  const envFileContent = fs.readFileSync(envPath, { encoding: 'utf8' });
  const lines = envFileContent.split('\n');

  let loadedCount = 0;
  for (const line of lines) {
    // Ignore comments and empty lines
    if (line.trim() === '' || line.trim().startsWith('#')) {
      continue;
    }

    const eqIndex = line.indexOf('=');
    if (eqIndex === -1) {
      continue;
    }
    
    const key = line.slice(0, eqIndex).trim();
    let value = line.slice(eqIndex + 1).trim();

    // Basic handling for values enclosed in quotes
    if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
      value = value.slice(1, -1);
    }
    
    if (key) {
      process.env[key] = value;
      loadedCount++;
    }
  }
  console.log(`Manually loaded ${loadedCount} variables from .env file.`);

} catch (error) {
  if (error.code === 'ENOENT') {
    console.warn(`Warning: .env file not found at ${envPath}. Skipping manual load.`);
  } else {
    console.error('Error manually loading .env file:', error);
  }
}
