<template>
    <div>
        <form>
            <h2>Any changes made here may require a log out / log in of the POS app.</h2>

            <div style="padding: 10px">
                <md-card>
                    <md-card-header>
                        <div class="md-title">General Information</div>
                        <!--<div class="md-subhead">Subtitle here</div>-->
                    </md-card-header>
                    <md-card-content>
                        <md-input-container>
                            <label>Name</label>
                            <md-input required v-model="restaurant.Name"></md-input>
                            <md-tooltip>The name of your establishment</md-tooltip>
                        </md-input-container>
                        <small>The name of your establishment is publicly visible on all Epicuri listings, e.g. the Guest App</small>

                        <md-input-container>
                            <label>Description</label>
                            <md-input v-model="restaurant.Description"></md-input>
                            <md-tooltip>The description of your establishment</md-tooltip>
                        </md-input-container>
                        <small>The description of your establishment is publicly visible on all Epicuri listings, e.g. the Guest App</small>

                        <md-input-container>
                            <label>VAT/Tax Number</label>
                            <md-input v-model="restaurant.VATNumber"></md-input>
                        </md-input-container>
                        <small>Please ensure this is a valid tax identifier for your country. It will appear on customer receipts. Leave blank if not applicable.</small>

                        <md-input-container>
                            <label>ID</label>
                            <md-input disabled v-model="restaurant.staffFacingId">{{restaurant.staffFacingId}}</md-input>
                            <md-tooltip>This is the ID you use to log into Epicuri.</md-tooltip>
                        </md-input-container>
                        <small>This ID is for information only; it cannot be edited or reset</small>
                    </md-card-content>
                </md-card>
            </div>

            <div style="padding: 10px">
                <md-card>
                    <md-card-header>
                        <div class="md-title">Address</div>
                    </md-card-header>
                    <md-card-content>
                        <md-input-container>
                            <label>Street</label>
                            <md-input v-model="restaurant.Address.Street"></md-input>
                        </md-input-container>
                        <md-input-container>
                            <label>Town</label>
                            <md-input v-model="restaurant.Address.Town"></md-input>
                        </md-input-container>
                        <md-input-container>
                            <label>City</label>
                            <md-input v-model="restaurant.Address.City"></md-input>
                        </md-input-container>
                        <md-input-container>
                            <label>Postal/ZIP Code</label>
                            <md-input v-model="restaurant.Address.Postcode"></md-input>
                        </md-input-container>
                    </md-card-content>
                </md-card>
            </div>

            <div style="padding: 10px">
                <md-card>
                    <md-card-header>
                        <div class="md-title">Online & Contact</div>
                    </md-card-header>
                    <md-card-content>
                        <md-input-container>
                            <label>Phone number (public)</label>
                            <md-input v-model="restaurant.Telephone1"></md-input>
                            <md-tooltip>The phone number for your establishment</md-tooltip>
                        </md-input-container>
                        <small>The phone number for your establishment is publicly visible on all Epicuri listings, e.g. the Guest App</small>
                        <md-input-container>
                            <label>Phone number (private)</label>
                            <md-input v-model="restaurant.Telephone2"></md-input>
                        </md-input-container>
                        <small>A secondary phone number (optional). Usually used for internal correspondence</small>
                        <md-input-container>
                            <label>Email (public)</label>
                            <md-input v-model="restaurant.email"></md-input>
                        </md-input-container>
                        <small>This is the guest-facing email address for your establishment. It will be visible on all Epicuri listings, e.g Guest App</small>
                        <md-input-container>
                            <label>Email (private)</label>
                            <md-input v-model="restaurant.internalEmailAddress"></md-input>
                        </md-input-container>
                        <small>This email will be used for all internal alerts such as reservations, cash up emails etc</small>
                        <md-input-container>
                            <label>Website</label>
                            <md-input v-model="restaurant.website"></md-input>
                        </md-input-container>
                    </md-card-content>
                </md-card>
            </div>

            <div style="padding: 10px">
                <md-card v-if="xero">
                    <md-card-header>
                        <div class="md-title">Xero</div>
                    </md-card-header>
                    <div v-if="xeroRequiresConnection">
                        <div v-if="xeroConnectionInProgress">
                            <md-progress class="md-warn" md-indeterminate></md-progress>
                            A new window will open for you to log in and authorize this connection. Once authorized, your mappings will appear here.<br>
                            Waiting for authorization...
                        </div>
                        <div v-else>
                            <md-button @click="connectToXero">
                                <img src="../images/xero-connect-blue.svg" alt="Connect to Xero">
                            </md-button>
                            <br>
                            <small>A new window will open for you to log in and authorize this connection. Once authorized, your mappings will appear here.</small>
                        </div>
                    </div>
                    <div v-else>
                        <md-card-content>
                            Xero is <strong>CONNECTED</strong><br>
                            Please amend your Epicuri-to-Xero mapping rules below. For more help, please consult the <a href="https://epicuri.freshdesk.com/solution/articles/5000823409-xero-accounting-integration">online documentation</a>
                            <md-table>
                                <md-table-header>
                                    <md-table-row>
                                        <md-table-head>Rule Type</md-table-head>
                                        <md-table-head>Name</md-table-head>
                                        <md-table-head>Tax</md-table-head>
                                        <md-table-head>Account Mapping (Code)</md-table-head>
                                    </md-table-row>
                                </md-table-header>
                                <md-table-body>
                                    <md-table-row v-for="rule in xeroMappings.rules" v-bind:key="rule.id">
                                        <md-table-cell>{{rule.ruleType}}</md-table-cell>
                                        <md-table-cell>{{rule.typeName}}</md-table-cell>
                                        <md-table-cell>{{rule.taxId ? rule.taxName : "(Not Applicable)"}}</md-table-cell>
                                        <md-table-cell>
                                            <md-input-container>
                                                <md-select v-model="xeroMappings.ruleToCode[rule.id]">
                                                    <md-option v-for="account in xeroMappings.accounts" :value="account.code" :key="account.code">{{account.name + " (" + account.code + ")"}}</md-option>
                                                </md-select>
                                            </md-input-container>
                                        </md-table-cell>
                                    </md-table-row>
                                </md-table-body>
                            </md-table>
                        </md-card-content>
                        <md-card-actions>
                            <md-button class="md-raised md-primary" @click="onMappingsSave">Save</md-button>
                        </md-card-actions>
                    </div>
                </md-card>
            </div>

            <div style="padding: 10px">
                <md-card>
                    <md-card-header>
                        <div class="md-title">Operational Settings</div>
                    </md-card-header>

                    <md-card-header>
                        <div class="md-subhead">Printer & Receipt Settings</div>
                    </md-card-header>
                    <md-card-content>
                        <md-input-container>
                            <label>Receipt footer message</label>
                            <md-input v-model="restaurant.ReceiptFooter"></md-input>
                        </md-input-container>
                        <small>Appears at the bottom of the bill printout.</small>
                    </md-card-content>
                    <md-card-content>
                        <md-input-container>
                            <label>Bill ID Prefix</label>
                            <md-input v-model="restaurant.RestaurantDefaults.BillPrefix"></md-input>
                        </md-input-container>
                        <small>Optional prefix that will appear before the ID on the receipt printout</small>
                    </md-card-content>
                    <md-card-content>
                        <md-input-container>
                            <label>VAT Number Prefix</label>
                            <md-input v-model="restaurant.RestaurantDefaults.TaxReferenceLabel"></md-input>
                        </md-input-container>
                        <small>Optional prefix that will appear before the VAT/Tax number on the receipt printout</small>
                    </md-card-content>
                    <md-card-content>
                        <label>Print short code to kitchen</label>
                        <md-input-container>
                            <md-switch v-model="restaurant.RestaurantDefaults.PrintShortCode"></md-switch>
                        </md-input-container>
                        <small>If a menu item has a short code, print the short code to the kitchen/bar instead of the full menu item name</small>
                    </md-card-content>
                    <md-card-header>
                        <div class="md-subhead">Booking Fusebox</div>
                    </md-card-header>
                    <md-card-content>
                        <md-input-container>
                            <label>Max guests on a reservation</label>
                            <md-input type="number" v-model="restaurant.RestaurantDefaults.MaxCoversPerReservation"></md-input>
                        </md-input-container>
                        <small>The maximum number of guests on a particular reservation. Affects booking widget and Guest App.</small>
                    </md-card-content>
                    <md-card-content>
                        <md-input-container>
                            <label>Max guests within "Reservation Time Slot"</label>
                            <md-input type="number" v-model="restaurant.RestaurantDefaults.MaxActiveReservationsCovers"></md-input>
                        </md-input-container>
                        <small>The maximum number of guests within Reservation Time Slot. Affects booking widget and Guest App.</small>
                    </md-card-content>
                    <md-card-content>
                        <md-input-container>
                            <label>Max number of reservations within "Reservation Time Slot"</label>
                            <md-input type="number" v-model="restaurant.RestaurantDefaults.MaxActiveReservations"></md-input>
                        </md-input-container>
                        <small>The maximum number of active reservations within "Reservation Time Slot". Affects booking widget and Guest App.</small>
                    </md-card-content>
                    <md-card-content>
                        <md-input-container>
                            <label>Reservation Time Slot</label>
                            <md-input type="number" v-model="restaurant.RestaurantDefaults.ReservationTimeSlot"></md-input>
                        </md-input-container>
                        <small>[minutes] A span of time that other settings in this fusebox use to determine how busy the venue is.</small>
                    </md-card-content>


                </md-card>
            </div>

        </form>
    </div>
</template>

<script>
    let epicuri = require('../internal/epicuri.js');

    export default {
        name: "generalSettings",

        data: function() {
            return {
                restaurant:{
                    Name:"",
                    Description:"",
                    staffFacingId:"",
                    Address:{
                        Street:"",
                        Town:"",
                        City:"",
                        Postcode:""
                    },
                    Telephone1:"",
                    Telephone2:"",
                    email:"",
                    internalEmailAddress:"",
                    website:"",
                    ReceiptFooter:"",
                    RestaurantDefaults:{
                        BillPrefix:"",
                        TaxReferenceLabel:"Our VAT Number",
                        PrintShortCode: true,
                        MaxCoversPerReservation:6,
                        MaxActiveReservationsCovers: 30,
                        MaxActiveReservations:5,
                        ReservationTimeSlot:120
                    }
                },

                printers:[],
                integrations:[],

                xero:false,
                xeroRequiresConnection: false,
                xeroConnectionInProgress: false,
                xeroMappings:{},
            }
        },

        methods: {
            loadRestaurant: function() {
                epicuri.getRestaurant(this.$http,
                    (response) => {
                        this.restaurant = response.body;
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    })
            },
            loadIntegrations: function() {
                epicuri.getIntegrations(this.$http,
                    (response) => {
                        this.integrations = response.body;
                        for(let i = 0; i < this.integrations.length; i++) {
                            if(this.integrations[i].integration === "XERO") {
                                this.xeroPresent(this.integrations[i]);
                            }
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    })
            },
            loadPrinters: function() {
                epicuri.getPrinters(this.$http,
                    (response) => {
                        this.printers = response.body;
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    })
            },
            loadXeroMappings: function() {
                epicuri.getXeroMappings(this.$http,
                    (response) => {
                        this.xeroMappings = response.body;
                        this.xeroMappings.ruleToCode = {};
                        for(let i = 0; i < this.xeroMappings.rules.length; i++) {
                            this.xeroMappings.ruleToCode[i] = this.xeroMappings.rules[i].code;
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    })
            },
            xeroPresent: function (integration) {
                this.xero = true;
                if(!integration.kvData.data.XERO_REAL_TOKEN_AVAILABLE && !integration.kvData.data.XERO_TEMP_TOKEN_AVAILABLE) {
                    this.xeroRequiresConnection = true;
                } else {
                    this.xeroCheckTokenValid();
                    this.xeroRequiresConnection = false;
                }
            },
            xeroCheckTokenValid: function() {
                epicuri.getXeroEnsureConnectionValidity(this.$http,
                    (response) => {
                        if(response.body.flag) {
                            console.log("load mappings");
                            this.loadXeroMappings();
                        } else {
                            console.log("connection required");
                            this.xeroRequiresConnection = true;
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    })
            },
            connectToXero: function () {
                this.xeroConnectionInProgress = true;
                epicuri.getXeroAuth(this.$http,
                    (response) => {
                        window.open(response.body.Message);
                        this.checkXeroAuth();
                    },
                    (response) => {
                        this.xeroConnectionInProgress = false;
                        console.log("ERROR" + response)
                    })
            },
            checkXeroAuth: async function() {
                let xeroLastCheck = new Date().getTime();
                await this.authIntegrationsChecker(xeroLastCheck);
            },
            authIntegrationsChecker: function (xeroLastCheck) {
                this.loadIntegrations();

                let currentTime = new Date().getTime();
                if((currentTime - xeroLastCheck) > 120000) {
                    console.log("iterations timeout");
                    this.xeroConnectionInProgress = false;
                    return;
                }
                if (this.xeroRequiresConnection){
                    console.log("still requires connection");
                    setTimeout(this.authIntegrationsChecker, 5000, xeroLastCheck);
                } else {
                    this.loadXeroMappings();
                }
            },
            onMappingsSave: function () {
                console.log(this.xeroMappings.ruleToCode);
                for(let i = 0; i < this.xeroMappings.rules.length; i++) {
                    if(this.xeroMappings.ruleToCode[i]) {
                        this.xeroMappings.rules[i].xeroAccountCode = value;
                    }
                }
            }
        },
        created: function() {
            this.loadRestaurant();
            this.loadIntegrations();
            this.loadPrinters();
        }
    }
</script>

<style scoped>

</style>