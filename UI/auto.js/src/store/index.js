import Vue from 'vue'
import Vuex from 'vuex'
import types from "./mutations-types"
import * as api from '@/common/api.js'
import auth from '@/common/auth.js'

Vue.use(Vuex)

export default new Vuex.Store({
  getters: {
    isApp: state => state.isApp,
    token: state => state.token,
    user: state => state.user,
    args: state => state.args
  },
  state: {
    isApp: false,
    token: false,
    user: null,
    args: null,
  },
  mutations: {
    [types.ISAPP](state, isApp) {
      state.isApp = isApp;
    },
    [types.TOKEN](state, token) {
      state.token = token;
    },
    [types.USER](state, user) {
      state.user = user;
    },
    [types.ARGS](state, args) {
      state.args = args;
    }
  },
  actions: {
    /**
     * 
     * @param {登录} store 
     * @param {*} form 
     */
    signIn(store, form) {
      let args = form || auth.getRemember();
      return new Promise((resolve, reject) => {
        if (args) {
          Vue.axios.post(api.login, args).then(res => {
            if (res.code == 200) {
              auth.remember(args);
              auth.login({
                ...res.data
              });
              store.commit(types.TOKEN, res.data);
            }
            resolve(res);
          })
        } else {
          reject("没有账号信息");
        }
      })
    }
  }
})
