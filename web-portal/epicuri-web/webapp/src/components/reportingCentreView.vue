<template>
    <div>

        <md-layout md-gutter class="report-wrapper">

            <md-layout md-flex="100" class="report-wrapper-layout">
                <h1>Create Report: {{this.reportHeadlineName}}</h1>
                <p>
                    {{this.reportDetails}}
                </p>
            </md-layout>
            <md-layout md-flex="100" class="report-wrapper-layout">
                <h3 v-if="this.reportName != 'paymentSense'">Start Date：</h3>
                <date-picker :date="startTime" :option="option" :limit="limit" class="date-picker-custom"
                             v-show="this.reportName != 'paymentSense'"></date-picker>

                <h3 v-if="this.reportName != 'paymentSense'">End Date：</h3>
                <date-picker :date="endTime" :option="option2" :limit="limit" class="date-picker-custom"
                             v-if="this.reportName != 'paymentSense'"></date-picker>
            </md-layout>
            <md-layout md-flex="100" class="report-wrapper-layout" v-if="this.reportName == 'customerDetails'">
                <h1>Privacy Policy</h1>
                <p>
                    The following report contains customer contact information. Please tick this box to acknowledge that
                    all data provided in this report is still bound under the terms and conditions of Epicuri.
                </p>
                <md-checkbox class="privacy-policy" id="privacy_policy" name="privacy_policy" v-model="privacy_policy">
                    I agree with Privacy Policy
                </md-checkbox>
            </md-layout>
            <md-layout md-flex="100" class="report-wrapper-layout"  v-if="this.reportName == 'itemsAggregated'">
                <md-checkbox v-model="aggregateByPLU">
                    Aggregate by SKU
                </md-checkbox>
            </md-layout>
            <md-button @click.native="getReportMetrics(startTime.time, endTime.time, aggregateByPLU)" class="generate-report"
                       v-if="this.reportName != 'paymentSense'">
                Generate Report
            </md-button>

            <md-layout md-flex="100" md-column v-if="this.reportName == 'paymentSense'">
                <md-subheader>Terminals</md-subheader>
                <md-table v-once v-if="psTerminals.length>0">
                    <md-table-header>
                        <md-table-row>
                            <md-table-head>Terminal ID</md-table-head>
                            <md-table-head>Location</md-table-head>
                            <md-table-head>Actions</md-table-head>
                        </md-table-row>
                    </md-table-header>
                    <md-table-body>
                        <md-table-row v-for="terminal in psTerminals">
                            <md-table-cell>{{terminal.tpi}}</md-table-cell>
                            <md-table-cell>{{terminal.location}}</md-table-cell>
                            <md-table-cell>
                                <md-button class="md-icon-button" @click="generatePSReport(terminal.tpi, 'END_OF_DAY')">
                                    <md-icon>create_new_folder</md-icon>
                                    <md-tooltip>Create a new END OF DAY report for this PDQ and store it in the database
                                    </md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button" @click="generatePSReport(terminal.tpi, 'BANKING')">
                                    <md-icon>account_balance</md-icon>
                                    <md-tooltip>Create a new BANKING report for this PDQ and store it in the database
                                    </md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button" @click="generatePSReport(terminal.tpi, 'X_BALANCE')">
                                    <md-icon>shuffle</md-icon>
                                    <md-tooltip>Create a new X BALANCE report for this PDQ and store it in the database
                                    </md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button" @click="generatePSReport(terminal.tpi, 'Z_BALANCE')">
                                    <md-icon>call_split</md-icon>
                                    <md-tooltip>Create a new Z BALANCE report for this PDQ and store it in the database
                                    </md-tooltip>
                                </md-button>
                            </md-table-cell>
                        </md-table-row>
                    </md-table-body>
                </md-table>
                <h1 v-else>
                    PAYMENTSENSE: TERMINALS NOT ONLINE
                </h1>

                <md-subheader>Reports</md-subheader>
                <md-table v-once v-if="psReports.length>0">
                    <md-table-header>
                        <md-table-row>
                            <md-table-head>Terminal ID</md-table-head>
                            <md-table-head>Report Time</md-table-head>
                            <md-table-head>Report Type</md-table-head>
                            <md-table-head>Email</md-table-head>
                            <md-table-head>View</md-table-head>
                        </md-table-row>
                    </md-table-header>
                    <md-table-body>
                        <md-table-row v-for="report in psReports">
                            <md-table-cell>{{report.tpi}}</md-table-cell>
                            <md-table-cell>{{report.reportTime}}</md-table-cell>
                            <md-table-cell>{{report.reportType}}</md-table-cell>
                            <md-table-cell>
                                <md-button class="md-icon-button" @click="emailPSReport(report.requestId)">
                                    <md-icon>email</md-icon>
                                    <md-tooltip>Email spreadsheet to {{restaurant.internalEmailAddress}}</md-tooltip>
                                </md-button>
                            </md-table-cell>
                            <md-table-cell>
                                <md-button class="md-icon-button" @click="showPSReport(report)">
                                    <md-icon>view_headline</md-icon>
                                    <md-tooltip>Show underlying report</md-tooltip>
                                </md-button>
                            </md-table-cell>
                        </md-table-row>
                    </md-table-body>
                </md-table>
                <h1 v-else>
                    PAYMENTSENSE: REPORTS NOT FOUND
                </h1>
            </md-layout>

        </md-layout>
        <md-dialog-alert
                :md-title="warningTitle"
                :md-content-html="contentHtmlWarning"
                ref="warning-dialog">

        </md-dialog-alert>
        <md-dialog-alert
                :md-title="ppTitle"
                :md-content-html="contentHtmlPP"
                ref="pp-warning-dialog">
        </md-dialog-alert>
        <md-dialog-alert
                :md-title="psTitle"
                :md-content-html="paymentSenseDialogMessage"
                ref="ps-warning-dialog">
        </md-dialog-alert>
        <md-dialog-alert
                :md-title="psErrorTitle"
                :md-content-html="paymentSenseNotFoundMessage"
                ref="psnf-warning-dialog">
        </md-dialog-alert>
        <md-dialog-alert
                :md-title="psErrorTitle"
                :md-content-html="paymentSenseErrorDialogMessage"
                ref="ps-report-error-dialog">
        </md-dialog-alert>
        <md-dialog-alert
                :md-title="psTitle"
                :md-content-html="paymentSenseSuccessDialogMessage"
                ref="ps-report-success-dialog">
        </md-dialog-alert>
        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="ps-wait-for-reports">
            <md-dialog-title>Requesting report...</md-dialog-title>
            <md-dialog-content>
                This could take up to a 2 minutes
                <md-progress md-indeterminate></md-progress>
            </md-dialog-content>
            <md-dialog-actions>
                <md-button class="md-primary" @click="closeDialog('ps-wait-for-reports')">Cancel</md-button>
            </md-dialog-actions>
        </md-dialog>
        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="show_report">
            <md-dialog-title>Report Data</md-dialog-title>
            <md-dialog-content>
                <a>This is the underlying data from PaymentSense. Click the Email icon to receive an Excel Spreadsheet</a>
                <br>
                {{psReportToShow}}
            </md-dialog-content>
            <md-dialog-actions>
                <md-button class="md-primary" @click="closeDialog('show_report')">OK</md-button>
            </md-dialog-actions>
        </md-dialog>

        <md-snackbar ref="emailPSSnackbar" :md-duration="snackDuration">
            <span>{{emailPSSnackbarMessage}}</span>
            <md-button class="md-accent" md-theme="light-blue" @click="$refs.emailPSSnackbar.close()">Retry</md-button>
        </md-snackbar>
    </div>
</template>

<script>
    import myDatepicker from 'vue-datepicker'
    import MdDialogAlert from "../../node_modules/vue-material/src/components/mdDialog/presets/mdDialogAlert.vue";

    let epicuri = require('../internal/epicuri.js');

    export default {
        data: function () {
            return {
                // for Vue 2.0
                startTime: {
                    time: ''
                },
                endTime: {
                    time: ''
                },
                option: {
                    type: 'min',
                    week: ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'],
                    month: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
                    format: 'DD-MM-YYYY HH:mm',
                    placeholder: 'Start date and time',
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
                    type: 'min',
                    week: ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'],
                    month: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
                    format: 'DD-MM-YYYY HH:mm',
                    placeholder: 'End date and time',
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
                    format: 'YYYY-MM-DD HH:mm'
                },
                multiOption: {
                    type: 'multi-day',
                    week: ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'],
                    month: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
                    format: "YYYY-MM-DD HH:mm"
                },
                limit: [
                    {
                        type: 'weekday',
                        available: [0, 1, 2, 3, 4, 5, 6]
                    },
                    {
                        type: 'fromto',
                        from: '',
                        to: ''
                    }
                ],
                reportName: '',
                reportDetails: '',
                reportHeadlineName: '',
                privacy_policy: false,
                warningTitle: "Warning",
                ppTitle: "Warning",
                psTitle: "PaymentSense",
                aggregateByPLU: false,
                psErrorTitle: "Error",
                contentHtmlWarning: "Please select start and end date!",
                contentHtmlPP: "Please accept Privacy Policy!",
                paymentSenseDialogMessage: "Reports currently not available.",
                paymentSenseErrorDialogMessage: "Reports could not be generated. Please contact Epicuri Support at support@epicuri.co.uk",
                paymentSenseSuccessDialogMessage: "Reports have been generated and stored in the Epicuri database",
                paymentSenseNotFoundMessage: "This restaurant does not use PaymentSense - no reports found.",

                //restaurant
                restaurant: {},

                //paymentsense
                psTerminals: [],
                psReports: [],
                psReportToShow:"",

                //snack
                snackDuration:4000,
                emailPSSnackbarMessage: ""

            }
        },
        route: {
            canReuse: true
        },
        components: {
            MdDialogAlert,
            'date-picker': myDatepicker
        },
        mounted: function () {
            this.onLoad();
        },
        methods: {
            onLoad: function () {
                this.reportName = this.$route.params.reportName;
                switch (this.reportName) {
                    case 'customerDetails':
                        this.reportHeadlineName = 'Customer Details';
                        this.reportDetails = 'Details of all the customers at the restaurant where contact details have been captured. Who are your high value and loyal customers?Use this reports to engage them.Encourage the use of the free Epicuri guest app to capture more of this priceless information!';
                        break;
                    case 'itemsAggregated':
                        this.reportHeadlineName = 'Menu Item Sales Aggregated';
                        this.reportDetails = 'Aggregated view of menu items sold. How many time each item was sold, when it was last sold, at what average price. Use this report to ​engineer your menu, identify your star items and revitalise the others. (The values here do not include adjustments made to the overall bill!). Optionally aggregate by SKU if you use SKUs in menu items.';
                        break;
                    case 'payments':
                        this.reportHeadlineName = 'Payments & Discounts Details Report';
                        this.reportDetails = 'Details of how payments have been made to your restaurant (cash, visa, amex etc) including any adjustments made to the overall bill. Use this report to understand how revenue is flowing into your books.';
                        break;
                    case 'itemDetails':
                        this.reportHeadlineName = 'Menu Item Sales Details';
                        this.reportDetails = 'Each individual sale from the Front-of-House. Details of the individual items sold, how they were sold, which items were voided and why.';
                        break;
                    case 'modifierSales':
                        this.reportHeadlineName = 'Modifier Details';
                        this.reportDetails = 'Details about each modifier sold for each order.'
                        break;
                    case 'revenues':
                        this.reportHeadlineName = 'On-Premise & Takeaway Revenue';
                        this.reportDetails = 'The details and totals listed by each table/on-site and every takeaway sold. Use this report to see how your tables and takeaways contribute to revenue across time.';
                        break;
                    case 'paymentSense':
                        this.reportHeadlineName = 'PaymentSense EOD';
                        this.reportDetails = 'PaymentSense End of Day Report. Generate a report for a particular PDQ, or pull reports stored in the Epicuri Database. For any questions regarding this report should be directed to PaymentSense Support.';
                        epicuri.getRestaurant(this.$http, (response) => {
                            this.restaurant = response.body;
                            this.getTerminalsAndReports();
                        }, {});
                        break;
                    case 'reservations':
                        this.reportHeadlineName = 'Reservations';
                        this.reportDetails = 'A list of all of your reservations with customer contact details.';
                        break;
                    case 'takeaways':
                        this.reportHeadlineName = 'Takeaways';
                        this.reportDetails = 'A list of all of your takeaway bookings with customer contact details.';
                        break;
                    case 'cashups':
                        this.reportHeadlineName = 'Cash Ups (Z Reports)';
                        this.reportDetails = 'A list of all cash ups (Z reports) created via the app. For payment and adjustment details please use the Payments & Discounts Details Report.';
                        break;
                }
            },
            getTerminalsAndReports: function() {
                epicuri.getPSTerminals(this.$http,
                    (response) => {
                        this.psTerminals = response.body.terminals;
                    },
                    (response) => {
                        console.log("Could not get PS terminals");
                    });
                epicuri.getPSReports(this.$http,
                    (response) => {
                        this.psReports = response.body;
                    },
                    (response) => {
                        console.log("Could not get PS reports");
                    });            },
            getReportMetrics: function (startDate, endDate, aggregateByPLU) {
                if (!startDate || !endDate) {
                    this.openDialog("warning-dialog")
                } else if (this.privacy_policy === false && this.reportName == 'customerDetails') {
                    this.openDialog("pp-warning-dialog")
                } else {
                    epicuri.getReportingMetrics(this.$http, this.$route.params.reportName, startDate, endDate, aggregateByPLU,
                        (response) => {
                            this.downloadCSV(this.$route.params.reportName + 'Report.csv', 'data:text/csv;charset=UTF-8,' + encodeURIComponent(response.body));
                        },
                        (response) => {
                            console.log("ERROR " + response)
                        }
                    )
                }

            },
            downloadCSV: function (fileName, urlData) {
                var aLink = document.createElement('a');
                aLink.download = fileName;
                aLink.href = urlData;

                var event = new MouseEvent('click');
                aLink.dispatchEvent(event);
            },
            openDialog: function (ref) {
                this.$refs[ref].open();
            },
            closeDialog: function (ref) {
                this.$refs[ref].close();
            },
            getPSReport: function () {
                epicuri.getPaymentSenseReport(this.$http,
                    (response) => {
                        console.log(response);
                        let blob = new Blob([response.body], {type: 'application/vnd.ms-excel'});
                        //let blob = new Blob([response.body],{type:'binary'});
                        //let blob = new Blob([response.body],{type:'application/vnd.ms-excel'});

                        let aLink = document.createElement('a');
                        aLink.download = this.$route.params.reportName + ".xls";
                        aLink.href = URL.createObjectURL(blob);

                        let event = new MouseEvent('click');
                        aLink.dispatchEvent(event);
                        //this.downloadCSV(this.$route.params.reportName + ".xls", blob);


                    },
                    (response) => {
                        switch (response) {
                            case 400:
                                this.openDialog("psnf-warning-dialog");
                                break;
                            case 0:
                                this.openDialog("ps-warning-dialog");
                                break;
                            default:
                                this.openDialog("ps-warning-dialog");
                        }
                        console.log("ERROR " + response)
                    }
                )
            },
            generatePSReport: function (terminal, type) {
                this.openDialog("ps-wait-for-reports");
                epicuri.postPSReportRequest(this.$http, terminal, type,
                    (response) => {
                        this.closeDialog("ps-wait-for-reports");
                        this.openDialog("ps-report-success-dialog");
                        this.getTerminalsAndReports();
                    },
                    (response) => {
                        this.closeDialog("ps-wait-for-reports");
                        this.openDialog("ps-report-error-dialog");
                    });
            },
            emailPSReport: function (requestId) {
                epicuri.postPSEmailReport(this.$http, requestId,
                    (response) => {
                        this.emailPSSnackbarMessage = "Email sent. Please check Junk/Spam as attachments might be caught by your spam filter.";
                        this.$refs.emailPSSnackbar.open();
                    },
                    (response) => {
                        this.emailPSSnackbarMessage = "Error in sending email; please contact support@epicuri.co.uk";
                        this.$refs.emailPSSnackbar.open();
                    });
            },
            showPSReport: function (report) {
                this.psReportToShow = JSON.stringify(report, null, 4);
                this.openDialog('show_report')
            }
        },
        watch: {
            '$route': function (newRoute, oldRoute) {
                this.onLoad();
                this.startTime = {
                    time: ''
                };

                this.endTime = {
                    time: ''
                }
            },
        },
    }
</script>

<style>

    h1, p {
        width: 100%;
    }

    .report-wrapper .report-wrapper-layout {
        margin: 50px auto 0 auto;
        justify-content: flex-start;
        text-align: left;
        padding: 0 30%;
    }

    .generate-report {
        margin: 50px auto;
        background-color: #ddd;
    }

    .date-picker-custom {
        width: 100%;
    }

    .datepickbox input {
        cursor: pointer;
    }

    .privacy-policy {
        cursor: pointer;
    }

    .report-wrapper h1 {
        line-height: 30px;
    }

</style>
