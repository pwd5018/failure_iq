import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Proxy API calls to the Spring Boot backend during local development.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
