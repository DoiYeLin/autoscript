<template>
  <div
    id="vr15_simplan"
    v-if="active"
    :class="{ 'slideInUp': value, 'slideOutDown': !value }"
    class="animated faster-plus"
  >
    <!-- @touchmove.prevent -->

    <van-nav-bar :fixed="true" :placeholder="true" :title="title">
      <template #left>
        <van-button type="primary" @click="close">关闭</van-button>
      </template>
      <template #right>
        <van-button type="primary" @click="stop">停止</van-button>
      </template>
    </van-nav-bar>
    <div class="header_action"></div>

    <div class="content">
      <div class="item"></div>
    </div>
  </div>
</template>

<script>
import * as api from "@/common/api";
import { mapGetters } from "vuex";
import {
  setLocalKey,
  getLocalKey,
  setSessionKey,
  removeSessionKey,
} from "@/common/utils";

export default {
  name: "sm_simplan",
  props: {
    value: {
      type: Object,
      default() {
        return null;
      },
    },
    gm: String,
    title: String,
  },
  data() {
    return {
      active: false,
    };
  },
  watch: {
    value: {
      handler() {
        if (!this.active && this.value) {
          this.active = true;
        } else {
          setTimeout(() => {
            this.active = false;
          }, 500);
        }
      },
      deep: true,
      immediate: true,
    },
    gm: {
      handler() {
        console.log("simplan " + this.gm);
      },
      deep: true,
      immediate: true,
    },
  },
  computed: {},
  mounted() {},
  created() {},
  methods: {
    save() {
      const toast = this.$toast.loading({
        duration: 0, // 持续展示 toast
        message: "处理中..",
      });

      let args = {
        id: this.form.id,
      };
      this.http.post(api.vr15_plan_doiu, args).then((res) => {
        toast.clear();
        if (res.code == 200) {
          this.$toast.success("成功");
          this.$emit("input", null);
          setTimeout(() => {
            this.$emit("success");
          }, 500);
        } else {
          this.$toast.fail(res.msg);
        }
        console.log(res);
      });
    },
    stop() {
      const toast = this.$toast.loading({
        duration: 0, // 持续展示 toast
        message: "处理中.."
      });
      this.http
        .post(api.vr15_plan_sim_action, { gm: this.gm, id: this.value.id, open: false })
        .then((res) => {
          toast.clear()
          if( res.code == 200){
            this.$toast.success("成功");
            this.$emit("stopDone");
          } else {
            this.$toast.fail(res.msg);
          }
        });
    },
    close() {
      this.$emit("input", null);
      setTimeout(() => {
        this.$emit("cancel");
      }, 500);
    },
  },
};
</script>

<style lang="less">
#vr15_simplan.hide {
  display: none;
}
#vr15_simplan {
  width: calc(100vw);
  height: calc(100vh);
  position: fixed;
  top: 0px;
  left: 0px;
  bottom: 0px;
  z-index: 1;
  text-align: center;
  background: @gray-9;

  .header_action {
    .van-button {
      width: 100%;
    }
  }
  .content {
  }
}
</style>