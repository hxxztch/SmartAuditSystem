 import { createApp } from 'vue'
 import pinia from '@/store'
 import router from '@/router'
 import ElementPlus from 'element-plus'
 import 'element-plus/dist/index.css'
 import zhCn from 'element-plus/es/locale/lang/zh-cn'
 import * as ElementPlusIconsVue from '@element-plus/icons-vue'
 import App from './App.vue'
 import '@/assets/styles/index.scss'
 import '@/router/permission-guard'
 
 const app = createApp(App)
 
 // 注册Element Plus图标
 for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
   app.component(key, component)
 }
 
 app.use(pinia)
 app.use(router)
 app.use(ElementPlus, { locale: zhCn })
 app.mount('#app')
