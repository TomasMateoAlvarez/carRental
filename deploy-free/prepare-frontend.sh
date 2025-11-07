#!/bin/bash

# Preparar Frontend de CarRental para Vercel (Deployment Gratuito)
set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')] $1${NC}"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

error() {
    echo -e "${RED}❌ $1${NC}"
    exit 1
}

FRONTEND_DIR="/Users/mateoalvarez/IdeaProjects/carrental-frontend"

echo "🚀 PREPARANDO FRONTEND PARA VERCEL (GRATUITO)"
echo "============================================="
echo ""

# Verificar que el directorio del frontend existe
if [ ! -d "$FRONTEND_DIR" ]; then
    error "No se encontró el directorio del frontend: $FRONTEND_DIR"
fi

cd "$FRONTEND_DIR"

# Verificar que es un proyecto React/Vite
if [ ! -f "package.json" ]; then
    error "No se encontró package.json en $FRONTEND_DIR"
fi

# 1. Crear configuración de entorno para producción
log "Creando archivos de configuración de entorno..."

# Archivo de ejemplo para variables de entorno
cat > .env.example << 'EOF'
# CarRental Frontend - Variables de Entorno (Ejemplo)

# Backend API URL (cambiar por tu URL de Render)
VITE_API_BASE_URL=https://tu-backend.onrender.com/api/v1

# Configuración de la aplicación
VITE_APP_NAME=CarRental SaaS
VITE_APP_VERSION=1.0.0

# Configuración de autenticación
VITE_JWT_STORAGE_KEY=carrental_token

# URLs públicas
VITE_FRONTEND_URL=https://tu-frontend.vercel.app
EOF

# Archivo para producción (Vercel leerá automáticamente las env vars)
cat > .env.production << 'EOF'
# CarRental Frontend - Producción
VITE_API_BASE_URL=$VITE_API_BASE_URL
VITE_APP_NAME=CarRental SaaS
VITE_APP_VERSION=1.0.0
VITE_JWT_STORAGE_KEY=carrental_token
VITE_FRONTEND_URL=$VITE_FRONTEND_URL
EOF

success "Archivos de entorno creados"

# 2. Crear configuración de Vercel
log "Creando vercel.json para configuración optimizada..."

cat > vercel.json << 'EOF'
{
  "version": 2,
  "name": "carrental-frontend",
  "builds": [
    {
      "src": "package.json",
      "use": "@vercel/static-build",
      "config": {
        "distDir": "dist"
      }
    }
  ],
  "routes": [
    {
      "src": "/(.*\\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot))",
      "headers": {
        "Cache-Control": "public, max-age=31536000, immutable"
      }
    },
    {
      "src": "/manifest.json",
      "headers": {
        "Content-Type": "application/json"
      }
    },
    {
      "src": "/(.*)",
      "dest": "/index.html"
    }
  ],
  "headers": [
    {
      "source": "/(.*)",
      "headers": [
        {
          "key": "X-Frame-Options",
          "value": "DENY"
        },
        {
          "key": "X-Content-Type-Options",
          "value": "nosniff"
        },
        {
          "key": "Referrer-Policy",
          "value": "strict-origin-when-cross-origin"
        },
        {
          "key": "X-XSS-Protection",
          "value": "1; mode=block"
        }
      ]
    }
  ],
  "env": {
    "VITE_API_BASE_URL": "@vite_api_base_url",
    "VITE_FRONTEND_URL": "@vite_frontend_url"
  }
}
EOF

success "vercel.json creado"

# 3. Optimizar package.json para Vercel
log "Optimizando package.json..."

# Backup del package.json original
cp package.json package.json.backup

# Crear script temporal para modificar package.json
cat > modify_package.cjs << 'EOF'
const fs = require('fs');
const packageJson = JSON.parse(fs.readFileSync('package.json', 'utf8'));

// Agregar scripts de build optimizados para Vercel
packageJson.scripts = {
  ...packageJson.scripts,
  "build": "vite build",
  "build:vercel": "npm run build",
  "preview": "vite preview",
  "vercel-build": "npm run build"
};

// Configurar engines (opcional)
packageJson.engines = {
  "node": ">=18.0.0",
  "npm": ">=8.0.0"
};

// Optimizaciones para build
if (!packageJson.browserslist) {
  packageJson.browserslist = {
    "production": [
      ">0.2%",
      "not dead",
      "not op_mini all"
    ],
    "development": [
      "last 1 chrome version",
      "last 1 firefox version",
      "last 1 safari version"
    ]
  };
}

fs.writeFileSync('package.json', JSON.stringify(packageJson, null, 2));
console.log('✅ package.json optimizado para Vercel');
EOF

node modify_package.cjs
rm modify_package.cjs

success "package.json optimizado"

# 4. Crear archivo de configuración Vite optimizado
log "Optimizando vite.config.js para producción..."

if [ -f "vite.config.js" ]; then
    cp vite.config.js vite.config.js.backup
fi

cat > vite.config.js << 'EOF'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],

  // Configuración para Vercel
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false, // Deshabilitar para reducir tamaño
    minify: 'esbuild',
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          antd: ['antd'],
          icons: ['@ant-design/icons']
        }
      }
    },
    chunkSizeWarningLimit: 1000
  },

  // Configuración del servidor de desarrollo
  server: {
    port: 5173,
    host: true
  },

  // Preview para testing local
  preview: {
    port: 4173,
    host: true
  },

  // Variables de entorno
  define: {
    __APP_VERSION__: JSON.stringify(process.env.npm_package_version)
  }
})
EOF

success "vite.config.js optimizado"

# 5. Crear archivo de configuración para API
log "Creando configuración centralizada de API..."

mkdir -p src/config

cat > src/config/api.js << 'EOF'
// Configuración centralizada de API para CarRental

// URL base de la API (desde variables de entorno)
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083/api/v1';

// URLs de endpoints
export const API_ENDPOINTS = {
  // Authentication
  LOGIN: '/auth/login',
  REGISTER: '/auth/register',
  ME: '/auth/me',

  // Vehicles
  VEHICLES: '/vehicles',
  VEHICLES_AVAILABLE: '/vehicles/available',

  // Reservations
  RESERVATIONS: '/reservations',

  // Users (admin)
  USERS: '/users',

  // Maintenance
  MAINTENANCE: '/maintenance',

  // Dashboard
  DASHBOARD: '/dashboard/kpis',

  // Health check
  HEALTH: '/actuator/health'
};

// Configuración de axios
export const API_CONFIG = {
  baseURL: API_BASE_URL,
  timeout: 30000, // 30 segundos
  headers: {
    'Content-Type': 'application/json'
  }
};

// Helper para construir URLs completas
export const getApiUrl = (endpoint) => {
  return `${API_BASE_URL}${endpoint}`;
};

// Configuración de autenticación
export const AUTH_CONFIG = {
  tokenKey: import.meta.env.VITE_JWT_STORAGE_KEY || 'carrental_token',
  tokenPrefix: 'Bearer '
};

// Configuración de la app
export const APP_CONFIG = {
  name: import.meta.env.VITE_APP_NAME || 'CarRental SaaS',
  version: import.meta.env.VITE_APP_VERSION || '1.0.0',
  frontendUrl: import.meta.env.VITE_FRONTEND_URL || 'http://localhost:5173'
};

console.log('🔗 API configurada:', {
  baseUrl: API_BASE_URL,
  environment: import.meta.env.MODE
});
EOF

success "Configuración de API creada"

# 6. Verificar build local
log "Verificando que el build funciona correctamente..."

if npm run build; then
    success "✅ Build exitoso - El proyecto está listo para Vercel"

    # Mostrar tamaño del build
    if [ -d "dist" ]; then
        BUILD_SIZE=$(du -sh dist | cut -f1)
        echo "📦 Tamaño del build: $BUILD_SIZE"
    fi
else
    error "❌ Error en build. Revisa los errores arriba antes de deployar."
fi

# 7. Crear README específico para deployment
cat > ../CarRental/deploy-free/VERCEL-DEPLOYMENT.md << 'EOF'
# Deployment del Frontend en Vercel (Gratuito)

## 🎯 Paso 1: Preparar Repositorio

1. **Commit los cambios** del frontend:
```bash
cd carrental-frontend
git add .
git commit -m "Prepare frontend for Vercel deployment"
git push origin main
```

## 🚀 Paso 2: Crear Proyecto en Vercel

1. **Ir a Vercel**: https://vercel.com
2. **Sign Up** con GitHub (recomendado)
3. **Import Project**:
   - Select Git Repository: [tu-repositorio-carrental]
   - Framework Preset: Vite
   - Root Directory: `carrental-frontend`

## ⚙️ Paso 3: Configurar Variables de Entorno

En Vercel Dashboard → Settings → Environment Variables:

```
VITE_API_BASE_URL=https://tu-backend.onrender.com/api/v1
VITE_FRONTEND_URL=https://tu-proyecto.vercel.app
VITE_APP_NAME=CarRental SaaS
VITE_APP_VERSION=1.0.0
```

## 🔧 Configuraciones de Build

Vercel detectará automáticamente:
- **Framework**: Vite
- **Build Command**: `npm run build`
- **Output Directory**: `dist`
- **Install Command**: `npm install`

## 📊 Capacidad Free Tier

- ✅ 100GB de ancho de banda/mes
- ✅ Builds ilimitados
- ✅ Despliegues automáticos
- ✅ SSL automático
- ✅ CDN global
- ✅ Custom domain gratuito

## 🌐 Configurar Dominio Personalizado (Opcional)

1. En Vercel → Settings → Domains
2. Agregar tu dominio: `carrental.tudominio.com`
3. Configurar DNS según las instrucciones de Vercel

## 🧪 Testing Post-Deployment

Una vez deployado, verificar:

1. **Frontend funcionando**: https://tu-proyecto.vercel.app
2. **Conexión con API**: Verificar en Network tab del browser
3. **Autenticación**: Login/logout funcionando
4. **CORS**: Sin errores de origen cruzado

## 🔄 Auto-Deploy

- ✅ Cada push a `main` despliega automáticamente
- ✅ Preview deploys para branches
- ✅ Rollback con un click

## 🐛 Troubleshooting

### Error CORS
Si hay errores CORS, verificar:
1. `VITE_API_BASE_URL` apunta correctamente al backend
2. Backend tiene CORS configurado para el frontend URL
3. Variables de entorno están correctas en Vercel

### Build Fails
Si el build falla:
1. Verificar que `npm run build` funciona localmente
2. Revisar dependencias en package.json
3. Check build logs en Vercel dashboard

### API No Conecta
Si el frontend no conecta con la API:
1. Verificar que Render backend está activo
2. Test manual: `curl https://tu-backend.onrender.com/actuator/health`
3. Revisar variables de entorno

¡Tu frontend estará funcionando con SSL automático y CDN global!
EOF

echo ""
echo "🎉 FRONTEND PREPARADO PARA VERCEL"
echo "================================="
echo ""
echo "📁 Archivos creados:"
echo "   ✅ .env.example"
echo "   ✅ .env.production"
echo "   ✅ vercel.json"
echo "   ✅ vite.config.js (optimizado)"
echo "   ✅ src/config/api.js"
echo "   ✅ package.json (optimizado)"
echo ""
echo "📋 Próximos pasos:"
echo "   1. Hacer commit y push al repositorio"
echo "   2. Configurar Vercel según VERCEL-DEPLOYMENT.md"
echo "   3. Configurar variables de entorno"
echo ""
echo "💡 El frontend estará optimizado para:"
echo "   ✅ Vercel free tier"
echo "   ✅ Build size mínimo"
echo "   ✅ CDN global"
echo "   ✅ SSL automático"
echo "   ✅ Auto-deploy en cada push"
echo ""
success "¡Frontend listo para deployment gratuito!"