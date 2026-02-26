<template>
    <div>
        <h1 style="text-align:center"><span><i>[BETA]</i></span></h1>
        <md-subheader>Once stock processing is complete, please relaunch 'Menu Manager' in the Epicuri POS app to see changes.</md-subheader>
        <md-button class="md-icon-button md-raised md-primary" @click="triggerEditStockDialog(null)">
            <md-icon>add</md-icon>
            <md-tooltip md-direction="top">Add a new SKU</md-tooltip>
        </md-button>
        <md-layout md-flex-xsmall="600" md-flex-small="600" md-flex-medium="800" md-flex-large="800" md-flex-xlarge="800">
            <md-table @sort="onSort" v-if="stockControlEnabled">
                <md-table-header>
                    <md-table-row>
                        <md-table-head></md-table-head>
                        <md-table-head md-sort-by="plu">SKU</md-table-head>
                        <md-table-head md-sort-by="trackable">Tracked</md-table-head>
                        <md-table-head md-sort-by="level">Current Stock Level</md-table-head>
                        <md-table-head>Menu Items</md-table-head>
                    </md-table-row>
                </md-table-header>
                <md-table-body>
                    <md-table-row v-for="stockLevel in stockControl" v-bind:data="stockLevel" v-bind:key="stockLevel.id" v-bind:class="{nottrackable : !stockLevel.trackable}">
                        <md-table-cell>
                            <md-button class="md-icon-button md-primary" @click="triggerEditStockDialog(stockLevel)">
                                <md-icon>edit</md-icon>
                                <md-tooltip>Edit</md-tooltip>
                            </md-button>
                            <md-button class="md-fab md-icon-button md-warn" @click="triggerDeleteStockDialog(stockLevel)">
                                <md-icon>delete</md-icon>
                                <md-tooltip>Delete SKU permanently</md-tooltip>
                            </md-button>
                        </md-table-cell>
                        <md-table-cell>
                            {{stockLevel.plu}}
                        </md-table-cell>
                        <md-table-cell>
                            {{stockLevel.trackable ? 'YES' : 'NO'}}
                        </md-table-cell>
                        <md-table-cell>
                            {{stockLevel.level}}
                        </md-table-cell>
                        <md-table-cell>
                            {{getNumberOfItemsForStockLevel(stockLevel)}}
                        </md-table-cell>
                    </md-table-row>
                </md-table-body>
            </md-table>
            <md-subheader v-else>
                Stock control is currently disabled. Please contact <br><a href="mailto:support@epicuri.co.uk"> Epicuri Support</a>
            </md-subheader>
        </md-layout>

        <!--Edit dialogs-->
        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="new_stock">
            <md-dialog-title>Create/Edit new SKU</md-dialog-title>
            <md-dialog-content>
                <form novalidate>
                    <md-input-container>
                        <label>SKU</label>
                        <md-input required v-model="currentStockLevel.plu"></md-input>
                    </md-input-container>
                    <md-input-container>
                        <label>Current Stock Level</label>
                        <md-input required v-model="currentStockLevel.level"></md-input>
                    </md-input-container>
                    <md-checkbox v-model="currentStockLevel.trackable">Track Stock</md-checkbox>
                </form>
                <md-button class="md-info" @click="openItemSelection()">Apply to items ({{getNumberOfItemsForStockLevel(currentStockLevel)}})</md-button>
                <md-dialog-actions>
                    <md-button class="md-primary" @click="closeDialog('new_stock', true)">Cancel</md-button>
                    <md-button class="md-primary" @click="saveStockLevel()">Save</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>
        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="item_selection">
            <md-dialog-title>Apply SKU to Items</md-dialog-title>
            <md-dialog-content>
                <form novalidate>
                    <md-input-container>
                        <label>Items</label>
                        <md-select multiple v-model="selectedItems">
                            <md-option v-for="item in menuItems" :key="item.Id" :value="item.Id">
                                {{item.Name}}
                            </md-option>
                        </md-select>
                    </md-input-container>
                </form>
            </md-dialog-content>
            <md-dialog-actions>
                <md-button class="md-primary" @click="closeDialog('item_selection')">Cancel</md-button>
                <md-button class="md-primary" @click="applyItemSelection()">OK</md-button>
            </md-dialog-actions>
        </md-dialog>

        <md-dialog-confirm
                :md-title="deleteDialogTitle"
                md-content-html="This will delete this SKU and disassociate all menu items to it."
                md-ok-text="Delete"
                md-cancel-text="Cancel"
                @close="onDeleteDialogClose"
                ref="delete_stock">
        </md-dialog-confirm>

        <!--Snackbars-->
        <md-snackbar ref="snackbar" :md-duration="5000">
            <span>{{snackBarMessage}}</span>
            <md-button class="md-accent" md-theme="light-blue" @click="$refs.snackbar.close()">Close</md-button>
        </md-snackbar>
    </div>
</template>

<script>
    let epicuri = require('../internal/epicuri.js');

    export default {
        name: "stock_control",
        computed: {
            deleteDialogTitle: function () {
                if(!this.currentStockLevel.plu) {
                    return "";
                }
                return "Delete SKU: " + this.currentStockLevel.plu + "?"
            }
        },
        data: function () {
            return {
                stockControl:[],
                stockControlEnabled: false,
                currentStockLevel:{},
                restaurantId:"",
                snackBarMessage:"",
                menuItems:[],
                selectedItems:[]
            }
        },
        created: function () {
            this.loadMenuItems();
            this.loadRestaurant();
            this.loadStockControl();
        },
        methods: {
            loadRestaurant: function() {
                epicuri.getRestaurant(this.$http,
                    (response) => {
                        let restaurant = response.body;
                        if(restaurant.RestaurantDefaults.EnableStockCountdown) {
                            this.stockControlEnabled = true;
                        }
                        this.restaurantId = restaurant.id;
                    },
                    (response) => {
                        console.log("ERROR " + response)
                    });
            },
            loadStockControl: function() {
                epicuri.getStockControl(this.$http,
                    (response) => {
                        this.stockControl.splice(0, this.stockControl.length);
                        for(let i = 0; i < response.body.length; i++) {
                            this.stockControl.push(response.body[i]);
                        }
                    },
                    (response) => {
                        console.log("ERROR " + response)
                    })
            },
            loadMenuItems: function() {
                epicuri.getMenuItems(this.$http,
                    (response) => {
                        this.menuItems = response.body;
                        this.menuItems.sort(function(a, b){
                            if(a.Name < b.Name) return -1;
                            if(a.Name > b.Name) return 1;
                            return 0;
                        });
                        this.resetSelections();
                    },
                    (response) => {
                        this.snackBarMessage = "Could not get menu items: " + response.body.Message;
                        this.$refs.snackbar.open();
                    }
                );
            },
            onSort: function(sort) {
                this.stockControl = _.orderBy(this.stockControl, [item => item[sort.name]], sort.type);
            },
            resetSelections: function() {
                this.selectedItems.splice(0,this.selectedItems.length);
            },
            getNumberOfItemsForStockLevel: function(stockLevel) {
                if(!stockLevel.id) {
                    return 0;
                }
                let count = 0;
                for(let i = 0; i < this.menuItems.length; i++) {
                    let item = this.menuItems[i];
                    if(item.plu === stockLevel.plu) {
                        count++;
                    }
                }
                return count;
            },
            triggerEditStockDialog: function (item) {
                this.openDialog('new_stock');
                if(item === null) {
                    this.currentStockLevel = {id: null, plu: "", level:0, restaurantId: this.restaurantId, trackable: false};
                } else {
                    this.currentStockLevel = item;
                }
            },
            triggerDeleteStockDialog: function (item) {
                this.currentStockLevel = item;
                this.openDialog('delete_stock');
            },
            saveStockLevel: function() {
                if(this.currentStockLevel.id === null) {
                    epicuri.postStockControl(this.$http, this.currentStockLevel, this.onSave, this.onSaveFail);
                } else {
                    epicuri.putStockControl(this.$http, this.currentStockLevel, this.onSave, this.onSaveFail);
                }

                if(this.selectedItems.length > 0) {
                    let items = [];
                    for(let i = 0; i < this.menuItems.length; i++) {
                        for(let j = 0; j < this.selectedItems.length; j++) {
                            if(this.selectedItems[j] === this.menuItems[i].Id) {
                                this.menuItems[i].plu = this.currentStockLevel.plu;
                                items.push(this.menuItems[i]);
                            }
                        }
                    }
                    if(items.length > 0) {
                        epicuri.putMenuItems(this.$http, items, ()=>{}, (response)=>{console.log("ERROR: " + response)});
                    }
                    this.resetSelections();
                }
                this.currentStockLevel = {};
            },
            openItemSelection: function() {
                if(this.currentStockLevel.plu !== "" && this.currentStockLevel.plu !== null && typeof(this.currentStockLevel.plu) !== 'undefined') {
                    this.resetSelections();
                    for (let i = 0; i < this.menuItems.length; i++) {
                        if (this.menuItems[i].plu === this.currentStockLevel.plu) {
                            this.selectedItems.push(this.menuItems[i].Id);
                        }
                    }
                }
                this.openDialog('item_selection');
            },
            applyItemSelection: function() {
                this.closeDialog('item_selection');
                //nothing else to do at the moment
            },
            onDeleteDialogClose: function(closeType) {
                if(closeType === 'ok') {
                    epicuri.deleteStockControl(this.$http, this.currentStockLevel,
                        () => {
                            let found = -1;
                            for(let i = 0; i < this.stockControl.length; i++) {
                                if(this.stockControl[i].id === this.currentStockLevel.id) {
                                    found = i;
                                    break;
                                }
                            }
                            if(found > -1) {
                                this.stockControl.splice(found,1);
                            }
                            this.resetTemps();
                            this.loadStockControl();
                        },
                        () => {
                            this.snackBarMessage = "Error deleting SKU: " + response.body.Message;
                            this.$refs.snackbar.open();
                            this.resetTemps();
                        })
                }
            },
            onSave: function() {
                this.closeDialog('new_stock');
                if(this.currentStockLevel.id === null || typeof(this.currentStockLevel.id) === 'undefined') {
                    this.stockControl.push(this.currentStockLevel);
                }
                this.currentStockLevel = {};
                this.loadStockControl();
            },
            onSaveFail: function(response) {
                this.closeDialog('new_stock');
                this.snackBarMessage = "Could not update this item: " + response.body.Message;
                this.$refs.snackbar.open();
                this.resetTemps();
            },
            resetTemps: function() {
                this.currentStockLevel = {};
            },
            openDialog: function(ref) {
                this.$refs[ref].open();
            },
            closeDialog: function(ref, resetSelection=false) {
                this.$refs[ref].close();
                if(resetSelection) {
                    this.resetSelections();
                }
            }
        }
    }
</script>

<style scoped>
    .nottrackable {
        background-color: lightgray;
    }
</style>