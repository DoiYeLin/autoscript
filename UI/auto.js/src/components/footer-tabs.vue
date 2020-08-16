<template>
  <div>
    <van-tabbar
      v-model="active"
      :active-color="tab.active"
      :inactive-color="tab.inactive"
      route
      @change="tagChagne"
      class="animated slideInUp faster"
    >
      <van-tabbar-item v-for="item in tab.items" :key="item.path" :to="item.path" @click="itemClick">
        <template #icon>
          <i :class="item.icon"></i>
        </template>
        <span style="font-size: 16px">{{item.name}}</span>
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script>
import { Notify } from "vant";

export default {
  name: "footerTabs",
  data() {
    return {
      active: 0,
      firstClkCount: 0,
      tab: {
        active: "rgb(53, 59, 72)",
        inactive: "SILVER",
        items: [
          { path: "/home", name: "Game" },
          { path: "/plan", name: "方案" },
          { path: "/bet", name: "投注" }
        ]
      }
    };
  },
  watch: {
    $route: function(to, from) {
      this.routeChange(to.path);
    }
  },
  created() {},
  methods: {
    tagChagne(index) {
      // Notify({ type: 'primary', message: index });
    },
    routeChange(path) {
      let has = this._.filter(this.tab.items, { path: path });
      if (has && has.length > 0) {
        this.activeIndex = path;
        this.show = true;
      } else {
        this.show = false;
      }
    },
    itemClick() {
      if(this.active > 0) {
        this.firstClkCount = 0;
      } else {
        this.firstClkCount++;
        if(this.firstClkCount > 5) {
          window.location.reload();
        }
      }

    }
  }
};
</script>

<style lang="less">
</style>
