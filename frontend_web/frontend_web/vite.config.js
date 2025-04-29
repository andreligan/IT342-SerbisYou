import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite'

export default defineConfig(({ mode }) => {
  // Load environment variables
  const env = loadEnv(mode, process.cwd());
  
  return {
    base: './',
    plugins: [
      tailwindcss(),
      react()
    ],
    server: {
      proxy: {
        '/api': {
          target: env.VITE_API_URL || 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
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