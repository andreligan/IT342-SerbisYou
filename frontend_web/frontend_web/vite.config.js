import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig(({ mode }) => {
  // Load environment variables
  const env = loadEnv(mode, process.cwd());
  
  // Get API URL from environment or use the Render URL
  const apiUrl = env.VITE_API_URL || 'https://serbisyo-backend.onrender.com';
  
  return {
    // Add this base configuration to ensure assets use absolute paths
    base: '/',
    plugins: [
      tailwindcss(),
      react()
    ],
    server: {
      proxy: {
        // Keep the /api prefix when forwarding to the backend
        '/api': {
          target: apiUrl,
          changeOrigin: true,
          secure: false,
          // Don't rewrite the path - send it as-is
        },
      },
    },
    // Add build optimization
    build: {
      outDir: 'dist',
      sourcemap: false,
      minify: 'terser',
      target: 'es2015',
      assetsInlineLimit: 0,
      assetsDir: 'assets',
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