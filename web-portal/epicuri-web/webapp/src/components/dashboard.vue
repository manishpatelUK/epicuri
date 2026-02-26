<template>
    <div id="dashboard">
        <md-button class="md-fab md-primary toggle-menu-button" @click.native="toggleLeftSidenav"
                   v-if="showToggleButton">
            <md-icon>menu</md-icon>
        </md-button>
        <md-toolbar class="epicuri-navbar">
            <img class="dashboard-logo" src="../images/dashboard-logo.png"/>
        </md-toolbar>
        <sidebar ref="sidebar" v-if="!showToggleButton"></sidebar>
        <md-sidenav class="md-left side-navigation" ref="leftSidenav" @open="open('Left')" @close="close('Left')">
            <md-toolbar class="sidebar-toggle">
                <img class="dashboard-logo-toggle" src="../images/dashboard-logo.png"/>
                <div class="md-toolbar-container">
                    <sidebar></sidebar>
                </div>
            </md-toolbar>
        </md-sidenav>
        <div class="main-view-wrapper">
            <kitchen v-if="routingMap.kitchen"></kitchen>
            <table_planning v-if="routingMap.table_planning"></table_planning>
            <reporting_centre v-if="routingMap.reporting_centre"></reporting_centre>
            <business_intelligence v-if="routingMap.business"></business_intelligence>
            <menu_management v-if="routingMap.menu_management"></menu_management>
            <modifier_management v-if="routingMap.modifier_management"></modifier_management>
            <menu_structure_management v-if="routingMap.menu_structure_management"></menu_structure_management>
            <stock_control v-if="routingMap.stock_control"></stock_control>
            <staff_management v-if="routingMap.staff_management"></staff_management>
            <general_settings v-if="routingMap.general_settings"></general_settings>
            <opening_hours v-if="routingMap.opening_hours"></opening_hours>
            <absolute_closures v-if="routingMap.absolute_closures"></absolute_closures>
        </div>
    </div>
</template>

<script>

    let sidebar = require('./sidebar.vue');
    let kitchen = require('./pendingOrdersView.vue');
    let reporting_centre = require('./reportingCentreView.vue');
    let business_intelligence = require('./businessIntelligenceView.vue');
    let menu_management = require('./menuManagementView.vue');
    let modifier_management = require('./modifierManagementView.vue');
    let menu_structure_management = require('./menuStructureView.vue');
    let stock_control = require('./stockControlView.vue');
    let staff_management = require('./staffManagementView.vue');
    let opening_hours = require('./openingHours.vue');
    let absolute_closures = require('./absoluteClosures.vue');
    let table_planning = require('./tablePlanningView.vue');
    let general_settings = require('./generalSettings.vue');
    let epicuri = require('../internal/epicuri');

    export default {
        data: function () {

            return {

                routingMap:{
                    'kitchen': false,
                    'business': false,
                    'reporting_centre': false,
                    'menu_management': false,
                    'modifier_management': false,
                    'menu_structure_management':false,
                    'stock_control':false,
                    'staff_management': false,
                    'general_settings': false,
                    'opening_hours': false,
                    'absolute_closures': false,
                    'table_planning': false
                },

                showToggleButton: false,
                currentComponent: null,
                confirm: {
                    title: 'Warning!',
                    contentHtml: 'Are you sure you want to logout?',
                    ok: 'Agree',
                    cancel: 'Disagree'
                },
                printerData: [],
                windowWidth: window.innerWidth
            }
        },
        components: {
            kitchen,
            reporting_centre,
            business_intelligence,
            table_planning,
            sidebar,
            menu_management,
            menu_structure_management,
            stock_control,
            modifier_management,
            staff_management,
            general_settings,
            opening_hours,
            absolute_closures
        },
        mounted: function () {
            this.onLoad();
            window.addEventListener('resize', this.handleWindowResize);
        },
        methods: {
            handleWindowResize(event) {
                this.windowWidth = event.currentTarget.innerWidth;
                this.showToggleButton = this.windowWidth <= 1024;
            },
            toggleLeftSidenav() {
                this.$refs.leftSidenav.toggle();
            },
            open(ref) {
                console.log('Opened: ' + ref);
            },
            close(ref) {
                console.log('Closed: ' + ref);
            },
            onLogout: function () {
                epicuri.eraseCookie('epicuri.co.uk.epa.current');
                GLOBAL_ROUTER.push({name: 'root'});
            },
            openDialog(ref) {
                this.$refs[ref].open();
            },
            closeDialog(ref) {
                this.$refs[ref].close();
            },
            onOpen() {
                console.log('Opened');
            },
            onClose(type) {
                if (type === "ok") {
                    this.onLogout();
                } else {
                    console.log('Closed', type);
                }
            },
            onLoad: function () {
                for(let property in this.routingMap) {
                    if(this.routingMap.hasOwnProperty(property)) {
                        this.routingMap[property] = property == this.$route.name;
                    }
                }

                this.showToggleButton = window.innerWidth <= 1024;
            }
        },
        watch: {
            '$route': function (newRoute, oldRoute) {
                this.onLoad();
            },
        },
    };
</script>

<style>

    #dashboard {
        height: 100vh;
    }

    .main-view-wrapper {
        width: calc(100% - 270px);
        margin-left: 270px;
        padding-top: 74px;
    }

    .dashboard-logo {
        width: 200px;
    }

    .md-list-expand-container .md-list-item {
        color: #bbb !important;
    }

    .md-list-expand-container .md-list-item .md-list-item-container {
        padding-left: 85px;
    }

    .epicuri-navbar-list .md-list-item .md-list-item-container:hover {
        cursor: pointer;
        background-color: hsla(0, 0%, 25%, .8) !important;
        text-decoration: none;
    }

    .epicuri-navbar {
        position: fixed !important;
        width: 100% !important;
        z-index: 6;
    }

    .epicuri-navbar {
        background-color: #EE6E09 !important;
    }

    .epicuri-navbar-list {
        background-color: #545454 !important;
        color: #FFF !important;
    }

    .epicuri-navbar-list .md-list-item-container {
        background-color: #545454 !important;
    }

    /*.md-icon {*/
    /*color: #FFF!important;*/
    /*}*/
    .epicuri-navbar-subitem-list {
        background-color: #545454 !important;
    }

    .active .md-list-item-container {
        background-color: rgba(187, 187, 187, 0.2) !important;
    }

    .toggle-menu-button {
        position: fixed;
        top: 0;
        right: 20px;
        width: 48px !important;
        height: 48px !important;
        z-index: 8;
    }

    .sidebar-toggle {
        background-color: #EE6E09 !important;
        padding-left: 0;
    }

    .dashboard-logo-toggle {
        width: 160px;
        margin-left: 10px;
        margin-top: 10px;
    }

    .placeholder-div {
        background-image: url("../images/splash-background.png");
        height: calc(100vh - 100px);
        width: 100%;
        background-size: contain;
        text-align: center;
    }

    .placeholder-div h1 {
        padding-top: 30vh;
    }

    @media screen and (max-width: 1024px) {

        .main-view-wrapper {
            width: 100%;
            margin: 0 auto;
        }

        .orders-list .md-flex-20 {
            min-width: 25%;
            -ms-flex: 0 1 25%;
            flex: 0 1 25%;
        }

        #dashboard {
            height: auto;
        }

        .side-navigation > .md-sidenav-content {
            background-color: #545454 !important;
        }
    }
</style>
