<template>
  <div id="index">
    <van-nav-bar :fixed="true" :placeholder="true" title="Auto.Js">
      <template #left>
        <!-- <van-icon class="icon iconellipsis-vertical" /> -->
      </template>
      <template #right>
        <van-button icon=" icon icontuichu2" type="primary" @click="signout"></van-button>
      </template>
    </van-nav-bar>
    <van-grid>
      <van-grid-item icon=" icon iconvr" style="color: rgb(30, 144, 255)" text="Vr1.5" to="/vr15" />
      <van-grid-item icon=" icon iconlaohu1" style="color: rgb(255, 165, 2)"  text="龙虎斗" />
      <van-grid-item icon=" icon icontianjia1" text="虚位以待.." />
      <van-grid-item icon=" icon icontianjia1" text="虚位以待.." />
    </van-grid>
  </div>
</template>

<script>
import * as api from "@/common/api";
import { mapState, mapActions, mapGetters, mapMutations } from "vuex";

export default {
  name: "home",
  data() {
    return {};
  },
  computed: {
    ...mapGetters({
      isApp: "isApp",
      user: "user"
    })
  },
  filters: {},
  created() {},
  activated() {
    let _this = this;
  },
  methods: {
    signout() {
      this.http.post(api.signout).then(res => {
        if (res.code == 200) {
          this.$router.push({ path: "login" });
          console.log("退出");
        }
      });
    }
  }
};
</script>
