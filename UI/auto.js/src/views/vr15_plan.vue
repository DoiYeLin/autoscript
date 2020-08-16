<template>
  <div id="vr15_plan">
    <van-nav-bar :fixed="true" :placeholder="true" :title="title">
      <template #left>
        <van-icon name="arrow-left" @click="back" />
      </template>
      <template #right>
        <van-button type="primary" @click="showPlanView = true">添加方案</van-button>
      </template>
    </van-nav-bar>

    <div class="content">
      <van-row v-for="item in list" :key="item.id" class="plan">
        <van-col span="12">
          <van-cell :title="item.name" is-link @click="see(item)">
            <template #right-icon></template>
          </van-cell>
        </van-col>
        <van-col span="12">
          <van-button type="warning" size="small" v-if="!item.simulate" @click="sim(item)">模拟</van-button>
          <van-button
            type="warning"
            size="small"
            v-else
            @click="seeSimPlan = item"
            style="background-color:#ed86a5"
          >模拟中</van-button>
          <van-button type="info" size="small" v-if="!item.runing" @click="run(item)">运行</van-button>
          <van-button
            type="info"
            size="small"
            v-else
            @click="run(item)"
            style="background-color:#498f59"
          >运行中</van-button>
          <van-button type="danger" size="small" @click="del(item)">删除</van-button>
        </van-col>
      </van-row>
    </div>
    <add-plan
      v-if="gm"
      v-model="showPlanView"
      :gm="gm"
      :title="title"
      :plan="curPlan"
      @success="apsuc"
      @cancel="cancel"
    />
    <sm-simplan v-if="gm" v-model="seeSimPlan" :gm="gm" @stopDone="simStopDone" />

    <van-popup v-model="showSimChilds" round closeable position="bottom" :style="{ height: '50%' }">
      <div style="width: 90%; margin:0px auto; padding: 1rem" v-if="simChilds">
        <h2>投注方式</h2>
        <p>{{simChilds.desc}}</p>
        <div v-for="(item, inx) in simChilds.picks" :key="inx">
          <p>{{item.label}}</p>
          <van-checkbox-group v-model="picks" direction="horizontal">
            <van-checkbox
              v-for="simc in item.items"
              :key="simc.name"
              :name="simc.name"
            >{{simc.label}}</van-checkbox>
          </van-checkbox-group>
        </div>
        <br />
        <van-button type="primary" block @click="simAction(null)">确定</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script>
import * as api from "@/common/api";
import addPlan from "@/components/add-plan";
import smSimplan from "@/components/sm-simplan";
import { mapState, mapActions, mapGetters, mapMutations } from "vuex";

export default {
  name: "vr15_plan",
  components: {
    addPlan,
    smSimplan,
  },
  data() {
    return {
      gm: null,
      title: null,
      list: [],
      showPlanView: false,
      curPlan: null,
      showSimChilds: false,
      simChilds: null,
      picks: [],
      seeSimPlan: null,
    };
  },
  computed: {
    ...mapGetters({
      isApp: "isApp",
      user: "user",
    }),
  },
  filters: {},
  beforeCreate() {},
  created() {
    this.gm = this.$route.query.gm;
    if (this.gm == "sm") {
      this.title = "双面玩法 投注方案";
    } else {
      this.title = "标准龙虎 投注方案";
    }
    this.load();
  },
  activated() {
    let _this = this;
  },
  methods: {
    load() {
      this.http.post(api.vr15_plan, { gm: this.gm }).then((res) => {
        console.log(res);
        if (res.code == 200) {
          this.list = res.data;
        } else {
          this.$toast.fail(res.msg || "加载失败");
        }
      });
    },
    see(item) {
      this.curPlan = item;
      this.showPlanView = true;
      console.log("see");
    },
    sim(item) {
      this.http.post(api.vr15_plan_plcks, { key: item.key }).then((res) => {
        if (res.code == 200 && res.data) {
          console.log(res.data);
          this.showSimChilds = true;
          this.simChilds = res.data;
          this.simChilds.pid = item.id;
        } else {
          this.simAction(item.id);
        }
      });
    },
    simAction(_id) {
      let args = {
        gm: this.gm,
        open: true,
      };
      if (_id) {
        args.id = _id;
      } else {
        args.id = this.simChilds.pid;
        args.picks = this.picks.join(",");
      }

      this.http.post(api.vr15_plan_sim_action, args).then((res) => {
        if (res.code == 200) {
          this.showSimChilds = false;
          this.load();
        }
      });
    },
    simStopDone() {
      this.load();
    },
    run(item) {},
    del(item) {
      this.$dialog.confirm({ title: "删除？" }).then((res) => {
        this.http.post(api.vr15_plan_del, { id: item.id }).then((res) => {
          if (res.code == 200) {
            this.load();
            this.$toast.success("成功");
          } else {
            this.$toast.fail(res.msg || "失败");
          }
        });
        console.log("del");
      });
    },
    apsuc() {
      this.load();
    },
    cancel() {
      this.curPlan = null;
      console.log("已取消");
    },
  },
};
</script>
<style lang="less">
#vr15_plan {
  .content {
    .van-row {
      background: white;
    }
    .van-row.plan {
      margin-bottom: 10px;
    }
    .van-col:last-child {
      line-height: 40px;
    }
  }
}
</style>
