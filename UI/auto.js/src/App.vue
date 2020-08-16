<template>
  <div id="app">
    <transition mode="out-in" :enter-active-class="pageTransition.enter">
        <!-- include 匹配的时页面名，不是路由名，页面名在data里面定义 -->
        <keep-alive :include="keepLive">
          <router-view class="child-view" />
        </keep-alive>
    </transition>
    <footer-tabs v-if="footerTabs" ></footer-tabs>
    <van-number-keyboard safe-area-inset-bottom />
  </div>
</template>

<script>
import { mapState, mapActions, mapGetters, mapMutations } from "vuex";
import types from "@/store/mutations-types";
import * as utils from "@/common/utils";
import footerTabs from "@/components/footer-tabs.vue";

export default {
  name: "app",
  components: {
    footerTabs
  },
  data() {
    return {
      tabs: false,
      pageTransition: {
        enter: "animated fadeIn faster-plus",
        leave: "animated fadeOut faster-plus"
      },
      keepLive: ['home', 'plan', 'center', 'vr15'],
      pvs: ["/", "/home", "/plan", "/center"],
      statusBar: [{ theme: "#353B48", path: ["/center"] }],
      footerTabs: false,
      curRouter: null
    };
  },
  computed: {
    ...mapGetters({
      isApp: "isApp",
      token: "token"
    })
  },
  watch: {
    $route(to, from) {
      this.curRouter = to.path;
      let pv = this._.indexOf(this.pvs, to.path);
      if (pv > -1) {
        this.footerTabs = true;
      } else {
        let timeout = setTimeout(() => {
          this.footerTabs = false;
          clearTimeout(timeout);
        }, 150);
      }
      if (this.isApp) {
        let sbar = this._.filter(
          this.statusBar,
          item => item.path.indexOf(to.path) > -1
        );
        let timeout = null;
        if (sbar.length > 0) {
          timeout = setTimeout(() => {
            plus.navigator.setStatusBarStyle("light");
            plus.navigator.setStatusBarBackground(sbar[0].theme);
            clearTimeout(timeout);
          }, 220);
        } else {
          timeout = setTimeout(() => {
            plus.navigator.setStatusBarStyle("dark");
            plus.navigator.setStatusBarBackground("#FDFEFE");
            clearTimeout(timeout);
          }, 155);
        }
      }
    }
  },
  created() {
    let _this = this;
    const plusReady = () => {
      _this.toggleApp(true);
      // 是否支持沉浸式
      let isImmersedStatusbar = plus.navigator.isImmersedStatusbar();
      // this.$notify({ type: "warning", message: isImmersedStatusbar+''});
      plus.navigator.setStatusBarBackground("#FDFEFE");

      var immersed = 0;
      var ms = /Html5Plus\/.+\s\(.*(Immersed\/(\d+\.?\d*).*)\)/gi.exec(
        navigator.userAgent
      );
      if (ms && ms.length >= 3) {
        // 当前环境为沉浸式状态栏模式
        immersed = parseFloat(ms[2]); // 获取状态栏的高度
      }

      // this.$http.post(api.checkAppVersion).then(res => {
      //     let upgInfo = JSON.parse(res.object);
      //     if (upgInfo.versionCode > plus.runtime.versionCode) {
      //         this.$alert(upgInfo.content, upgInfo.title, {
      //             confirmButtonText: '更新',
      //             confirmButtonClass: 'danger'
      //         }).then(({value}) => {
      //             plus.runtime.openURL(upgInfo.url);
      //         })
      //     }
      // })

      // 移除滚动条
      var wv = plus.webview.currentWebview();
      wv.setStyle({
        scrollIndicator: "none"
      });

      // Android处理返回键
      let quit = false;
      window.plus.key.addEventListener(
        "backbutton",
        function() {
          if (utils.getSessionKey("cancelBack")) {
            return false;
          }
          if (_this.curRouter) {
            if (
              _this.pvs.indexOf(_this.curRouter) > -1 ||
              _this.curRouter == "/login"
            ) {
              if (quit) {
                window.plus.runtime.quit();
              } else {
                _this.$toast({
                  message: "再按一次退出",
                  position: "bottom",
                  duration: 800
                });
                quit = true;
                let quitTime = setTimeout(() => {
                  quit = false;
                }, 800);
              }
            } else {
              _this.$router.go(-1);
            }
          }
        },
        false
      );
    };
    if (window.plus) {
      plusReady();
    } else {
      document.addEventListener("plusready", plusReady, false);
    }
  },
  methods: {
    ...mapMutations({
      toggleApp: types.ISAPP
    })
  }
};
</script>

<style lang="less">
@import "assets/css/style";
</style>
