<template>
  <div id="login">
    <div class="header">
      <div class="block1"></div>
      <!-- <div class="logo animated bounceInDown">
        <i class="icon icontilong"></i>
      </div> -->
      <div class="title animated rubberBand delay-1s">
        <div class="">Auto.Js</div>
      </div>
    </div>
    <van-form @submit="login" class="animated fadeIn">
      <van-field
        v-model="form.account"
        name="用户名"
        placeholder="用户名"
        left-icon=" icon iconzhanghao2"
        :rules="[{ required: true, message: '请填写用户名' }]"
      />
      <input type="password" style="display:none;width:0;height:0;" />
      <van-field
        v-model="form.password"
        type="password"
        name="密码"
        placeholder="密码"
        left-icon=" icon iconmima"
        :rules="[{ required: true, message: '请填写密码' }]"
      />

      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">登&nbsp;&nbsp;&nbsp;&nbsp;录</van-button>
      </div>
    </van-form>
  </div>
</template>

<script>
import { mapActions, mapGetters, mapMutations } from "vuex";
import auth from "@/common/auth";

export default {
  data() {
    return {
      form: {
        account: 'system',
        password: 'q2n1B$?(}KyT05iY%Ci]b|Z^8xLthfc0-18VRCz1',
        remember: false
      }
    };
  },
  created() {
    let remember = auth.getRemember();
    if (remember) {
      this.form = remember;
    }
  },
  activated() {
    console.log("activated");
  },
  methods: {
    ...mapActions({
      signIn: "signIn"
    }),
    login() {
      this.$toast.loading("登录中..");
      this.signIn(this.form).then(res => {
        this.$toast.clear();
        if (res.code == 200) {
          this.$router.push({
            path: "home"
          });
        } else {
          this.$notify({ type: "warning", message: res.msg });
        }
      });
    }
  }
};
</script>

<style lang="less">
#login {
  // height: calc(100vh);
  // background: white;
  
}
</style>
