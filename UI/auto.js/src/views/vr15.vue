<template>
  <div id="vr15">
    <van-nav-bar :fixed="true" :placeholder="true" title="Vr1.5">
      <template #left>
        <van-icon name="arrow-left" @click="back" />
      </template>
      <template #right>
        <!-- <van-button type="info" @click="openAlign">校准</van-button> -->
        <van-button type="primary" @click="onSort">排序</van-button>
        <van-button type="info" icon="arrow-left" @click="chsDatProv"></van-button>
        <vc-date-picker
          v-model="chsDateVal"
          :popover="{ placement: 'bottom', visibility: 'click' }"
          color="red"
          is-dark
          @dayclick="onChsDate"
        >
          <van-button type="info">{{chsDateText}}</van-button>
        </vc-date-picker>
        <van-button type="info" icon="arrow" @click="chsDatNext"></van-button>
        <van-button icon=" icon icondaochu1" type="warning" @click="showExport = true"></van-button>
      </template>
    </van-nav-bar>

    <div class="content">
      <van-row class="issboxheader">
        <van-col style="width: 100px">期号</van-col>
        <van-col style="width: 60px">开奖</van-col>
        <div v-if="gameMode == 'lh'">
          <van-col
            style="width: 60px"
            v-for="(item, index) in lh"
            :key="index"
            v-show="item.show"
          >{{item.label}}</van-col>
        </div>
        <div v-if="gameMode == 'sm'">
          <van-col
            v-for="(item, index) in sm"
            :key="index"
            :style="index == 0 ? 'width: 200px': 'width: 100px'"
            v-show="item.show"
          >{{item.label}}</van-col>
        </div>
      </van-row>
      <div
        class="issbox"
        :class="{'all-active': smbdsactive == 0,  'bs-active': smbdsactive == 1,'ds-active': smbdsactive == 2 }"
      >
        <van-list v-model="loading" :finished="finished" finished-text="没有更多了">
          <van-row v-for="(item, inx) in numbers" :key="inx">
            <van-col style="width: 100px">{{item.issue}}</van-col>
            <van-col style="width: 60px">{{item.number}}</van-col>
            <!-- 标准龙虎 -->
            <div v-if="gameMode == 'lh'">
              <van-col
                style="width: 60px"
                v-for="(_lh, index) in lh"
                :key="index"
                v-show="_lh.show"
                v-html="item.lh[_lh.key]"
              ></van-col>
            </div>
            <!-- 双面玩法 -->
            <div v-if="gameMode == 'sm'">
              <van-col
                v-for="(_sm, index) in sm"
                :key="index"
                :style="index == 0 ? 'width: 200px': 'width: 100px'"
                v-show="_sm.show"
                v-html="item.sm[_sm.key]"
              ></van-col>
            </div>
          </van-row>
        </van-list>
      </div>
    </div>
    <div v-if="gameMode == 'sm'" class="smactive">
      <button @click="smbdsactive = 0">全部</button>
      <button @click="smbdsactive = 1">大小</button>
      <button @click="smbdsactive = 2">单双</button>
    </div>
    <div class="action">
      <van-row>
        <van-col span="4">
          <van-button
            @click="showGameMode = !showGameMode"
            style="background:rgb(75, 101, 132);"
            type="primary"
          >{{showGameMode ? '关闭' : '选择'}}</van-button>
        </van-col>
        <van-col span="6">
          <van-button
            @click="gameMode = 'lh'"
            type="primary"
            style="background: rgb(87, 75, 144)"
          >标准龙虎</van-button>
        </van-col>
        <van-col span="6">
          <van-button
            @click="gameMode = 'sm'"
            type="primary"
            style="background: rgb(48, 51, 107)"
          >双面玩法</van-button>
        </van-col>
        <van-col span="4">
          <van-button @click="reload" style="background: rgb(247, 143, 179);">刷新</van-button>
        </van-col>
        <van-col span="4">
          <van-button
            @click="go('/vr15_plan', {'gm': gameMode})"
            style="background: rgb(255, 99, 72);"
          >方案</van-button>
        </van-col>
      </van-row>
    </div>
    <van-popup
      v-model="showGameMode"
      :overlay="false"
      position="left"
      :duration="0.1"
      :lock-scroll="false"
    >
      <div v-if="gameMode == 'lh'">
        <div v-for="(item, index) in lh" :key="index">
          <van-button
            :type="item.show ? 'warning' : 'danger'"
            @click="item.show = !item.show"
          >{{item.label}}</van-button>
        </div>
      </div>
      <div v-if="gameMode == 'sm'">
        <div v-for="(item, index) in sm" :key="index">
          <van-button
            :type="item.show ? 'warning' : 'danger'"
            @click="item.show = !item.show"
          >{{item.label}}</van-button>
        </div>
      </div>
    </van-popup>

    <van-popup
      v-model="showCountMode"
      :overlay="false"
      position="right"
      :duration="0.1"
      :lock-scroll="false"
      style="z-index: 0"
    >
      <div v-for="(value, name) in counts" :key="name" style="padding: 10px">
        <div>
          <span style="padding-right: 10px">{{ name.replace("_", "") }}</span>
          <span>:</span>
          <span style="padding-left: 10px">{{value}}</span>
        </div>
      </div>
    </van-popup>

    <van-popup
      v-model="showExport"
      position="bottom"
      :duration="0.1"
      :lock-scroll="false"
      :style="{ height: '30%' }"
      round
    >
      <van-divider :style="{ padding: '0 16px' }">导出条数</van-divider>
      <van-form>
        <van-field v-model="exportXls.start" type="number" label="开始行" />
        <van-field v-model="exportXls.limit" type="number" label="截止行" />
        <br />
        <van-button
          type="primary"
          block
          color="linear-gradient(to right, #4bb0ff, #6149f6)"
          @click="exportDone"
        >导出</van-button>
      </van-form>
    </van-popup>

    <!-- <van-popup v-model="showChsDate" position="bottom">
      <van-datetime-picker v-model="chsDateVal" @confirm="onChsDate" type="date" />
    </van-popup>-->
  </div>
</template>

<script>
import * as api from "@/common/api";
import DatePicker from "v-calendar/lib/components/date-picker.umd";
import { mapState, mapActions, mapGetters, mapMutations } from "vuex";

export default {
  name: "vr15",
  components: {
    DatePicker,
  },
  data() {
    return {
      isDesc: true,
      loading: false,
      finished: false,
      showGameMode: false,
      showCountMode: true,
      showExport: false,
      showChsDate: false,
      chsDateVal: null,
      chsDateText: null,
      counts: null,
      page: 1,
      orgnums: [],
      numbers: [],
      gameMode: "sm",
      // playMode: "lh",
      lh: [
        { key: "wq", label: "万千", show: true },
        { key: "wb", label: "万百", show: true },
        { key: "ws", label: "万十", show: true },
        { key: "wg", label: "万个", show: true },
        { key: "qb", label: "千百", show: true },
        { key: "qs", label: "千十", show: true },
        { key: "qg", label: "千个", show: true },
        { key: "bs", label: "百十", show: true },
        { key: "bg", label: "百个", show: true },
        { key: "sg", label: "十个", show: true },
      ],
      sm: [
        { key: "zh", label: "总和", show: true },
        { key: "w", label: "万位", show: true },
        { key: "q", label: "千位", show: true },
        { key: "b", label: "百位", show: true },
        { key: "s", label: "十位", show: true },
        { key: "g", label: "个位", show: true },
      ],
      smbdsactive: 0,
      exportXls: {
        start: 0,
        limit: 1000,
      },
    };
  },
  computed: {
    ...mapGetters({
      isApp: "isApp",
      user: "user",
    }),
  },
  filters: {},
  created(arg) {
    let day = this.moment().hour();
    if (day > 8) {
      this.chsDateVal = new Date();
    } else {
      this.chsDateVal = this.moment().subtract(1, "days").toDate();
    }
    this.chsDateText = this.moment(this.chsDateVal).format("YYYY/MM/DD");
    // this.interval();
    this.loadc();
  },
  activated() {
    let _this = this;
  },
  methods: {
    signout() {
      this.http.post(api.signout).then((res) => {
        if (res.code == 200) {
          this.$router.push({ path: "login" });
          console.log("退出");
        }
      });
    },
    analysisLh(numbers) {
      numbers.forEach((item) => {
        let number = item.number;
        let lh = {};
        this.lh.forEach((lhitem) => {
          let res = -1;
          if (lhitem.key == "wq") {
            if (parseInt(number[0]) > parseInt(number[1])) {
              res = 0;
            } else if (parseInt(number[0]) < parseInt(number[1])) {
              res = 1;
            } else {
              res = 2;
            }
          }
          if (lhitem.key == "wb") {
            if (parseInt(number[0]) > parseInt(number[2])) {
              res = 0;
            } else if (parseInt(number[0]) < parseInt(number[2])) {
              res = 1;
            } else {
              res = 2;
            }
          }

          if (lhitem.key == "ws") {
            if (parseInt(number[0]) > parseInt(number[3])) {
              res = 0;
            } else if (parseInt(number[0]) < parseInt(number[3])) {
              res = 1;
            } else {
              res = 2;
            }
          }
          if (lhitem.key == "wg") {
            if (parseInt(number[0]) > parseInt(number[4])) {
              res = 0;
            } else if (parseInt(number[0]) < parseInt(number[4])) {
              res = 1;
            } else {
              res = 2;
            }
          }
          if (lhitem.key == "qb") {
            if (parseInt(number[1]) > parseInt(number[2])) {
              res = 0;
            } else if (parseInt(number[1]) < parseInt(number[2])) {
              res = 1;
            } else {
              res = 2;
            }
          }
          if (lhitem.key == "qs") {
            if (parseInt(number[1]) > parseInt(number[3])) {
              res = 0;
            } else if (parseInt(number[1]) < parseInt(number[3])) {
              res = 1;
            } else {
              res = 2;
            }
          }
          if (lhitem.key == "qg") {
            if (parseInt(number[1]) > parseInt(number[4])) {
              res = 0;
            } else if (parseInt(number[1]) < parseInt(number[4])) {
              res = 1;
            } else {
              res = 2;
            }
          }
          if (lhitem.key == "bs") {
            if (parseInt(number[2]) > parseInt(number[3])) {
              res = 0;
            } else if (parseInt(number[2]) < parseInt(number[3])) {
              res = 1;
            } else {
              res = 2;
            }
          }
          if (lhitem.key == "bg") {
            if (parseInt(number[2]) > parseInt(number[4])) {
              res = 0;
            } else if (parseInt(number[2]) < parseInt(number[4])) {
              res = 1;
            } else {
              res = 2;
            }
          }
          if (lhitem.key == "sg") {
            if (parseInt(number[3]) > parseInt(number[4])) {
              res = 0;
            } else if (parseInt(number[3]) < parseInt(number[4])) {
              res = 1;
            } else {
              res = 2;
            }
          }

          if (res == 0) {
            lh[lhitem.key] = '<span class="cl1">龙</span>';
          }
          if (res == 1) {
            lh[lhitem.key] = '<span class="cl2">虎</span>';
          }
          if (res == 2) {
            lh[lhitem.key] = '<span class="cl3">和</span>';
          }
        });
        item.lh = lh;
      });
      return numbers;
    },
    analysisSm(numbers) {
      let count = 0;

      let last = {
        sumBs: null,
        sumDs: null,
        wanBs: null,
        wanDs: null,
        qianBs: null,
        qianDs: null,
        baiBs: null,
        baiDs: null,
        shiBs: null,
        shiDs: null,
        geBs: null,
        geDs: null,
      };

      for (const key in last) {
        if (last.hasOwnProperty(key)) {
          last[key] = {
            key: null,
            count: 0,
          };
        }
      }

      let counts = {
        // six: 0,
        // seven: 0,
        // eight: 0,
        // nine: 0,
        // ten: 0,
        // eleven: 0,
        // twelve: 0,
        // thirteen: 0,
        // fourteen: 0,
        // fifteen: 0,
        // sixteen: 0,
      };
      numbers.reverse().forEach((item) => {
        let number = item.number;
        let sm = {};

        this.sm.forEach((smitem) => {
          let n = -1,
            res = "";

          let bs = null,
            ds = null;
          if (smitem.key == "zh") {
            (bs = last.sumBs), (ds = last.sumDs);
            n =
              parseInt(number[0]) +
              parseInt(number[1]) +
              parseInt(number[2]) +
              parseInt(number[3]) +
              parseInt(number[4]);
            res += '<span class="cl1">' + n + "</span> - ";
            if (n <= 22) {
              if (bs.key != "SMALL") {
                bs.key = "SMALL";
                bs.count = 0;
              }
              bs.count++;
              res += '<span class="bs cl4">小 ' + bs.count + "</span>";
            } else {
              if (bs.key != "BIG") {
                bs.key = "BIG";
                bs.count = 0;
              }
              bs.count++;
              res += '<span class="bs cl5">大 ' + bs.count + "</span>";
            }
            if (bs.count > 2) {
              counts["_" + bs.count] = counts["_" + bs.count]
                ? counts["_" + bs.count] + 1
                : 1;
            }
          }
          if (smitem.key == "w") {
            (bs = last.wanBs), (ds = last.wanDs);
            n = parseInt(number[0]);
          }
          if (smitem.key == "q") {
            (bs = last.qianBs), (ds = last.qianDs);
            n = parseInt(number[1]);
          }
          if (smitem.key == "b") {
            (bs = last.baiBs), (ds = last.baiDs);
            n = parseInt(number[2]);
          }
          if (smitem.key == "s") {
            (bs = last.shiBs), (ds = last.shiDs);
            n = parseInt(number[3]);
          }
          if (smitem.key == "g") {
            (bs = last.geBs), (ds = last.geDs);
            n = parseInt(number[4]);
          }
          if (!res) {
            if (n <= 4) {
              if (bs.key !== "SMALL") {
                bs.key = "SMALL";
                bs.count = 0;
              }
              bs.count++;
              res += '<span class="bs cl4">小 ' + bs.count + "</span>";
            } else {
              if (bs.key !== "BIG") {
                bs.key = "BIG";
                bs.count = 0;
              }
              bs.count++;
              res += '<span class="bs cl5">大 ' + bs.count + "</span>";
            }
            if (bs.count > 2) {
              counts["_" + bs.count] = counts["_" + bs.count]
                ? counts["_" + bs.count] + 1
                : 1;
            }
          }
          if (n % 2 == 0) {
            if (ds.key !== "DOUBLE") {
              ds.key = "DOUBLE";
              ds.count = 0;
            }
            ds.count++;

            res += '<span class="ds cl6">双 ' + ds.count + "</span>";
          } else {
            if (ds.key !== "SINGLE") {
              ds.key = "SINGLE";
              ds.count = 0;
            }
            ds.count++;
            res += '<span class="ds cl7">单 ' + ds.count + "</span>";
          }

          if (ds.count > 2) {
            counts["_" + ds.count] = counts["_" + ds.count]
              ? counts["_" + ds.count] + 1
              : 1;
          }

          sm[smitem.key] = res;
        });
        item.sm = sm;
      });
      this.counts = counts;
      if (this.isDesc) {
        return numbers.reverse();
      }
      return numbers;
    },
    onSort() {
      this.isDesc = !this.isDesc;
      this.numbers = this.analysisSm(this.orgnums);
    },
    onChsDate() {
      setTimeout(() => {
        this.chsDateText = this.moment(this.chsDateVal).format("YYYY/MM/DD");
        console.log("date :>> ", this.chsDateText);
        this.showChsDate = false;
        this.reload();
      }, 500);
    },
    chsDatProv() {
      this.chsDateVal = this.moment(this.chsDateVal)
        .subtract(1, "days")
        .toDate();
      this.onChsDate(this.chsDateVal);
    },
    chsDatNext() {
      this.chsDateVal = this.moment(this.chsDateVal).add(1, "days").toDate();
      this.onChsDate(this.chsDateVal);
    },
    interval() {
      // let timeout = setTimeout(() => {
      //   this.loadc();
      //   clearTimeout(timeout);
      //   this.interval();
      // }, 3000);
    },
    reload() {
      let toast = this.$toast.loading({
        message: "加载中...",
      });
      this.page = 1;
      this.numbers = [];
      this.finished = false;
      this.loadc().then((res) => {
        toast.clear();
      });
    },
    loadc() {
      return new Promise((resolve, reject) => {
        this.http
          .post(api.c30, {
            day: this.moment(this.chsDateVal).format("YYYYMMDD"),
            page: this.page,
          })
          .then((res) => {
            this.orgnums = res.data;
            let numbers = this.analysisLh(res.data);
            numbers = this.analysisSm(numbers);
            this.numbers = this._.concat(this.numbers, numbers);
            this.page++;
            this.loading = false;
            this.finished = true;
            resolve(res);
          });
      });
    },
    openAlign() {
      const toast = this.$toast.loading({
        duration: 0, // 持续展示 toast
        message: "校准中..",
      });
      this.http.post(api.vr15_align).then((res) => {
        toast.clear();
        if (res.code == 200) {
          this.reload();
          this.$toast.success("校准成功");
        } else {
          this.$toast.fail(res.msg || "校准失败");
        }
      });
    },
    exportDone(value) {
      this.showExport = false;
      let day = this.moment(value).format("YYYY-MM-DD");
      const toast = this.$toast.loading({
        duration: 0, // 持续展示 toast
        message: "处理中..",
      });
      this.http
        .post(api.vr15_export, this.exportXls, { responseType: "blob" })
        .then((res) => {
          console.log(res);
          const fileName = res.headers["content-disposition"]
            .split(";")[1]
            .split("filename=")[1];

          let blob = new Blob([res.data], {
            type: "application/vnd.ms-excel",
          });
          let objectUrl = URL.createObjectURL(blob);
          let link = document.createElement("a");
          link.style.display = "none";
          link.href = objectUrl;
          link.setAttribute("download", fileName);
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link); // 下载完成移除元素
          window.URL.revokeObjectURL(blob); // 释放掉blob对象
          toast.clear();
        });
    },
  },
};
</script>
