const prd = (process.env.NODE_ENV === 'production');

const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const CompressionWebpackPlugin = require('compression-webpack-plugin');
const productionGzipExtensions = /\.(js|css|json|txt|html|ico|svg)(\?.*)?$/i;

// vue.config.js
module.exports = {
  publicPath: prd ? './' : '/',
  assetsDir: 'static',
  lintOnSave: true,
  runtimeCompiler: true,
  productionSourceMap: !prd,
  parallel: require("os").cpus().length > 1,
  devServer: {
    proxy: {
      "/api": {
        target: 'https://127.0.0.1:8010',
        // target: 'http://s.fhfuz.com/',
        changeOrigin: true,
        ws: true,//websocket支持
        pathRewrite: {
          // '^/api': '/api',     // rewrite path
          '^/api': '/',     // rewrite path
        },
      },
    }
  },
  css: {
    loaderOptions: {
      less: {
        modifyVars: {
          // 'red': '#f06374',
          hack: `true; @import "~@/assets/css/theme";`,
        },
      },
    },
  },
  configureWebpack: config => {
    if (prd) {
      const plugins = [];
      plugins.push(
        new CompressionWebpackPlugin({
          filename: '[path].gz[query]',
          algorithm: 'gzip',
          test: productionGzipExtensions,
          threshold: 10240,
          minRatio: 0.8
        })
      );
      config.plugins = [
        ...config.plugins,
        ...plugins
      ];
    }
  },
  chainWebpack: config => {
    if (prd) {
      // 生产环境
      config.entry('app').clear().add('./src/main-prd.js')
      config.externals = {
        vue: 'Vue',
        vuex: 'Vuex',
        // 'vue-router': 'VueRouter',
        'axios': 'axios',
        lodash: {
          commonjs: "lodash",
          amd: "lodash",
          root: "_" // 指向全局变量
        },
        moment: {
          commonjs: "moment",
          amd: "moment",
          root: "moment" // 指向全局变量
        }
      }


      config.plugin('webpack-report')
        .use(BundleAnalyzerPlugin, [{
          analyzerMode: 'static',
        }]);
    } else {
      // 开发环境
      config.entry('app').clear().add('./src/main-dev.js')
    }

    config.plugin('html').tap(args => {
      args[0].isPrd = prd
      return args
    })
  },
};