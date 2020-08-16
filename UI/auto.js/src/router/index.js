const prd = (process.env.NODE_ENV === 'production');
import Vue from 'vue';
import VueRouter from 'vue-router';
import { sync } from "vuex-router-sync";
import store from "../store";
import auth from '@/common/auth'
import app from "../App.vue";

Vue.use(VueRouter)

// const _import = require('./_import_' + process.env.NODE_ENV)

const routeConfig = [{
  path: '/login',
  component: () => import('@/views/login')
}, {
  // 首页
  path: '/',
  component: () => import('@/views/home')
},
{
  // 首页
  path: '/home',
  component: () => import('@/views/home')
},
{
  // 首页
  path: '/vr15',
  component: () => import('@/views/vr15')
}, {
  // 方案
  path: '/vr15_plan',
  component: () => import('@/views/vr15_plan')
}, {
  path: '*',
  component: import('@/components/404')
}]

const router = new VueRouter({
  routes: routeConfig,
  // mode: 'history',
  mode: 'hash',
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { x: 0, y: 0 }
    }
  }
})

sync(store, router)
const { state } = store;
const whites = ['/login', '/forget-pass'];

router.beforeEach((route, redirect, next) => {
  // NProgress.start()

  if (whites.indexOf(route.path) < 0) {
    let token = auth.getToken();
    if (token) {
      next()
      // if (!store.getters.user) {
      //   store.dispatch('getInfo').then(res => {
      //     store.dispatch('getArgs').then(res => {
      //       next()
      //     });
      //   })
      // } else {
      //   next()
      // }
    } else {
      next({
        path: 'login'
      });
    }
  } else {
    next();
  }


  // router.afterEach(() => {
  //     NProgress.done() // finish progress bar
  // })
});

export default router
