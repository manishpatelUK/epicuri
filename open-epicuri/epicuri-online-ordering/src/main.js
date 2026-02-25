// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import Vuetify from 'vuetify'
import VueResource from 'vue-resource';
import VueStripeCheckout from 'vue-stripe-checkout';
import App from './App'
import router from './router'
import 'vuetify/dist/vuetify.min.css'
import 'vue-tel-input/dist/vue-tel-input.css';
import VueTelInput from 'vue-tel-input'

Vue.config.productionTip = false
Vue.use(Vuetify)
Vue.use(VueResource)
Vue.use(VueTelInput)

/* EDIT THIS CONFIGURATION FOR YOUR VENUE
* IF YOU DO NOT HAVE THIS INFORMATION, PLEASE CONTACT EPICURI SUPPORT */
Vue.mixin({
  data: function () {
    return {
      get g_restaurantId () {
        //return '58ab16bde4b0af26e64d49f1' // DEV
        return '60754b75888ac401fff09cd6' // EPICURI WILL PROVIDE YOU WITH THIS ID
      },
      get g_publicToken () {
        //return 'abc'
        return '6564f3df41ba4e76920dcd0f6fbea7d0' // EPICURI WILL PROVIDE YOU WITH THIS TOKEN
      },
      get g_defaultTelephoneNumber () {
        return '01733 565606' // YOUR TELEPHONE NUMBER, IN CASE SOMETHING GOES WRONG WITH THE WIDGET
      },
      get g_validLocations () {
        return ['GB'] // PHONE NUMBER VALIDATION
      }
    }
  }
})

// Vue.use(VueStripeCheckout, 'pk_test_0uLMayRAzxqdSIjWsmSfCDuo');
Vue.use(VueStripeCheckout, 'pk_live_5PFLgL2uoPK4pGF0aDaIB4q8');

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
