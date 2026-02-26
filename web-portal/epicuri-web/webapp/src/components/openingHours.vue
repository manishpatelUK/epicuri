<template>
    <div>
        <h1>OPENING HOURS</h1>
        <md-tabs md-fixed class="opening-hours-tab" @change="switchView">

            <md-tab id="reservation" md-label="Reservations">
                <h2>RESERVATIONS</h2>
                <md-layout md-gutter>
                    <md-layout>
                        <md-card class="opening-day-card" v-for="(openedHour,index) in openingHoursData"
                                 v-bind:class="{disableTimeSlot: openedHour.length == 0}">
                            <!--v-bind:class="{disableTimeSlot: JSON.stringify(openedHour[0]) == JSON.stringify(emptyTimeSlot)}"-->
                            <md-card-area md-inset>
                                <md-card-header>
                                    <h2 class="md-title">{{index}}</h2>
                                </md-card-header>
                            </md-card-area>

                            <md-card-content>
                                <h3 class="md-subheading">Today's availability</h3>
                                <div class="card-reservation">
                                    <h3 class="closed-label" v-if="openedHour.length == 0"> CLOSED ALL DAY </h3>
                                    <div class="time-slot-wrapper" v-for="(timeslot, timeslotIndex) in openedHour">
                                        <span class="access-time"><md-icon>access_time</md-icon></span>
                                        <md-button class="custom-time">{{timeslot.hourOpen}}:{{timeslot.minuteOpen}}
                                        </md-button>
                                        <span class="access-time"> - </span>
                                        <md-button class="custom-time">{{timeslot.hourClose}}:{{timeslot.minuteClose}}
                                        </md-button>
                                        <md-button class="md-icon-button custom-time-edit"
                                                   @click="openDialog('delete_slots_dialog', index);getTimeSlot(timeslot, timeslotIndex)">
                                            <md-icon>remove_circle_outline</md-icon>
                                        </md-button>
                                        <md-button class="md-icon-button custom-time-edit"
                                                   @click="openDialog('edit_slot_dialog', index);getTimeSlot(timeslot, timeslotIndex)">
                                            <md-icon>edit</md-icon>
                                        </md-button>

                                    </div>
                                </div>
                            </md-card-content>

                            <md-card-actions>
                                <md-button class="md-fab md-mini add-time-button"
                                           @click="openDialog('add_slot_dialog', index)">
                                    <md-icon>add</md-icon>
                                </md-button>
                                <md-button class="md-primary open-all-day-btn"
                                           @click="openDialog('open_all_day_dialog', index)"
                                           :disabled="JSON.stringify(openedHour[0]) == JSON.stringify(allDayTimeSlot)">
                                    Open All Day
                                </md-button>
                                <md-button class="md-primary close-all-day-btn"
                                           @click="openDialog('clear_slots_dialog', index)"
                                           :disabled="openedHour.length == 0">
                                    Close All Day
                                </md-button>
                            </md-card-actions>
                        </md-card>
                    </md-layout>
                </md-layout>
            </md-tab>

            <md-tab id="takeaway" md-label="Takeaways">
                <h2>TAKEAWAYS</h2>
                <md-layout md-gutter>
                    <md-layout>
                        <md-card class="opening-day-card"  v-for="(openedHour,index) in openingHoursData"
                                 v-bind:class="{disableTimeSlot: openedHour.length == 0}">
                            <md-card-area md-inset>
                                <md-card-header>
                                    <h2 class="md-title">{{index}}</h2>
                                </md-card-header>
                            </md-card-area>

                            <md-card-content>
                                <h3 class="md-subheading">Today's availability</h3>
                                <div class="card-reservation">
                                    <h3 class="closed-label" v-if="openedHour.length == 0"> CLOSED ALL DAY </h3>

                                    <div class="time-slot-wrapper" v-for="(timeslot, timeslotIndex) in openedHour">
                                        <span class="access-time"><md-icon>access_time</md-icon></span>
                                        <md-button class="custom-time">{{timeslot.hourOpen}}:{{timeslot.minuteOpen}}
                                        </md-button>
                                        <span class="access-time"> - </span>
                                        <md-button class="custom-time">{{timeslot.hourClose}}:{{timeslot.minuteClose}}
                                        </md-button>
                                        <md-button class="md-icon-button custom-time-edit"
                                                   @click="openDialog('delete_slots_dialog', index);getTimeSlot(timeslot, timeslotIndex)">
                                            <md-icon>remove_circle_outline</md-icon>
                                        </md-button>
                                        <md-button class="md-icon-button custom-time-edit"
                                                   @click="openDialog('edit_slot_dialog', index);getTimeSlot(timeslot, timeslotIndex)">
                                            <md-icon>edit</md-icon>
                                        </md-button>

                                    </div>
                                </div>
                            </md-card-content>

                            <md-card-actions>
                                <md-button class="md-fab md-mini add-time-button"
                                           @click="openDialog('add_slot_dialog', index)">
                                    <md-icon>add</md-icon>
                                </md-button>
                                <md-button class="md-primary open-all-day-btn"
                                           @click="openDialog('open_all_day_dialog', index)"
                                           :disabled="JSON.stringify(openedHour[0]) == JSON.stringify(allDayTimeSlot)">
                                    Open All Day
                                </md-button>
                                <md-button class="md-primary close-all-day-btn"
                                           @click="openDialog('clear_slots_dialog', index)"
                                           :disabled="openedHour.length == 0">
                                    Close All Day
                                </md-button>
                            </md-card-actions>
                        </md-card>
                    </md-layout>
                </md-layout>
            </md-tab>
        </md-tabs>

        <!-- OPEN ALL DAY MODAL -->

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="open_all_day_dialog">
            <md-dialog-title>Open all day</md-dialog-title>

            <md-dialog-content>
                Are you sure you want to open all day?
            </md-dialog-content>

            <md-dialog-actions>
                <md-button class="md-primary" @click.native="openAllDay()">Yes</md-button>
                <md-button class="md-primary" @click="closeDialog('open_all_day_dialog')">No</md-button>
            </md-dialog-actions>
        </md-dialog>

        <!-- CLEAR TIME SLOTS MODAL -->

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="clear_slots_dialog">
            <md-dialog-title>Clear time slot</md-dialog-title>

            <md-dialog-content>
                Are you sure you want to clear all time slots?
            </md-dialog-content>

            <md-dialog-actions>
                <md-button class="md-primary" @click.native="closeAllDay()">Yes</md-button>
                <md-button class="md-primary" @click="closeDialog('clear_slots_dialog')">No</md-button>
            </md-dialog-actions>
        </md-dialog>

        <!-- DELETE MODAL -->

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="delete_slots_dialog">
            <md-dialog-title>Delete time slot</md-dialog-title>

            <md-dialog-content>
                Are you sure you want to delete this time slots?
            </md-dialog-content>

            <md-dialog-actions>
                <md-button class="md-primary" @click.native="deleteTimeSlotFun()">Yes</md-button>
                <md-button class="md-primary" @click="closeDialog('delete_slots_dialog')">No</md-button>
            </md-dialog-actions>
        </md-dialog>

        <!-- ADD NEW TIME SLOT MODAL -->

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="add_slot_dialog" class="new-time-slot-dialog">
            <md-dialog-title>Add new opening times</md-dialog-title>
            <md-dialog-content>
                <form>
                    <label>Time<br></label>
                    <md-checkbox v-model="setTimesManually">Set Manually</md-checkbox>
                    <div v-if="!setTimesManually">
                        <md-input-container>
                            <vue-timepicker :minute-interval="10" format="HH:mm"
                                            v-model="addStartTimeModel"></vue-timepicker>
                            <span class="access-time">&nbsp;-&nbsp;</span>
                            <vue-timepicker :minute-interval="10" format="HH:mm"
                                            v-model="endStartTimeModel"></vue-timepicker>
                        </md-input-container>
                    </div>
                    <div v-else>
                        <md-input-container>
                            <label>Start time</label>
                            <md-input type="number" placeholder="HH" v-model="addStartTimeModel.HH"></md-input>
                            <span class="access-time">&nbsp;:&nbsp;</span>
                            <md-input type="number" placeholder="mm" v-model="addStartTimeModel.mm"></md-input>
                        </md-input-container>
                        <md-input-container>
                            <label>End time</label>
                            <md-input type="number" placeholder="HH" v-model="endStartTimeModel.HH"></md-input>
                            <span class="access-time">&nbsp;:&nbsp;</span>
                            <md-input type="number" placeholder="mm" v-model="endStartTimeModel.mm"></md-input>
                        </md-input-container>
                    </div>
                    <p class="error-message" v-if="isErrorEmpty">Please select all start and end times</p>
                    <p class="error-message" v-if="isError">Start time cannot be later than end time<br>{{setTimesManually ? "Valid manual values must be between 0 and 24" : ""}}</p>
                </form>
            </md-dialog-content>

            <md-dialog-actions>
                <md-button class="md-primary"
                           @click="addTimeSLotFun(addStartTimeModel,endStartTimeModel, 'add_slot_dialog')">Add
                </md-button>
                <md-button class="md-primary" @click="closeDialog('add_slot_dialog')">Cancel</md-button>
            </md-dialog-actions>
        </md-dialog>

        <!-- EDIT TIME SLOT MODAL -->

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="edit_slot_dialog" class="edit-time-slot-dialog">
            <md-dialog-title>Edit time slot</md-dialog-title>

            <md-dialog-content>
                <form>
                    <label>Time<br></label>
                    <md-checkbox v-model="setTimesManually">Set Manually</md-checkbox>
                    <div v-if="!setTimesManually">
                        <md-input-container>
                            <vue-timepicker :minute-interval="10" v-model="editStartTimeModel"></vue-timepicker>
                            <span class="access-time">&nbsp;-&nbsp;</span>
                            <vue-timepicker :minute-interval="10" v-model="editEndTimeModel"></vue-timepicker>
                        </md-input-container>
                    </div>
                    <div v-else>
                        <md-input-container>
                            <label>Start time</label>
                            <md-input type="number" placeholder="HH" v-model="editStartTimeModel.HH"></md-input>
                            <span class="access-time">&nbsp;:&nbsp;</span>
                            <md-input type="number" placeholder="mm" v-model="editStartTimeModel.mm"></md-input>
                        </md-input-container>
                        <md-input-container>
                            <label>End time</label>
                            <md-input type="number" placeholder="HH" v-model="editEndTimeModel.HH"></md-input>
                            <span class="access-time">&nbsp;:&nbsp;</span>
                            <md-input type="number" placeholder="mm" v-model="editEndTimeModel.mm"></md-input>
                        </md-input-container>
                    </div>
                    <p class="error-message" v-if="isError">Start time cannot be later than end time<br>{{setTimesManually ? "Valid manual values must be between 0 and 24" : ""}}</p>
                </form>
            </md-dialog-content>

            <md-dialog-actions>
                <md-button class="md-primary"
                           @click="editTimeSlotFun(editStartTimeModel, editEndTimeModel, 'edit_slot_dialog')">Save
                </md-button>
                <md-button class="md-primary" @click="closeDialog('edit_slot_dialog')">Cancel</md-button>
            </md-dialog-actions>
        </md-dialog>
    </div>
</template>

<script>

    let epicuri = require('../internal/epicuri.js');
    let VueTimepicker = require('vue2-timepicker');
    let emptyTimeSlot = {
        hourOpen: "00",
        minuteOpen: "00",
        hourClose: "00",
        minuteClose: "00"
    };
    let allDayTimeSlot = {
        hourOpen: "00",
        minuteOpen: "00",
        hourClose: 24,
        minuteClose: "00"
    };

    export default {
        data: function () {
            return {
                openingHoursData: [],
                dataToDisplay: [],
                daysOfWeek: [
                    "MONDAY",
                    "TUESDAY",
                    "WEDNESDAY",
                    "THURSDAY",
                    "FRIDAY",
                    "SATURDAY",
                    "SUNDAY"
                ],
                addStartTimeModel: {
                    HH: "",
                    mm: "00"
                },
                endStartTimeModel: {
                    HH: "",
                    mm: "00"
                },
                dayIndex: "",
                timeSlotEdit: {},
                timeSlotIndex: 0,
                editStartTimeModel: {
                    HH: "",
                    mm: ""
                },
                editEndTimeModel: {
                    HH: "",
                    mm: ""
                },
                emptyTimeSlot: {
                    hourOpen: "00",
                    minuteOpen: "00",
                    hourClose: "00",
                    minuteClose: "00"
                },
                allDayTimeSlot: {
                    hourOpen: "00",
                    minuteOpen: "00",
                    hourClose: 24,
                    minuteClose: "00"
                },
                setTimesManually:false,
                isError: false,
                isErrorEmpty: false
            }
        },
        components: {VueTimepicker},
        mounted: function () {

        },
        watch: {
            addStartTimeModel: function (startTime) {
                this.isErrorEmpty = false;
                this.isError = false;
            },
            endStartTimeModel: function (endTime) {
                this.isErrorEmpty = false;
                this.isError = false;
            },
            editStartTimeModel: function (startTime) {
                this.isErrorEmpty = false;
                this.isError = false;
            },
            editEndTimeModel: function (endTime) {
                this.isErrorEmpty = false;
                this.isError = false;
            }
        },
        methods: {

            switchView: function (tabIndex) {

                switch (tabIndex) {
                    case 0:
                        this.$router.push({name: "opening_hours", query: {type: "RESERVATION"}});
                        this.onLoad("RESERVATION");
                        break;
                    case 1:
                        this.$router.push({name: "opening_hours", query: {type: "TAKEAWAY"}});
                        this.onLoad("TAKEAWAY");
                        break;
                }
            },
            onLoad: function (type) {

                epicuri.getOpeningHours(this.$http, type,
                    (response) => {
                        this.openingHoursData = response.body.hours;
                        this.dataToDisplay = this.openingHoursData;

                        for (var obj in this.dataToDisplay) {
                            for (var i = 0; i < this.dataToDisplay[obj].length; i++) {
                                var object = this.dataToDisplay[obj][i];


                                if (object.hourOpen <= 9) {
                                    this.openingHoursData[obj][i].hourOpen = "0" + object.hourOpen.toString();
                                }
                                if (object.minuteOpen <= 9) {
                                    this.openingHoursData[obj][i].minuteOpen = "0" + object.minuteOpen.toString();
                                }
                                if (object.hourClose <= 9) {
                                    this.openingHoursData[obj][i].hourClose = "0" + object.hourClose.toString();
                                }
                                if (object.minuteClose <= 9) {
                                    this.openingHoursData[obj][i].minuteClose = "0" + object.minuteClose.toString();
                                }

                                if (JSON.stringify(emptyTimeSlot) === JSON.stringify(object)) {
                                    this.openingHoursData[obj][i].hourClose = "24";
                                }
                            }
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    }
                )
            },
            openDialog(ref, index) {
                this.$refs[ref].open();
                this.dayIndex = index;
            },
            closeDialog(ref) {
                this.$refs[ref].close();
                this.isError = false;
                this.isErrorEmpty = false;
            },
            addTimeSLotFun: function (startTimeModel, endTimeModel, refModal) {
                let timeSlotObject = {
                    hourOpen: parseInt(startTimeModel.HH),
                    minuteOpen: parseInt(startTimeModel.mm),
                    hourClose: parseInt(endTimeModel.HH),
                    minuteClose: parseInt(endTimeModel.mm)
                };

                if ((JSON.stringify(emptyTimeSlot) === JSON.stringify(this.openingHoursData[this.dayIndex][0])) || (JSON.stringify(allDayTimeSlot) === JSON.stringify(this.openingHoursData[this.dayIndex][0]))) {

                    if (isNaN(timeSlotObject.hourOpen) || isNaN(timeSlotObject.minuteOpen) || isNaN(timeSlotObject.hourClose) || isNaN(timeSlotObject.minuteClose)) {
                        this.isErrorEmpty = true;
                    } else if (timeSlotObject.hourOpen > timeSlotObject.hourClose) {
                        this.isError = true;
                    } else if (timeSlotObject.hourOpen === timeSlotObject.hourClose && timeSlotObject.minuteOpen > timeSlotObject.minuteClose) {
                        this.isError = true;
                    } else {
                        this.isError = false;
                        this.isErrorEmpty = false;

                        this.openingHoursData[this.dayIndex] = [];
                        this.openingHoursData[this.dayIndex].push(timeSlotObject);

                        let payloadObject = {
                            hours: this.openingHoursData
                        };

                        epicuri.sendOpeningHoursSlot(this.$http, this.$route.query.type, payloadObject,
                            (response) => {
                                this.closeDialog(refModal);
                                this.onLoad(this.$route.query.type);
                                this.addStartTimeModel = {
                                    HH: "",
                                    mm: "00"
                                };
                                this.endStartTimeModel = {
                                    HH: "",
                                    mm: "00"
                                };
                            },
                            (response) => {
                                console.log(response)
                            }
                        );

                    }

                } else {

                    if (isNaN(timeSlotObject.hourOpen) || isNaN(timeSlotObject.minuteOpen) || isNaN(timeSlotObject.hourClose) || isNaN(timeSlotObject.minuteClose)) {
                        this.isErrorEmpty = true;
                    } else if (timeSlotObject.hourOpen > timeSlotObject.hourClose) {
                        this.isError = true;
                    } else if (timeSlotObject.hourOpen == timeSlotObject.hourClose && timeSlotObject.minuteOpen > timeSlotObject.minuteClose) {
                        this.isError = true;
                    } else {
                        this.isError = false;
                        this.isErrorEmpty = false;

                        this.openingHoursData[this.dayIndex].push(timeSlotObject);

                        let payloadObject = {
                            hours: this.openingHoursData
                        };

                        epicuri.sendOpeningHoursSlot(this.$http, this.$route.query.type, payloadObject,
                            (response) => {
                                this.closeDialog(refModal);
                                this.onLoad(this.$route.query.type);
                                this.addStartTimeModel = {
                                    HH: "",
                                    mm: "00"
                                };
                                this.endStartTimeModel = {
                                    HH: "",
                                    mm: "00"
                                };
                            },
                            (response) => {
                                console.log(response)
                            }
                        );
                    }
                }

            },
            editTimeSlotFun: function (editStartTime, editEndTime, refModal) {

                let timeSlotObject = {
                    hourOpen: parseInt(editStartTime.HH),
                    minuteOpen: parseInt(editStartTime.mm),
                    hourClose: parseInt(editEndTime.HH),
                    minuteClose: parseInt(editEndTime.mm)
                };

                if (timeSlotObject.hourOpen > timeSlotObject.hourClose) {
                    this.isError = true;
                } else if (timeSlotObject.hourOpen == timeSlotObject.hourClose && timeSlotObject.minuteOpen > timeSlotObject.minuteClose) {
                    this.isError = true;
                } else {
                    this.isError = false;

                    this.openingHoursData[this.dayIndex][this.timeSlotIndex] = timeSlotObject;

                    let payloadObject = {
                        hours: this.openingHoursData
                    };

                    epicuri.sendOpeningHoursSlot(this.$http, this.$route.query.type, payloadObject,
                        (response) => {
                            this.closeDialog(refModal);
                            this.onLoad(this.$route.query.type);
                        },
                        (response) => {
                            console.log(response)
                        }
                    );
                }

            },
            deleteTimeSlotFun: function () {

                this.openingHoursData[this.dayIndex].splice(this.timeSlotIndex, 1);

                let payloadObject = {
                    hours: this.openingHoursData
                };

                epicuri.sendOpeningHoursSlot(this.$http, this.$route.query.type, payloadObject,
                    (response) => {
                        this.closeDialog("delete_slots_dialog");
                        this.onLoad(this.$route.query.type);
                    },
                    (response) => {
                        console.log(response)
                    }
                );
            },
            getTimeSlot(timeslot, timeslotIndex) {

                this.timeSlotEdit = timeslot;
                this.timeSlotIndex = timeslotIndex;

                this.editStartTimeModel = {
                    HH: timeslot.hourOpen,
                    mm: timeslot.minuteOpen
                };
                this.editEndTimeModel = {
                    HH: timeslot.hourClose,
                    mm: timeslot.minuteClose
                };
            },

            closeAllDay() {

                this.openingHoursData[this.dayIndex] = [];

                let payloadObject = {
                    hours: this.openingHoursData
                };

                epicuri.sendOpeningHoursSlot(this.$http, this.$route.query.type, payloadObject,
                    (response) => {
                        this.onLoad(this.$route.query.type);
                        this.closeDialog("clear_slots_dialog")
                    },
                    (response) => {
                        console.log(response)
                    }
                );

            },

            openAllDay() {
                let timeSlotObject = {
                    hourOpen: 0,
                    minuteOpen: 0,
                    hourClose: 24,
                    minuteClose: 0
                };
                this.openingHoursData[this.dayIndex] = [];

                this.openingHoursData[this.dayIndex].push(timeSlotObject);

                let payloadObject = {
                    hours: this.openingHoursData
                };

                epicuri.sendOpeningHoursSlot(this.$http, this.$route.query.type, payloadObject,
                    (response) => {
                        this.onLoad(this.$route.query.type);
                        this.closeDialog("open_all_day_dialog")
                    },
                    (response) => {
                        console.log(response)
                    }
                );

            }
        }
    }
</script>

<style>
    h1 {
        line-height: 1.8;
    }
    .opening-hours-tab .md-tabs-navigation-scroll-container .md-active {
        border-right: 2px solid #EE6E09;
        background-color: #EE6E09;
    }

    .opening-hours-tab .md-tab-header {
        background-color: #545454;
        color: #fff!important;
    }

    .opening-hours-tab .md-tab-indicator {
        background-color: #EE6E09!important;
    }

    .opening-hours-layout {
        flex-basis: 100%;
    }

    .md-offset-2 {
        margin-right: 0.6%;
        margin-top: 10px;
    }

    #reservation, #takeaways {
        padding: 2px;
    }

    .custom-time {
        display: inline-block;
        vertical-align: top;

        /*float: left;*/
        min-width: 40px;
        margin: 0 0 5px 0;
        padding: 0 5px;
    }

    .custom-time-edit {
        float: right;
        display: inline-block;
        vertical-align: top;
        padding-top: 5px !important;
        margin: 0 !important;
        min-width: 20px !important;
        width: 20px !important;
        height: 20px !important;
    }

    .custom-time-edit > .md-ink-ripple {
        display: none;
    }

    .custom-time-edit .material-icons {
        margin-top: 5px !important;
        font-size: 20px;
        width: 20px;
        min-width: 20px;
    }

    .access-time {
        /*float: left;*/
        display: inline-block;
        vertical-align: top;
        padding-top: 6px;
    }

    .closed-all-day {
        width: 100%;
    }

    .opening-hours-tab .md-tabs-content {
        min-height: 80vh !important;
    }

    .add-time-button {
        float: right;
        width: 35px !important;
        height: 35px !important;
        margin-bottom: 10px!important;
        margin-right: 0;
    }

    .new-time-slot-dialog > .md-dialog {
        height: 350px;
    }

    .edit-time-slot-dialog > .md-dialog {
        height: 350px;
    }

    .new-time-slot-dialog .md-input-container:after {
        background-color: transparent;
    }

    .edit-time-slot-dialog .md-input-container:after {
        background-color: transparent;
    }

    .time-picker:hover {
        cursor: pointer;
    }

    .opening-hours-tab .md-card-content {
        /*padding-bottom: 0;*/
    }

    .disableTimeSlot {
        background-color: #ddd !important;
    }

    .closed-label {
        margin: 0;
        font-weight: bold;
        color: #EE6E09;
    }

    .opening-day-card {
        flex-basis: 15%;
        margin-bottom: 20px;
        margin-right: 20px;
        justify-content: space-between;
    }

    .opening-day-card .md-card-header {
        padding-bottom: 0;
    }

    .error-message {
        color: red;
    }

    .opening-day-card .md-card-actions {
        display: block;
        padding: 0 16px 16px 16px;
        /*position: absolute;*/
        /*bottom: 0;*/
    }

    .close-all-day-btn, .open-all-day-btn {
        display: block;
        width: 100%;
        margin: 0 !important;
    }

    .close-all-day-btn {
        padding-right: 8px;
    }

    @media screen and (max-width: 1780px) {

        .opening-hours-layout {
            max-width: 18%;
            flex: 0 1 18%;
        }
    }

    @media screen and (max-width: 1440px) {

        .opening-hours-layout {
            max-width: 20%;
            flex: 0 1 20%;
        }
        .opening-day-card {
            flex-basis: 20%;
        }
    }

    @media screen and (max-width: 1280px) {

        .opening-hours-layout {
            max-width: 30%;
            flex: 0 1 30%;
        }
        .opening-day-card {
            flex-basis: 30%;
        }
    }

    @media screen and (max-width: 1024px) {

        .opening-hours-layout {
            max-width: 24%;
            flex: 0 1 24%;
        }

        .opening-day-card {
            flex-basis: 30%;
        }

        #reservation, #takeaways {
            padding-left: 20px;
        }
    }

</style>
