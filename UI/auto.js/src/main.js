import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import axios from '@/common/axios'
// import 'vant/lib/index.less';
import initVant from '@/common/initVant'
import _ from 'lodash'
import moment from 'moment'

Vue.config.productionTip = false

Vue.prototype.$store = store
Vue.prototype.$http = axios;
Vue.prototype.http = axios;
Vue.prototype._ = _;
Vue.prototype.moment = moment;


Vue.axios = axios;
Vue.http = axios;

Vue.use(initVant)

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
