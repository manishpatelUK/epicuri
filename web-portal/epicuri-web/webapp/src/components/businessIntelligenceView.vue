<template>
    <div class="bi-wrapper">
        <h1 class="bi-headline">You are viewing {{reportPeriod}} report</h1>
        <span class="date-label">Start Date：</span>
        <date-picker :date="startTime" :option="option" :limit="limit"></date-picker>
        &nbsp;
        <span class="date-label">End Date：</span>
        <date-picker :date="endTime" :option="option2" :limit="limit"></date-picker>

        <md-button @click.native="customBIMetrics(startTime.time,endTime.time); reportPeriod = 'from ' + startTime.time + ' to ' + endTime.time" class="generate-btn">Generate</md-button>
        <div class="charts-wrapper">
            <h1>Average # of Sessions</h1>
            <column-chart :stacked="true" :data="sessionsData"></column-chart>
        </div>

        <div class="charts-wrapper">
            <h1>Average # of Items Ordered</h1>
            <column-chart :stacked="true" :data="itemsData"></column-chart>
        </div>
        <md-layout md-gutter class="charts-wrapper">
            <h1>Self Service Summary</h1>
            <md-layout md-flex="24" class="epicuri-generated-layout">
                <md-card>
                    <md-card-header>
                        <div class="md-title"><span class="epicuri-title">Epicuri</span> has helped you capture</div>
                    </md-card-header>
                    <md-card-content class="generated-by-epicuri">
                        {{salesReport.items}}
                    </md-card-content>
                    <md-card-content>
                        items via seated self service
                    </md-card-content>
                </md-card>
            </md-layout>
            <md-layout md-flex="24" class="epicuri-generated-layout">
                <md-card>
                    <md-card-header>
                        <div class="md-title"><span class="epicuri-title">Epicuri</span> has helped you capture</div>
                    </md-card-header>
                    <md-card-content class="generated-by-epicuri">
                        {{salesReport.reservations}}
                    </md-card-content>
                    <md-card-content>
                        tables at your restaurant
                    </md-card-content>
                </md-card>
            </md-layout>
            <md-layout md-flex="24" class="epicuri-generated-layout">
                <md-card>
                    <md-card-header>
                        <div class="md-title"><span class="epicuri-title">Epicuri</span> has helped you capture</div>
                    </md-card-header>
                    <md-card-content class="generated-by-epicuri">
                        {{salesReport.takeaways}}
                    </md-card-content>
                    <md-card-content>
                        takeaways booked via self service
                    </md-card-content>
                </md-card>
            </md-layout>
            <md-layout md-flex="24" class="epicuri-generated-layout">
                <md-card>
                    <md-card-header>
                        <div class="md-title"><span class="epicuri-title">Epicuri</span> has helped you capture</div>
                    </md-card-header>
                    <md-card-content class="generated-by-epicuri">
                        {{salesReport.revenue}}
                    </md-card-content>
                    <md-card-content>
                        in revenue via self service
                    </md-card-content>
                </md-card>
            </md-layout>
        </md-layout>
        <md-layout md-gutter class="charts-wrapper">
            <md-layout md-flex="33">
                <h1>Top Food Items</h1>
                <md-table class="popular-items-table">
                    <md-table-header>
                        <md-table-row>
                            <md-table-head>Name</md-table-head>
                            <md-table-head md-numeric>#Ordered</md-table-head>
                        </md-table-row>
                    </md-table-header>

                    <md-table-body>
                        <md-table-row v-for="(food, index) in popularItems.Food">
                            <md-table-cell class="meal-name">{{food.name}}</md-table-cell>
                            <md-table-cell md-numeric class="ordered-count">{{food.number}}</md-table-cell>
                        </md-table-row>
                    </md-table-body>
                </md-table>
            </md-layout>
            <md-layout md-flex="33">
                <h1>Top Drink Items</h1>
                <md-table class="popular-items-table">
                    <md-table-header>
                        <md-table-row>
                            <md-table-head>Name</md-table-head>
                            <md-table-head md-numeric>#Ordered</md-table-head>
                        </md-table-row>
                    </md-table-header>

                    <md-table-body>
                        <md-table-row v-for="(drink, index) in popularItems.Drink" :key="index">
                            <md-table-cell class="meal-name">{{drink.name}}</md-table-cell>
                            <md-table-cell md-numeric class="ordered-count">{{drink.number}}
                            </md-table-cell>
                        </md-table-row>
                    </md-table-body>
                </md-table>
            </md-layout>
            <md-layout md-flex="33">
                <h1>Top Other Items</h1>
                <md-table class="popular-items-table">
                    <md-table-header>
                        <md-table-row>
                            <md-table-head>Name</md-table-head>
                            <md-table-head md-numeric>#Ordered</md-table-head>
                        </md-table-row>
                    </md-table-header>

                    <md-table-body>
                        <md-table-row v-for="(other, index) in popularItems.Other" :key="index">
                            <md-table-cell class="meal-name">Drink Name</md-table-cell>
                            <md-table-cell md-numeric class="ordered-count">10
                            </md-table-cell>
                        </md-table-row>
                    </md-table-body>
                </md-table>
            </md-layout>
        </md-layout>
    </div>
</template>

<script>
    import myDatepicker from 'vue-datepicker'
    import moment from 'moment';
    let epicuri = require('../internal/epicuri.js');

    export default {
        data: function () {
            return {
                sessionsData: [],
                itemsData: [],
                itemsOrderedData: [
                    {name: "Food", data: [["0", 32], ["1", 46], ["2", 28], ["3", 21], ["4", 20], ["5", 13], ["6", 27]]},
                    {
                        name: "Non Food Items",
                        data: [["0", 32], ["1", 46], ["2", 28], ["3", 21], ["4", 20], ["5", 13], ["6", 27]]
                    }
                ],
                startTime: {
                    time: ''
                },
                endTime: {
                    time: ''
                },
                option: {
                    type: 'day',
                    week: ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'],
                    month: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
                    format: 'DD-MM-YYYY',
                    placeholder: 'Start date',
                    inputStyle: {
                        'display': 'inline-block',
                        'padding': '6px',
                        'line-height': '22px',
                        'font-size': '16px',
                        'border': '2px solid #fff',
                        'box-shadow': '0 1px 3px 0 rgba(0, 0, 0, 0.2)',
                        'border-radius': '2px',
                        'color': '#5F5F5F'
                    },
                    color: {
                        header: '#EE6E09',
                        headerText: '#FFF'
                    },
                    buttons: {
                        ok: 'Ok',
                        cancel: 'Cancel'
                    },
                    overlayOpacity: 0.5, // 0.5 as default
                    dismissible: true // as true as default
                },
                option2: {
                    type: 'day',
                    week: ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'],
                    month: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
                    format: 'DD-MM-YYYY',
                    placeholder: 'End date',
                    inputStyle: {
                        'display': 'inline-block',
                        'padding': '6px',
                        'line-height': '22px',
                        'font-size': '16px',
                        'border': '2px solid #fff',
                        'box-shadow': '0 1px 3px 0 rgba(0, 0, 0, 0.2)',
                        'border-radius': '2px',
                        'color': '#5F5F5F'
                    },
                    color: {
                        header: '#EE6E09',
                        headerText: '#FFF'
                    },
                    buttons: {
                        ok: 'Ok',
                        cancel: 'Cancel'
                    },
                    overlayOpacity: 0.5, // 0.5 as default
                    dismissible: true // as true as default
                },
                timeoption: {
                    type: 'min',
                    week: ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'],
                    month: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
                    format: 'DD-MM-YYYY HH:mm'
                },
                multiOption: {
                    type: 'multi-day',
                    week: ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'],
                    month: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
                    format: "DD-MM-YYYY HH:mm"
                },
                limit: [
                    {
                        type: 'weekday',
                        available: [0, 1, 2, 3, 4, 5, 6]
                    },
                    {
                        type: 'fromto',
                        from: this.currentTime,
                        to: this.currentTime
                    }
                ],
                currentTime: '',
                itemsReport: [],
                sessionsReport: [],
                salesReport: [],
                popularItems: {},
                reportPeriod: ""
            }
        },
        route: {
            canReuse: true
        },
        components: {
            'date-picker': myDatepicker,
            moment
        },
        mounted: function () {
            this.onGetBi();
        },
        methods: {
            onGetBi: function () {
                let periodId = parseInt(this.$route.query.periodId);
                let currentDate = moment().format('DD-MM-YYYY');
                let forever = moment().format('01-01-2000');


                switch (periodId) {
                    case 0:
                        this.reportPeriod = "WEEK";
                        this.customBIMetrics(moment().subtract(7, 'd').format('DD-MM-YYYY'), currentDate);
                        break;
                    case 1:
                        this.reportPeriod = "MONTH";
                        this.customBIMetrics(moment().subtract(28, 'd').format('DD-MM-YYYY'), currentDate);
                        break;
                    case 2:
                        this.reportPeriod = "3 MONTHS";
                        this.customBIMetrics(moment().subtract(84, 'd').format('DD-MM-YYYY'), currentDate);
                        break;
                    case 3:
                        this.reportPeriod = "YEAR";
                        this.customBIMetrics(moment().subtract(336, 'd').format('DD-MM-YYYY'), currentDate);
                        break;
                    case 4:
                        this.reportPeriod = "FOREVER";
                        this.customBIMetrics(forever, currentDate);
                        break;
                    default:
                        this.reportPeriod = "Custom";
                }
            },
            customBIMetrics: function (startDate, endDate) {
                epicuri.getBIMetrics(this.$http, startDate, endDate,
                    (response) => {
                        this.itemsReport = response.body.averageItemsReport;
                        this.sessionsReport = response.body.averageSessionsReport;
                        this.salesReport = response.body.epicuriSalesReport;
                        this.popularItems = response.body.popularItemsReport;
                        this.sessionsData = [
                            {
                                name: 'Seated',
                                data: [
                                    [
                                        'Monday', this.sessionsReport['monday'].seated
                                    ],
                                    [
                                        'Tuesday', this.sessionsReport['tuesday'].seated
                                    ],
                                    [
                                        'Wednesday', this.sessionsReport['wednesday'].seated
                                    ],
                                    [
                                        'Thursday', this.sessionsReport['thursday'].seated
                                    ],
                                    [
                                        'Friday', this.sessionsReport['friday'].seated
                                    ],
                                    [
                                        'Saturday', this.sessionsReport['saturday'].seated
                                    ],
                                    [
                                        'Sunday', this.sessionsReport['sunday'].seated
                                    ],
                                ]
                            },
                            {
                                name: 'Delivery',
                                data: [
                                    [
                                        'Monday', this.sessionsReport['monday'].delivery
                                    ],
                                    [
                                        'Tuesday', this.sessionsReport['tuesday'].delivery
                                    ],
                                    [
                                        'Wednesday', this.sessionsReport['wednesday'].delivery
                                    ],
                                    [
                                        'Thursday', this.sessionsReport['thursday'].delivery
                                    ],
                                    [
                                        'Friday', this.sessionsReport['friday'].delivery
                                    ],
                                    [
                                        'Saturday', this.sessionsReport['saturday'].delivery
                                    ],
                                    [
                                        'Sunday', this.sessionsReport['sunday'].delivery
                                    ],
                                ]
                            },
                            {
                                name: 'Collection',
                                data: [
                                    [
                                        'Monday', this.sessionsReport['monday'].collection
                                    ],
                                    [
                                        'Tuesday', this.sessionsReport['tuesday'].collection
                                    ],
                                    [
                                        'Wednesday', this.sessionsReport['wednesday'].collection
                                    ],
                                    [
                                        'Thursday', this.sessionsReport['thursday'].collection
                                    ],
                                    [
                                        'Friday', this.sessionsReport['friday'].collection
                                    ],
                                    [
                                        'Saturday', this.sessionsReport['saturday'].collection
                                    ],
                                    [
                                        'Sunday', this.sessionsReport['sunday'].collection
                                    ],
                                ]
                            },
                        ];
                        this.itemsData = [
                            {
                                name: 'Drink',
                                data: [
                                    [
                                        'Monday', this.itemsReport['monday'].Drink
                                    ],
                                    [
                                        'Tuesday', this.itemsReport['tuesday'].Drink
                                    ],
                                    [
                                        'Wednesday', this.itemsReport['wednesday'].Drink
                                    ],
                                    [
                                        'Thursday', this.itemsReport['thursday'].Drink
                                    ],
                                    [
                                        'Friday', this.itemsReport['friday'].Drink
                                    ],
                                    [
                                        'Saturday', this.itemsReport['saturday'].Drink
                                    ],
                                    [
                                        'Sunday', this.itemsReport['sunday'].Drink
                                    ],
                                ]
                            },
                            {
                                name: 'Food',
                                data: [
                                    [
                                        'Monday', this.itemsReport['monday'].Food
                                    ],
                                    [
                                        'Tuesday', this.itemsReport['tuesday'].Food
                                    ],
                                    [
                                        'Wednesday', this.itemsReport['wednesday'].Food
                                    ],
                                    [
                                        'Thursday', this.itemsReport['thursday'].Food
                                    ],
                                    [
                                        'Friday', this.itemsReport['friday'].Food
                                    ],
                                    [
                                        'Saturday', this.itemsReport['saturday'].Food
                                    ],
                                    [
                                        'Sunday', this.itemsReport['sunday'].Food
                                    ],
                                ]
                            },
                            {
                                name: 'Other',
                                data: [
                                    [
                                        'Monday', this.itemsReport['monday'].Other
                                    ],
                                    [
                                        'Tuesday', this.itemsReport['tuesday'].Other
                                    ],
                                    [
                                        'Wednesday', this.itemsReport['wednesday'].Other
                                    ],
                                    [
                                        'Thursday', this.itemsReport['thursday'].Other
                                    ],
                                    [
                                        'Friday', this.itemsReport['friday'].Other
                                    ],
                                    [
                                        'Saturday', this.itemsReport['saturday'].Other
                                    ],
                                    [
                                        'Sunday', this.itemsReport['sunday'].Other
                                    ],
                                ]
                            },
                        ]
                    },
                    (response) => {
                        console.log("ERROR: " + response)
                    }
                )
            }
        },
        watch: {
            '$route': function (newRoute, oldRoute) {
                this.onGetBi();
            },
        },
    }
</script>

<style>
    .bi-wrapper {
        padding: 50px;
    }

    .charts-wrapper {
        margin: 70px auto;
    }

    .charts-wrapper h1 {
        width: 100%;
    }

    .charts-wrapper .epicuri-generated-layout {
        margin-right: 1%;
    }

    .charts-wrapper:nth-child(1) {
        margin-top: 50px;
    }

    .generated-by-epicuri {
        font-weight: bolder;
        font-size: 30px !important;
        color: #EE6E09;
        padding-bottom: 0 !important;
    }

    .epicuri-title {
        font-weight: bold;
        color: #EE6E09;
    }

    .date-label {
        font-size: 18px;
        font-weight: bold;
    }

    .generate-btn {
        width: 150px;
        background-color: #ddd;
        margin: 0 10px 0 30px;
        padding: 0;
        height: 40px;
    }

    .ordered-count {
        font-weight: bold;
    }

    .meal-name {
        font-weight: bold;
    }

    .popular-items-table {
        width: 80%;
        max-height: 300px;
        height: 300px;
        border: 1px solid #ddd;
    }

    .bi-headline {
        text-align: center;
        margin-bottom: 100px;
    }

</style>
