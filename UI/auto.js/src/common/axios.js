import axios from "axios";
import qs from "qs";
import router from "../router";
import auth from "@/common/auth";
import store from '@/store'
import {
  getLocalKey
} from "../common/utils";
import { Notify } from 'vant';

// axios 配置
axios.defaults.timeout = 50000;
//axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8';
// 网页路径
axios.defaults.baseURL = '/';


// axios.defaults.headers.common['Cache-Control'] = 'no-cache';


const pending = {}
const CancelToken = axios.CancelToken
// key: 请求标识；isRequest 完成请求后也需要执行删除记录，所以添加此参数避免执行无用操作
const removePending = (key, isRequest = false) => {
  if (pending[key] && isRequest) {
    pending[key]('取消重复请求')
  }
  delete pending[key]
}


/**
 * config: 请求数据
 * isReuest: 请求拦截器中 config.url = '/users', 响应拦截器中 config.url = 'http://localhost:3000/users'，所以加上一个标识来计算请求的全路径
 */
const getRequestIdentify = (config, isReuest = false) => {
  let url = config.url
  if (config.url) {
    url = config.baseURL + config.url.substring(1, config.url.length)
  }
  return config.method === 'get' ? encodeURIComponent(url + JSON.stringify(config.params)) : encodeURIComponent(config.url +
    JSON.stringify(config.data))
}


//POST传参序列化
axios.interceptors.request.use((config) => {
  // 发送请求之前，拦截重复请求(即当前正在进行的相同请求)
  let requestData = getRequestIdentify(config, true)
  removePending(requestData, true)
  config.cancelToken = new CancelToken((c) => {
    pending[requestData] = c
  })


  let client = null;
  // let app = getLocalKey("app");
  let app = store.getters.isApp;
  if (app) {
    let appVersionCode = plus.runtime.versionCode;
    client = "APP_" + appVersionCode;
  } else {
    client = 'H5';
  }
  config.headers['Client'] = client;


  if (config.method === 'post') {
    // config.data._t = new Date().getTime();
    config.data = qs.stringify(config.data);
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

//返回状态判断
axios.interceptors.response.use(response => {

  // 把已经完成的请求从 pending 中移除
  let requestData = getRequestIdentify(response.config)
  removePending(requestData)
  if (response.headers['content-type'].indexOf('stream') > -1) {
    return response
  } else {
    let isret = true;
    if (response.data.code) {
      if (response.data.code == -1) {
        // 服务错误
        isret = false;
        Notify({ type: 'danger', message: '服务错误' });
      }
      // 已退出，重新登录
      if (response.data.code === -10) {
        isret = false;
        auth.logout();
        Notify({ type: 'warning', message: '登录已过期' });
        router.push({ path: 'login' });
      }
      if (response.data.code === 301) {
        isret = false;
        auth.logout();
        Notify({ type: 'danger', message: '非法访问' });
        router.push({ path: 'login' });
      }
    }
    if (isret) {
      return response.data;
    }
  }
},
  error => {
    if (error.response) {
      //全局ajax错误信息提示
      Notify({ type: 'danger', message: '网络错误' });
    }
    return Promise.reject(error);
  });

export function fetch(url, config = {
  method: 'get'
}) {
  return axios.request({
    ...config,
    url
  })
}

export default axios
