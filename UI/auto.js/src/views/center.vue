<template>
  <div id="center">
    <van-pull-refresh v-model="isLoading" @refresh="onRefresh" :head-height="100">
      <template #pulling="props">
        <div
          style="width:140px; margin: 0px auto;"
          :style="{ transform: `scale(${props.distance / 100})`}"
        >
          <i class="icon icontilong qlLoadIcon"></i>
        </div>
      </template>

      <!-- 释放提示 -->
      <template #loosing>
        <i class="icon icontilong qlLoadIcon loosing"></i>
      </template>

      <!-- 加载提示 -->
      <template #loading>
        <i class="icon icontilong qlLoadIcon qlLoading" :style="{ transform: ``}"></i>
      </template>
      <!-- 内容 -->
      <div class="view" v-if="user">
        <div class="header bottomShadow">
          <br />
          <van-row class="info">
            <van-col :span="18">账号：{{user.phoneNumber || user.account}}</van-col>
            <van-col :span="6" class="right">
              <i class="icon iconhaoping2" style="font-size:18px"></i>优质商家
            </van-col>
          </van-row>
          <van-row class="info">
            <van-col :span="24" @click="copyInvite">推荐码：{{user.recommendCode}} (点击复制)</van-col>
          </van-row>
          <van-row gutter="20" class="money">
            <van-col :span="13">
              余额:
              <b style="font-size:25px;">{{user.balance}}</b>
            </van-col>
            <van-col :span="11" class="right">
              返利:
              <b style="font-size:25px;">{{user.rakeBalance}}</b>
            </van-col>
          </van-row>
          <van-row type="flex"></van-row>
        </div>
        <div class="placeholder"></div>
        <div class="actions">
          <van-grid :column-num="3" :border="false">
            <van-grid-item
              icon=" icon iconjiagebaozhangfuxing"
              text="充值中心"
              @click="alert('请联系客服充值')"
            />
            <van-grid-item icon=" icon iconzanwuchongzhijilu" text="资金流水" to="balanceFlows" />
            <van-grid-item icon=" icon iconchaxun" text="任务统计" to="daytask" />
            <van-grid-item icon=" icon iconjiagetesou1" text="充值记录" to="rechargeRecord" />
            <van-grid-item icon=" icon iconshezhi" text="设置省份" to="setdirgeo" />
            <van-grid-item icon=" icon icontiaozheng" text="调整价格" to="setprice" />
            <van-grid-item icon=" icon iconxiaji" text="邀请好友" to="lower" />
            <van-grid-item icon=" icon iconshouyi1" text="返利流水" to="rebateRecord" />
            <van-grid-item icon=" icon iconship" text="修改密码" to="repass" />
            <van-grid-item icon=" icon iconkefu" text="联系客服" @click="linkcustomer" />
            <van-grid-item icon=" icon iconshiyongbangzhu" text="使用教程" to="tutorial" />
            <!-- <van-grid-item icon=" icon iconfankui" text="提个建议" to="suggest" /> -->
            <van-grid-item />
          </van-grid>
        </div>
        <div class="placeholder"></div>
        <van-button type="primary" round class="bigbtn" block @click="signOutActionSheet = true">退出</van-button>
      </div>
    </van-pull-refresh>
    <van-action-sheet
      v-model="signOutActionSheet"
      :actions="signOutActions"
      @select="signOutAction"
      cancel-text="取消"
    />
  </div>
</template>

<script>
import * as api from "@/common/api";
import { mapActions, mapGetters, mapMutations } from "vuex";
import { getBaseUrl } from "@/common/utils";
export default {
  name: "center",
  data() {
    return {
      isLoading: false,
      signOutActionSheet: false,
      signOutActions: [
        {
          name: "直接退出"
        },
        {
          name: "退出并清除登录账号",
          color: "red"
        }
      ]
    };
  },
  computed: {
    ...mapGetters({
      isApp: "isApp",
      user: "user",
      args: "args"
    })
  },
  created() {
    
  },
  activated() {
    console.log("activated");
    this.$store.dispatch("getInfo");
  },
  deactivated() {
    console.log("deactivated");
  },
  methods: {
    ...mapActions({
      doSignOut: "signOut"
    }),
    alert(msg) {
      this.$dialog.alert({
        title: "提示",
        message: msg
      });
    },
    copyInvite() {
      let inviteCode = "邀请码：" + this.user.recommendCode;
      let inviteUrl =
        "邀请链接：" +
        this.args.pc_domain +
        "register?code=" +
        this.user.recommendCode;
      let invite = inviteCode + "\n" + inviteUrl;
      this.copy(invite);
    },
    copy(arg) {
      this.$copyText(arg).then(
        e => {
          this.$toast.success("已复制");
        },
        e => {
          this.$notify({
            type: "warning",
            message: "复制失败"
          });
        }
      );
    },
    linkcustomer() {
      let cusArray = this.args.customer_contact.split(",");
      let cusJoin = "";
      cusArray.forEach(element => {
        cusJoin = cusJoin + element + "\n";
      });
      console.log(cusJoin);
      this.alert(cusJoin);
    },
    onRefresh() {
      this.$store.dispatch("getInfo").then(res => {
        setTimeout(() => {
          this.isLoading = false;
        }, 500);
      });
    },
    signOutAction(item, inx) {
      this.signOutActionSheet = false;
      let clear = inx == 0 ? false : true;
      this.$toast.loading("正在退出..");
      this.doSignOut(clear).then(res => {
        this.$toast.clear();
        this.$router.push("login");
      });
    }
  }
};
</script>
<style lang="less">
#center {
  .van-pull-refresh {
    background-color: rgb(53, 59, 72);
  }
  .view {
    background-color: rgb(245, 246, 250);
    .actions {
      background: white;
      .van-grid {
        background: white;
      }
      .icon {
        color: rgb(15, 185, 177);
      }
      .van-grid-item__text {
        font-size: 14px;
        color: black;
      }
    }
    .header {
      height: 120px;
      padding: 0px 10px 20px 10px;
      color: antiquewhite;
      font-size: 14px;
      background-color: rgb(53, 59, 72);
      .van-row {
        height: 30px;
        line-height: 30px;
      }
    }
  }
}
</style>