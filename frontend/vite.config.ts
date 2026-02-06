import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [vue()],
    
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
      },
      extensions: ['.ts', '.vue', '.json', '.js'],
    },
    
    server: {
      host: '0.0.0.0',
      port: 5173,
      open: true,
      cors: true,
      hmr: {
        host: 'localhost',
        port: 5173,
        protocol: 'ws',
      },
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8081',
          changeOrigin: true,
          secure: false,
          ws: true,
          timeout: 30000,
          rewrite: (path) => path,
        },
        '/ws': {
          target: env.VITE_WS_BASE_URL || 'ws://localhost:8081',
          changeOrigin: true,
          secure: false,
          ws: true,
        },
      },
    },
    
    build: {
      target: 'es2015',
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: mode === 'development',
      minify: 'esbuild',
      chunkSizeWarningLimit: 1000,
      rollupOptions: {
        output: {
          manualChunks: {
            'vue-vendor': ['vue', 'vue-router'],
            'antd-vendor': ['ant-design-vue'],
            'utils': ['axios', 'hls.js'],
          },
          chunkFileNames: 'assets/[name]-[hash].js',
          entryFileNames: 'assets/[name]-[hash].js',
          assetFileNames: 'assets/[name]-[hash].[ext]',
        },
      },
    },

    esbuild: {
      drop: mode === 'production' ? ['console', 'debugger'] : [],
    },
    
    optimizeDeps: {
      include: ['vue', 'vue-router', 'axios', 'ant-design-vue', 'hls.js'],
    },
  }
})
