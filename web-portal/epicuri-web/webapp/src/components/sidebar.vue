<template>
    <div>
        <div class="phone-viewport">
            <md-list class="epicuri-navbar-list">
                <md-list-item>
                    <md-icon class="sidebar-icons">restaurant</md-icon>
                    <span>On Screen Display</span>
                    <md-list-expand>
                        <md-list class="epicuri-navbar-subitem-list">
                            <md-list-item v-for="(printer,index) in printerData"
                                          @click.native="navigateToKitchen(printer.Id)">
                                {{printer.Name}}
                            </md-list-item>
                        </md-list>
                    </md-list-expand>
                </md-list-item>
                <!--<md-list-item @click.native="navigateToTablePlanning">
                    <md-icon class="sidebar-icons">event_seat</md-icon>
                    <span>Table Planning</span>
                </md-list-item>-->
                <md-list-item>
                    <md-icon md-iconset="fa fa-bar-chart" class="sidebar-icons"></md-icon>
                    <span>Business Intelligence</span>
                    <md-list-expand>
                        <md-list class="epicuri-navbar-subitem-list">
                            <md-list-item class="md-inset" @click.native="navigateToBI(0)">Week</md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToBI(1)">Month</md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToBI(2)">Three Months</md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToBI(3)">Year</md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToBI(4)">Forever</md-list-item>
                        </md-list>
                    </md-list-expand>
                </md-list-item>
                <md-list-item>
                    <md-icon md-iconset="fa fa-flag" class="sidebar-icons"></md-icon>
                    <span>Reporting Centre</span>
                    <md-list-expand>
                        <md-list class="epicuri-navbar-subitem-list">
                            <md-list-item class="md-inset" @click.native="navigateToRC('customerDetails')">
                                Customer Details
                            </md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToRC('reservations')">
                                Reservations
                            </md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToRC('takeaways')">
                                Takeaways
                            </md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToRC('itemsAggregated')">
                                Menu Item Sales<br> (Aggregated)
                            </md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToRC('modifierSales')">
                                Modifier Sales
                            </md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToRC('payments')">Payment Details
                            </md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToRC('itemDetails')">Menu Item Sales <br>
                                (Details)
                            </md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToRC('revenues')">On-Premise & Takeaway <br>
                                Revenue
                            </md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToRC('cashups')">Historical <br>
                                Cash Ups
                            </md-list-item>
                            <md-list-item v-if="paymentSense" class="md-inset" @click.native="navigateToRC('paymentSense')">PaymentSense <br>
                                EOD
                            </md-list-item>
                        </md-list>
                    </md-list-expand>
                </md-list-item>
                <md-list-item>
                    <md-icon class="sidebar-icons">date_range</md-icon>
                    <span>Day Management</span>
                    <md-list-expand>
                        <md-list class="epicuri-navbar-subitem-list">
                            <md-list-item class="md-inset" @click.native="navigateToDayManagement('opening_hours', 'RESERVATION')">
                                Daily Opening<br>Hours
                            </md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToDayManagement('absolute_closures', 'RESERVATION')">
                                Absolute Closures
                            </md-list-item>
                        </md-list>
                    </md-list-expand>
                </md-list-item>
                <md-list-item>
                    <md-icon class="sidebar-icons">fastfood</md-icon>
                    <span>Menu Management</span>
                    <md-list-expand>
                        <md-list class="epicuri-navbar-subitem-list">
                            <md-list-item class="md-inset" @click.native="navigateToMenuStructureManagement">
                                Menu Structure
                            </md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToMenuManagement">
                                Menu Items
                            </md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToModifierManagement">
                                Modifiers
                            </md-list-item>
                            <md-list-item class="md-inset" @click.native="navigateToStockControl">
                                Stock Control
                            </md-list-item>
                        </md-list>
                    </md-list-expand>
                </md-list-item>
                <md-list-item @click.native="navigateToStaffManagement">
                    <md-icon class="sidebar-icons">supervisor_account</md-icon>
                    <span>Staff Management</span>
                </md-list-item>
                <!--<md-list-item @click.native="navigateToSettings">
                    <md-icon class="sidebar-icons">settings</md-icon>
                    <span>Settings</span>
                </md-list-item>-->

                <md-list-item @click.native="openDialog('logout-dialog')">
                    <md-icon class="sidebar-icons">exit_to_app</md-icon>
                    <span>Logout</span>
                </md-list-item>
            </md-list>

            <md-dialog-confirm
                    :md-title="confirm.title"
                    :md-content-html="confirm.contentHtml"
                    :md-ok-text="confirm.ok"
                    :md-cancel-text="confirm.cancel"
                    @open="onOpen"
                    @close="onClose"
                    ref="logout-dialog">
            </md-dialog-confirm>
        </div>
    </div>
</template>

<script>
    let epicuri = require('../internal/epicuri');
    let kitchen = require('./pendingOrdersView.vue');
    let reporting_centre = require('./reportingCentreView.vue');
    let business_intelligence = require('./businessIntelligenceView.vue');

    export default {
        data: function () {
            return {
                confirm: {
                    title: 'Log out',
                    contentHtml: 'Are you sure you want to log out?<br>Any unsaved items will be lost',
                    ok: 'Yes',
                    cancel: 'No'
                },
                printerData: [],
                paymentSense: false
            }
        },
        mounted: function () {
            this.determineConfiguration();
        },
        methods: {
            toggleLeftSidenav() {
                this.$refs.leftSidenav.toggle();
            },
            toggleRightSidenav() {
                this.$refs.rightSidenav.toggle();
            },
            closeRightSidenav() {
                this.$refs.rightSidenav.close();
            },
            open(ref) {
                console.log('Opened: ' + ref);
            },
            close(ref) {
                console.log('Closed: ' + ref);
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
            onLogout: function () {
                epicuri.eraseCookie('epicuri.co.uk.epa.current');
                sessionStorage.clear();
                this.$router.push({name: "root"})
            },
            navigateToKitchen: function (printerId) {
                sessionStorage.setItem("printerBar", printerId);
                this.$router.push({name: "kitchen", query: {printerId: printerId}});
            },
            navigateToBI: function (periodId) {
                this.$router.push({name: "business", query: {periodId: periodId}})
            },
            navigateToDayManagement: function (dayManagementView, queryType) {
                this.$router.push({name: dayManagementView, query: {type: queryType}})
            },
            navigateToRC: function (reportId) {
                this.$router.push({name: "reporting_centre", params: {reportName: reportId}})
            },
            navigateToMenuManagement: function () {
                this.$router.push({name: "menu_management"})
            },
            navigateToMenuStructureManagement: function () {
                this.$router.push({name:"menu_structure_management"})
            },
            navigateToModifierManagement: function() {
                this.$router.push({name: "modifier_management"})
            },
            navigateToStockControl: function() {
                this.$router.push({name: "stock_control"})
            },
            navigateToStaffManagement: function () {
                this.$router.push({name: "staff_management"})
            },
            navigateToSettings: function () {
                this.$router.push({name: "general_settings"})
            },
            navigateToTablePlanning: function () {
                this.$router.push({name: "table_planning"})
            },
            navigateToView: function (viewName, subViewId) {
                if (subViewId === null) {
                    this.$router.push({name: viewName})
                } else if (viewName === 'kitchen') {
                    this.$router.push({name: viewName, params: {printerId: subViewId}})
                } else if (viewName === 'business') {
                    this.$router.push({name: viewName, params: {periodId: subViewId}})
                } else if (viewName === 'reporting_centre') {
                    this.$router.push({name: viewName, params: {reportName: subViewId}})
                } else {
                    this.$router.push({name: 'dashboard'})
                }
            },
            onGetPrinters: function () {
                epicuri.getPrinters(this.$http,
                    (response) => {
                        this.printerData = response.body;
                    },
                    (response) => {
                        console.log("ERROR " + response)
                    });
            },
            determineConfiguration: function () {
                this.onGetPrinters();
                epicuri.getRestaurant(this.$http,
                    (response) => {
                        try {
                            if(response.body.PaymentSense.host.length > 0) {
                                this.paymentSense = true;
                            }
                        } catch (e) {
                            this.paymentSense = false;
                        }
                    },
                    (response) => {
                        this.paymentSense = false;
                    });
            }
        }
    };

</script>

<style>

    .md-list-expand-container .md-list-item {
        color: #bbb !important;
    }

    .md-list-expand-container .md-list-item .md-list-item-container {
        padding-left: 85px;
        font-size: 14px;
        line-height: 20px;
    }

    .md-list .md-list-item .md-list-item-container:hover {
        cursor: pointer;
        background-color: hsla(0, 0%, 25%, .8) !important;
        text-decoration: none;
    }

    .phone-viewport {
        width: 255px;
        height: calc(100vh - 64px);
        display: inline-block;
        position: fixed;
        top: 64px;
        overflow: hidden;
        background-color: #545454;
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
        padding: 0 5px 0 10px;
    }

    .sidebar-icons {
        color: #FFF !important;
    }

    .epicuri-navbar-list .md-list-item .md-list-item-container .md-icon {
        color: #FFF !important;
    }

    .epicuri-navbar-subitem-list {
        background-color: #545454 !important;
    }

    @media screen and (max-width: 1024px) {
        .phone-viewport {
            width: 100%;
            padding-left: 0;
            margin: 0 auto;
            height: auto;
        }

        .epicuri-navbar-list {
            margin: 0 !important;
        }

    }
</style>
