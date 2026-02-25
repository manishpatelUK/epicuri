import Vue from 'vue'
import Router from 'vue-router'
import OnlineOrder from '@/components/OnlineOrder'
import 'babel-polyfill'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'OnlineOrder',
      component: OnlineOrder
    }
  ]
})
