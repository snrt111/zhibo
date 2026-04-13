import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import * as Sentry from '@sentry/vue'
import { BrowserTracing } from '@sentry/tracing'

const app = createApp(App)

// 集成 Sentry 错误监控
Sentry.init({
  app,
  dsn: 'https://your-sentry-dsn@sentry.io/your-project-id',
  integrations: [
    new BrowserTracing({
      routingInstrumentation: Sentry.vueRouterInstrumentation(router),
      tracingOrigins: ['localhost', 'your-production-domain.com', /^https:\/\/api\.*/],
    }),
  ],
  // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
  // We recommend adjusting this value in production
  tracesSampleRate: 1.0,
})

app.use(router)
app.use(Antd)
app.mount('#app')
