<template>
    <div id="app">
        <router-view></router-view>
    </div>
</template>

<script>
   let login = require('./login.vue');
   let dashboard = require('./dashboard.vue');
   let epicuri = require('../internal/epicuri');
   let queryparams = require('../internal/queryparams');
   require('../images/favicon.ico');
   const authCookie = "epicuri.co.uk.epa.current";

   export default {
      name: 'app',
      data() {
         return {
            loggedIn: false
         }
      },
      components: {login, dashboard},
      mounted: function () {
         let token = queryparams.getAllUrlParams().Auth;
         let printerId = queryparams.getAllUrlParams().Printer;
         let periodId = queryparams.getAllUrlParams().Period;

         if (!printerId === false) {
            epicuri.setLocalToken(token);
            sessionStorage.setItem("printerBar", printerId);
            this.$router.push({name: "kitchen_app", query: {printerId: printerId}});
            this.loggedIn = true;
         } else if (epicuri.findLocalToken(authCookie)) {
            this.loggedIn = true;
         } else {
            this.loggedIn = !!epicuri.findLocalToken();
            this.$router.push({name: "root"})
         }


         if (!periodId === false) {
            epicuri.setLocalToken(token);
            sessionStorage.setItem("periodId", periodId);
            this.$router.push({name: "business_app", query: {periodId: periodId}});
            this.loggedIn = true;
         } else if (epicuri.findLocalToken(authCookie)) {
            this.loggedIn = true;
         } else {
            this.loggedIn = !!epicuri.findLocalToken();
            this.$router.push({name: "root"})
         }



         //todo check if there is "kitchen=true" to redirect to kitchen view
      },
      watch: {
         '$route': function (newRoute, oldRoute) {
            
            if(!epicuri.findLocalToken(authCookie) === true) {
               this.loggedIn = false;
               this.$router.push({name: "root"})
            } else {
               this.loggedIn = true;
            }

            if (this.loggedIn === true && newRoute.name === "root") {
               this.$router.push({name: oldRoute.name})
            }
         },
      },
   }
</script>

<style>

</style>
