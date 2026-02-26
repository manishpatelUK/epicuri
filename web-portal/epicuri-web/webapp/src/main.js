import Vue from 'vue';
import VueResource from 'vue-resource';
import VueRouter from 'vue-router';
import Chartkick from 'chartkick';
import VueChartkick from 'vue-chartkick';
import Chart from 'chart.js';
import myDatepicker from 'vue-datepicker'
import Moment from 'moment';
import VueMaterial from 'vue-material';
import 'vue-material/dist/vue-material.css';
import VueTimepicker from 'vue2-timepicker';
import draggable from 'vuedraggable';
import 'frappe-gantt';
import 'lodash';
import VueCharts from 'vue-charts';
import ToggleButton from 'vue-js-toggle-button'

Vue.use(VueCharts);
Vue.use(draggable);
Vue.use(VueResource);
Vue.use(VueMaterial);
Vue.use(VueRouter);
Vue.use(VueChartkick, { Chartkick });
Vue.use(myDatepicker);
Vue.use(Moment);
Vue.use(VueTimepicker);
Vue.use(ToggleButton);

import App from './components/app.vue'
import login from './components/login.vue';
import dashboard from './components/dashboard.vue';
import kitchen from './components/pendingOrdersView.vue';
import business from './components/businessIntelligenceView.vue';

const routes = [
    {
        path: '/',
        name: 'root',
        component: login
    },
    {
        path: '/dashboard',
        name: 'dashboard',
        component: dashboard,
        props: true
    },
    {
        path: '/Kitchen/Orders',
        name: 'kitchen',
        component: dashboard,
        props: true
    },
    {
        path: '/TablePlanning',
        name: 'table_planning',
        component: dashboard,
        props: true
    },
    {
        path: '/Kitchen',
        name: 'kitchen_app',
        component: kitchen,
        props: true
    },
    {
        path: '/BusinessIntelligence',
        name: 'business',
        component: dashboard,
        props: true
    },
   {
        path: '/BI',
        name: 'business_app',
        component: business,
        props: true
    },
    {
        path: '/Reporting/:reportName',
        name: 'reporting_centre',
        component: dashboard,
        props: true
    },
    {
        path: '/MenuStructureManagement',
        name: 'menu_structure_management',
        component: dashboard,
        props: true
    },
    {
        path: '/StockControl',
        name: 'stock_control',
        component: dashboard,
        props: true
    },
    {
        path: '/MenuManagement',
        name: 'menu_management',
        component: dashboard,
        props: true
    },
    {
        path: '/ModifierManagement',
        name: 'modifier_management',
        component: dashboard,
        props: true
    },
    {
        path: '/StaffManagement',
        name: 'staff_management',
        component: dashboard,
        props: true
    },
    {
        path: '/Settings',
        name: 'general_settings',
        component: dashboard,
        props: true
    },
    {
        path: '/OpeningHours',
        name: 'opening_hours',
        component: dashboard,
        props: true
    },
    {
        path: '/AbsoluteClosures',
        name: 'absolute_closures',
        component: dashboard,
        props: true
    }
];

window.GLOBAL_ROUTER = new VueRouter({
    routes,
    // mode: 'history'
});

new Vue({
    el: '#app',
    router: GLOBAL_ROUTER,
    render: h => h(App),
    mounted: function () {
        window.GLOBAL_HTTP = this.$http;
    }
});

Vue.http.interceptors.push((request, next) => {

    next(function(response) {
        if(response.status === 500) {
           /* alert("Server is currently going under maintenance. Please wait.");
            setTimeout(function () {
                location.reload();
            },5000)*/
            console.log("SERVER DOWN")

        } else {
            console.log("ALL OK")
        }
    });
})