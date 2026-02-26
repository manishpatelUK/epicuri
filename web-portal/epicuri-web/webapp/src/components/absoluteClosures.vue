<template>
    <div>
        <h1>ABSOLUTE CLOSURES</h1>
        <md-tabs md-fixed class="absolute-closures-tab" @change="switchView">
            <md-tab id="reservations" md-label="Reservations">
                <md-layout md-gutter>
                    <md-layout>
                        <md-card class="absolute-closure-card" v-for="(absoluteClosure, index) in absoluteClosuresData">
                            <md-card-area md-inset>
                                <md-card-header>
                                    <span class="time-icon"><md-icon>access_time</md-icon></span>
                                    <span class="md-title">Closure</span>
                                </md-card-header>
                            </md-card-area>

                            <md-card-content>
                                <h3>{{absoluteClosure.start}}</h3>
                                <span>to</span>
                                <h3>{{absoluteClosure.end}}</h3>
                            </md-card-content>
                            <md-card-actions>
                                <div class="card-reservation">
                                    <md-button class="md-fab md-mini remove-closure"
                                               @click="openDialog('delete_closure_dialog', index)">
                                        <md-icon>remove</md-icon>
                                        <!--<md-tooltip md-direction="right">Delete closure</md-tooltip>-->

                                    </md-button>
                                    <md-button class="md-fab md-mini md-warn edit-closure"
                                               @click="openEditDialog('edit_closure_dialog', index)">
                                        <md-icon>edit</md-icon>
                                        <!--<md-tooltip md-direction="left">Edit closure</md-tooltip>-->

                                    </md-button>
                                </div>
                            </md-card-actions>
                        </md-card>
                    </md-layout>
                </md-layout>
            </md-tab>
            <md-tab id="takeaways" md-label="Takeaways">
                <md-layout md-gutter>
                    <md-layout>
                        <md-card class="absolute-closure-card" v-for="(absoluteClosure, index) in absoluteClosuresData">
                            <md-card-area md-inset>
                                <md-card-header>
                                    <span class="time-icon"><md-icon>access_time</md-icon></span>
                                    <span class="md-title">Closure</span>
                                </md-card-header>
                            </md-card-area>

                            <md-card-content>
                                <h3>{{absoluteClosure.start}}</h3>
                                <span>to</span>
                                <h3>{{absoluteClosure.end}}</h3>
                            </md-card-content>
                            <md-card-actions>
                                <div class="card-reservation">
                                    <md-button class="md-fab md-mini remove-closure"
                                               @click="openDialog('delete_closure_dialog', index)">
                                        <md-icon>remove</md-icon>
                                        <md-tooltip md-direction="right">Delete closure</md-tooltip>

                                    </md-button>
                                    <md-button class="md-fab md-mini md-warn edit-closure"
                                               @click="openEditDialog('edit_closure_dialog', index)">
                                        <md-icon>edit</md-icon>
                                        <md-tooltip md-direction="left">Edit closure</md-tooltip>

                                    </md-button>
                                </div>
                            </md-card-actions>
                        </md-card>
                    </md-layout>
                </md-layout>
            </md-tab>
        </md-tabs>
        <md-button class="md-fab md-primary add-closure"
                   @click="openDialog('add_closure_dialog')">
            <md-icon>add</md-icon>
            <!--<md-tooltip md-direction="top">Add closure</md-tooltip>-->
        </md-button>


        <!-- ADD NEW CLOSURE MODAL -->

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="add_closure_dialog" class="add-closure-dialog">
            <md-dialog-title>Add new closure</md-dialog-title>
            <md-dialog-content>
                <md-input-container class="closures-modal-container">
                    <h4 class="modal-headers">Start date</h4>
                    <date-picker :date="startTime" :option="timeOption" :limit="limit"></date-picker>
                    <h4 class="modal-headers">End date</h4>
                    <date-picker :date="endTime" :option="timeOption" :limit="limit"></date-picker>
                </md-input-container>
                <!--<p class="error-message" v-if="isErrorEmpty">Please select all start and end times</p>-->
                <!--<p class="error-message" v-if="isError">Start time cannot be later than end time</p>-->
            </md-dialog-content>

            <md-dialog-actions>
                <md-button class="md-primary"
                           @click="addClosure(startTime.time, endTime.time)">Add
                </md-button>
                <md-button class="md-primary" @click="closeDialog('add_closure_dialog')">Cancel</md-button>
            </md-dialog-actions>
        </md-dialog>

        <!-- EDIT CLOSURE MODAL -->

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="edit_closure_dialog" class="add-closure-dialog">
            <md-dialog-title>Edit closure</md-dialog-title>
            <md-dialog-content>
                <md-input-container class="closures-modal-container">
                    <h4 class="modal-headers">Start date</h4>
                    <date-picker :date="startTime" :option="timeOption" :limit="limit"></date-picker>
                    <h4 class="modal-headers">End date</h4>
                    <date-picker :date="endTime" :option="timeOption" :limit="limit"></date-picker>
                </md-input-container>
                <!--<p class="error-message" v-if="isErrorEmpty">Please select start and end dates</p>-->
                <!--<p class="error-message" v-if="isError">Start time cannot be later than end time</p>-->
            </md-dialog-content>

            <md-dialog-actions>
                <md-button class="md-primary"
                           @click="editClosure(startTime.time, endTime.time)">Save
                </md-button>
                <md-button class="md-primary" @click="closeDialog('edit_closure_dialog')">Cancel</md-button>
            </md-dialog-actions>
        </md-dialog>

        <!-- DELETE CLOSURE MODAL -->

        <md-dialog md-open-from="#fab" class="delete-closure-modal" md-close-to="#fab" ref="delete_closure_dialog">
            <md-dialog-title>Delete closure</md-dialog-title>
            <md-dialog-content>
                <md-icon>warning</md-icon>

                <p>Are you sure you want to delete this closure?</p>
            </md-dialog-content>
            <md-dialog-actions>
                <md-button class="md-primary"
                           @click="deleteClosure()">Yes
                </md-button>
                <md-button class="md-primary" @click="closeDialog('delete_closure_dialog')">No</md-button>
            </md-dialog-actions>
        </md-dialog>
    </div>
</template>

<script>
    import MdCardActions from "../../node_modules/vue-material/src/components/mdCard/mdCardActions.vue";

    let epicuri = require('../internal/epicuri.js');
//    import myDatepicker from 'vue-datepicker';
    let datetimepicker = require('./vue-datepicker.vue')
    import moment from 'moment';

    export default {
        data: function () {
            return {
                absoluteClosuresData: [],
                startTime: {
                    time: ''
                },
                endTime: {
                    time: ''
                },
                timeOption: {
                    type: 'min',
                    week: ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'],
                    month: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
                    format: 'YYYY-MM-DD HH:mm',
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
                    overlayOpacity: 0.5,
                    dismissible: true,
                    placeholder: 'YYYY-MM-DD 00:00',

                },
                limit: [
                    {
                        type: 'weekday',
                        available: [0, 1, 2, 3, 4, 5, 6]
                    },
                    {
                        type: 'fromto',
                        from: moment().subtract(1, 'day').format('YYYY-MM-DD HH:mm'),
                        to: ''
                    }
                ],
                isError: false,
                isErrorEmpty: false,
                closureIndex: 0
            }
        },
        components: {
            MdCardActions,
            'date-picker': datetimepicker,
            moment
        },
        mounted: function () {
//            this.onLoad('RESERVATION');
        },
        watch: {
            isError: function (whaaat) {
                setTimeout(function () {
                    console.log("test error false")
                },1500)
            }
        },
        methods: {
            switchView: function (tabIndex) {

                switch (tabIndex) {
                    case 0:
                        this.$router.push({name: "absolute_closures", query: {type: "RESERVATION"}});
                        this.onLoad("RESERVATION");
                        break;
                    case 1:
                        this.$router.push({name: "absolute_closures", query: {type: "TAKEAWAY"}});
                        this.onLoad("TAKEAWAY");
                        break;
                }
            },
            onLoad: function (type) {

                epicuri.getAbsoluteClosures(this.$http, type,
                    (response) => {
                        this.absoluteClosuresData = response.body.closures;
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    }
                )
            },
            addClosure: function (startTime, endTime) {

                let payloadObject = {
                    start: startTime,
                    end: endTime
                };

                let start = new Date(payloadObject.start);
                let end = new Date(payloadObject.end);

                if (isNaN(start) || isNaN(end)) {
                    alert("Please select value from date picker.");
                } else if (start > end) {
                    alert("Start date can not be later than end date.");
                } else {

                    this.absoluteClosuresData.push(payloadObject);

                    let payload = {
                        closures: this.absoluteClosuresData
                    };

                    epicuri.sendAbsoluteClosure(this.$http, this.$route.query.type, payload,
                        (response) => {
                            this.onLoad(this.$route.query.type);
                            this.closeDialog("add_closure_dialog");
                        },
                        (response) => {
                            console.log("ERROR" + response)
                        }
                    )
                }
            },
            openDialog: function (ref, closureIndex) {
                this.closureIndex = closureIndex;
                this.$refs[ref].open();
                this.startTime.time = "";
                this.endTime.time = "";
            },
            openEditDialog: function (ref, closureIndex) {
                this.closureIndex = closureIndex;
                this.$refs[ref].open();

//                let momentObj = moment(this.absoluteClosuresData[closureIndex].start, 'MMMM-Do-YYYY HH:mm');
//                let momentStringStart = momentObj.format('YYYY-MM-DD HH:mm');
//                let momentObj1 = moment(this.absoluteClosuresData[closureIndex].end, 'MMMM-Do-YYYY HH:mm');
//                let momentStringEnd = momentObj1.format('YYYY-MM-DD HH:mm');

                this.startTime.time = this.absoluteClosuresData[closureIndex].start;
                this.endTime.time = this.absoluteClosuresData[closureIndex].end;

            },
            closeDialog(ref) {
                this.$refs[ref].close();
                this.isError = false;
                this.isErrorEmpty = false;
            },
            deleteClosure: function () {

                this.absoluteClosuresData.splice(this.closureIndex, 1);

                let payload = {
                    closures: this.absoluteClosuresData
                };

                epicuri.sendAbsoluteClosure(this.$http, this.$route.query.type, payload,
                    (response) => {
                        this.onLoad(this.$route.query.type);
                        this.closeDialog("delete_closure_dialog");
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    }
                )
            },
            editClosure: function (startTime, endTime) {
                let payloadObject = {
                    start: startTime,
                    end: endTime
                };

                let start = new Date(payloadObject.start);
                let end = new Date(payloadObject.end);

                if (isNaN(start) || isNaN(end)) {
//                    this.isErrorEmpty = true;
                    alert("Please select value from date picker.");

                } else if (start > end) {
//                    this.isError = true;
                    alert("Start date can not be later than end date.");

                } else {
                    this.isError = false;
                    this.isErrorEmpty = false;

                    this.absoluteClosuresData[this.closureIndex] = payloadObject;

                    let payload = {
                        closures: this.absoluteClosuresData
                    };

                    epicuri.sendAbsoluteClosure(this.$http, this.$route.query.type, payload,
                        (response) => {
                            this.onLoad(this.$route.query.type);
                            this.closeDialog("edit_closure_dialog");
                        },
                        (response) => {
                            console.log("ERROR" + response)
                        }
                    )
                }
            },
            formatDate: function (array, isReverse) {

                switch (isReverse) {
                    case true:
                        for (let i = 0; i < array.length; i++) {
                            let obj = array[i];
                            let momentObj = moment(obj.start, 'YYYY-MM-DD HH:mm');
                            let momentString = momentObj.format('MMMM-Do-YYYY HH:mm');
                            let momentObj1 = moment(obj.end, 'YYYY-MM-DD HH:mm');
                            let momentString1 = momentObj1.format('MMMM-Do-YYYY HH:mm');
                            let formatedObject = {
                                start: momentString.replace(/-/g, ' '),
                                end: momentString1.replace(/-/g, ' ')
                            };
                            array[i] = formatedObject;
                        }
                        break;
                    case false:
                        for (let i = 0; i < array.length; i++) {
                            let obj = array[i];
                            let momentObj = moment(obj.start, 'MMMM-Do-YYYY HH:mm');
                            let momentString = momentObj.format('YYYY-MM-DD HH:mm');
                            let momentObj1 = moment(obj.end, 'MMMM-Do-YYYY HH:mm');
                            let momentString1 = momentObj1.format('YYYY-MM-DD HH:mm');
                            let formatedObject = {
                                start: momentString,
                                end: momentString1
                            };

                            array[i] = formatedObject;
                        }
                        break;
                }
                return array;
            }
        }
    }
</script>

<style>

    .absolute-closures-tab .md-tabs-navigation-scroll-container .md-active {
        border-right: 2px solid #EE6E09;
        background-color: #EE6E09;
    }

    .remove-closure {
        float: right;
        margin-left: 10px !important;
    }

    .add-closure {
        position: fixed;
        bottom: 8vh;
        right: 5vw;
    }

    .remove-closure > i, .edit-closure > i {
        width: 20px;
        min-width: 20px;
        min-height: 20px;
        height: 20px;
        font-size: 20px;
    }

    .add-closure > i {
        font-size: 30px;
        width: 30px;
        min-width: 30px;
        min-height: 30px;
        height: 30px;
    }

    .add-closure-dialog > .md-dialog {
        height: 50vh;
        width: 400px;
    }

    .modal-headers {
        width: 100%;
    }

    .closures-modal-container {
        display: block;
    }

    .closures-modal-container:after {
        background-color: transparent;
    }

    .absolute-closure-card {
        flex-basis: 15%;
        margin-bottom: 20px;
        margin-right: 20px;
        text-align: center;
    }

    #reservations {
        padding-left: 2px;
    }

    #takeaways {
        padding-left: 2px;
    }

    .time-icon {
        padding-top: 4px;
        vertical-align: top;
        display: inline-block;
        color: #EE6E09;
    }

    .absolute-closure-card > .md-card-actions {
        border-top: 1px solid #ddd;
        margin: 0 16px;
        padding-right: 0;
    }

    .delete-closure-modal {
        text-align: center;
    }

    .delete-closure-modal i {
        font-size: 40px;
        width: 40px;
        min-width: 40px;
        height: 40px;
    }

    .delete-closure-modal p {
        margin: 15px 0 10px 0;
    }

    .absolute-closures-tab .md-tab-header {
        background-color: #545454;
        color: #fff !important;
    }

    .absolute-closures-tab .md-tab-indicator {
        background-color: #EE6E09 !important;
    }

    .error-message {
        color: red;
    }

    @media screen and (max-width: 1780px) {

        .absolute-closure-card {
            flex-basis: 18%;
        }
    }

    @media screen and (max-width: 1580px) {

        .absolute-closure-card {
            flex-basis: 18%;
        }
    }

    @media screen and (max-width: 1440px) {

        .absolute-closure-card {
            flex-basis: 22%;
        }
    }

    @media screen and (max-width: 1280px) {

        .absolute-closure-card {
            flex-basis: 30%;
        }
    }

    @media screen and (max-width: 1024px) {

        .absolute-closure-card {
            flex-basis: 30%;
        }

        #reservation, #takeaways {
            padding-left: 20px;
        }

        .add-closure-dialog > .md-dialog {
            height: 70vh;
            width: 50%;
        }
    }

</style>
