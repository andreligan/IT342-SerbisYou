import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite'

export default defineConfig(({ mode }) => {
  // Load environment variables
  const env = loadEnv(mode, process.cwd());
  
  // Get API URL from environment
  const apiUrl = env.VITE_API_URL || 'http://localhost:8080';
  
  return {
    base: './',
    plugins: [
      tailwindcss(),
      react()
    ],
    server: {
      proxy: {
        // Only proxy /api prefix when in development
        '/api': {
          target: apiUrl,
          changeOrigin: true,
          secure: false,
          rewrite: (path) => path.replace(/^\/api/, '')
        },
      },
    },
    // Add build optimization
    build: {
      outDir: 'dist',
      sourcemap: false,
      minify: 'terser',
      target: 'es2015',
      assetsInlineLimit: 4096,
      rollupOptions: {
        output: {
          manualChunks: {
            vendor: ['react', 'react-dom', 'react-router-dom'],
          },
        },
      },
    }
  };
});