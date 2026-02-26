<template>
    <div>
        <h1 style="text-align:center"><span><i>[BETA]</i></span></h1>
        <md-subheader>
            Menus are composed of three levels: Menus contain categories; categories contain groups; groups contain individual menu items.
            Menu items can of course be assigned to more than one group.
            You set the course on the category level.
        </md-subheader>
        <div v-if="showMenus">
            <h1>Menus</h1>
            <md-button  class="md-icon-button md-raised md-primary" @click="triggerOpenMenuDialog(createMenu(),'edit_menu')">
                <md-icon>add</md-icon>
                <md-tooltip md-direction="top">Add a new menu</md-tooltip>
            </md-button>
            <md-table>
                <md-table-header>
                    <md-table-row>
                        <md-table-head></md-table-head>
                        <md-table-head>Name</md-table-head>
                        <md-table-head>Status</md-table-head>
                        <md-table-head>Categories</md-table-head>
                        <md-table-head></md-table-head>
                    </md-table-row>
                </md-table-header>
                <md-table-body>
                    <md-table-row v-for="menu in menus" v-bind:data="menu" v-bind:key="menu.Id">
                        <md-table-cell>
                            <div>
                                <md-button class="md-icon-button md-primary" @click="triggerOpenMenuDialog(menu, 'edit_menu')">
                                    <md-icon>edit</md-icon>
                                    <md-tooltip>Edit</md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button md-primary" @click="triggerOpenMenuDialog(menu, 'clone_menu')">
                                    <md-icon>file_copy</md-icon>
                                    <md-tooltip>Copy (including contents)</md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button md-warn" @click="triggerOpenMenuDialog(menu, 'delete_menu')">
                                    <md-icon>delete</md-icon>
                                    <md-tooltip>Delete menu permanently</md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button md-warn" @click="triggerDownload(menu)">
                                    <md-icon>archive</md-icon>
                                    <md-tooltip>Download to CSV format</md-tooltip>
                                </md-button>
                            </div>
                        </md-table-cell>
                        <md-table-cell>{{menu.MenuName}}</md-table-cell>
                        <md-table-cell>
                            <div v-if="menu.Active">
                            <span>
                                <md-icon class="md-size-1 md-primary">check</md-icon>
                                Active
                            </span>
                            </div>
                            <div v-else>
                            <span>
                                <md-icon class="md-size-1 md-primary">close</md-icon>
                                Inactive
                            </span>
                            </div>
                        </md-table-cell>
                        <md-table-cell>{{menu.MenuCategories.length}}</md-table-cell>
                        <md-table-cell>
                            <md-button class="md-icon-button md-list-action md-size-2x" @click="onMenuClick(menu)" >
                                <md-icon>keyboard_arrow_right</md-icon>
                                <md-tooltip>Go to categories within {{menu.MenuName}}</md-tooltip>
                            </md-button>
                        </md-table-cell>
                    </md-table-row>
                </md-table-body>
            </md-table>
        </div>
        <div v-if="showCategories">
            <h1>{{selectedMenu.MenuName}} > <i>Categories</i></h1>
            <md-button class="md-icon-button md-raised" @click="categoryViewBack">
                <md-icon>keyboard_return</md-icon>
                <md-tooltip>Back</md-tooltip>
            </md-button>
            <md-button class="md-icon-button md-raised md-primary" @click="triggerOpenCategoryDialog(createCategory(),'edit_category')">
                <md-icon>add</md-icon>
                <md-tooltip>Add a new category</md-tooltip>
            </md-button>

            <md-table>
                <md-table-header>
                    <md-table-row>
                        <md-table-head></md-table-head>
                        <md-table-head>Name</md-table-head>
                        <md-table-head>Course</md-table-head>
                        <md-table-head>Groups</md-table-head>
                        <md-table-head></md-table-head>
                    </md-table-row>
                </md-table-header>

                <md-table-body>
                    <md-table-row v-for="category in selectedMenuCategories" v-bind:data="category" v-bind:key="category.Id">
                        <md-table-cell>
                            <div>
                                <md-button class="md-icon-button md-primary" @click="triggerOpenCategoryDialog(category, 'edit_category')">
                                    <md-icon>edit</md-icon>
                                    <md-tooltip>Edit</md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button md-primary" @click="triggerOpenCategoryDialog(category, 'clone_category')">
                                    <md-icon>file_copy</md-icon>
                                    <md-tooltip>Copy category including contents</md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button md-primary md-warn" @click="triggerOpenCategoryDialog(category, 'delete_category')">
                                    <md-icon>delete</md-icon>
                                    <md-tooltip>Delete category permanently (this does not delete the underlying menu items)</md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button md-list-action md-size-2x" @click="onCategoryUp(selectedMenu, category)" >
                                    <md-icon>keyboard_arrow_up</md-icon>
                                    <md-tooltip>Move category up the list within this menu</md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button md-list-action md-size-2x" @click="onCategoryDown(selectedMenu, category)" >
                                    <md-icon>keyboard_arrow_down</md-icon>
                                    <md-tooltip>Move ordering up the list within this menu</md-tooltip>
                                </md-button>
                            </div>
                        </md-table-cell>
                        <md-table-cell>{{category.CategoryName}}</md-table-cell>
                        <md-table-cell>{{category.DefaultCourses.length === 0 ? '(Course not set)' : category.DefaultCourses[0].Name}}</md-table-cell>
                        <md-table-cell>{{category.MenuGroups.length}}</md-table-cell>
                        <md-table-cell>
                            <md-button class="md-icon-button md-list-action md-size-2x" @click="onCategoryClick(category)" >
                                <md-icon>keyboard_arrow_right</md-icon>
                                <md-tooltip>Go to groups within {{category.CategoryName}}</md-tooltip>
                            </md-button>
                        </md-table-cell>
                    </md-table-row>
                </md-table-body>
            </md-table>
        </div>

        <div v-if="showGroups">
            <h1>{{selectedMenu.MenuName}} > {{selectedCategory.CategoryName}} > <i>Groups</i></h1>
            <md-button class="md-icon-button md-raised" @click="groupViewBack">
                <md-icon>keyboard_return</md-icon>
                <md-tooltip>Back</md-tooltip>
            </md-button>
            <md-button  class="md-icon-button md-raised md-primary" @click="triggerOpenGroupDialog(createGroup(),'edit_group')">
                <md-icon>add</md-icon>
                <md-tooltip>Add a group</md-tooltip>
            </md-button>

            <md-table>
                <md-table-header>
                    <md-table-row>
                        <md-table-head></md-table-head>
                        <md-table-head>Name</md-table-head>
                        <md-table-head>Items</md-table-head>
                        <md-table-head></md-table-head>
                    </md-table-row>
                </md-table-header>
                <md-table-body>
                    <md-table-row v-for="group in selectedCategoryGroups" v-bind:data="group" v-bind:key="group.Id">
                        <div>
                            <md-table-cell>
                                <md-button class="md-icon-button md-primary" @click="triggerOpenGroupDialog(group, 'edit_group')">
                                    <md-icon>edit</md-icon>
                                    <md-tooltip>Edit</md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button md-primary" @click="triggerOpenGroupDialog(group, 'clone_group')">
                                    <md-icon>file_copy</md-icon>
                                    <md-tooltip>Copy to another menu & category</md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button md-primary md-warn" @click="triggerOpenGroupDialog(group, 'delete_group')">
                                    <md-icon>delete</md-icon>
                                    <md-tooltip>Delete group permanently (this does not delete the underlying menu items)</md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button md-list-action md-size-2x" @click="onGroupUp(selectedMenu, selectedCategory, group)" >
                                    <md-icon>keyboard_arrow_up</md-icon>
                                    <md-tooltip>Move group ordering up within this category</md-tooltip>
                                </md-button>
                                <md-button class="md-icon-button md-list-action md-size-2x" @click="onGroupDown(selectedMenu, selectedCategory, group)" >
                                    <md-icon>keyboard_arrow_down</md-icon>
                                    <md-tooltip>Move group ordering up within this category</md-tooltip>
                                </md-button>
                            </md-table-cell>
                        </div>
                        <md-table-cell>{{group.GroupName}}</md-table-cell>
                        <md-table-cell>{{group.MenuItemIds.length}}</md-table-cell>
                        <md-table-cell>
                            <md-button class="md-icon-button md-list-action md-size-2x" @click="onGroupClick(group)" >
                                <md-icon>keyboard_arrow_right</md-icon>
                                <md-tooltip>Go to menu items within {{group.GroupName}}</md-tooltip>
                            </md-button>
                        </md-table-cell>
                    </md-table-row>
                </md-table-body>
            </md-table>
        </div>

        <div v-if="showItems">
            <h1>{{selectedMenu.MenuName}} > {{selectedCategory.CategoryName}} > {{selectedGroup.GroupName}} > <i>Items</i></h1>
            <md-button class="md-icon-button md-raised" @click="itemsViewBack">
                <md-icon>keyboard_return</md-icon>
                <md-tooltip>Back</md-tooltip>
            </md-button>

            <md-button class="md-icon-button md-primary" @click="removeFromGroup">
                <md-icon>clear</md-icon>
                <md-tooltip>Remove selected items from group {{selectedGroup.GroupName}}</md-tooltip>
            </md-button>
            <md-button class="md-icon-button md-primary" @click="alphabetiseGroup">
                <md-icon>text_rotate_vertical</md-icon>
                <md-tooltip>Sort all items alphabetically by name</md-tooltip>
            </md-button>
            <!--<md-button class="md-icon-button md-primary" @click="copyToGroup">
                <md-icon>file_copy</md-icon>
                <md-tooltip>Copy to a different group</md-tooltip>
            </md-button>-->

            <md-table>
                <md-table-header>
                    <md-table-row>
                        <md-table-head></md-table-head>
                        <md-table-head md-sort-by="Name">Name</md-table-head>
                        <md-table-head md-sort-by="Description">Description</md-table-head>
                    </md-table-row>
                </md-table-header>
                <md-table-body>
                    <md-table-row v-for="menuItem in selectedGroup.MenuItems" v-bind:data="menuItem" v-bind:key="menuItem.Id" v-bind:class="{menuitemunavailable : menuItem.Unavailable}">
                        <md-table-cell>
                            <md-checkbox v-model="menuItem.selected"></md-checkbox>
                            <md-button class="md-icon-button md-list-action md-size-2x" @click="triggerRemoveItemFromGroupDialog(menuItem, 'edit_group')">
                                <md-icon>clear</md-icon>
                                <md-tooltip>Remove from group {{selectedGroup.GroupName}}</md-tooltip>
                            </md-button>
                            <md-button class="md-icon-button md-list-action md-size-2x" @click="onItemUp(selectedMenu, selectedCategory, selectedGroup, menuItem)" >
                                <md-icon>keyboard_arrow_up</md-icon>
                                <md-tooltip>Move item ordering up within this group</md-tooltip>
                            </md-button>
                            <md-button class="md-icon-button md-list-action md-size-2x" @click="onItemDown(selectedMenu, selectedCategory, selectedGroup, menuItem)" >
                                <md-icon>keyboard_arrow_down</md-icon>
                                <md-tooltip>Move item ordering up within this group</md-tooltip>
                            </md-button>
                        </md-table-cell>
                        <md-table-cell>{{menuItem.Name}}</md-table-cell>
                        <md-table-cell>{{menuItem.Description}}</md-table-cell>
                    </md-table-row>
                </md-table-body>
            </md-table>
        </div>

        <!--Edit dialogs-->
        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="edit_menu">
            <md-dialog-title>{{selectedMenu.Id == null ? 'Create' : 'Edit'}} {{selectedMenu.MenuName}}</md-dialog-title>
            <md-dialog-content>
                <form novalidate>
                    <md-input-container>
                        <label>Menu name</label>
                        <md-input required v-model="selectedMenu.MenuName"></md-input>
                    </md-input-container>
                    <md-checkbox v-model="selectedMenu.Active">
                        Active
                        <md-tooltip>Toggle whether this menu should be visible to waiters and diners</md-tooltip>
                    </md-checkbox>
                </form>
                <md-dialog-actions>
                    <md-button class="md-primary" @click="closeDialog('edit_menu')">Cancel</md-button>
                    <md-button class="md-primary" @click="saveMenu(selectedMenu)">Save</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="edit_category">
            <md-dialog-title>{{selectedCategory.Id == null ? 'Create' : 'Edit'}} {{selectedCategory.CategoryName}}</md-dialog-title>
            <md-dialog-content>
                <form novalidate>
                    <md-input-container>
                        <label>Category name</label>
                        <md-input required v-model="selectedCategory.CategoryName"></md-input>
                    </md-input-container>

                    <!--todo technically this should be repeated for every service-->
                    <md-input-container>
                        <label for="serviceSelector">Service</label>
                        <md-select required name="serviceSelector" id="serviceSelector" v-model="selectedServiceId">
                            <md-option v-for="service in services" :value="service.Id" :key="service.Id" @click="setServiceId(service)">{{service.ServiceName}}</md-option>
                        </md-select>
                    </md-input-container>
                    <md-input-container>
                        <label for="courseSelector">Course</label>
                        <md-select required name="courseSelector" id="courseSelector" v-model="selectedCourseId">
                            <md-option v-for="course in courses[selectedServiceId]" :value="course.Id" :key="course.Id">{{course.Name}}</md-option>
                        </md-select>
                    </md-input-container>
                </form>

                <md-dialog-actions>
                    <md-button class="md-primary" @click="closeDialog('edit_category')">Cancel</md-button>
                    <md-button class="md-primary" @click="saveCategory(selectedCategory, selectedCourseId)">Save</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>

        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="edit_group">
            <md-dialog-title>{{selectedGroup.Id == null ? 'Create' : 'Edit'}} {{selectedGroup.GroupName}}</md-dialog-title>
            <md-dialog-content>
                <form novalidate>
                    <md-input-container>
                        <label>Group name</label>
                        <md-input required v-model="selectedGroup.GroupName"></md-input>
                    </md-input-container>
                </form>

                <md-dialog-actions>
                    <md-button class="md-primary" @click="closeDialog('edit_group')">Cancel</md-button>
                    <md-button class="md-primary" @click="saveGroup(selectedGroup)">Save</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>

        <!-- Cloning dialogs -->
        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="clone_menu">
            <md-dialog-title>Make a copy of {{selectedMenu.MenuName}}?</md-dialog-title>
            <md-dialog-content>
                <form novalidate>
                    <md-subheader>This will make a copy of this menu, including categories, groups and items.</md-subheader>
                    <md-input-container>
                        <label>New menu name</label>
                        <md-input required v-model="newMenuName"></md-input>
                    </md-input-container>
                </form>
                <md-dialog-actions>
                    <md-button class="md-primary" @click="closeDialog('clone_menu')">Cancel</md-button>
                    <md-button v-bind:disabled="newMenuName===''" class="md-primary" @click="cloneMenu(selectedMenu.Id, newMenuName)">Save</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>
        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="clone_category">
            <md-dialog-title>Make a copy of {{selectedCategory.CategoryName}}?</md-dialog-title>
            <md-dialog-content>
                <form novalidate>
                    <md-subheader>This will make a copy of this category, including groups and items.<br>The new category will be placed optionally in a different menu.</md-subheader>
                    <md-input-container>
                        <label>New category</label>
                        <md-input required v-model="newCategoryName"></md-input>
                    </md-input-container>

                    <md-input-container>
                        <label for="copyToMenuSelector">Copy to menu:</label>
                        <md-select required name="copyToMenuSelector" id="copyToMenuSelector" v-model="selectedMenuIdClone">
                            <md-option v-for="menu in menus" :value="menu.Id">{{menu.MenuName}}</md-option>
                        </md-select>
                    </md-input-container>

                </form>
                <md-dialog-actions>
                    <md-button class="md-primary" @click="closeDialog('clone_category')">Cancel</md-button>
                    <md-button v-bind:disabled="newCategoryName==='' || selectedMenuIdClone ===''" class="md-primary" @click="cloneCategory(selectedCategory.Id, newCategoryName, selectedMenuIdClone)">Save</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>
        <md-dialog md-open-from="#fab" md-close-to="#fab" ref="clone_group">
            <md-dialog-title>Make a copy of {{selectedGroup.GroupName}}?</md-dialog-title>
            <md-dialog-content>
                <form novalidate>
                    <md-subheader>This will make a copy of this group, including underlying items.<br>The new group will be placed in the same group ({{selectedMenu.MenuName}}>{{selectedCategory.CategoryName}}) and can be moved to another category later.</md-subheader>
                    <md-input-container>
                        <label>New group</label>
                        <md-input required v-model="newGroupName"></md-input>
                    </md-input-container>

                    <md-input-container>
                        <label for="copyToCategorySelector">Copy to menu &gt; category:</label>
                        <md-select required name="copyToCategorySelector" id="copyToCategorySelector" v-model="selectedCategoryIdClone">
                            <md-option v-for="item in menuCategories" :value="item.CategoryId" >{{item.MenuName}} > {{item.CategoryName}}</md-option>
                        </md-select>
                    </md-input-container>
                </form>
                <md-dialog-actions>
                    <md-button class="md-primary" @click="closeDialog('clone_group')">Cancel</md-button>
                    <md-button v-bind:disabled="newGroupName===''" class="md-primary" @click="cloneGroup(selectedGroup.Id, newGroupName, selectedCategoryIdClone)">Save</md-button>
                </md-dialog-actions>
            </md-dialog-content>
        </md-dialog>

        <!--Deleting dialogs-->
        <md-dialog-confirm
                :md-title="selectedMenu.MenuName"
                md-content-html="Are you sure you want to delete this menu? <br><strong>This is not a reversible action!</strong><br><br>(This will not delete underlying menu items)"
                md-ok-text="Delete"
                md-cancel-text="Cancel"
                @close="onDeleteMenuCloseDialog"
                ref="delete_menu">
        </md-dialog-confirm>
        <md-dialog-confirm
                :md-title="selectedCategory.CategoryName"
                md-content-html="Are you sure you want to delete this category?<br><br>(This will not delete underlying menu items)"
                md-ok-text="Delete"
                md-cancel-text="Cancel"
                @close="onDeleteCategoryCloseDialog"
                ref="delete_category">
        </md-dialog-confirm>
        <md-dialog-confirm
                :md-title="selectedGroup.GroupName"
                md-content-html="Are you sure you want to delete this group?<br><br>(This will not delete underlying menu items)"
                md-ok-text="Delete"
                md-cancel-text="Cancel"
                @close="onDeleteGroupCloseDialog"
                ref="delete_group">
        </md-dialog-confirm>

    </div>
</template>

<script>
    let epicuri = require('../internal/epicuri.js');

    export default {

        data: function () {
            return {
                menus:[],
                menuItems:[],
                services:[],
                serviceIds:[],
                courses:{},
                taxes:{},
                printers:{},

                selectedMenu:{},
                selectedMenuCategories:[],
                selectedCategory:{},
                selectedCategoryGroups:[],
                selectedGroup:{},
                selectedCopyGroup:{},
                selectedServiceId:"",
                selectedCourseId:"",
                selectedMenuCategory:"",

                selectedCategoryIdClone:"",
                selectedMenuIdClone:"",

                menuCategoryGroups:[],
                menuCategories:[],

                newMenuName:'',
                newCategoryName:'',
                newGroupName:'',

                showMenus: true,
                showCategories: false,
                showGroups:false,
                showItems:false
            }
        },
        methods: {
            loadMenus: function(callback) {
                epicuri.getMenus(this.$http,
                    (response) => {
                        this.menus.splice(0, this.menus.length);
                        for(let i = 0; i < response.body.length; i++) {
                            this.menus.push(response.body[i]);
                        }

                        //some data prep
                        for(let i = 0; i < this.menus.length; i++) {
                            for(let j = 0; j < this.menus[i].MenuCategories.length; j++) {
                                this.menus[i].MenuCategories[j].selectedCourse = this.menus[i].MenuCategories[j].DefaultCourses[0];
                            }
                        }

                        this.menuCategoryGroups.splice(0, this.menuCategoryGroups.length);
                        this.menuCategories.splice(0, this.menuCategories.length);

                        for(let i = 0; i < this.menus.length; i++) {
                            let menu = this.menus[i];
                            for (let j = 0; j < menu.MenuCategories.length; j++) {
                                let category = menu.MenuCategories[j];
                                let menuCategory = {
                                    MenuName: menu.MenuName,
                                    MenuId: menu.Id,
                                    CategoryName: category.CategoryName,
                                    CategoryId: category.Id
                                };
                                this.menuCategories.push(menuCategory);

                                if(category.Id === this.selectedCategory.Id) {
                                    this.selectedMenu = menu;
                                    this.selectedCategory = category;
                                    this.selectedMenuCategories = this.selectedMenu.MenuCategories;
                                    this.selectedCategoryGroups = this.selectedCategory.MenuGroups;
                                }

                                for (let k = 0; k < category.MenuGroups.length; k++) {
                                    let group = category.MenuGroups[k];
                                    let menuCategoryGroup = {
                                        MenuName: menu.MenuName,
                                        CategoryName: category.CategoryName,
                                        GroupName: group.GroupName,
                                        GroupId: group.Id
                                    };
                                    this.menuCategoryGroups.push(menuCategoryGroup);

                                    if(group.Id === this.selectedGroup.Id) {
                                        this.selectedGroup = category.MenuGroups[k];
                                    }
                                }
                            }
                        }

                        if(callback !== undefined) {
                            callback();
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
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
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    });
            },
            loadServices: function() {
                epicuri.getServices(this.$http,
                    (response) => {
                        this.serviceIds.splice(0,this.serviceIds.length);
                        this.services = response.body;

                        for(let i = 0; i < this.services.length; i++) {
                            this.serviceIds.push(this.services[i].Id);
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    });
            },
            loadCourses: function() {
                epicuri.getCourses(this.$http,
                    (response) => {
                        let coursesArray = response.body;
                        let servicesDone = [];
                        for(let i = 0; i < coursesArray.length; i++) {
                            if(servicesDone.includes(coursesArray[i].ServiceId)) {
                                this.courses[coursesArray[i].ServiceId].push(coursesArray[i]);
                            } else {
                                this.courses[coursesArray[i].ServiceId] = [];
                                this.courses[coursesArray[i].ServiceId].push(coursesArray[i]);
                                servicesDone.push(coursesArray[i].ServiceId);
                            }
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    });
            },
            loadTaxes: function() {
                epicuri.getTaxes(this.$http,
                    (response) => {
                        let body = response.body;
                        for(let i = 0; i < body.length; i++) {
                            this.taxes[body[i].Id] = body[i].Name;
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    })
            },
            loadPrinters: function() {
                epicuri.getPrinters(this.$http,
                    (response) => {
                        let body = response.body;
                        for(let i = 0; i < body.length; i++) {
                            this.printers[body[i].Id] = body[i].Name;
                        }
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    });
            },
            saveMenu: function(menu) {
                if(menu.Id !== null) {
                    this.updateFromCopy(menu, this.menus);
                    epicuri.putMenu(this.$http, menu,
                        (response) => {
                            this.closeDialog('edit_menu');
                            this.loadMenus();
                        },
                        (response) => {
                            console.log("ERROR" + response)
                        });
                } else {
                    epicuri.postMenu(this.$http, menu,
                        (response) => {
                            this.closeDialog('edit_menu');
                            this.loadMenus();
                        },
                        (response) => {
                            console.log("ERROR" + response)
                        });
                }
            },
            saveCategory: function(category, courseId) {
                if(category.Id !== null) {
                    this.updateFromCopy(category, this.selectedMenuCategories);
                    this.updateCourseOnCategory(category, courseId, this.selectedServiceId);
                    epicuri.putCategory(this.$http, category,
                        (response) => {
                            this.closeDialog('edit_category');
                            this.loadMenus();
                        },
                        (response) => {
                            this.closeDialog('edit_category');
                            console.log("ERROR" + response)
                        });
                } else {
                    category.DefaultCourseIds = [];
                    category.DefaultCourseIds.push(courseId);
                    epicuri.postCategory(this.$http, category,
                        (response) => {
                            this.closeDialog('edit_category');
                            this.loadMenus(() => {
                                this.selectedMenuCategories.splice(0, this.selectedMenuCategories.length);
                                for(let i = 0; i < this.menus.length; i++) {
                                    if(this.selectedMenu.Id === this.menus[i].Id) {
                                        this.selectedMenuCategories = this.menus[i].MenuCategories;
                                        break;
                                    }
                                }
                            });
                        },
                        (response) => {
                            this.closeDialog('edit_category');
                            console.log("ERROR" + response)
                        });
                }
                this.selectedService = "";
            },
            saveGroup: function(group) {
                if(group.Id !== null) {
                    this.updateFromCopy(group, this.selectedCategoryGroups);
                    epicuri.putGroup(this.$http, group,
                        (response) => {
                            this.closeDialog('edit_group');
                            this.loadMenus();
                        },
                        (response) => {
                            this.closeDialog('edit_group');
                            console.log("ERROR" + response)
                        });
                } else {
                    epicuri.postGroup(this.$http, group,
                        (response) => {
                            this.closeDialog('edit_group');
                            this.loadMenus(() => {
                                this.selectedCategoryGroups.splice(0, this.selectedCategoryGroups.length);
                                MENU_LOOP:for(let i = 0; i < this.menus.length; i++) {
                                    if (this.selectedMenu.Id === this.menus[i].Id) {
                                        CAT_LOOP:for (let j = 0; j < this.menus[i].MenuCategories.length; j++) {
                                            if(this.selectedCategory.Id === this.menus[i].MenuCategories[j].Id) {
                                                this.selectedCategoryGroups = this.menus[i].MenuCategories[j].MenuGroups;
                                                this.selectedCategory = this.menus[i].MenuCategories[j];
                                                break MENU_LOOP;
                                            }
                                        }
                                    }
                                }
                            });
                        },
                        (response) => {
                            this.closeDialog('edit_group');
                            console.log("ERROR" + response)
                        });
                }

            },
            cloneMenu: function(menuId, newName) {
                epicuri.cloneMenu(this.$http, menuId, newName,
                    (response) => {
                        this.loadMenus();
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    });

                this.newMenuName = '';
                this.closeDialog('clone_menu');

            },
            cloneCategory: function(categoryId, newName, newMenuId) {
                epicuri.cloneCategory(this.$http, categoryId, newName, newMenuId,
                    (response) => {
                        this.loadMenus();
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    });

                this.newCategoryName= '';
                this.closeDialog('clone_category');
            },
            cloneGroup: function(groupId, newName, selectedMenuCategory) {
                epicuri.cloneGroup(this.$http, groupId, newName, selectedMenuCategory,
                    (response) => {
                        this.loadMenus();
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    });
                this.newGroupName= '';
                this.closeDialog('clone_group');
            },
            onMenuClick: function(menu) {
                this.selectedMenu = menu;
                this.selectedMenuCategories = this.selectedMenu.MenuCategories;
                this.switchViews([false,true,false,false]);
            },
            onCategoryClick: function (category) {
                this.selectedCategory = category;
                this.selectedCategoryGroups = this.selectedCategory.MenuGroups;
                this.switchViews([false,false,true,false]);
            },
            onGroupClick: function (group) {
                this.selectedGroup = group;
                this.switchViews([false,false,false,true]);
            },
            categoryViewBack: function() {
                this.selectedMenu = {};
                this.switchViews([true,false,false,false]);
            },
            groupViewBack: function() {
                this.selectedCategory = {};
                this.switchViews([false,true,false,false]);
            },
            itemsViewBack: function() {
                this.selectedGroup = {};
                this.switchViews([false,false,true,false]);
            },
            switchViews: function (array) {
                this.showMenus = array[0];
                this.showCategories = array[1];
                this.showGroups = array[2];
                this.showItems = array[3];
            },
            triggerOpenMenuDialog: function(menu, ref) {
                this.selectedMenu = this.jsonCopy(menu);
                this.openDialog(ref);
            },
            triggerOpenCategoryDialog: function(category, ref) {
                this.selectedCategory = this.jsonCopy(category);
                this.newCategoryName = category.CategoryName;
                this.selectedService= "";
                this.selectedCourseId = "";
                this.openDialog(ref);
            },
            triggerOpenGroupDialog: function(group, ref) {
                this.selectedGroup = this.jsonCopy(group);
                this.openDialog(ref);
            },
            triggerDownload: function(menu) {
                let data = "ID,Item,Price,Description,Tax,Printer,Type,Unavailable,Menu,Category,Group\r\n";
                for(let i = 0; i < menu.MenuCategories.length; i++) {
                    let category = menu.MenuCategories[i];
                    for(let j = 0; j < category.MenuGroups.length; j++) {
                        let group = category.MenuGroups[j];
                        for(let k = 0; k < group.MenuItems.length; k++) {
                            let item = group.MenuItems[k];
                            data += item.Id + ",";
                            data += this.escapeCommas(item.Name) + ",";
                            data += item.Price.toFixed(2) + ",";
                            data += this.escapeCommas(item.Description) + ",";
                            data += this.taxes[item.TaxTypeId] + ",";
                            data += this.printers[item.DefaultPrinter] + ",";
                            data += this.getMenuTypeId(item.MenuItemTypeId) + ",";
                            data += item.Unavailable + ",";
                            data += this.escapeCommas(menu.MenuName) + ",";
                            data += this.escapeCommas(category.CategoryName) + ",";
                            data += this.escapeCommas(group.GroupName) + ",";

                            data += "\r\n";
                        }
                    }
                }
                const blob = new Blob([data], {type: 'text/plain'})
                const e = document.createEvent('MouseEvents'),
                a = document.createElement('a');
                a.download = menu.MenuName + ".csv";
                a.href = window.URL.createObjectURL(blob);
                a.dataset.downloadurl = ['text/json', a.download, a.href].join(':');
                //e.initEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
                e.initEvent('click', true, false);
                a.dispatchEvent(e);
            },
            escapeCommas:function(s) {
                if(typeof(s) === 'undefined' || s == null) {
                    return "";
                }
                if(s.includes(",")) {
                    return "\"" + s + "\"";
                } else {
                    return s;
                }
            },
            getMenuTypeId:function(id) {
                if(id === 0) {
                    return "Food";
                } else if(id === 1) {
                    return "Drink";
                } else {
                    return "Other";
                }
            },
            createMenu: function() {
                return {Id:null, MenuName: 'New Menu', Active:true};
            },
            createCategory: function() {
                return {Id:null, DefaultCourses:[], DefaultCourseIds:[], CategoryName:'New Category', MenuId:this.selectedMenu.Id}
            },
            createGroup: function() {
                return {Id:null, Name:'New Group', MenuItemIds:[], MenuCategoryId:this.selectedCategory.Id};
            },
            onDeleteMenuCloseDialog: function(outcome) {
                if(outcome === 'ok') {
                    console.log("delete");
                    epicuri.deleteMenu(this.$http, this.selectedMenu.Id,
                        (response) => {
                            this.loadMenus();
                        },
                        (response) => {
                            console.log("ERROR" + response)
                        });
                }
            },
            onDeleteCategoryCloseDialog: function(outcome) {
                if(outcome === 'ok') {
                    console.log("delete");
                    epicuri.deleteCategory(this.$http, this.selectedCategory.Id,
                        (response) => {
                            this.loadMenus(()=>{
                                let index = -1;
                                for(let i = 0; i < this.selectedMenuCategories.length; i++) {
                                    if(this.selectedMenuCategories[i].Id === this.selectedCategory.Id) {
                                        index =i;
                                        break;
                                    }
                                }
                                if(index > -1) {
                                    this.selectedMenuCategories.splice(index, 1);
                                }
                                this.selectedCategory = {};
                            });
                        },
                        (response) => {
                            console.log("ERROR" + response)
                        });
                }
            },
            onDeleteGroupCloseDialog: function(outcome) {
                if(outcome === 'ok') {
                    console.log("delete");
                    epicuri.deleteGroup(this.$http, this.selectedGroup.Id,
                        (response) => {
                            this.loadMenus(() => {
                                let index = -1;
                                for(let i = 0; i < this.selectedCategoryGroups.length; i++) {
                                    if(this.selectedCategoryGroups[i].Id === this.selectedGroup.Id) {
                                        index =i;
                                        break;
                                    }
                                }
                                if(index > -1) {
                                    this.selectedCategoryGroups.splice(index, 1);
                                }
                                this.selectedGroup = {};
                            });
                        },
                        (response) => {
                            console.log("ERROR" + response)
                        });
                }
            },
            onCategoryUp: function(menu, category) {
                let index = this.getCategoryIndex(menu,category, true);

                if(index > -1) {
                    let cat1 = menu.MenuCategories[index];
                    let cat2 = menu.MenuCategories[index-1];
                    cat1.Order = index-1;
                    cat2.Order = index;

                    this.swapElements(menu.MenuCategories, index, index-1);

                    epicuri.putCategory(this.$http,cat1,(r)=>{},(r)=>{});
                    epicuri.putCategory(this.$http,cat2,(r)=>{},(r)=>{});
                }
            },
            onCategoryDown: function(menu, category) {
                let index = this.getCategoryIndex(menu,category, false);

                if(index > -1) {
                    let cat1 = menu.MenuCategories[index];
                    let cat2 = menu.MenuCategories[index+1];
                    cat1.Order = index+1;
                    cat2.Order = index;

                    this.swapElements(menu.MenuCategories, index+1, index);

                    epicuri.putCategory(this.$http,cat1,(r)=>{},(r)=>{});
                    epicuri.putCategory(this.$http,cat2,(r)=>{},(r)=>{});
                }
            },
            getCategoryIndex: function(menu, category, up) {
                for(let i = 0; i < menu.MenuCategories.length; i++) {
                    let categoryInArray = menu.MenuCategories[i];
                    if(up) {
                        if (categoryInArray.Id === category.Id && i === 0) {
                            return -1;
                        }
                    } else {
                        if (categoryInArray.Id === category.Id && i === (menu.MenuCategories.length-1)) {
                            return -1;
                        }
                    }
                    if(categoryInArray.Id === category.Id) {
                        return i;
                    }
                }
            },
            onGroupUp: function(menu, category, group) {
                let index = this.getGroupIndex(category,group, true);

                if(index > -1) {
                    let group1 = category.MenuGroups[index];
                    let group2 = category.MenuGroups[index-1];
                    group1.Order = index-1;
                    group2.Order = index;

                    this.swapElements(category.MenuGroups, index, index-1);

                    epicuri.putGroup(this.$http,group1,(r)=>{},(r)=>{});
                    epicuri.putGroup(this.$http,group2,(r)=>{},(r)=>{});
                }
            },
            onGroupDown: function(category, group) {
                let index = this.getGroupIndex(category,group, false);

                if(index > -1) {
                    let group1 = category.MenuGroups[index];
                    let group2 = category.MenuGroups[index+1];
                    group1.Order = index+1;
                    group2.Order = index;

                    this.swapElements(category.MenuGroups, index+1, index);

                    epicuri.putGroup(this.$http,group1,(r)=>{},(r)=>{});
                    epicuri.putGroup(this.$http,group2,(r)=>{},(r)=>{});
                }
            },
            getGroupIndex: function(category, group, up) {
                for(let i = 0; i < category.MenuGroups.length; i++) {
                    let groupInArray = category.MenuGroups[i];
                    if(up) {
                        if (groupInArray.Id === group.Id && i === 0) {
                            return -1;
                        }
                    } else {
                        if (groupInArray.Id === group.Id && i === (category.MenuGroups.length-1)) {
                            return -1;
                        }
                    }
                    if(groupInArray.Id === group.Id) {
                        return i;
                    }
                }
            },
            onItemUp: function(menu, category, group, menuItem) {
                let index = this.getItemIndex(group, menuItem, true);

                if(index > -1) {
                    this.swapElements(group.MenuItemIds, index, index-1);
                    this.swapElements(group.MenuItems, index, index-1);
                    epicuri.putGroup(this.$http,group,(r)=>{},(r)=>{});
                }
            },
            onItemDown: function(menu, category, group, menuItem) {
                let index = this.getItemIndex(group, menuItem, false);
                if(index > -1) {
                    this.swapElements(group.MenuItemIds, index+1, index);
                    this.swapElements(group.MenuItems, index+1, index);
                    epicuri.putGroup(this.$http,group,(r)=>{},(r)=>{});
                }
            },
            getItemIndex: function(group, menuItem, up) {
                for(let i = 0; i < group.MenuItemIds.length; i++) {
                    let itemIdInArray = group.MenuItemIds[i];
                    if(up) {
                        if(itemIdInArray === menuItem.Id && i === 0) {
                            return -1;
                        }
                    } else {
                        if(itemIdInArray === menuItem.Id && i === (group.MenuItemIds.length-1)) {
                            return -1;
                        }
                    }
                    if(itemIdInArray === menuItem.Id) {
                        return i;
                    }
                }
            },
            removeFromGroup: function() {
                let items = this.getSelectedMenuItemsInGroup();

                for(let i = this.selectedGroup.MenuItemIds.length - 1; i >= 0; i--) {
                    if(this.containsMenuItemId(items, this.selectedGroup.MenuItemIds[i])) {
                        this.selectedGroup.MenuItemIds.splice(i, 1);
                    }
                }

                for(let i = this.selectedGroup.MenuItems.length - 1; i >= 0; i--) {
                    if(this.containsMenuItemId(items, this.selectedGroup.MenuItems[i].Id)) {
                        this.selectedGroup.MenuItems.splice(i, 1);
                    }
                }

                epicuri.putGroup(this.$http,this.selectedGroup,(r)=>{},(r)=>{});

                this.clearSelectedMenuItems();
            },
            alphabetiseGroup: function() {
                let items = [];
                for(let i = 0; i < this.selectedGroup.MenuItems.length; i++) {
                    items.push(this.selectedGroup.MenuItems[i]);
                }
                if(items.length === 0) return;

                items.sort(function(a, b){
                    if(a.Name < b.Name) return -1;
                    if(a.Name > b.Name) return 1;
                    return 0;
                });

                this.selectedGroup.MenuItems.splice(0,this.selectedGroup.MenuItems.length);
                this.selectedGroup.MenuItemIds.splice(0, this.selectedGroup.MenuItemIds.length);

                for(let i = 0; i < items.length; i++) {
                    this.selectedGroup.MenuItems.push(items[i]);
                    this.selectedGroup.MenuItemIds.push(items[i].Id);
                }

                epicuri.putGroup(this.$http,this.selectedGroup,(r)=>{},(r)=>{});
            },
            triggerRemoveItemFromGroupDialog: function(menuItem) {
                menuItem.selected = true;
                this.removeFromGroup();
            },
            containsMenuItemId: function(array, id) {
                for(let i = 0; i < array.length; i++) {
                    if(array[i].Id === id) {
                        return true;
                    }
                }
                return false;
            },
            copyToGroup: function() {
                this.openDialog('copy_items_to_group');
                /*this.clearSelectedMenuItems(); TODO - do this later */
            },
            copyItemsToGroup: function(group) {
                console.log(group);

                this.closeDialog('copy_items_to_group')
                this.clearSelectedMenuItems();

            },
            swapElements: function(array, index1, index2) {
                array.splice(index2, 1, array.splice(index1, 1, array[index2])[0]);
            },
            openDialog: function(ref) {
                this.$refs[ref].open();
            },
            closeDialog: function(ref) {
                this.$refs[ref].close();
            },
            getSelectedMenuItemsInGroup: function() {
                let items = [];
                for(let i = 0; i < this.selectedGroup.MenuItems.length; i++) {
                    if(this.selectedGroup.MenuItems[i].selected) {
                        items.push(this.selectedGroup.MenuItems[i]);
                    }
                }
                return items;
            },
            getSelectedMenuItemsInGroupLength: function() {
                try {
                    let array = this.getSelectedMenuItemsInGroup();
                    if (!array) {
                        return 0;
                    }
                    return array.length;
                } catch(err) {
                    return 0;
                }
            },
            clearSelectedMenuItems: function() {
                for(let i = 0; i < this.selectedGroup.MenuItems.length; i++) {
                    this.selectedGroup.MenuItems[i].selected = false;
                }
            },
            jsonCopy: function(object) {
                return JSON.parse(JSON.stringify(object));
            },
            updateFromCopy: function(object, array) {
                for(let i = 0; i < array.length; i++) {
                    if(array[i].Id === object.Id) {
                        array[i] = object;
                    }
                }
            },
            setServiceId: function(service) {
                this.selectedServiceId = service.Id;
            },
            updateCourseOnCategory: function (category, courseId, serviceId) {
                let array = [];
                for(let i = 0; i < category.DefaultCourseIds.length; i++) {
                    if(!category.DefaultCourseIds[i].startsWith(serviceId)) {
                        array.push(category.DefaultCourseIds[i]);
                    }
                }
                if(!this.arrayContains(courseId, array)) {
                    array.push(courseId);
                }
                category.DefaultCourseIds  = array;
                epicuri.putCategory(this.$http, category, (response)=>{this.loadMenus()}, (response)=>{});
            },
            arrayContains: function(element, array) {
                return (array.indexOf(element) > -1);
            }
        },
        onLoad: function (type) {

        },
        created: function() {
            this.loadMenus();
            this.loadMenuItems();
            this.loadServices();
            this.loadCourses();
            this.loadTaxes();
            this.loadPrinters();
        },
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