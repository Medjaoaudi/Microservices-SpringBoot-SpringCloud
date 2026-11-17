import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Le front appelle directement l'API Gateway (http://localhost:8888, CORS configuré côté Gateway).
export default defineConfig({
  plugins: [react()],
  server: { port: 5173, strictPort: true },
  preview: { port: 5173, strictPort: true },
});
