import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import axios from '@/common/axios'
import initVant from '@/common/initVant'
import VueClipboard from 'vue-clipboard2'
import VCalendar from 'v-calendar';

// Use v-calendar & v-date-picker components
Vue.use(VCalendar, {
  componentPrefix: 'vc',  // Use <vc-calendar /> instead of <v-calendar />
});


Vue.config.productionTip = false

Vue.prototype.$store = store
Vue.prototype.$http = axios;
Vue.prototype.http = axios;
Vue.axios = axios;
Vue.http = axios;
Vue.prototype._ = _;
Vue.prototype.moment = moment;

Vue.use(initVant)
Vue.use(VueClipboard);

Vue.prototype.go = (url, args) => {
  router.push({path: url, query: args})
}

Vue.prototype.back = () => {
  router.back()
}

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
