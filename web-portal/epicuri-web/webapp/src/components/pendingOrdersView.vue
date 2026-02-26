<template>
    <div>
        <switchSessionButtons v-on:switch-sort="reloadPage"></switchSessionButtons>
        <md-layout md-gutter class="orders-list">
            <md-layout>
                <md-card class="orders-card" md-with-hover
                         v-bind:class="{ markCard: order.done == true, takeawayClass: order.sessionType == 'TAKEAWAY' }"
                         v-for="(order, index) in orders" @click.native="listIndex = index">
                    <md-card-header>
                        <div class="md-title">{{order.title}}</div>
                        <div class="md-subhead">
                            <span class="orders-table" v-if="!!order.partyName">{{order.partyName}}</span>
                            <span class="orders-table" v-if="!!order.tableName">Table: {{order.tableName}}</span>
                            <span v-if="order.covers"> ({{order.covers}} covers)</span>
                        </div>
                        <div class="md-subhead">{{order.timePrepend}}: <span class="orders-table">{{order.sessionTime}}</span></div>
                    </md-card-header>

                    <md-card-content class="order-card-content md-scrollbar">
                        <md-list class="orders-list-style" v-for="(item, ordersListIndex) in order.courses"
                                 v-bind:class="{ markList: order.done == true }" ref="order_list">
                            <span class="item-course">{{item.course}}
                                <b v-if="item.items.length == 1" >{{ item.items.length }} item</b>
                                <b v-if="item.items.length > 1" >{{ item.items.length }} items</b>
                            </span>
                            <md-list-item class="orders-list-item" v-for="(orderItem, itemIndex) in item.items"
                                          v-bind:class="{
                                                            markListItem: orderItem.done == true,
                                                            expandModifier: !!orderItem.modifiers && !!!orderItem.modifiers,
                                                            expandNote: !!orderItem.note && !!!orderItem.note,
                                                            expandModifierAndNote: !!orderItem.note && !!orderItem.modifiers
                                                        }">
                                <p class="first-p">{{orderItem.quantity}}&nbsp;x&nbsp;
                                    <span class="order-item-name">{{orderItem.menuItemName}} </span><br>
                                    <span class="md-subhead note-order" v-if="orderItem.note.length > 1">{{orderItem.note}} </span><br v-if="orderItem.note.length > 1">
                                    <span v-if="orderItem.modifiers">
                                           (<span class="second-span"
                                                  v-for="(modifier, modifierInde) in orderItem.modifiers">
                                                   {{modifier}}<span class="comma-span"
                                                                     v-if="orderItem.modifiers.length > 1">, </span>
                                            </span> )<br>
                                    </span>
                                </p>

                                <!--<md-list-expand>-->
                                    <md-layout md-gutter>
                                        <md-button class="md-button md-raised mark-as-done-button"
                                                   v-if="orderItem.done == false"
                                                   @click.native="markAsDone(orderItem.orderId)">
                                            Done
                                        </md-button>
                                        <md-button class="md-button md-raised mark-as-done-button"
                                                   v-if="orderItem.done == true"
                                                   @click.native="markAsUnDone(orderItem.orderId)">
                                            Undo
                                        </md-button>
                                    </md-layout>
                                <!--</md-list-expand>-->
                            </md-list-item>
                        </md-list>
                    </md-card-content>

                    <md-card-actions class="card-bottom">
                        <md-icon class="scroll-info-warning" v-if="order.courses[0].items.length >= 6">
                            swap_vert
                            <md-tooltip md-direction="left">Scroll down to see more orders</md-tooltip>
                        </md-icon>
                        <md-button v-if="order.done == false" class="mark-all-as-done-button"
                                   @click.native="markAllAsDone(order.sessionId, order.batchId)">
                            Mark all as done
                        </md-button>
                        <md-button v-if="order.done == true" class="mark-all-as-done-button"
                                   @click.native="markAllAsUnDone(order.sessionId, order.batchId)">
                            Undo all
                        </md-button>
                    </md-card-actions>

                </md-card>
            </md-layout>
        </md-layout>
        <div v-if="orders.length == 0" class="placeholder-div">
            <h1>No items in queue</h1>
        </div>
    </div>
</template>

<script>
    let sidebar = require('./sidebar.vue');
    let epicuri = require('../internal/epicuri.js');
    let queryparams = require('../internal/queryparams');
    let switchSessionButtons = require('./switchSessionButtons.vue');

    export default {
        data: () => {
            return {
                orders: [],
                isActive: false,
                listIndex: 0,
                listItemIndex: 0,
                pollingInterval: null,
                radio: false
            }
        },
        components: {sidebar, switchSessionButtons},
        mounted: function () {
            if (sessionStorage.getItem("printerBar")) {
                this.onGetPrinterItems(sessionStorage.getItem("printerBar"));
            } else {
                this.onGetPrinterItems(this.$route.query.printerId);
            }
            this.pollingInterval = setInterval(function () {
                if (this.$route.query.printerId === undefined) {
                    this.onGetPrinterItems(sessionStorage.getItem("printerBar"));
                } else {
                    this.onGetPrinterItems(this.$route.query.printerId);
                }
            }.bind(this), 6000)

        },
        route: {
            canReuse: true
        },
        beforeDestroy: function () {
            clearInterval(this.pollingInterval);
        },
        methods: {
            markAsDone: function (assetId) {

                let request = {
                    orderId: assetId
                };
                //this.$refs.order_list[this.listIndex].$children[itemIndex].toggleExpandList();

                epicuri.markAsDone(this.$http, request,
                    (response) => {
                        this.onGetPrinterItems(this.$route.query.printerId);
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    }
                )

            },
            markAllAsDone: function (sessionId, batchId) {

                let request = {
                    sessionId: sessionId,
                    batchId: batchId,
                    printerId: this.$route.query.printerId
                };

                epicuri.markAllAsDone(this.$http, request,
                    (response) => {
                        this.onGetPrinterItems(this.$route.query.printerId);
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    }
                )

            },
            markAllAsUnDone: function (sessionId, batchId) {

                let request = {
                    sessionId: sessionId,
                    batchId: batchId,
                    printerId: this.$route.query.printerId
                };

                epicuri.markAllAsUnDone(this.$http, request,
                    (response) => {
                        this.onGetPrinterItems(this.$route.query.printerId);
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    }
                )

            },
            onGetPrinterItems: function (printerId) {

                epicuri.getPrinterItems(this.$http, printerId,
                    (response) => {
                        this.orders = response.body;
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    },
                    this.radio
                )
            },
            markAsUnDone: function (assetId) {

                let request = {
                    orderId: assetId
                };
                //this.$refs.order_list[this.listIndex].$children[itemIndex].toggleExpandList();

                epicuri.markAsUnDone(this.$http, request,
                    (response) => {
                        this.onGetPrinterItems(this.$route.query.printerId);
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    }
                )
            },
            closeExpander: function (index) {
                this.isActive = this.isActive !== true;
            },
            reloadPage: function (radio) {
                this.radio = radio;
                this.onGetPrinterItems(this.$route.query.printerId);
            }
        },
        watch: {
            '$route': function (newRoute, oldRoute) {
                this.onGetPrinterItems(this.$route.query.printerId);
            }
        }
    }
</script>

<style>

    .orders-card {
        flex-basis: 16%;
        overflow: hidden;
        height: 50vh;
        margin-bottom: 15px;
        margin-right: 10px;
    }

    .md-card .card-bottom {
        /*position: absolute;*/
        bottom: 0;
        right: 0;
        position: relative;
        z-index: 3;
        min-height: 60px;
    }

    .md-offset-2 {
        margin-left: 0.6%;
        margin-top: 10px;
    }

    .takeawayClass .md-list .md-list-item-expand .md-list-item-container {
        background-color: transparent;
    }

    .md-card {
        width: 100% !important;
    }

    .md-accept {
        background-color: #16a085;
        width: 40px;
    }

    .md-accept:hover {
        background-color: #16886d !important;
    }

    .orders-list-item .md-list-item-container {
        flex-wrap: wrap;
    }

    .orders-list-item p {
        flex-basis: 80%;
    }

    .orders-list-item .first-p {
        margin-bottom: 0;
        line-height: 1.4;
        margin-top: 0;
    }

    .expandModifier .first-p, .expandNote .first-p, .expandModifierAndNote .first-p {
        margin-bottom: 0;
        line-height: 1.4;
        /*margin-top: 16px;*/
    }

    /*.orders-list-item .md-list-item {*/
    /*padding: 5px 0;*/
    /*border-bottom: 1px solid #eee;*/
    /*}*/

    /*.orders-list-item .md-list-item:hover {*/
    /*background-color: #eee;*/
    /*}*/

    .mark-all-as-done-button {
        background-color: #F5F5F5;
        margin: 10px 0 !important;
    }

    .orders-table {
        font-weight: bolder;
    }

    .orders-list-style .md-list-item {
        border-bottom: 1px solid #eee;
    }

    .order-item-name {
        font-weight: 500;
    }

    .orders-list-style .md-list-item .md-list-item-container .md-icon {
        color: #000 !important;
    }

    .item-course {
        font-size: 16px;
        font-weight: bolder;
        padding: 3px 0;
    }

    .mark-as-done-button {
        margin: 10px auto;
    }

    .markCard {
        background-color: #ddd !important;

    }

    .markList {
        background-color: #ddd !important;

    }

    .markListItem {
        border-bottom: 1px solid #e6e6e6 !important;
    }

    .expandModifierAndNote {
        min-height: 70px;
    }

    .expandModifier,  .expandNote{
        min-height: 50px;
    }

    .second-span:last-of-type .comma-span {
        display: none;
    }

    .markListItem .md-list-item-container {
        background-color: #ddd !important;
        color: #aaa;
        padding: 0 5px;
    }

    .orders-list-style .markListItem .md-list-item-container .md-icon {
        color: #aaa !important;
    }

    .mark-as-done-prompt {
        padding: 5px 10px;
    }

    .orders-list .order-card-content {
        /*height: 315px;*/
        /*overflow-y: scroll;*/
        overflow: auto;
    }

    .order-card-content .md-list-item-container {
        padding-left: 5px;
        padding-right: 2px;
    }

    .scroll-info-warning {
        color: #EE6E09 !important;
        /*margin: 0 0 0 auto;*/
        /*margin-right: 7px;*/
        /*position: absolute;*/
        /*right: 0;*/
        /*top: 0;*/
    }

    .md-flex-16 {
        min-width: 16%;
        -ms-flex: 0 1 16%;
        flex: 0 1 16%;
    }

    .orders-list .md-flex-16:first-child {
        margin-left: 0;
    }

    .orders-list .md-flex-16:nth-child(7n + 0) {
        margin-left: 0;
    }

    .orders-list .takeawayClass {
        background-color: rgba(238, 110, 9, 0.3) !important;
    }

    .orders-list .takeawayClass .orders-list-style {
        background-color: transparent;
    }

    .orders-list .takeawayClass .orders-list-style .md-list-item-container {
        background-color: transparent;
    }

    .takeawayClass .md-theme-default.md-list .md-list-item-expand .md-list-item-container {
        background-color: rgba(238, 110, 9, 0) !important;
    }
    .md-toolbar + .md-toolbar {
        margin-top: 16px;
    }
    .md-radio {
        display: flex;
    }
    .md-list.orders-list-style.md-theme-default span b{
        background: #EE6E09;
        color: #615454;
        border-radius: 7px;
        padding: 0 4px;
        float: right;
    }
   .md-active .md-subhead.note-order{
       white-space:normal !important;
    }
  .orders-list-item:not(md-active) .md-subhead.note-order{
       white-space: nowrap;
       overflow: hidden;
       text-overflow: ellipsis;
       max-width: 125px;
       display: inline-block;
   }

    @media screen and (max-width: 2560px) {
        .md-flex-16 {
            min-width: 10%;
            -ms-flex: 0 1 10%;
            flex: 0 1 10%;
        }
    }

    @media screen and (max-width: 1921px) {
        .md-flex-16 {
            min-width: 16%;
            -ms-flex: 0 1 16%;
            flex: 0 1 16%;
        }
    }

    @media screen and (max-width: 1780px) {
        .orders-card {
            flex-basis: 19%;
        }
    }

    @media screen and (max-width: 1440px) {

        .orders-card {
            flex-basis: 19%;
        }
        .orders-list-item p {
            font-size: 15px;
        }
        .md-card .md-title {
            font-size: 22px;
        }
        .md-card .md-card-header {
            padding: 10px;
        }
        .md-card .md-card-header + .md-card-content {
            padding: 0 10px 10px 10px;
        }
        .md-card .md-card-header:first-child > .md-title:first-child {
            margin-top: 0;
        }
        .md-card .md-card-actions {
            padding: 8px 10px;
        }
    }

    @media screen and (max-width: 1280px) {

        .orders-card {
            flex-basis: calc(25% - 10px);
        }
        .md-card .md-title {
            font-size: 1.5em;
        }
        .orders-list {
            padding: 10px;
        }

    }

    @media screen and (max-width: 1024px) {

        .orders-card {
            flex-basis: calc(25% - 10px);
            height: 55vh;
        }
        /*.orders-card:nth-of-type(4n) {*/
            /*margin-right: 0;*/
        /*}*/
        .orders-list {
            padding: 10px;
        }
        .orders-list .order-card-content {
            /*height: 50vh;*/
            /*overflow-y: scroll;*/
            overflow: auto;
        }
        .md-card .md-title {
            font-size: 1.5em;
        }
        .md-card .md-card-header {
            padding: 10px;
        }
        .md-card .md-card-header + .md-card-content {
            padding: 0 10px 10px 10px;
        }
        .md-card .md-card-actions .md-button:last-child {
            width: 100%;
        }
    }
    @media screen and (max-width: 768px) {

        .orders-card {
            flex-basis: calc(33.333333% - 10px);
        }
    }

    @media screen
    and (min-device-width: 600px)
    and (max-device-height: 1280px)
    and (orientation: portrait) {
        .orders-card {
            height: 50vh;
            flex-basis: calc(33.333333% - 10px);
        }

        .orders-card:nth-of-type(3n) {
            margin-right: 0;
        }
        .orders-list {
            padding: 10px;
        }
        .orders-list .order-card-content {
            /*height: 50vh;*/
            /*overflow-y: scroll;*/
            overflow: auto;
        }
        .md-card .md-title {
            font-size: 1.5em;
        }
    }

    @media screen
    and (min-device-width: 600px)
    and (max-device-width: 1280px)
    and (orientation: landscape) {
        .orders-card {
            height: 65vh;
        }
        .orders-card {
            flex-basis: calc(20% - 10px);
        }
    }
    @media screen
    and (min-device-width: 600px)
    and (max-device-width: 1024px)
    and (orientation: landscape) {
        .orders-card {
            height: 65vh;
        }
        .orders-card {
            flex-basis: calc(25% - 10px);
        }
    }

</style>
