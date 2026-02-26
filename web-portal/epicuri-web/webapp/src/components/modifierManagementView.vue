<template>
    <div>
        <h1 style="text-align:center"><span><i>[BETA]</i></span></h1>
        <div v-if="showEditItem === false">
            <md-subheader>These are your "modifier groups". A group defines a set of modifiers. For example, you can have a group called "Mixers", and have modifiers within it called "Cola", "Lemonade" and so on.</md-subheader>
            <md-divider></md-divider>
            <br>
            <md-button class="md-icon-button md-raised md-accent" @click="addItem">
                <md-icon>add</md-icon>
                <md-tooltip md-direction="top">Add a modifier group</md-tooltip>
            </md-button>

            <md-table>
                <md-table-header>
                    <md-table-row>
                        <md-table-head></md-table-head>
                        <md-table-head></md-table-head>
                        <md-table-head>Name</md-table-head>
                        <md-table-head>Associated Modifier Values</md-table-head>
                        <md-table-head></md-table-head>
                    </md-table-row>
                </md-table-header>
                <md-table-body>
                    <md-table-row v-for="modifierGroup in modifierGroups" v-bind:key="modifierGroup.Id">
                        <md-table-cell>
                            <md-button class="md-icon-button md-primary" @click="onEditItem(modifierGroup)">
                                <md-icon>edit</md-icon>
                                <md-tooltip>Edit</md-tooltip>
                            </md-button>
                        </md-table-cell>
                        <md-table-cell>
                            <md-button class="md-icon-button md-warn" @click="onModifierGroupDeleteSelected(modifierGroup)">
                                <md-icon>delete</md-icon>
                                <md-tooltip>Delete this group, underlying modifier values and any menu item associations</md-tooltip>
                            </md-button>
                        </md-table-cell>
                        <md-table-cell>{{modifierGroup.GroupName}}</md-table-cell>
                        <md-table-cell>{{modifierGroup.Modifiers.length}} modifiers</md-table-cell>
                        <md-table-cell>{{modifierGroup.summaryText}}</md-table-cell>
                    </md-table-row>
                </md-table-body>
            </md-table>
        </div>
        <div v-else>
            <form>
                <md-input-container>
                    <label>Name</label>
                    <md-input required v-model="selectedItem.GroupName"></md-input>
                </md-input-container>
                <md-input-container>
                    <label>Minimum number of selections</label>
                    <md-input required v-model="selectedItem.LowerLimit"></md-input>
                    <md-tooltip md-direction="top">Set to 0 if this is an optional modifier</md-tooltip>
                </md-input-container>
                <md-input-container>
                    <label>Maximum number of selections</label>
                    <md-input required v-model="selectedItem.UpperLimit"></md-input>
                </md-input-container>
                <md-input-container v-if="selectedItem.Id">
                    <label>Menu Items</label>
                    <md-chips v-model="selectedItemMenuItems" md-static></md-chips>
                </md-input-container>
            </form>
            <br>
            <br>
            <md-subheader>Modifiers</md-subheader>

            <md-button class="md-fab md-mini md-plain" @click="addRow">
                <md-icon>add</md-icon>
                <md-tooltip md-direction="top">Add a modifier</md-tooltip>
            </md-button>

            <div>
                <md-table>
                    <md-table-header>
                        <md-table-row>
                            <md-table-head></md-table-head>
                            <md-table-head md-sort-by="ModifierValue">Modifier Name</md-table-head>
                            <md-table-head md-sort-by="Price">Modifier Price</md-table-head>
                            <md-table-head md-sort-by="TaxTypeId">Tax</md-table-head>
                        </md-table-row>
                    </md-table-header>
                    <md-table-body>
                        <md-table-row v-model="selectedItem.Modifiers" v-for="modifier in selectedItem.Modifiers" v-bind:data="modifier" v-bind:key="modifier.Id">
                            <md-table-cell>
                                <md-button class="md-icon-button" @click="onModifierDelete(modifier)">
                                    <md-icon>delete</md-icon>
                                </md-button>
                            </md-table-cell>
                            <md-table-cell>
                                <md-input-container>
                                    <md-input v-model="modifier.ModifierValue"></md-input>
                                </md-input-container>
                            </md-table-cell>
                            <md-table-cell>
                                <md-input-container>
                                    <md-input v-model="modifier.Price"></md-input>
                                </md-input-container>
                            </md-table-cell>
                            <md-input-container>
                                <label for="tax">Tax</label>
                                <md-select name="tax" id="tax" v-model="modifier.TaxTypeId">
                                    <md-option v-for="tax in taxes" :value="tax.Id" :key="tax.Id">{{tax.Name}}</md-option>
                                </md-select>
                            </md-input-container>
                        </md-table-row>
                    </md-table-body>
                </md-table>
            </div>

            <md-button class="md-raised md-primary" @click="onSave">Save</md-button>
            <md-button class="md-raised md-dense" @click="onCancel">Cancel</md-button>

        </div>

        <md-snackbar md-position="top center" ref="error_snackbar" md-duration="2500">
            <span>Error in saving item</span>
        </md-snackbar>
        <md-snackbar md-position="top center" ref="ok_snackbar" md-duration="2500">
            <span>Saved</span>
        </md-snackbar>
        <md-snackbar md-position="top center" ref="deleted_snackbar" md-duration="2500">
            <span>Deleted</span>
        </md-snackbar>

        <md-dialog-confirm
                md-title="Delete Modifier Group?"
                md-content-html="This will delete the selected modifier permanently. Continue?"
                md-ok-text="Delete"
                md-cancel-text="Cancel"
                @close="onModifierGroupDelete"
                ref="delete_group">
        </md-dialog-confirm>
    </div>
</template>

<script>
    let epicuri = require('../internal/epicuri.js');

    export default {
        name: "modifier_management",

        data: function () {
            return {
                modifierGroups:[],
                taxes:[],
                menuItems:[],
                modifierIdToItemArray:{},
                selectedItemMenuItems:[],

                showEditItem: false,
                selectedItem: null,
                selectedItemCopy: null,
                selectedGroupForDelete: {}
            }
        },

        created: function() {
            this.loadModifierGroups();
            this.loadMenuItems();
            this.loadTaxes();
        },

        methods: {
            loadModifierGroups: function() {
                this.modifierGroups.splice(0, this.modifierGroups.length);

                epicuri.getModifierGroups(this.$http,
                    (response) => {
                        this.modifierGroups = response.body;
                        this.modifierGroups.sort(function(a, b){
                            if(a.GroupName < b.GroupName) return -1;
                            if(a.GroupName > b.GroupName) return 1;
                            return 0;
                        });

                        for(let i = 0; i < this.modifierGroups.length; i++) {
                            this.updateSummary(this.modifierGroups[i]);
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    })
            },
            updateSummary: function(modifierGroup) {
                if(modifierGroup.LowerLimit === 0) {
                    modifierGroup.summaryText = 'Optional';
                } else {
                    modifierGroup.summaryText = 'Mandatory (minimum ' + modifierGroup.LowerLimit + ' selections)';
                }

                modifierGroup.summaryText += ' (maximum ' + modifierGroup.UpperLimit + ' selections)';
            },
            loadTaxes: function() {
                epicuri.getTaxes(this.$http,
                    (response) => {
                        this.taxes = response.body;
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    })
            },
            loadMenuItems: function() {
                epicuri.getMenuItems(this.$http,
                    (response) => {
                        this.menuItems = response.body;

                        for(let i = 0; i < this.menuItems.length; i++) {
                            let item = this.menuItems[i];
                            for(let j = 0; j < item.ModifierGroups.length; j++) {
                                let modifier = item.ModifierGroups[j];
                                if(this.modifierIdToItemArray[modifier] === undefined) {
                                    this.modifierIdToItemArray[modifier] = [];
                                }
                                if(!this.arrayContains(item.Id, this.modifierIdToItemArray[modifier])) {
                                    this.modifierIdToItemArray[modifier].push(item.Name);
                                }
                            }
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    });
            },
            arrayContains: function(element, array) {
                return (array.indexOf(element) > -1);
            },
            bulkDelete: function() {
                //todo
            },
            addItem: function() {
                this.showEditItem = true;
                this.resetSelectedItem();
            },
            addRow:function() {
                this.selectedItem.Modifiers.push({
                    ModifierValue: 'New Modifier',
                    Price: 0,
                    TaxTypeId: this.taxes[0].Id,
                    ModifierGroupId: this.selectedItem.Id
                });
            },
            onModifierDelete:function(modifier) {
                let i = this.selectedItem.Modifiers.length;
                while(i--) {
                    if(this.selectedItem.Modifiers[i].Id === modifier.Id) {
                        this.selectedItem.Modifiers.splice(i,1);
                        break;
                    }
                }
            },
            onModifierGroupDeleteSelected:function(modifierGroup) {
                this.openDialog('delete_group');
                this.selectedGroupForDelete = modifierGroup;
            },
            onModifierGroupDelete:function(closeType) {
                if(!this.selectedGroupForDelete.Id) {
                    return;
                }

                if(closeType !== 'ok') {
                    return;
                }

                epicuri.deleteModifierGroup(this.$http, this.selectedGroupForDelete.Id,
                    (response) => {
                        this.$refs.deleted_snackbar.open();
                        let i = this.modifierGroups.length;
                        while(i--) {
                            if(this.modifierGroups[i].Id === this.selectedGroupForDelete.Id) {
                                this.modifierGroups.splice(i,1);
                                break;
                            }
                        }
                        this.selectedGroupForDelete = {};
                        this.loadModifierGroups();
                    },
                    (response) => {
                        this.selectedGroupForDelete = {};
                    })
            },
            openDialog: function(ref) {
                this.$refs[ref].open();
            },
            onEditItem: function(modifierGroup) {
                this.showEditItem = true;
                this.selectedItem = modifierGroup;
                this.selectedItemCopy = JSON.parse(JSON.stringify(this.selectedItem));
                this.selectedItemMenuItems = this.modifierIdToItemArray[this.selectedItem.Id];
            },
            onSave: function () {
                if(this.selectedItem.Id) {
                    epicuri.putModifierGroup(this.$http, this.selectedItem,
                        (response) => {
                            this.$refs.ok_snackbar.open();
                            this.updateSummary(this.selectedItem);
                            this.onCancel(false);
                            this.loadModifierGroups();
                        },
                        (response) => {
                            this.$refs.error_snackbar.open();
                        });
                } else {
                    epicuri.postModifierGroup(this.$http, this.selectedItem,
                        (response) => {
                            this.$refs.ok_snackbar.open();
                            this.updateSummary(this.selectedItem);
                            this.onCancel(true);
                            this.loadModifierGroups();
                        },
                        (response) => {
                            this.$refs.error_snackbar.open();
                        });
                }
                this.showEditItem = false;
                this.resetSelectedItem();
            },
            onCancel: function (newItem=false) {
                this.showEditItem = false;
                this.selectedItem = null;
                if(!newItem) {
                    for (let i = 0; i < this.modifierGroups.length; i++) {
                        if (this.modifierGroups[i].Id === this.selectedItemCopy.Id) {
                            this.modifierGroups[i] = JSON.parse(JSON.stringify(this.selectedItemCopy));
                            break;
                        }
                    }
                }
                this.resetSelectedItem();
            },
            resetSelectedItem: function() {
                this.selectedItem = {
                    GroupName: 'New Modifier Group',
                    LowerLimit: 0,
                    UpperLimit: 1,
                    Modifiers: []
                };
                this.selectedItemMenuItems = [];
            }
        }
    }
</script>

<style scoped>

</style>