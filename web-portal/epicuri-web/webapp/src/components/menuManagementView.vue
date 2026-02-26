<template>
    <div>
        <div v-if="showEditMenuItem === false">
            <h1 style="text-align:center"><span><i>[BETA]</i></span></h1>
            <h2>Filters</h2>
            <form>
                <md-input-container>
                    <label for="menuFilter">Menus</label>
                    <md-select name="menuFilter" id="menuFilter" multiple v-model="filter_menus">
                        <md-option v-for="menu in menus" :value="menu.Id">
                            {{menu.MenuName}}
                        </md-option>
                    </md-select>
                </md-input-container>

                <md-checkbox v-model="filter_orphanedItemsOnly">
                    <md-tooltip md-direction="right">"Orphaned" items are those that have been created but are not yet part of any menu</md-tooltip>
                    Only show orphaned items
                </md-checkbox>

                <md-input-container>
                    <label for="printerFilter">Printers</label>
                    <md-select name="printerFilter" id="printerFilter" multiple v-model="filter_printers">
                        <md-option v-for="printer in printers" :value="printer.Id">
                            {{printer.Name}}
                        </md-option>
                    </md-select>
                </md-input-container>

                <md-checkbox v-model="filter_includeItemsWithNoMod">
                    <md-tooltip md-direction="right">Include menu items that have no modifier(s) attached to it; or select modifiers from below</md-tooltip>
                    All items including those without a modifier
                </md-checkbox>

                <md-input-container v-if="!filter_includeItemsWithNoMod">
                    <label for="modifierFilter">Modifiers</label>
                    <md-select name="modifierFilter" id="modifierFilter" multiple v-model="filter_modifiers">
                        <md-option v-for="modifier in modifierGroups" :value="modifier.Id">
                            {{modifier.GroupName}}
                        </md-option>
                    </md-select>
                </md-input-container>

                <md-input-container>
                    <label for="taxFilter">Taxes</label>
                    <md-select name="taxFilter" id="taxFilter" multiple v-model="filter_tax">
                        <md-option v-for="tax in taxes" :value="tax.Id">
                            {{tax.Name}}
                        </md-option>
                    </md-select>
                </md-input-container>


                <md-input-container>
                    <label>Search</label>
                    <md-input v-model="filter_text"></md-input>
                </md-input-container>

                <md-button class="md-raised md-primary" @click="clearFilters()">reset filters</md-button>
            </form>


            <hr>
            <h2>Menu Items</h2>

            <md-button class="md-icon-button md-raised md-primary" @click="selectAll">
                <md-icon v-if="!items_selected">select_all</md-icon>
                <md-icon v-if="items_selected">border_clear</md-icon>
                <md-tooltip md-direction="top" v-if="!items_selected">Select all items</md-tooltip>
                <md-tooltip md-direction="top" v-if="items_selected">De-select all items</md-tooltip>
            </md-button>

            <md-menu md-size="5">
                <md-tooltip md-direction="top">Bulk edit selected items</md-tooltip>
                <md-button class="md-icon-button md-raised md-primary" md-menu-trigger>
                    <md-icon>edit</md-icon>
                </md-button>

                <md-menu-content>
                    <md-menu-item @click="openDialog('select_group')">
                        <md-icon>playlist_add</md-icon>
                        <span>Assign to Menu Group</span>
                    </md-menu-item>
                    <md-menu-item @click="openDialog('select_modifier_groups')">
                        <md-icon>kitchen</md-icon>
                        <span>Assign Modifiers</span>
                    </md-menu-item>
                    <md-menu-item @click="openDialog('select_allergens')">
                        <md-icon>how_to_reg</md-icon>
                        <span>Assign Allergens</span>
                    </md-menu-item>
                    <md-menu-item @click="openDialog('select_diets')">
                        <md-icon>accessibility</md-icon>
                        <span>Assign Diet</span>
                    </md-menu-item>
                    <md-menu-item @click="markUnavailability(true)">
                        <md-icon>remove_circle_outline</md-icon>
                        <span>Mark Unavailable</span>
                    </md-menu-item>
                    <md-menu-item @click="markUnavailability(false)">
                        <md-icon>check</md-icon>
                        <span>Mark Available</span>
                    </md-menu-item>
                    <md-menu-item @click="openDialog('select_printer')">
                        <md-icon>print</md-icon>
                        <span>Set Printer</span>
                    </md-menu-item>
                    <md-menu-item @click="openDialog('select_absolute_price')">
                        <md-icon>attach_money</md-icon>
                        <span>Set Price (Absolute)</span>
                    </md-menu-item>
                    <md-menu-item @click="openDialog('select_scaled_price')">
                        <md-icon>attach_money</md-icon>
                        <span>Set Price (Scaled)</span>
                    </md-menu-item>
                    <md-menu-item @click="openDialog('select_tax')">
                        <md-icon>account_balance</md-icon>
                        <span>Set Tax Rate</span>
                    </md-menu-item>
                </md-menu-content>
            </md-menu>

            <md-button class="md-icon-button md-raised md-warn" @click="bulkDelete">
                <md-icon>delete</md-icon>
                <md-tooltip md-direction="top">Delete selected menu items</md-tooltip>
            </md-button>
            <md-button class="md-icon-button md-raised md-accent" @click="addMenuItem">
                <md-icon>add</md-icon>
                <md-tooltip md-direction="top">Add a menu item</md-tooltip>
            </md-button>

            <div>
                <md-table @sort="onSort">
                    <md-table-header>
                        <md-table-row>
                            <md-table-head></md-table-head>
                            <md-table-head md-sort-by="Name">Name</md-table-head>
                            <md-table-head md-sort-by="Price" >Price</md-table-head>
                            <md-table-head md-sort-by="plu">SKU</md-table-head>
                            <md-table-head md-sort-by="TaxTypeId">Tax</md-table-head>
                            <md-table-head md-sort-by="DefaultPrinter">Printer</md-table-head>
                        </md-table-row>
                    </md-table-header>
                    <md-table-body>
                        <md-table-row v-for="menuItem in getFilteredItems()" v-bind:data="menuItem" v-bind:key="menuItem.Id" v-bind:class="{menuitemunavailable : menuItem.Unavailable}">
                            <md-table-cell>
                                <div>
                                    <md-checkbox v-model="menuItem.selected"></md-checkbox>
                                    <md-button class="md-icon-button md-primary" @click="onMenuItemClickEdit(menuItem)">
                                        <md-icon>edit</md-icon>
                                    </md-button>
                                    <md-button class="md-icon-button md-primary" @click="onMenuItemClickDelete(menuItem)">
                                        <md-icon>delete</md-icon>
                                    </md-button>
                                    <md-button class="md-icon-button md-primary" @click="onItemClickInfo(menuItem)">
                                        <md-icon>info</md-icon>
                                    </md-button>
                                </div>
                            </md-table-cell>
                            <md-table-cell>{{menuItem.Name}}</md-table-cell>
                            <md-table-cell md-numeric>{{menuItem.Price}}</md-table-cell>
                            <md-table-cell>{{menuItem.plu}}</md-table-cell>
                            <md-table-cell>{{taxIdToTax[menuItem.TaxTypeId].Name}}</md-table-cell>
                            <md-table-cell>{{printerIdToPrinter[menuItem.DefaultPrinter].Name}}</md-table-cell>
                        </md-table-row>
                    </md-table-body>
                </md-table>
            </div>
        </div>
        <div v-if="showEditMenuItem">
            <md-input-container>
                <label>Name</label>
                <md-input required v-model="selectedItem.Name"></md-input>
            </md-input-container>
            <md-input-container>
                <label>Description</label>
                <md-input v-model="selectedItem.Description"></md-input>
            </md-input-container>
            <md-input-container>
                <label>Price</label>
                <md-input required v-model="selectedItem.Price"></md-input>
            </md-input-container>
            <md-input-container>
                <label>Alias</label>
                <md-input v-model="selectedItem.ShortCode"></md-input>
            </md-input-container>
            <md-input-container>
                <label>Image URL</label>
                <md-input v-model="selectedItem.imageURL"></md-input>
            </md-input-container>
            <md-checkbox v-model="selectedItem.Unavailable">
                <label>Unavailable</label>
            </md-checkbox>

            <div class="field-group">
                <md-input-container>
                    <label for="tax">Tax</label>
                    <md-select name="tax" id="tax" v-model="selectedItem.TaxTypeId">
                        <md-option v-for="tax in taxes" :value="tax.Id">{{tax.Name}}</md-option>
                    </md-select>
                </md-input-container>
            </div>

            <md-input-container>
                <label>SKU</label>
                <md-input v-model="selectedItem.plu"></md-input>
                <md-button @click="openDialog('select_sku')">Select from existing</md-button>
            </md-input-container>

            <div class="field-group">
                <md-input-container>
                    <label for="printer">Printer</label>
                    <md-select name="printer" id="printer" v-model="selectedItem.DefaultPrinter">
                        <md-option v-for="printer in printers" :value="printer.Id">{{printer.Name}}</md-option>
                    </md-select>
                </md-input-container>
            </div>

            <div class="field-group">
                <md-input-container>
                    <label for="itemType">Item Type</label>
                    <md-select name="itemType" id="itemType" v-model="selectedItem.MenuItemTypeId">
                        <md-option v-for="itemType in itemTypes" :value="itemType.id">{{itemType.name}}</md-option>
                    </md-select>
                </md-input-container>
            </div>

            <md-input-container>
                <label for="allergies">Allergens</label>
                <md-select name="allergies" id="allergies" multiple v-model="selectedAllergies">
                    <md-option v-for="allergy in allergies"
                               :key="allergy.Key"
                               :value="allergy">
                        {{allergy.Value}}
                    </md-option>
                </md-select>
            </md-input-container>

            <md-input-container>
                <label for="diets">Diets</label>
                <md-select name="diets" id="diets" multiple v-model="selectedDiets">
                    <md-option v-for="diet in diets"
                               :key="diet.Key"
                               :value="diet">
                        {{diet.Value}}
                    </md-option>
                </md-select>
            </md-input-container>

            <md-input-container>
                <label for="modifiers">Modifiers</label>
                <md-select name="modifiers" id="modifiers" multiple v-model="selectedModifierGroups">
                    <md-option v-for="modifierGroup in modifierGroups"
                               :key="modifierGroup.Id"
                               :value="modifierGroup">
                        {{modifierGroup.GroupName}}
                    </md-option>
                </md-select>
            </md-input-container>

            <md-button @click="onMenuItemSave">save</md-button>
            <md-button @click="onMenuItemCancel">cancel</md-button>
        </div>

        <!-- snackbars -->
        <md-snackbar md-position="top center" ref="error_snackbar" md-duration="2500">
            <span>Error in saving item</span>
        </md-snackbar>
        <md-snackbar md-position="top center" ref="ok_snackbar" md-duration="2500">
            <span>Saved</span>
        </md-snackbar>
        <md-snackbar md-position="top center" ref="delete_ok_snackbar" md-duration="2500">
            <span>Deleted!</span>
        </md-snackbar>
        <md-snackbar md-position="top center" ref="no_items_selected_snackbar" md-duration="2500">
            <span>Select more than 1 item to bulk edit</span>
        </md-snackbar>

        <!-- dialogs -->
        <md-dialog-confirm
                :md-title="deleteItem.title"
                :md-content-html="deleteItem.contentHtml"
                :md-ok-text="deleteItem.ok"
                :md-cancel-text="deleteItem.cancel"
                @open="onOpenDeleteItemDialog"
                @close="onCloseDeleteItemDialog"
                ref="deleteItemDialog">
        </md-dialog-confirm>
        <md-dialog-confirm
                :md-title="saveItem.title"
                :md-content-html="saveItem.contentHtml"
                ref="saveItemDialog">
        </md-dialog-confirm>
        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="select_modifier_groups">
            <md-dialog-title>Select Modifier Group for {{selectedItemsLength}} Items</md-dialog-title>
            <md-dialog-content>
                <md-list>
                    <md-list-item v-for="modifierGroup in modifierGroups">
                        <md-checkbox v-model="modifierGroup.selected">{{modifierGroup.GroupName}} ({{modifierGroup.Modifiers.length}} values)</md-checkbox>
                    </md-list-item>
                </md-list>
            </md-dialog-content>
            <md-dialog-actions>
                <md-button  class="md-primary" @click="closeDialog('select_modifier_groups')">Cancel</md-button>
                <md-button  class="md-primary" @click="assignModifiersToItems()">Apply</md-button>
            </md-dialog-actions>
        </md-dialog>

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="select_allergens">
            <md-dialog-title>Select Allergens for {{selectedItemsLength}} Items</md-dialog-title>
            <md-dialog-content>
                <md-list>
                    <md-list-item v-for="allergy in allergies">
                        <md-checkbox v-model="allergy.selected">{{allergy.Value}}</md-checkbox>
                    </md-list-item>
                </md-list>
            </md-dialog-content>
            <md-dialog-actions>
                <md-button  class="md-primary" @click="closeDialog('select_allergens')">Cancel</md-button>
                <md-button  class="md-primary" @click="assignAllergiesToItems()">Apply</md-button>
            </md-dialog-actions>
        </md-dialog>

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="select_diets">
            <md-dialog-title>Select Dietary Information for {{selectedItemsLength}} Items</md-dialog-title>
            <md-dialog-content>
                <md-list>
                    <md-list-item v-for="diet in diets">
                        <md-checkbox v-model="diet.selected">{{diet.Value}}</md-checkbox>
                    </md-list-item>
                </md-list>
            </md-dialog-content>
            <md-dialog-actions>
                <md-button  class="md-primary" @click="closeDialog('select_diets')">Cancel</md-button>
                <md-button  class="md-primary" @click="assignDietsToItems()">Apply</md-button>
            </md-dialog-actions>
        </md-dialog>

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="select_printer">
            <md-dialog-title>Select printer for {{selectedItemsLength}} items</md-dialog-title>
            <md-dialog-content>
                <md-select v-model="selectedBulkEditPrinter">
                    <md-option v-for="printer in printers" :value="printer.Id">{{printer.Name}}</md-option>
                </md-select>
                <md-dialog-actions>
                    <md-button  class="md-primary" @click="closeDialog('select_printer')">Cancel</md-button>
                    <md-button  class="md-primary" @click="assignPrinterToItems()">Apply</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="select_absolute_price">
            <md-dialog-title>Set Price for {{selectedItemsLength}} items</md-dialog-title>
            <md-dialog-content>
                <md-input-container>
                    <label>Price</label>
                    <md-input type="number" v-model="bulkPrice"></md-input>
                </md-input-container>
                <md-dialog-actions>
                    <md-button  class="md-primary" @click="closeDialog('select_absolute_price')">Cancel</md-button>
                    <md-button :disabled="bulkPrice < 0" class="md-primary" @click="assignPriceToItems()">Apply</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="select_scaled_price">
            <md-dialog-title>Set Price for {{selectedItemsLength}} items</md-dialog-title>
            <md-dialog-content>
                <md-subheader>Scale the price for all selected items by % or a set amount</md-subheader>
                <md-input-container>
                    <label>Price {{absoluteOrPercentage ? '(%)' : ''}}</label>
                    <md-input type="number" v-model="bulkPrice"></md-input>
                    <md-tooltip>Use a negative number to decrease the price by a set amount or by a certain %. E.g. To slash prices by 10, put -10. To half all prices, put -50 *and* set the switch to %</md-tooltip>
                </md-input-container>
                <md-subheader>{{absoluteOrPercentage ? (bulkPrice < 0 ? 'Decrease' : 'Increase') : (bulkPrice < 0 ? 'Subtract' : 'Add')}} by {{bulkPrice}} {{absoluteOrPercentage ? '%' : ''}}</md-subheader>
                <md-checkbox v-model="absoluteOrPercentage" id="absoluteOrPercentageSwitch" name="absoluteOrPercentageSwitch" class="md-primary">Scale as a %</md-checkbox>
                <md-dialog-actions>
                    <md-button  class="md-primary" @click="closeDialog('select_scaled_price')">Cancel</md-button>
                    <md-button class="md-primary" @click="assignPriceToItemsScaled()">Apply</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="item_info">
            <md-dialog-title>Where is "{{selectedItemName}}" used?</md-dialog-title>
            <md-dialog-content>
                <md-subheader>Menu > Category > Group</md-subheader>
                <md-list>
                    <md-list-item v-for="item in selectedItemInGroups">{{item}}</md-list-item>
                </md-list>

            </md-dialog-content>

            <md-dialog-actions>
                <md-button  class="md-primary" @click="closeDialog('item_info')">OK</md-button>
            </md-dialog-actions>
        </md-dialog>


        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="select_group" >
            <md-dialog-title>Which group should these items be added to?</md-dialog-title>
            <md-dialog-content>
                <div>
                    <label for="selectMenuForGroupAssign">Menu</label>
                    <md-select v-model="groupAssignmentSelectedMenu" name="selectMenuForGroupAssign" id="selectMenuForGroupAssign">
                        <md-option v-for="menu in menus" :value="menu.Id">{{menu.MenuName}}</md-option>
                    </md-select>
                </div>
                <div>
                    <label for="selectCategoryForGroupAssign">Category</label>
                    <md-select v-model="groupAssignmentSelectedCategory" name="selectCategoryForGroupAssign" id="selectCategoryForGroupAssign">
                        <md-option v-for="category in menuIdToCategoriesArrayMap[groupAssignmentSelectedMenu]" :value="category.Id">{{category.CategoryName}}</md-option>
                    </md-select>
                </div>
                <div>
                    <label for="selectGroupForGroupAssign">Group</label>
                    <md-select v-model="groupAssignmentSelectedGroup" name="selectGroupForGroupAssign" id="selectGroupForGroupAssign">
                        <md-option v-for="group in categoryIdToGroupsArrayMap[groupAssignmentSelectedCategory]" :value="group.Id">{{group.GroupName}}</md-option>
                    </md-select>
                </div>

                <md-dialog-actions>
                    <md-button  class="md-primary" @click="closeDialog('select_group')">Cancel</md-button>
                    <md-button v-if="groupAssignmentSelectedGroup !== ''"  class="md-primary" @click="assignItemsToGroup(groupAssignmentSelectedGroup)">Add</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="select_sku" v-if="selectedItem !== null">
            <md-dialog-title>Select existing SKU</md-dialog-title>
            <md-dialog-content>
                <md-select v-model="selectedItem.plu">
                    <md-option v-for="item in stockControl" :value="item.plu" :key="item.plu">{{item.plu}}</md-option>
                </md-select>
                <md-dialog-actions>
                    <md-button  class="md-primary" @click="closeDialog('select_sku')">OK</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="select_tax">
            <md-dialog-title>Select Tax Rate</md-dialog-title>
            <md-dialog-content>
                <md-select v-model="selectedBulkEditTax">
                    <md-option v-for="tax in taxes" :value="tax.Id">{{tax.Name}}</md-option>
                </md-select>
                <md-dialog-actions>
                    <md-button  class="md-primary" @click="closeDialog('select_tax')">Cancel</md-button>
                    <md-button  class="md-primary" @click="assignTaxToItems()">Apply</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>
    </div>
</template>

<script>
    let epicuri = require('../internal/epicuri.js');

    export default {
        name:"menu_structure_management",
        data: function () {
            return {
                menus: [],
                menuIdToItemArrayMap:{},
                menuIdToGroup:{},
                printers: [],
                taxes: [],
                menuItems: [],
                allPreferences:[],
                allergies:[],
                diets:[],
                modifierGroups:[],
                itemTypes:[{id:0, name:'Food'},{id:1, name:'Drink'},{id:2, name:'Other'}],
                stockControl:[],

                //for table
                printerIdToPrinter:{},
                taxIdToTax:{},

                //filters
                filter_tax:[],
                filter_menus: [],
                filter_printers: [],
                filter_modifiers: [],
                filter_text: "",
                items_selected:false,
                filter_orphanedItemsOnly: false,
                filter_includeItemsWithNoMod: true,

                //for editing menu items
                showEditMenuItem: false,
                selectedItem: null,
                selectedItemPrinter:"Printer",
                selectedAllergies:[],
                selectedDiets:[],
                selectedModifierGroups:[],
                bulkPrice:0,

                selectedBulkEditPrinter:"",
                selectedBulkEditTax: "",

                //dialogs
                deleteItem: {
                    title: 'Delete Item(s)?',
                    contentHtml: 'Item deletion is <strong>permanent</strong>! <br><br>If you would like to remove an item from a menu, it can be unassigned rather than deleted. <br>Are you sure you want to delete selected item(s)?',
                    ok: 'Delete',
                    cancel: 'Cancel',
                    singleItemToDelete: null,
                    multipleItemsToDelete:[]
                },
                absoluteOrPercentage: false,

                saveItem: {
                    title: 'Oops! We need some more details',
                    contentHtml: "Items required"
                },
                saveItemBaseText: 'The following items are required: <br><br>',

                //for assigning to group
                menuIdToCategoriesArrayMap:{},
                categoryIdToGroupsArrayMap:{},
                groupAssignmentSelectedMenu:"",
                groupAssignmentSelectedCategory:"",
                groupAssignmentSelectedGroup:""
            }
        },
        computed: {
            selectedItemsLength: function () {
                return this.getSelectedItems().length;
            },
            selectedItemName: function () {
                return this.selectedItem === null ? "(Item not selected)" : this.selectedItem.Name;
            },
            selectedItemInGroups: function () {
                return this.selectedItem === null ? [] : this.menuIdToGroup[this.selectedItem.Id];
            }
        },
        components: {
        },
        mounted: function () {

        },
        created: function() {
            this.loadMenus();
            this.loadPrinters();
            this.loadTaxes();
            this.loadPreferences();
            this.loadMenuItems();
            this.loadModifierGroups();
            this.loadStockControl();
        },
        watch: {},
        methods: {
            loadMenus: function() {
                epicuri.getMenus(this.$http,
                    (response) => {
                        this.menus = response.body;
                        this.menuIdToItemArrayMap = {};
                        this.filter_menus = [];

                        this.repopulateMenuFilter();
                        //update items into map
                        for(let j = 0; j < this.menus.length; j++) {
                            let menu = this.menus[j];
                            let categories = menu.MenuCategories;
                            this.menuIdToCategoriesArrayMap[menu.Id] = categories;
                            for(let k = 0; k < categories.length; k++) {
                                let category = categories[k];
                                let groups = category.MenuGroups;
                                this.categoryIdToGroupsArrayMap[category.Id] = groups;
                                if (typeof groups !== 'undefined') {
                                    for(let l = 0; l < groups.length; l++) {
                                        let group = groups[l];
                                        if(!(menu.Id in this.menuIdToItemArrayMap)) {
                                            this.menuIdToItemArrayMap[menu.Id] = []; //initialise an array
                                        }
                                        this.menuIdToItemArrayMap[menu.Id] = this.menuIdToItemArrayMap[menu.Id].concat(group.MenuItemIds);

                                        for(let m = 0; m < group.MenuItemIds.length; m++) {
                                            let itemId = group.MenuItemIds[m];
                                            if (!(itemId in this.menuIdToGroup)) {
                                                this.menuIdToGroup[itemId] = [];
                                            }
                                            let value = menu.MenuName + " > " + category.CategoryName + " > " + group.GroupName;
                                            if (!(this.arrayContains(value, this.menuIdToGroup[itemId]))) {
                                                this.menuIdToGroup[itemId].push(value);
                                            }
                                        }

                                    }
                                }
                            }
                        }

                        if(this.menus.length > 0) {
                            this.groupAssignmentSelectedMenu = this.menus[0].Id;
                        }

                    },
                    (response) => {
                        console.log("ERROR" + response)
                    });
            },
            loadPrinters: function() {
                epicuri.getPrinters(this.$http,
                    (response) => {
                        this.printers = response.body;
                        this.repopulatePrinterFilter();
                        if(this.printers.length > 0) {
                            this.selectedBulkEditPrinter = this.printers[0].Id;
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    });
            },
            loadTaxes: function() {
                epicuri.getTaxes(this.$http,
                    (response) => {
                        this.taxes = response.body;
                        this.repopulateTaxFilter();
                        if(this.taxes.length > 0) {
                            this.selectedBulkEditTax = this.taxes[0].Id
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    });
            },
            loadMenuItems: function() {
                let itemsCurrentlySelected = {};
                for(let i = 0; i < this.menuItems.length; i++) {
                    if(this.menuItems[i].selected) {
                        itemsCurrentlySelected[this.menuItems[i].Id] = true;
                    } else {
                        itemsCurrentlySelected[this.menuItems[i].Id] = false;
                    }
                }

                epicuri.getMenuItems(this.$http,
                    (response) => {
                        this.menuItems = response.body;
                        this.menuItems.sort(function(a, b){
                            if(a.Name < b.Name) return -1;
                            if(a.Name > b.Name) return 1;
                            return 0;
                        });

                        //reset selected items
                        for(let i = 0; i < this.menuItems.length; i++) {
                            this.menuItems[i].selected = itemsCurrentlySelected[this.menuItems[i].Id];
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    });
            },
            loadPreferences: function() {
                epicuri.getPreferences(this.$http,
                    (response) => {
                        this.allPreferences = response.body;
                        this.allergies = this.allPreferences['Allergies'];
                        this.diets = this.allPreferences['DietaryRequirements'];
                    },
                    (response) => {
                        console.log("ERROR " + response)
                    });
            },
            loadModifierGroups: function() {
                epicuri.getModifierGroups(this.$http,
                    (response) => {
                        this.modifierGroups = response.body;
                        //this.repopulateModifiersFilter();
                    },
                    (response) => {
                        console.log("ERROR " + response)
                    });
            },
            loadStockControl: function() {
                epicuri.getStockControl(this.$http,
                    (response) => {
                        this.stockControl = response.body;
                    },
                    (response) => {
                        console.log("ERROR " + response);
                    });
            },
            getFilteredItems: function() {
                let filter_menuItems = [];
                for(let i =0; i < this.menuItems.length; i++) {
                    let menuItem = this.menuItems[i];
                    if(this.matchesFilters(menuItem)) {
                        filter_menuItems.push(menuItem);
                    }
                }
                return filter_menuItems;
            },
            matchesFilters: function(menuItem) {
                return this.matchesText(menuItem)
                        && this.matchesMenus(menuItem)
                        && this.matchesPrinters(menuItem)
                        && this.matchesModifiers(menuItem)
                        && this.matchesTaxes(menuItem);
            },
            matchesText: function(menuItem){
                if(this.filter_text == null || this.filter_text === '' || (this.filter_text.trim() === '')) {
                    return true;
                }
                let regexp = new RegExp(this.filter_text, "i");
                return regexp.test(menuItem.Name) || regexp.test(menuItem.Description);
            },
            matchesPrinters: function(menuItem){
                for(let i = 0; i < this.filter_printers.length; i++) {
                    if(this.filter_printers[i] === menuItem.DefaultPrinter) {
                        return true;
                    }
                }

                return false;
            },
            matchesModifiers: function(menuItem){
                if(this.filter_includeItemsWithNoMod) {
                    return true;
                }
                for(let i = 0; i < this.filter_modifiers.length; i++) {
                    if(this.arrayContains(this.filter_modifiers[i], menuItem.ModifierGroups)) {
                        return true;
                    }
                }

                return false;
            },
            matchesTaxes: function(menuItem){
                for(let i = 0; i < this.filter_tax.length; i++) {
                    if(this.filter_tax[i] === menuItem.TaxTypeId) {
                        return true;
                    }
                }

                return false;
            },
            matchesMenus: function(menuItem){
                if(this.filter_orphanedItemsOnly) {
                    if(this.isItemOrphaned(menuItem)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    for (let i = 0; i < this.filter_menus.length; i++) {
                        if(typeof(this.menuIdToItemArrayMap[this.filter_menus[i]]) === "undefined") {
                            return true;
                        }
                        if (this.arrayContains(menuItem.Id, this.menuIdToItemArrayMap[this.filter_menus[i]])) {
                            return true;
                        }
                    }
                    return false;
                }
            },
            isItemOrphaned: function(menuItem) {
                for(let i = 0; i < this.filter_menus.length; i++) {
                    if(typeof(this.menuIdToItemArrayMap[this.filter_menus[i]]) === "undefined") {
                        continue;
                    }
                    if(this.arrayContains(menuItem.Id,this.menuIdToItemArrayMap[this.filter_menus[i]])) {
                        return false;
                    }
                }
                return true;
            },
            onMenuItemClickEdit: function(item) {
                this.showEditMenuItem = true;
                this.selectedItem = item;

                if(this.selectedItem.DefaultPrinter != null) {
                    this.selectedItemPrinter = this.printerIdToPrinter[this.selectedItem.DefaultPrinter].Name;
                }

                for(let i = 0; i < this.allergies.length; i++) {
                    let allergy = this.allergies[i];
                    if(this.arrayContains(allergy.Key,this.selectedItem.allergyIds)) {
                        this.selectedAllergies.push(allergy);
                    }
                }

                for(let i = 0; i < this.diets.length; i++) {
                    let diet = this.diets[i];
                    if(this.arrayContains(diet.Key,this.selectedItem.dietaryIds)) {
                        this.selectedDiets.push(diet);
                    }
                }

                for(let i = 0; i < this.modifierGroups.length; i++) {
                    let modifierGroup = this.modifierGroups[i];
                    if(this.arrayContains(modifierGroup.Id,this.selectedItem.ModifierGroups)) {
                        this.selectedModifierGroups.push(modifierGroup);
                    }
                }
            },
            arrayContains: function(element, array) {
                return (array.indexOf(element) > -1);
            },
            onMenuItemClickDelete: function(item) {
                this.deleteItem.singleItemToDelete = item;
                this.openDeleteItemDialog();
            },
            onItemClickInfo: function(item) {
                this.selectedItem = item;
                this.openDialog('item_info');
            },
            singleItemDelete: function(item) {
                epicuri.deleteMenuItem(this.$http, item.Id,
                    (response) => {
                        this.$refs.delete_ok_snackbar.open();
                        this.loadMenuItems();
                        this.onMenuItemCancel(); //resets stuff
                    },
                    (response) => {
                        this.$refs.error_snackbar.open();
                    });
            },
            multipleItemDelete: function(items) {
                epicuri.deleteMenuItems(this.$http, items,
                    (response) => {
                        this.$refs.delete_ok_snackbar.open();
                        this.loadMenuItems();
                        this.onMenuItemCancel(); //resets stuff
                    },
                    (response) => {
                        this.$refs.error_snackbar.open();
                    });
            },
            onMenuItemSave: function() {
                //check printer, item type, name
                let errors = '';
                if(this.selectedItem.Name === null || this.selectedItem.Name === "") {
                    errors += "<li>Name</li>";
                }
                if(this.selectedItem.DefaultPrinter === null || this.selectedItem.DefaultPrinter === "") {
                    errors += "<li>Printer</li>";
                }

                if(this.selectedItem.MenuItemTypeId !== 0 && this.selectedItem.MenuItemTypeId !== 1 && this.selectedItem.MenuItemTypeId !== 2) {
                    errors += "<li>Item Type</li>";
                }
                if(errors !== '') {
                    this.saveItem.contentHtml = this.saveItemBaseText + errors;
                    this.$refs['saveItemDialog'].open();

                    return;
                }

                //update the selected allergies, diets and modifiers
                this.updateOptions(this.selectedAllergies, 'allergyIds');
                this.updateOptions(this.selectedDiets, 'dietaryIds');
                this.updateOptions(this.selectedModifierGroups, 'ModifierGroups', 'Id');

                if(this.selectedItem.Id) {
                    epicuri.putMenuItem(this.$http, this.selectedItem,
                        (response) => {
                            this.$refs.ok_snackbar.open();
                            this.loadMenuItems();
                            this.onMenuItemCancel(); //resets stuff
                        },
                        (response) => {
                            this.$refs.error_snackbar.open();
                        });
                } else {
                    epicuri.postMenuItem(this.$http, this.selectedItem,
                        (response) => {
                            this.$refs.ok_snackbar.open();
                            this.loadMenuItems();
                            this.onMenuItemCancel(); //resets stuff
                        },
                        (response) => {
                            this.$refs.error_snackbar.open();
                        });
                }
            },
            updateOptions: function(array, key, objectKey='Key') {
                this.selectedItem[key] = [];
                for(let i = 0; i < array.length; i++) {
                    this.selectedItem[key].push((array[i])[objectKey]);
                }
            },
            onMenuItemCancel: function() {
                this.showEditMenuItem = false;
                this.selectedItem = null;

                this.selectedAllergies = [];
                this.selectedDiets = [];
                this.selectedModifierGroups = [];
                this.selectedItemPrinter = "";

                for(let i = 0; i < this.allergies.length; i++) {
                    this.allergies.selected = false;
                }

                for(let i = 0; i < this.diets.length; i++) {
                    this.diets.selected = false;
                }
            },
            addMenuItem: function() {
                let defaultPrinter = this.printers.length > 0 ? this.printers[0].Id : null;
                let defaultTax = this.taxes.length > 0 ? this.taxes[0].Id : null;
                this.selectedItem = {Name:'', Price:0, DefaultPrinter:defaultPrinter, MenuItemTypeId:0, TaxTypeId:defaultTax};
                this.showEditMenuItem = true;
            },
            getSelectedItems: function() {
                let array = [];
                for(let i = 0; i < this.menuItems.length; i++) {
                    if(this.menuItems[i].selected) {
                        array.push(this.menuItems[i]);
                    }
                }

                return array;
            },
            assignItemsToGroup: function(group) {
                let array = this.getSelectedItems();
                if(array.length <= 1) {
                    this.$refs.no_items_selected_snackbar.open();
                    this.closeDialog('select_group');
                    return;
                }

                for(let i = 0; i < array.length; i++) {
                    if(!this.arrayContains(array[i].Id, group.MenuItemIds)) {
                        group.MenuItemIds.push(array[i].Id);
                    }
                }

                epicuri.putGroup(this.$http, group, (response) => {
                        this.$refs.ok_snackbar.open();
                        this.loadMenuItems();
                        this.onMenuItemCancel(); //resets stuff
                    },
                    (response) => {
                        this.$refs.error_snackbar.open();
                    });

                this.closeDialog('select_group');
            },
            assignModifiersToItems: function() {
                let array = this.getSelectedItems();
                if(array.length <= 1) {
                    this.$refs.no_items_selected_snackbar.open();
                    return;
                }

                let modifierGroupIds = [];
                for(let i = 0; i < this.modifierGroups.length; i++) {
                    if(this.modifierGroups[i].selected) {
                        modifierGroupIds.push(this.modifierGroups[i].Id);
                    }
                }

                for(let i = 0; i < array.length; i++) {
                    for(let j = 0; j < modifierGroupIds.length; j++) {
                        if(!this.arrayContains(modifierGroupIds[j], array[i].ModifierGroups)) {
                            array[i].ModifierGroups.push(modifierGroupIds[j]);
                        }
                    }
                }

                epicuri.putMenuItems(this.$http, array,
                    (response) => {
                        this.$refs.ok_snackbar.open();
                        this.loadMenuItems();
                        this.onMenuItemCancel(); //resets stuff

                        for(let i = 0; i < this.modifierGroups.length; i++) {
                            if(this.modifierGroups[i].selected) {
                                this.modifierGroups[i].selected = false;
                            }
                        }
                    },
                    (response) => {
                        this.$refs.error_snackbar.open();
                    });

                this.closeDialog('select_modifier_groups');
            },
            assignAllergiesToItems: function() {
                let array = this.getSelectedItems();
                if(array.length <= 1) {
                    this.$refs.no_items_selected_snackbar.open();
                    this.closeDialog('select_allergens');
                    return;
                }

                for(let i = 0; i < array.length; i++) {
                    for(let j = 0; j < this.allergies.length; j++) {
                        if((!this.arrayContains(this.allergies[j].Key,array[i].allergyIds))
                            && this.allergies[j].selected) {
                            array[i].allergyIds.push(this.allergies[j].Key);
                        }
                    }
                }

                epicuri.putMenuItems(this.$http, array,
                    (response) => {
                        this.$refs.ok_snackbar.open();
                        this.loadMenuItems();
                        this.onMenuItemCancel(); //resets stuff

                        this.closeDialog('select_allergens');
                    },
                    (response) => {
                        this.$refs.error_snackbar.open();
                        this.closeDialog('select_allergens');
                    });
            },
            assignDietsToItems: function() {
                let array = this.getSelectedItems();
                if(array.length <= 1) {
                    this.$refs.no_items_selected_snackbar.open();
                    this.closeDialog('select_diets');
                    return;
                }

                for(let i = 0; i < array.length; i++) {
                    for(let j = 0; j < this.diets.length; j++) {
                        if((!this.arrayContains(this.diets[j].Key,array[i].dietaryIds))
                            && this.diets[j].selected) {
                            array[i].dietaryIds.push(this.diets[j].Key);
                        }
                    }
                }

                epicuri.putMenuItems(this.$http, array,
                    (response) => {
                        this.$refs.ok_snackbar.open();
                        this.loadMenuItems();
                        this.onMenuItemCancel(); //resets stuff

                        this.closeDialog('select_diets');
                    },
                    (response) => {
                        this.$refs.error_snackbar.open();
                        this.closeDialog('select_diets');
                    });
            },
            markUnavailability: function(bool) {
                let array = this.getSelectedItems();
                for(let i = 0; i < array.length; i++) {
                    array[i].Unavailable = bool;
                }
                epicuri.putMenuItems(this.$http, array,
                    (response) => {
                        this.$refs.ok_snackbar.open();
                        this.loadMenuItems();
                        this.onMenuItemCancel(); //resets stuff
                    },
                    (response) => {
                        this.$refs.error_snackbar.open();
                    });
            },
            assignPrinterToItems: function() {
                let array = this.getSelectedItems();
                for(let i = 0; i < array.length; i++) {
                    array[i].DefaultPrinter = this.selectedBulkEditPrinter;
                }
                this.putItems(array);
                this.closeDialog('select_printer');
                if(this.printers.length > 0) {
                    this.selectedBulkEditPrinter = this.printers[0].Id;
                }
            },
            putItems: function(array){
                epicuri.putMenuItems(this.$http, array,
                    (response) => {
                        this.$refs.ok_snackbar.open();
                        this.loadMenuItems();
                        this.onMenuItemCancel(); //resets stuff
                    },
                    (response) => {
                        this.$refs.error_snackbar.open();
                    });
            },
            assignTaxToItems: function() {
                let array = this.getSelectedItems();
                for(let i = 0; i < array.length; i++) {
                    array[i].TaxTypeId = this.selectedBulkEditTax;
                }
                this.putItems(array);
                this.closeDialog('select_tax');
                if(this.taxes.length > 0) {
                    this.selectedBulkEditTax = this.taxes[0].Id;
                }
            },
            assignPriceToItems: function() {
                let array = this.getSelectedItems();
                for(let i = 0; i < array.length; i++) {
                    array[i].Price = this.bulkPrice;
                }
                this.updatePricesAndCloseDialog(array, 'select_absolute_price');
            },
            assignPriceToItemsScaled: function() {
                let array = this.getSelectedItems();
                for(let i = 0; i < array.length; i++) {
                    let p1 = Number(array[i].Price);
                    let p2 = Number(this.bulkPrice);
                    if(this.absoluteOrPercentage) {
                        array[i].Price = (p1 + ((p2 / 100) * p1)).toFixed(2);
                    } else {
                        array[i].Price = (p1 + p2).toFixed(2);
                    }
                }
                this.updatePricesAndCloseDialog(array, 'select_scaled_price');
            },
            updatePricesAndCloseDialog: function(array, dialogName) {
                epicuri.putMenuItems(this.$http, array,
                    (response) => {
                        this.$refs.ok_snackbar.open();
                        this.loadMenuItems();
                        this.onMenuItemCancel(); //resets stuff
                    },
                    (response) => {
                        this.$refs.error_snackbar.open();
                    });

                this.closeDialog(dialogName);
            },
            bulkDelete: function() {
                let idArray = [];
                for(let i = 0; i < this.menuItems.length; i++) {
                    if(this.menuItems[i].selected) {
                        idArray.push(this.menuItems[i].Id);
                    }
                }

                if(idArray.length > 0) {
                    this.deleteItem.multipleItemsToDelete = idArray;
                    this.openDeleteItemDialog();
                }
            },
            selectAll: function() {
                let filter_menuItems = this.getFilteredItems();
                for(let i = 0; i < filter_menuItems.length; i++) {
                    filter_menuItems[i].selected = !this.items_selected;
                }
                this.items_selected = !this.items_selected;
            },
            onSort: function(sort) {
                this.menuItems = _.orderBy(this.menuItems, [item => item[sort.name]], sort.type);
            },
            openDeleteItemDialog: function() {
                this.openDialog('deleteItemDialog');
            },
            openDialog: function(ref) {
                this.$refs[ref].open();
            },
            closeDialog:function(ref) {
                this.$refs[ref].close();
            },
            onOpenDeleteItemDialog: function() {

            },
            onCloseDeleteItemDialog: function(type) {
                if(type === 'ok') {
                    if(this.deleteItem.multipleItemsToDelete.length > 0) {
                        this.multipleItemDelete(this.deleteItem.singleItemToDelete);
                    } else {
                        this.singleItemDelete(this.deleteItem.singleItemToDelete);
                    }
                }
                this.deleteItem.singleItemToDelete = null;
                this.deleteItem.multipleItemsToDelete = [];
            },
            clearFilters: function () {
                this.clearArray(this.filter_tax);
                this.repopulateTaxFilter();
                this.clearArray(this.filter_menus);
                this.repopulateMenuFilter();
                this.clearArray(this.filter_printers);
                this.repopulatePrinterFilter();
                this.clearArray(this.filter_modifiers);
                //this.repopulateModifiersFilter();
                this.filter_text = "";
                this.items_selected = false;
                this.filter_orphanedItemsOnly = false;
                for(let i = 0 ; i < this.menuItems.length; i++) {
                    this.menuItems[i].selected = false;
                }
            },
            clearArray: function(array) {
                array.splice(0, array.length);
            },
            repopulateTaxFilter: function() {
                for(let i = 0; i < this.taxes.length; i++) {
                    this.filter_tax.push(this.taxes[i].Id);
                    this.taxIdToTax[this.taxes[i].Id] = this.taxes[i];
                }
            },
            repopulateMenuFilter: function() {
                for(let i = 0; i < this.menus.length; i++) {
                    this.filter_menus.push(this.menus[i].Id);
                }
            },
            repopulatePrinterFilter: function() {
                for(let i = 0; i < this.printers.length; i++) {
                    this.filter_printers.push(this.printers[i].Id);
                    this.printerIdToPrinter[this.printers[i].Id] = this.printers[i];
                }
            },
            repopulateModifiersFilter: function() {
                for(let i = 0; i < this.modifierGroups.length; i++) {
                    this.filter_modifiers.push(this.modifierGroups[i].Id);
                }
            }
        },
        onLoad: function (type) {

        }
    }
</script>

<style>
    .menuitemunavailable {
        background-color: lightgray;
    }
    .menuitemorphaned {
        background-color: lightsalmon;
    }
</style>
