// src/main.js
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import store from "@/store";
import plugins from "./plugins";
import { registerLayoutComponents } from "@/layouts/export";
import eventBus from "@/utils/eventBus";
import { title } from "@/config";
import { applyThemeBodyClassFromStorage } from "@/utils/applyThemeBodyClass";
import { mockXHR } from "@/utils/static";

const ignoreErrors = [
  "ResizeObserver loop completed with undelivered notifications",
  "ResizeObserver loop limit exceeded"
];

window.addEventListener('error', e => {
  let errorMsg = e.message;
  ignoreErrors.forEach(m => {
    if (errorMsg.startsWith(m)) {
      console.error(errorMsg);
      if (e.error) {
        console.error(e.error.stack);
      }

      // 隐藏开发环境 overlay 报错界面
      const resizeObserverErrDiv = document.getElementById(
        'webpack-dev-server-client-overlay-div'
      );
      const resizeObserverErr = document.getElementById(
        'webpack-dev-server-client-overlay'
      );
      if (resizeObserverErr) {
        resizeObserverErr.setAttribute('style', 'display: none');
      }
      if (resizeObserverErrDiv) {
        resizeObserverErrDiv.setAttribute('style', 'display: none');
      }
    }
  });
});


const app = createApp(App);

app.use(store);
app.use(router);
plugins(app);
registerLayoutComponents(app);

app.config.globalProperties.$eventBus = eventBus;
app.config.globalProperties.$baseTitle = title;
window.$eventBus = eventBus;
window.$baseTitle = title;

if (process.env.VUE_APP_USE_MOCK === "true") {
  mockXHR();
}
applyThemeBodyClassFromStorage();
app.mount("#vue-admin-better");
