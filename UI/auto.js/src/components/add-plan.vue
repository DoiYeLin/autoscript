<template>
  <div
    id="vr15_addplan"
    v-if="active"
    :class="{ 'slideInUp': value, 'slideOutDown': !value }"
    class="animated faster-plus"
  >
    <!-- @touchmove.prevent -->

    <!-- <van-nav-bar :fixed="true" :placeholder="true" :title="title">
      <template #left>
      </template>
      <template #right>
      </template>
    </van-nav-bar>-->
    <div class="header_action">
      <van-row>
        <van-col span="4" v-for="(item, inx) in plans.keys" :key="inx">
          <van-button
            size="small"
            @click="planKeyChange(item.key)"
            :type="form.key == item.key ? 'info' : 'warning'"
          >{{item.name}}</van-button>
        </van-col>
      </van-row>
      <van-field v-model="form.name" center clearable label="方案标签" placeholder border />
      <van-field v-model="form.odds" center clearable label="赔率" placeholder border />
      <van-field v-model="form.accNum" center clearable label="执行账号数量" placeholder border />
      <van-field
        v-model="plans.repeatNum"
        center
        clearable
        label="投注次数"
        placeholder
        border
        use-button-slot
      >
        <van-button slot="button" size="small" type="primary" @click="generatePlans">确定</van-button>
      </van-field>
    </div>

    <div class="content">
      <div class="item" v-for="(item, index) in form.plans" :key="index">
        <van-cell :value="'投注：' + (index+1)">
          <template #extra>
            <button
              style="color: red; width: 40px; height: 25px; font-size: 12px; line-height: 10px;"
              @click="removeStep(index, item)"
            >删除</button>
          </template>
        </van-cell>
        <van-row>
          <van-col span="12">
            <van-field
              v-model="item.money"
              center
              clearable
              label="投注金额"
              placeholder
              border
              use-button-slot
            />
          </van-col>
          <van-col span="12">
            <van-field
              v-model="item.winJump"
              center
              clearable
              label="命中跳转"
              placeholder
              border
              use-button-slot
            />
          </van-col>
        </van-row>
        <van-row>
          <van-col span="12">
            <van-field
              v-model="item.failJump"
              center
              clearable
              label="未中跳转"
              placeholder
              border
              use-button-slot
            />
          </van-col>
          <van-col span="12" style="line-height: 40px">
            未中跳转方案
            <van-button
              type="primary"
              size="mini"
              @click="failJumpPlan(index)"
            >{{item.failJumpPlan ? plans.keys[item.failJumpPlan].name : '选择'}}</van-button>
          </van-col>
        </van-row>
      </div>
    </div>

    <div class="footer_action">
      <van-row>
        <van-col span="8">
          <van-button @click="close" type="primary" style="background: rgb(87, 75, 144)">取消</van-button>
        </van-col>
        <van-col span="8">
          <van-button @click="clear" type="primary" style="background: #e14ba0">清空</van-button>
        </van-col>
        <van-col span="8">
          <van-button @click="save" type="primary" style="background: rgb(48, 51, 107)">保存</van-button>
        </van-col>
      </van-row>
    </div>

    <van-popup v-model="failSwitchPlanView" position="bottom" :style="{ height: '30%' }">
      <van-picker
        title="未中切换方案"
        show-toolbar
        :columns="plans.keys"
        value-key="name"
        @confirm="failSwitchPlanDone"
      />
    </van-popup>
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
  name: "add_plan",
  props: {
    value: {
      type: Boolean,
      default() {
        return false;
      },
    },
    gm: String,
    title: String,
    plan: Object,
    autoClose: {
      type: Boolean,
      default() {
        return true;
      },
    },
  },
  data() {
    return {
      active: false,
      failSwitchPlanView: false,
      plans: {
        keys: [],
        repeatNum: 5,
        chsInx: null,
      },
      form: {
        key: 0,
        name: null,
        odds: null,
        plans: [],
        accNum: null,
      },
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
        console.log("addplan " + this.gm);
      },
      deep: true,
      immediate: true,
    },
    plan: {
      handler() {
        if (this.plan) {
          console.log(this.plan.accNum);
          this.plans.repeatNum = null;
          this.form.id = this.plan.id;
          this.form.key = this.plan.key;
          this.form.name = this.plan.name;
          this.form.odds = this.plan.odds;
          this.form.accNum = this.plan.accNum;
          this.form.plans = this.plan.step;
        } else {
          this.form = {
            key: 0,
            name: null,
            odds: null,
            plans: [],
            accNum: null,
          };
          this.plans.repeatNum = 5;
        }
      },
      deep: true,
      immediate: true,
    },
  },
  computed: {},
  mounted() {},
  created() {
    this.loadplks();
  },
  methods: {
    loadplks() {
      this.http.post(api.vr15_plan_plks, { gm: this.gm }).then((res) => {
        this.plans.keys = res.data;
      });
    },
    planKeyChange(key) {
      this.form.key = key;
    },
    generatePlans() {
      if (!this.plans.repeatNum) {
        return false;
      }
      for (let i = 0; i < this.plans.repeatNum; i++) {
        let failjump = i + 2;
        if (failjump > this.plans.repeatNum) {
          failjump = 1;
        }
        this.form.plans.push({
          money: null,
          winJump: 1,
          failJump: failjump,
          failJumpPlan: null,
        });
      }
      this.plans.repeatNum = null;
    },
    failJumpPlan(inx) {
      this.plans.chsInx = inx;
      this.failSwitchPlanView = true;
    },
    failSwitchPlanDone(value, index) {
      console.log(this.form.plans[this.plans.chsInx]);
      this.form.plans[this.plans.chsInx].failJumpPlan = value.key;
      this.failSwitchPlanView = false;
    },
    removeStep(stepInx, step) {
      this.form.plans.splice(stepInx, 1);
    },
    save() {
      if (!this.form.plans || this.form.plans.length == 0) {
        this.$toast.fail("没有创建方案步骤");
        return false;
      }
      const toast = this.$toast.loading({
        duration: 0, // 持续展示 toast
        message: "处理中..",
      });

      let args = {
        id: this.form.id,
        gm: this.gm,
        key: this.form.key,
        name: this.form.name,
        odds: this.form.odds,
        accNum: this.form.accNum,
        plans: decodeURIComponent(JSON.stringify(this.form.plans)),
      };
      this.http.post(api.vr15_plan_doiu, args).then((res) => {
        toast.clear();
        if (res.code == 200) {
          this.$toast.success("成功");
          this.$emit("input", false);
          setTimeout(() => {
            this.$emit("success");
          }, 500);
        } else {
          this.$toast.fail(res.msg);
        }
        console.log(res);
      });
    },
    close() {
      this.$emit("input", false);
      // this.active = false;
      setTimeout(() => {
        this.$emit("cancel");
      }, 500);
    },
    clear() {
      this.plans.repeatNum = 5;
      this.form = {
        key: 0,
        name: null,
        plans: [],
      };
    },
  },
};
</script>

<style lang="less">
#vr15_addplan.hide {
  display: none;
}
#vr15_addplan {
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
    input {
      padding-left: 10px;
      background: #2d2e30;
    }
  }
  .content {
    width: calc(100vw);
    height: calc(70vh);
    overflow-y: scroll;
    background: #2d2e30 !important;
    .item {
      .van-row {
        background: #2d2e30 !important;
      }
      .van-cell {
        margin-bottom: 8px;
        background: #2d2d36;
        input {
          background: #8e9399;
          color: white;
          padding: 0px 5px;
        }
      }
      .van-cell::after {
        border: 0px;
      }
      * {
        color: #a6a7a9;
      }
    }
  }
  .footer_action {
    width: 100%;
    height: 40px;
    background-color: @gray-9;
    position: fixed;
    bottom: 0px;
    left: 0px;
    text-align: center;

    .van-col {
      height: 40px;
      line-height: 40px;

      .van-button {
        width: 100%;
        color: white;
      }
    }
  }
}
</style>