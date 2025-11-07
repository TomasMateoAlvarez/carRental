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
