<template>
  <v-app>
    <v-content>
      <div v-if="authToken !== '' && !unavailable">
        <v-stepper v-model="progressStep">
          <v-stepper-header>
            <v-stepper-step :complete="progressStep > 1" step="1">Your Order</v-stepper-step>
            <v-divider></v-divider>
            <v-stepper-step :complete="progressStep > 2" step="2">Details</v-stepper-step>
            <v-divider></v-divider>
            <v-stepper-step :complete="progressStep > 3" step="3">Finish</v-stepper-step>
            <v-divider></v-divider>
          </v-stepper-header>
          <v-stepper-items>
            <v-stepper-content step="1">
              <v-container>
                <v-layout pa-1>
                  <v-flex xs8 shrink>
                    <template>
                      <div>
                        <v-tabs slider-color="yellow">
                          <v-tab v-model="showTab" ripple v-for="(itemGroup, index) in itemGroups" :key="index">
                            {{itemGroup.header}}
                          </v-tab>
                          <v-tab-item v-for="(itemGroup, index) in itemGroups" :key="index">
                            <v-list two-line>
                              <template v-for="item in itemGroup.items">
                                <v-list-tile-content>
                                  <v-container>
                                    <v-layout wrap>
                                      <v-flex xs8>
                                        {{item.Name}}
                                      </v-flex>
                                      <v-flex xs2>
                                        {{currencySymbol}}{{toPrice(item.Price)}}
                                      </v-flex>
                                      <v-flex xs2 v-if="!item.Unavailable">
                                        <v-icon color="green lighten-1" @click="onOrderPlus(item)">add</v-icon>
                                      </v-flex>
                                      <v-flex xs2 v-else>
                                        <span>Unavailable</span>
                                      </v-flex>
                                      <v-flex xs12>
                                        <span class="grey--text text--lighten-1">{{item.Description}}</span>
                                      </v-flex>
                                    </v-layout>
                                  </v-container>
                                </v-list-tile-content>
                                <v-divider></v-divider>
                              </template>
                            </v-list>
                          </v-tab-item>
                        </v-tabs>
                      </div>
                    </template>
                  </v-flex>
                  <v-flex xs4 ml-4>
                    <v-container>
                      <v-layout wrap>
                        <v-flex xs12>
                          <v-card class="grey lighten-3">
                            <v-card-title class="grey lighten-1">
                              <div>
                                <span class="headline">Your Order</span>
                              </div>
                              <v-layout align-center justify-end row>
                                <span class="headline">{{currencySymbol + toPrice(calculateTotal())}}</span>
                              </v-layout>
                            </v-card-title>

                            <v-list>
                              <v-list-tile v-for="item in basket" :key="item.tempId" avatar outline>
                                <v-container mt-1 mb-0 pa-1>
                                  <v-layout align-center justify-start wrap>
                                    <v-flex xs1>
                                      <v-icon color="red" @click="removeFromBasket(item)">remove_circle_outline</v-icon>
                                    </v-flex>
                                    <v-flex xs9>
                                      <v-subheader>{{item.MenuItem.Name+' (x'+item.Quantity+')'}}</v-subheader>
                                    </v-flex>
                                    <v-flex xs2>
                                      <v-subheader>{{currencySymbol + toPrice(calculateItemTotal(item))}}</v-subheader>
                                    </v-flex>
                                    <v-flex xs12 offset-xs2>
                                      <v-layout mb-1 mt-0 align-center justify-start>
                                        <span class="caption lighten-2 font-italic">{{modifiersToString(item.Modifiers)}}</span>
                                      </v-layout>
                                    </v-flex>
                                  </v-layout>
                                </v-container>
                              </v-list-tile>
                              <v-spacer></v-spacer>
                              <v-list-tile v-if="basket.length>0">
                                <v-layout align-center justify-end row>
                                  <v-list-tile-action >
                                    <v-btn class="padded-checkout-btn" color="green lighten-1" :disabled="!isCheckoutable()" @click="progressStepper(2)">Checkout {{currencySymbol + toPrice(calculateTotal())}}</v-btn>
                                  </v-list-tile-action>
                                </v-layout>
                              </v-list-tile>
                              <v-list-tile v-if="isOverMaximumValue()">
                                <v-layout>
                                  <v-list-tile-sub-title class="caption">Cannot take orders exceeding {{currencySymbol + toPrice(maxOrderValue)}}. <br>Please call {{restaurantPhoneNumber}} for larger orders.</v-list-tile-sub-title>
                                </v-layout>
                              </v-list-tile>
                              <v-list-tile v-if="isUnderMinimumValue()">
                                <v-layout>
                                  <v-list-tile-sub-title class="caption">Minimum order: {{currencySymbol + toPrice(minOrderValue)}}</v-list-tile-sub-title>
                                </v-layout>
                              </v-list-tile>
                            </v-list>
                          </v-card>
                        </v-flex>
                        <v-flex xs12 mt-4>
                          <v-card>
                            <v-card-title class="yellow lighten-1">
                              <div>
                                <span class="headline">Estimated Date/Time</span>
                              </div>
                            </v-card-title>
                            <v-layout>
                              <v-flex xs6 ml-2 mt-2>
                                <v-select :items="days" label="Date" v-model="selectedDay" v-on:change="onDaySelected"></v-select>
                              </v-flex>
                              <v-flex xs6>
                                <v-card-text class="headline">
                                  {{timeSlots.length === 0 ? 'CLOSED' : timeSlots[0]}}
                                </v-card-text>
                              </v-flex>
                              <v-flex xs6>
                                <v-card-text class="caption">
                                  +10 minutes approx for deliveries
                                </v-card-text>
                              </v-flex>
                            </v-layout>
                            <v-card-text>
                              Need to order for another time? Don't worry, you can select a time in the next step.
                            </v-card-text>
                          </v-card>
                        </v-flex>
                      </v-layout>
                    </v-container>
                  </v-flex>
                </v-layout>
              </v-container>
            </v-stepper-content>
            <v-stepper-content step="2">
              <template>
                <v-container>
                  <v-layout>
                    <v-flex xs4 offset-xs2 mr-1>
                      <v-card height="100%">
                        <v-card-title class="yellow lighten-1">Time / Notes</v-card-title>
                        <v-card-text class="caption">
                          Please allow extra time for deliveries. Times are approximate.
                        </v-card-text>
                        <v-card-text>
                          <v-select :items="timeSlots" :label="collectionOrDeliveryString + ' Time'" v-model="selectedTime"></v-select>
                        </v-card-text>
                        <v-card-text>
                          <v-textarea v-model="orderNote" box label="Notes" placeholder="Any special instructions? E.g. please don't ring the doorbell!"></v-textarea>
                        </v-card-text>
                      </v-card>
                    </v-flex>
                    <v-flex xs4 ml-1>
                      <v-card height="100%">
                        <v-card-title class="grey lighten-1">Order Details</v-card-title>
                        <v-form v-model="isNameAndNumberValid" ref="basicInfoForm">
                          <v-container>
                            <v-layout>
                              <v-flex>
                                <v-text-field required label="Your Name" v-model="orderName" :rules="nameRules"> </v-text-field>
                                <vue-tel-input @onInput="onPhoneNumberInput" :preferredCountries="g_validLocations"></vue-tel-input>
                                <p color="red" v-if="phoneNumberIsInvalidMessage.length !== 0">{{phoneNumberIsInvalidMessage}}</p>
                                <v-radio-group v-model="collectionOrDelivery">
                                  <v-radio :key="1" :value="1" :label="`Collection`"></v-radio>
                                  <v-radio :key="2" :value="2" :label="`Delivery`"></v-radio>
                                </v-radio-group>
                                <div v-if="collectionOrDelivery === 2">
                                  <v-text-field label="Address Line 1" v-model="address1" required :rules="addressLineRules"></v-text-field>
                                  <v-text-field label="Address Line 2" v-model="address2"></v-text-field>
                                  <v-text-field label="Town" v-model="town"></v-text-field>
                                  <v-text-field label="Postal/ZIP Code" v-model="postCode" required :rules="postCodeRules"></v-text-field>
                                </div>
                              </v-flex>
                            </v-layout>
                          </v-container>
                        </v-form>
                        <v-card-actions>
                          <v-container>
                            <v-layout align-center justify-center row>
                              <v-flex xs3>
                                <v-btn color="info" @click="progressStepper(1)">Back</v-btn>
                              </v-flex>
                              <v-flex xs3 ml-3>
                                <v-btn :disabled="!isTakeawayOrCollectionSelectionValid()" color="green" @click="progressStepper(3)">Pay Now</v-btn>
                              </v-flex>
                            </v-layout>
                          </v-container>
                        </v-card-actions>
                      </v-card>
                    </v-flex>
                  </v-layout>
                </v-container>
              </template>
              <v-container>
                <v-layout fluid>
                  <v-flex>
                    <div>
                      <!--todo better name/description & use integer prices?-->
                      <vue-stripe-checkout
                        ref="checkoutRef"
                        :name="restaurantName"
                        :description="collectionOrDeliveryString"
                        :currency="isoCurrency"
                        :amount="calculateTotal() * 100"
                        :allow-remember-me="false"
                        @done="onPaymentDone"
                        @opened="onPaymentOpened"
                        @closed="onPaymentClosed"
                        @canceled="onPaymentCanceled">
                      </vue-stripe-checkout>
                    </div>
                  </v-flex>
                </v-layout>
              </v-container>
            </v-stepper-content>
            <v-stepper-content step="3">
              <v-container v-if="paymentAndTakeawayComplete">
                <v-layout align-start justify-start row>
                  <v-flex xs-4 offset-xs-4>
                    <v-card height="100%">
                      <v-card-title class="green lighten-1">
                        <v-layout align-center justify-center row>
                          <span class="headline">Thank you!<br>{{confirmationString}}</span>
                        </v-layout>
                      </v-card-title>

                      <v-layout align-end justify-center row pa-2>
                        <h4 class="display-1">Estimated Time: {{prettyDateTime}}</h4>
                      </v-layout>

                      <div v-if="isDelivery()">
                        <v-subheader>
                          <v-layout align-center justify-center row>
                            DELIVERY ADDRESS
                          </v-layout>
                        </v-subheader>
                        <h6>{{address1}}</h6>
                        <h6 v-if="address2 !== ''">{{address2}}</h6>
                        <h6 v-if="town !== ''">{{town}}</h6>
                        <h6>{{postCode}}</h6>
                      </div>
                      <div v-else>
                        <v-layout align-center justify-center row>
                          <v-subheader>
                          COLLECTION ADDRESS
                          </v-subheader>
                        </v-layout>
                        <h6 class="headline">{{this.restaurantName}}</h6>
                        <h6 class="title">{{this.restaurantAddress.Street}}</h6>
                        <h6 class="title" v-if="this.restaurantAddress.Town !== null">{{this.restaurantAddress.Town}}, </h6>
                        <h6 class="title" v-if="this.restaurantAddress.City !== null">{{this.restaurantAddress.City}}, </h6>
                        <h6 class="title">{{this.restaurantAddress.Postcode}}</h6>
                      </div>

                      <!--<v-subheader>
                        <v-layout align-center justify-center row pt-3>
                          ORDER DETAILS
                        </v-layout>
                      </v-subheader>-->
                      <v-card-text class="headline">Your Order Number: {{customerTakeawayResponse.ReadableSessionId}}</v-card-text>
                      <v-card-text class="title">{{customerTakeawayResponse.isPaid ? 'PAID' : 'PAYMENT DUE'}} {{currencySymbol}}{{toPrice(customerTakeawayResponse.TakeawayOrder.Total + customerTakeawayResponse.TakeawayOrder.DeliveryCost)}}</v-card-text>
                      <v-card-text class="body-2">To update your order or to contact us please call {{restaurantPhoneNumber}}</v-card-text>

                      <v-card-actions>
                        <v-layout align-center justify-center row>
                          <v-btn color="info" @click="showEmailDialog = true">Email Receipt</v-btn>
                        </v-layout>
                      </v-card-actions>
                    </v-card>
                  </v-flex>
                </v-layout>
              </v-container>
              <v-container v-else-if="paymentAndTakeawayError">
                <template>
                    <v-layout align-center justify-center row>
                      <v-flex xs-4 offset-xs-4>
                        <v-card height="100%">
                          <v-layout align-center justify-center row>
                            <v-card-title class="headline red lighten-1">Sorry, we couldn't create your order!</v-card-title>
                          </v-layout>
                          <v-card-text class="caption">
                            {{this.paymentAndTakeawayErrorMessage}}
                          </v-card-text>
                          <v-card-actions>
                            <v-layout align-center justify-center row>
                              <v-btn color="error" @click="progressStepper(2)">Go Back</v-btn>
                            </v-layout>
                          </v-card-actions>
                        </v-card>
                      </v-flex>
                    </v-layout>
                </template>
              </v-container>
              <v-container v-else>
                <h1>Processing</h1>
                <v-progress-circular
                  :size="70"
                  :width="7"
                  color="purple"
                  indeterminate
                ></v-progress-circular>
              </v-container>
            </v-stepper-content>
          </v-stepper-items>
        </v-stepper>
      </div>
      <div v-else>
        {{errorMessageContact}}
      </div>
      <section class='app-stores'>
        <div class='container'>
          <p>
            <a href='https://play.google.com/store/apps/details?id=com.uk.epicuri.guest'>
              <img src='https://support.industry.siemens.com/cs/images/109747752/Download_on_GooglePlay.png?inline=1' class='img-responsive'>
            </a>
            <a href='https://itunes.apple.com/it/app/epicuri/id1372698448?l=en&mt=8'>
              <img src='https://speedify.com/wp-content/uploads/App_Store_Badge_EN.png' class='img-responsive'>
            </a>
          </p>
          <p>{{appMessage}}</p>
        </div>
      </section>

      <!--dialogs-->
      <template>
        <v-layout row justify-center>
          <v-dialog v-model="showModifierDialog" persistent max-width="450px">
            <v-card>
              <v-card-title>
                <span class="title">{{modifierDialogTitle}}</span>
              </v-card-title>
              <v-card v-for="modifierGroup in itemModifierGroups" :key="modifierGroup.Id">
                <v-card-text>
                  <v-container>
                    <v-layout wrap>
                      <v-flex xs4>
                        <span class="subheading">{{modifierGroup.modifierGroup.GroupName}}</span>
                      </v-flex>
                      <v-spacer></v-spacer>
                      <v-flex xs4>
                        <span class="grey--text body-2">({{modifierHelperText[modifierGroup.modifierGroup.Id]}})</span><br>
                      </v-flex>
                      <v-flex xs12 >
                        <v-checkbox v-for="modifier in modifierGroup.modifierGroup.Modifiers"
                                    :key="modifier.Id" :label="`${modifier.ModifierValue} (+${currencySymbol + toPrice(modifier.Price)})`"
                                    v-model="selectedModifiers[modifier.Id]"
                                    :disabled="itemModifierValuesDisabled[modifier.Id]"
                                    @change="onModifierChange(modifierGroup, modifier)"
                                    class="caption">
                        </v-checkbox>
                      </v-flex>
                    </v-layout>
                  </v-container>
                </v-card-text>
              </v-card>
              <v-card-actions>
                <v-layout align-end justify-end row>
                  <v-btn @click="onModifierDialogCancel">Cancel</v-btn>
                  <v-btn :disabled="!requiredModifiersSatisfied()" @click="onModifierDialogOK" color="success">{{modifierDialogOKText}}</v-btn>
                </v-layout>
              </v-card-actions>
            </v-card>
          </v-dialog>
        </v-layout>
      </template>
    </v-content>

    <template>
      <v-layout row justify-center>
        <v-dialog v-model="showWarningsDialog" persistent max-width="450px">
          <v-card>
            <v-card-title>
              <span class="title">Oops, there is a problem...</span>
            </v-card-title>
            <v-card-text>{{warningsMessage}}</v-card-text>
            <v-card-actions>
              <v-layout align-end justify-end row>
                <v-btn @click="onWarningsDialogOK" color="success">{{warningsDialogOKText}}</v-btn>
              </v-layout>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-layout>
    </template>

    <template>
      <v-layout row justify-center>
        <v-dialog v-model="showChargesDialog" persistent max-width="450px">
          <v-card>
            <v-card-title>
              <span class="title">Please confirm extra charges on this order</span>
            </v-card-title>
            <v-card-text>{{chargesMessage}}</v-card-text>
            <v-card-actions>
              <v-layout align-end justify-end row>
                <v-btn @click="onDeliveryChargesCancel">Go back</v-btn>
                <v-btn @click="onDeliveryChargesOK" color="success">OK</v-btn>
              </v-layout>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-layout>
    </template>

    <template>
      <v-layout row justify-center>
        <v-dialog v-model="showEmailDialog" persistent max-width="450px">
          <v-card>
            <v-card-title>
              <span class="title">Email Receipt</span>
            </v-card-title>
            <v-layout align-center justify-center row pa-3>
              <v-text-field label="Send Receipt To" v-model="emailReceiptAddress"></v-text-field>
            </v-layout>
            <v-card-actions>
              <v-layout align-end justify-end row>
                <v-btn color="grey" @click="showEmailDialog = false">Cancel</v-btn>
                <v-btn color="info" @click="sendEmail">Send</v-btn>
              </v-layout>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-layout>
    </template>

    <template>
      <v-layout row justify-center>
        <v-dialog v-model="showPayByCashDialog" persistent max-width="450px">
          <v-card>
            <v-card-title>
              <span class="title">{{cashOrCardTitle}}</span>
            </v-card-title>
            <v-card-text>{{cashOrCardMainText}}</v-card-text>
            <v-card-actions>
              <v-layout align-center justify-start row pa-3>
                <v-btn @click="onCashOrCardCancel()">Cancel</v-btn>
              </v-layout>
              <v-layout align-center justify-end row pa-3>
                <v-btn color="success" @click="cashOrCardResponse(false)">{{cashOnly ? 'OK' : 'Cash'}}</v-btn>
                <v-btn v-if="!cashOnly" color="info" @click="cashOrCardResponse(true)">Card</v-btn>
              </v-layout>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-layout>
    </template>

    <!--Snackbars-->
    <v-snackbar v-model="snackbar" :bottom="true" :timeout="6000">
      {{ snackbarText }}
      <v-btn color="pink" flat @click="snackbar = false">Close</v-btn>
    </v-snackbar>

    <template><v-footer class="pa-3"><v-spacer></v-spacer><div>Powered by epicuri.co.uk &copy; {{ new Date().getFullYear() }}</div></v-footer></template>
  </v-app>
</template>

<script>
const targetUrl = 'https://diner-api-prod.epicuri.co.uk/onlineorders'
//const targetUrl = 'http://diner-api-dev.epicuri.co.uk/onlineorders'
const authCookie = 'epicuri.co.uk.onlineordering'

let date = require('date-and-time')

export default {
  name: 'OnlineOrder',
  data: function () {
    return {
      appMessage: 'For ultra-convenience, use our FREE app for your takeaways and reservations!',
      errorMessageContact: 'Sorry, we cannot take orders online at this time. Please call ' + this.g_defaultTelephoneNumber,
      authToken: '',
      unavailable: false,
      restaurantName: '',
      restaurantPhoneNumber: '',
      restaurantImage: '',
      currencySymbol: '£',
      maxOrderValue: 0,
      minOrderValue: 0,
      maxWithoutCC: 0,
      isoCurrency: 'GBP',
      restaurantAddress: {},
      stripePublicKey: '',
      progressStep: 0,
      collectionOrDelivery: 1,
      menu: {},
      modifierValuesById: {},
      modifierGroupsById: {},
      modifierHelperText: {},
      itemGroups: [],
      showTab: 0,
      timeSlots: [],
      orderNote: null,
      paymentAndTakeawayComplete: false,

      // basket
      basket: [],

      // name / number
      orderName: '',
      orderNumber: '',
      isNameAndNumberValid: false,
      nameRules: [
        v => !!v || 'Name is required',
        v => v.length >= 2 || 'Name must be least 2 characters'
      ],

      // address fields
      address1: '',
      address2: '',
      town: '',
      postCode: '',
      addressLineRules: [
        v => !!v || 'First line of address is required',
        v => v.split('\\s').length < 2 || 'Street name and house number/name required'
      ],
      postCodeRules: [
        v => !!v || 'Post code is required',
        v => v.length >= 4 || 'Post code is required'
      ],

      // time
      selectedTime: '',
      days:[],
      selectedDay:"Today",
      selectedDayIndex: 0,

      // error messages
      phoneNumberIsInvalidMessage: 'Phone number is required',
      phoneNumberIsValid: false,
      paymentAndTakeawayError: false,
      paymentAndTakeawayErrorMessage: '',

      // dialogs
      showModifierDialog: false,
      currentItemInModifierDialog: {},
      modifierDialogTitle: 'Choices',
      itemModifierGroups: [],
      itemModifierValuesDisabled: {},
      selectedModifiers: {},
      modifierDialogOKText: 'Add',
      showWarningsDialog: false,
      warningsMessage:'',
      warningsDialogOKText: 'OK',
      showChargesDialog: false,
      chargesMessage: '',
      showEmailDialog: false,
      showPayByCashDialog: false,

      cashOnly: false,

      // snackbars
      snackbar: false,
      snackbarText: '',

      // result
      lastResponse: {},
      extraCosts: 0,
      customerTakeawayResponse: {},
      emailReceiptAddress: '',
      prettyDateTime: 'Unknown Date/time'
    }
  },
  computed: {
    collectionOrDeliveryString: function () {
      if (this.collectionOrDelivery === 1) {
        return 'Collection'
      } else if (this.collectionOrDelivery === 2) {
        return 'Delivery'
      }

      return ''
    },
    confirmationString: function () {
      if (this.collectionOrDelivery === 1) {
        return 'Your collection order is being prepared.'
      } else if (this.collectionOrDelivery === 2) {
        return 'Your delivery order is being prepared.'
      }

      return ''
    },
    cashOrCardTitle: function () {
      if(this.cashOnly) {
        return 'Online Payment Not Available'
      } else {
        return 'Pay now or later?'
      }
    },
    cashOrCardMainText: function () {
      if(this.cashOnly) {
        return 'Currently we are only taking payment on ' + (this.isDelivery() ? 'delivery.' : 'collection.') +  ' Press OK to submit your order.';
      } else {
        if(this.isDelivery()) {
          return 'Pay now by credit card or pay cash on delivery';
        } else {
          return 'Pay now by credit card or pay on collection';
        }
      }
    }
  },
  created: function () {
    this.doAuth()
  },
  methods: {
    doAuth: function () {
      let url = targetUrl + '/acquireToken?restaurantId=' + this.g_restaurantId + '&publicToken=' + this.g_publicToken
      this.poster(url, {}, this.onAuth, this.onAPIFail)
    },
    loadMenu: function () {
      let url = targetUrl + '/menu'
      this.getter(url, this.onMenu, this.onAPIFail)
    },
    loadTimeSlots: function () {
      let url = targetUrl + '/timeslots?day='+this.selectedDayIndex
      this.getter(url, this.onTimeSlots, this.onAPIFail)
    },
    calculateDays: function() {
      let pattern = date.compile('ddd D/M')
      let today = new Date();
      this.days = this.days.splice(0, this.days.length);

      this.days.push(this.selectedDay)
      for(let i = 1; i < 10; i++) {
        let thisDay = date.addDays(today, i)
        this.days.push(date.format(thisDay, pattern))
      }
    },
    onDaySelected: function() {
      for(let i = 0; i < this.days.length; i++) {
        if(this.days[i] === this.selectedDay) {
          this.selectedDayIndex = i;
          this.loadTimeSlots()
          return;
        }
      }
    },
    putBasket: function () {
      let payload = this.buildTakeawayPayloadWithCC()
      let url = targetUrl + '/takeaway'
      this.putter(url, payload, this.checkResponse,
        (response) => {
          this.paymentAndTakeawayError = true
          if(typeof(response.body['Message']) !== 'undefined') {
            this.showWarningsDialog = true
            this.warningsMessage = 'We found one or more issues with your order: ' + response.body.Message
          }
        })
    },
    postBasket: function (charge = null) {
      let payload = charge !== null ? this.buildTakeawayPayloadWithCC(charge.id) : this.buildTakeawayPayloadWithCC()
      let url = targetUrl + '/takeaway'
      this.poster(url, payload,
        (response) => {
          this.paymentAndTakeawayComplete = true
          this.customerTakeawayResponse = response;
          this.prettyDateTime = response.prettyDateTime
        },
        (response) => {
          this.snackbar = true
          this.snackbarText = 'Oops! Something went wrong... (' + response.body.Message + ')'
          this.paymentAndTakeawayComplete = false
          this.paymentAndTakeawayError = true
          this.paymentAndTakeawayErrorMessage = 'Could not complete transaction. Payment has *NOT* been taken. Please contact the restaurant to complete your order: ' + this.restaurantPhoneNumber
        })
    },
    buildTakeawayPayloadWithCC: function (chargeId = null) {
      let response = this.buildTakeawayPayload()
      if(chargeId !== null) {
        response.chargeId = chargeId
        response.payByCC = true
      }
      return response
    },
    buildTakeawayPayload: function() {
      return {
        RestaurantId: this.g_restaurantId,
        Name: this.orderName,
        Telephone: this.orderNumber,
        timeSlot: this.selectedTime,
        dateSlot: this.selectedDayIndex,
        Delivery: this.isDelivery(),
        Address: this.isDelivery() ? {Street: this.address1, Town: this.address2 === '' ? null : this.address2, Postcode: this.postCode} : null,
        Notes: this.orderNote,
        InstantiatedFromId: 5,
        Items: this.basket,
        payByCC: false
      }
    },
    onAuth: function (response) {
      this.authToken = response.token
      this.restaurantName = response.restaurantName
      this.restaurantPhoneNumber = response.restaurantPhoneNumber
      this.restaurantImage = response.restaurantImage
      this.currencySymbol = response.currencySymbol
      this.isoCurrency = response.isoCurrency
      this.maxOrderValue = response.maxOrderValue
      this.minOrderValue = response.minOrderValue
      this.maxWithoutCC = response.maxWithoutCC
      this.stripePublicKey = response.stripePublicKey
      this.restaurantAddress = response.address

      this.calculateDays()
      this.loadMenu()
      this.loadTimeSlots()
    },
    onMenu: function (response) {
      this.menu = response
      this.itemGroups = []
      for (let i = 0; i < this.menu.MenuCategories.length; i++) {
        let category = this.menu.MenuCategories[i]
        for (let j = 0; j < category.MenuGroups.length; j++) {
          let group = category.MenuGroups[j]
          if (group.MenuItems.length === 0) {
            continue
          }
          let groupItem = {
            items: [],
            GroupName: group.GroupName,
            CategoryName: category.CategoryName,
            GroupId: group.Id
          }
          if (groupItem.GroupName === groupItem.CategoryName) {
            groupItem.header = groupItem.GroupName
          } else if (groupItem.GroupName === 'Selection') {
            groupItem.header = groupItem.CategoryName
          } else {
            groupItem.header = groupItem.CategoryName + ' / ' + groupItem.GroupName
          }
          this.itemGroups.push(groupItem)

          for (let k = 0; k < group.MenuItems.length; k++) {
            groupItem.items.push(group.MenuItems[k])
          }
        }
      }

      for (let i = 0; i < this.menu.ModifierGroups.length; i++) {
        let modifierGroup = this.menu.ModifierGroups[i]
        this.modifierGroupsById[modifierGroup.Id] = modifierGroup

        if (modifierGroup.LowerLimit === 1 && modifierGroup.UpperLimit === 1) {
          this.modifierHelperText[modifierGroup.Id] = 'Choose 1'
        } else if (modifierGroup.LowerLimit === 0) {
          this.modifierHelperText[modifierGroup.Id] = 'Optional up to ' + modifierGroup.UpperLimit
        } else {
          this.modifierHelperText[modifierGroup.Id] = 'Choose between ' + modifierGroup.LowerLimit + ' and ' + modifierGroup.UpperLimit + ' options'
        }

        for (let j = 0; j < modifierGroup.Modifiers.length; j++) {
          let modifierValue = modifierGroup.Modifiers[j]
          this.modifierValuesById[modifierValue.Id] = modifierValue
        }
      }
    },
    onTimeSlots: function (response) {
      console.log(response)
      this.timeSlots = response.times
      if(this.timeSlots !== 0) {
        this.selectedTime = this.timeSlots[0]
      }
      /*if (this.timeSlots.length === 0) {
        this.unavailable = true
        this.errorMessageContact = "Oops! Looks like we're not taking orders at this time. Please call us on " + this.restaurantPhoneNumber
      } else {
        this.selectedTime = this.timeSlots[0]
      }*/
    },
    onAPIFail: function (response) {
      this.authToken = ''
    },
    isTakeawayOrCollectionSelectionValid: function () {
      if (!this.phoneNumberIsValid) {
        return false
      }

      return this.$refs.basicInfoForm.validate()
    },
    isDelivery: function () {
      return this.collectionOrDelivery === 2
    },
    onPhoneNumberInput: function ({ number, isValid, country }) {
      let found = false
      for (let i = 0; i < this.g_validLocations.length; i++) {
        if (this.g_validLocations[i] === country.iso2) {
          found = true
          break
        }
      }

      if (!found && isValid) {
        this.phoneNumberIsInvalidMessage = number + ' is valid, but we cannot send SMS confirmations to it'
        this.orderNumber = number
        this.phoneNumberIsValid = true;
      } else if (!found && !isValid) {
        this.phoneNumberIsInvalidMessage = 'Number is not valid: ' + number
        this.phoneNumberIsValid = false;
      } else if (isValid) {
        this.orderNumber = number
        this.phoneNumberIsInvalidMessage = ''
        this.phoneNumberIsValid = true;
      } else {
        this.phoneNumberIsInvalidMessage = 'Number is not valid: ' + number
        this.phoneNumberIsValid = false;
      }
    },
    progressStepper: function (nextStep) {
      if (this.progressStep === nextStep) {
        return
      }

      this.progressStep = nextStep

      if (this.progressStep === 2) {
        this.loadTimeSlots()
      } else if (this.progressStep === 3) {
        this.onCheckout()
      }
    },
    onOrderPlus: function (item) {
      if (item.ModifierGroups.length === 0) {
        this.addToBasket(item)
      } else {
        this.openModifierDialog(item)
      }
    },
    openModifierDialog: function (item) {
      this.resetTempItems()

      this.showModifierDialog = true
      this.currentItemInModifierDialog = item
      this.modifierDialogTitle = 'Choices for ' + item.Name
      this.itemModifierGroups.splice(0, this.itemModifierGroups.length)
      for (let i = 0; i < this.menu.ModifierGroups.length; i++) {
        for (let j = 0; j < item.ModifierGroups.length; j++) {
          if (item.ModifierGroups[j] === this.menu.ModifierGroups[i].Id) {
            this.itemModifierGroups.push({
              modifierGroup: this.menu.ModifierGroups[i]
            })
          }

          for (let k = 0; k < this.menu.ModifierGroups[i].Modifiers.length; k++) {
            this.itemModifierValuesDisabled[this.menu.ModifierGroups[i].Modifiers[k].Id] = false
          }
        }
      }
    },
    onModifierChange: function (modifierGroup, modifier) {
      let allModifierIds = []
      for (let i = 0; i < modifierGroup.modifierGroup.Modifiers.length; i++) {
        allModifierIds.push(modifierGroup.modifierGroup.Modifiers[i].Id)
      }

      let selected = []
      let notSelected = []

      for (let i = 0; i < modifierGroup.modifierGroup.Modifiers.length; i++) {
        let aModifier = modifierGroup.modifierGroup.Modifiers[i]
        if (typeof aModifier !== 'undefined' && this.selectedModifiers.hasOwnProperty(aModifier.Id) && this.selectedModifiers[aModifier.Id]) {
          selected.push(aModifier)
        } else {
          notSelected.push(aModifier)
        }
      }

      let upper = modifierGroup.modifierGroup.UpperLimit
      if (upper === selected.length) {
        // don't let the user make any more selections
        for (let i = 0; i < notSelected.length; i++) {
          this.itemModifierValuesDisabled[notSelected[i].Id] = true
        }
      } else {
        // any selections that were disabled before can now be re-enabled
        for (let i = 0; i < notSelected.length; i++) {
          this.itemModifierValuesDisabled[notSelected[i].Id] = false
        }
      }
    },
    getSelectedModifiers: function () {
      let modifiers = []
      for (let property in this.selectedModifiers) {
        if (typeof property !== 'undefined' && this.selectedModifiers.hasOwnProperty(property) && this.selectedModifiers[property]) {
          modifiers.push(property)
        }
      }
      return modifiers
    },
    onModifierDialogOK: function () {
      this.addToBasket(this.currentItemInModifierDialog, this.getSelectedModifiers())

      this.showModifierDialog = false
      this.resetTempItems()
    },
    onModifierDialogCancel: function () {
      this.showModifierDialog = false
      this.resetTempItems()
    },
    requiredModifiersSatisfied: function () {
      if (typeof this.currentItemInModifierDialog.Id === 'undefined' || this.currentItemInModifierDialog === {}) {
        return false
      }

      let selectedModifiers = this.getSelectedModifiers()
      for (let i = 0; i < this.currentItemInModifierDialog.ModifierGroups.length; i++) {
        let modifierGroup = this.modifierGroupsById[this.currentItemInModifierDialog.ModifierGroups[i]]
        let choices = this.getNumberOfModifierChoices(selectedModifiers, modifierGroup)
        if (!(choices >= modifierGroup.LowerLimit && choices <= modifierGroup.UpperLimit)) {
          return false
        }
      }

      return true
    },
    getNumberOfModifierChoices: function (selectedModifiers, modifierGroup) {
      let count = 0
      for (let i = 0; i < selectedModifiers.length; i++) {
        let modifier = this.modifierValuesById[selectedModifiers[i]]
        if (modifierGroup.Id === modifier.ModifierGroupId) {
          count++
        }
      }
      return count
    },
    sendEmail: function() {
      let re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      if(!re.test(this.emailReceiptAddress)) {
        this.snackbar = true
        this.snackbarText = 'Email address is invalid'
        return;
      }

      this.poster(targetUrl + "/email/" + this.customerTakeawayResponse.Id, {firstName: this.orderName, lastName:'', email:this.emailReceiptAddress, internationalCode:'', phoneNumber:this.orderNumber},
        ()=> {
          this.snackbar = true
          this.snackbarText = 'Email Receipt has been sent to ' + this.emailReceiptAddress
          this.showEmailDialog = false;
        },
        ()=> {
          this.snackbar = true
          this.snackbarText = 'Cannot send email at this time'
          this.showEmailDialog = false;
        })
    },
    itemIsInBasket: function (item) {
      for (let i = 0; i < this.basket.length; i++) {
        if (this.basket[i].MenuItemId === item.Id) {
          return true
        }
      }
      return false
    },
    getItemQuantity: function (item) {
      let count = 0
      for (let i = 0; i < this.basket.length; i++) {
        if (this.basket[i].MenuItemId === item.Id) {
          count += this.basket[i].Quantity
        }
      }

      return count
    },
    addToBasket: function (item, modifiers = []) {
      // check for existing
      let added = false
      for (let i = 0; i < this.basket.length; i++) {
        if (this.basket[i].MenuItemId === item.Id && this.arraysEqual(modifiers, this.basket[i].Modifiers)) {
          this.basket[i].Quantity = this.basket[i].Quantity + 1
          added = true
          break
        }
      }

      let tempIds = item.Id
      modifiers.sort()
      for (let i = 0; i < modifiers.length; i++) {
        let modifier = modifiers[i]
        tempIds += '.' + modifier
      }

      if (!added) {
        this.basket.push({
          tempId: tempIds,
          MenuItemId: item.Id,
          MenuItem: item,
          Quantity: 1,
          Modifiers: modifiers,
          Note: null
        })
      }
    },
    removeFromBasket: function (item) {
      if (item.Quantity > 1) {
        item.Quantity -= 1
      } else {
        for (let i = 0; i < this.basket.length; i++) {
          if (this.basket[i].tempId === item.tempId) {
            this.basket.splice(i, 1)
            break
          }
        }
      }
    },
    arraysEqual: function (array1, array2) {
      if (array1 === array2) return true
      if (array1 == null || array2 == null) return false
      if (array1.length !== array2.length) return false

      array1.sort()
      array2.sort()

      for (let i = 0; i < array1.length; i++) {
        if (array1[i] !== array2[i]) return false
      }

      return true
    },
    isCheckoutable: function () {
      if (this.basket.length === 0) {
        return false
      }

      if (this.isUnderMinimumValue()) {
        return false
      } else if (this.isOverMaximumValue()) {
        this.snackbar = true
        this.snackbarText = 'Cannot create an order that exceeds maximum value: ' + this.currencySymbol + this.toPrice(this.maxOrderValue)
        return false
      }

      if(this.timeSlots.length === 0) {
        return false;
      }

      return true
    },
    isUnderMinimumValue: function() {
      return this.calculateTotal() < this.minOrderValue
    },
    isOverMaximumValue: function() {
      return this.calculateTotal() > this.maxOrderValue;
    },
    calculateItemTotal: function (basketItem) {
      let total = basketItem.MenuItem.Price
      for (let i = 0; i < basketItem.Modifiers.length; i++) {
        total += this.modifierValuesById[basketItem.Modifiers[i]].Price
      }
      return total * basketItem.Quantity
    },
    calculateTotal: function () {
      let total = 0
      for (let i = 0; i < this.basket.length; i++) {
        total += this.calculateItemTotal(this.basket[i])
      }

      // add any extra costs
      total += this.extraCosts

      return total
    },
    resetTempItems: function () {
      this.itemModifierGroups.splice(0, this.itemModifierGroups.length)
      this.itemModifierValuesDisabled = {}
      this.modifierDialogTitle = 'Choices'
      this.selectedModifiers = {}
      this.currentItemInModifierDialog = {}
    },
    toPrice: function (price) {
      return price.toFixed(2)
    },
    modifiersToString: function(modifiers) {
      let array = [];
      for(let i = 0; i < modifiers.length; i++) {
        let modifier = modifiers[i];
        array.push(this.modifierValuesById[modifier].ModifierValue + ' (+' + this.currencySymbol + this.toPrice(this.modifierValuesById[modifier].Price) + ')');
      }
      return array.join(',');
    },
    onWarningsDialogOK: function() {
      this.showWarningsDialog = false
      this.progressStepper(2)
    },
    onDeliveryChargesOK: function() {
      this.showChargesDialog = false
      this.askCashOrCard()
    },
    onDeliveryChargesCancel: function() {
      this.showChargesDialog = false
      this.progressStepper(2)
    },
    onCheckout: function () {
      this.putBasket()
    },
    checkResponse: function(response) {
      this.lastResponse = response;
      // check for warnings
      if(typeof(response['Warning']) !== 'undefined' && response.Warning.length > 0) {
        this.showWarningsDialog = true
        this.warningsMessage = 'We found one or more issues with your order: ' + response.Warning.join('; ');
        return
      }

      // if there is an extra charge, ask the user if it OK to proceed
      if(response.extraCosts > 0) {
        this.extraCosts = response.extraCosts
        this.chargesMessage = 'An extra charge of '
          + this.currencySymbol
          + this.toPrice(response.extraCosts)
          + ' needs to be applied to this order: '
          + response.extraCostsReason
        this.showChargesDialog = true
        return
      }

      // if the total value is cashable, ask if they want to do a cash payment or pay by card now
      this.askCashOrCard()
    },
    askCashOrCard: function() {
      // don't bother asking for card if no stripe account is set up
      if(typeof(this.stripePublicKey) === 'undefined' || this.stripePublicKey == null ) {
        // cash only
        this.cashOnly = true;
      } else {
        this.cashOnly = false;
      }


      if((this.lastResponse.extraCosts + this.calculateTotal()) <= this.maxWithoutCC) {
        this.showPayByCashDialog = true
      } else {
        this.doStripeCheckout()
      }
    },
    onCashOrCardCancel: function() {
      this.showPayByCashDialog = false;
      this.progressStepper(2);
    },
    cashOrCardResponse: function(card) {
      this.showPayByCashDialog = false
      if(card) {
        this.doStripeCheckout()
      } else {
        this.postBasket();
      }
    },
    doStripeCheckout: async function () {
      const { token, args } = await this.$refs.checkoutRef.open()
    },
    onPaymentDone: function ({token, args}) {
      this.postBasket(token)
    },
    onPaymentOpened: function () {

    },
    onPaymentClosed: function () {

    },
    onPaymentCanceled: function () {
      this.progressStepper(2)
    },
    getter: function (endpoint, onSuccess, onFail) {
      let optionsAuth = {
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          'Authorization': this.authToken
        },
        timeout: 5000
      }

      this.$http.get(endpoint, optionsAuth)
        .then(
          response => {
            if (response.status >= 400) {
              console.log('Failed status: ' + JSON.stringify(response.data))
              onFail(response)
            } else {
              onSuccess(response.body)
            }
          },
          error => {
            console.log('Error: ' + JSON.stringify(error))
            onFail(error.status + ': ' + error.body)
          })
    },
    poster: function (endpoint, payload, onSuccess, onFail) {
      let optionsAuth = {
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          'Authorization': this.authToken
        },
        timeout: 5000
      }

      this.$http.post(endpoint, payload, optionsAuth)
        .then(
          response => {
            if (response.status >= 400) {
              console.log('Failed status: ' + JSON.stringify(response.data))
              onFail(response)
            } else {
              onSuccess(response.body)
            }
          },
          error => {
            console.log('Error: ' + JSON.stringify(error))
            onFail(error)
          })
    },
    putter: function (endpoint, payload, onSuccess, onFail) {
      let optionsAuth = {
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          'Authorization': this.authToken
        },
        timeout: 5000
      }

      this.$http.put(endpoint, payload, optionsAuth)
        .then(
          response => {
            if (response.status >= 400) {
              console.log('Failed status: ' + JSON.stringify(response.data))
              onFail(response)
            } else {
              onSuccess(response.body)
            }
          },
          error => {
            console.log('Error: ' + JSON.stringify(error))
            onFail(error)
          })
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .app-stores{
    font-size: 12px;
    text-align: center;
  }

  .app-stores img{
    max-width: 190px;
  }

  .padded-checkout-btn {
    padding: 10px !important;
  }
</style>
