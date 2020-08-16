<template>
  <div class="scan-code animated faster" @touchmove.prevent :class="{'zoomIn': active, 'zoomOut': !active}">
    <div class="scan-header">{{title}}</div>
    <div id="scanBox"></div>
    <div class="actions">
      <van-row type="flex" justify="space-around">
        <van-col>
          <van-icon class="icon iconback-m" @click="close" />
          <p>返回</p>
        </van-col>
        <!-- <van-col>
          <van-icon class="icon iconshandian" :class="{'green': isLight}" @click="openLight" />
          <p>灯光</p>
        </van-col> -->
        <van-col v-if="!autoClose">
          <van-icon
            class="icon iconautomatic"
            :class="{'green': !banAutoClose}"
            @click="innerAutoCloseAction"
          />
          <p>连续扫码</p>
        </van-col>
      </van-row>
    </div>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import {
  setLocalKey,
  getLocalKey,
  setSessionKey,
  removeSessionKey
} from "@/common/utils";

export default {
  name: "scanCode",
  props: {
    title: String,
    autoClose: {
      type: Boolean,
      default() {
        return true;
      }
    }
  },
  data() {
    return {
      active: true,
      scanPlus: null,
      isLight: false,
      // 禁止自动关闭，当 autoClose = false 是启用
      banAutoClose: false
    };
  },
  computed: {
    ...mapGetters({
      isApp: "isApp"
    })
  },
  mounted() {
    if (this.isApp) {
      this.initScanPlus();
    }
    //  else {
    //   this.$notify({ type: "warning", message: "请下载App扫码" });
    //   this.back();
    // }
  },
  created() {
    if (!this.autoClose) {
      // 扫码后自动关闭 缓存
      let sac = getLocalKey("scan_ban_auto_close");
      if (sac == "true") {
        this.banAutoClose = true;
      }
    }
  },
  methods: {
    initScanPlus() {
      // 拦截返回键
      setSessionKey("cancelBack", "1");

      let _this = this;

      this.$toast("加载中..");
      let timeout = setTimeout(() => {
        this.$toast.clear();
        plus.navigator.setStatusBarStyle("light");
        plus.navigator.setStatusBarBackground("#000");
        this.scanPlus = new plus.barcode.Barcode("scanBox", [plus.barcode.QR], {
          frameColor: "#CCFF33",
          scanbarColor: "#CCFF33",
          top: "100px",
          left: "0px",
          width: "100%",
          height: "500px",
          position: "absolute"
        });
        this.scanPlus.onmarked = (type, result) => {
          if (type == plus.barcode.QR) {
            if (_this.banAutoClose) {
              this.close();
            } else {
              setTimeout(() => {
                this.scanPlus.start();
              }, 1500);
            }
            this.$emit("done", result);
          } else {
            this.$notify({ type: "warning", message: "请扫描二维码" });
          }
        };
        clearTimeout(timeout);
        this.scanPlus.start();
      }, 800);
    },
    close() {
      if (this.scanPlus) {
        this.scanPlus.cancel();
        this.scanPlus.close();
        this.active = false;
        removeSessionKey("cancelBack");
        plus.navigator.setStatusBarBackground("#FDFEFE");
        plus.navigator.setStatusBarStyle("dark");
      }
      let timeout = setTimeout(() => {
        clearTimeout(timeout);
        this.$emit("close");
      }, 600);
    },
    openLight() {
      this.isLight = !this.isLight;
      this.scanPlus.setFlash(this.isLight);
    },
    innerAutoCloseAction() {
      this.banAutoClose = !this.banAutoClose;
      // if(this.banAutoClose) {
      //   this.$notify({ type: "danger", message: "自动关闭" });
      // } else {
      //   this.$notify({ type: "success", message: "连续扫码" });
      // }
      setLocalKey("scan_ban_auto_close", this.banAutoClose);
    }
  }
};
</script>

<style lang="less">
.scan-code {
  width: calc(100vw);
  height: calc(100vh);
  position: fixed;
  top: 0px;
  left: 0px;
  z-index: 9;
  text-align: center;
  background: black;
  overflow: hidden;
  .scan-header {
    height: calc(20vh);
    line-height: calc(20vh);
    font-size: 18px;
    font-weight: bold;
    color: whitesmoke;
  }
  #scanBox {
    width: calc(100vw);
    height: calc(60vh);
    z-index: 3;
  }
  .actions {
    width: calc(100vw);
    height: calc(20vh);
    .van-row {
      height: 100%;
      .van-col {
        height: 100%;
        padding-top: calc(5vh);
        .icon {
          color: white;
          font-size: 35px;
        }
        .green {
          color: @fluorescent;
        }
        .red {
          color: @red;
        }
        p {
          margin: 0px;
          padding: 0px;
          color: rgb(247, 241, 227);
        }
      }
    }
  }
}
</style>