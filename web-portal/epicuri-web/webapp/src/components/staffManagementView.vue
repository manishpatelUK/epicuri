<template>
    <div>
        <h2 class="menu-management-header">Change your staff permissions here based on role to enable/disable access to certain features in the app.
            Please ensure each staff member has the correct role via Login Manager on the app.</h2>
        <md-layout>
            <md-card class="roles-card" v-for="(role, index) in staffPermissionsData">
                <md-card-header>
                    <h3 class="roles-header">
                        {{role.roleReadableName}}
                    </h3>
                </md-card-header>
                <md-card-content>
                    <md-list class="roles-list" v-for="(capability, index1) in role.booleanCapabilities">
                        <md-list-item class="roles-list-item">
                            {{capability.capabilityReadableName}}
                            <md-checkbox class="roles-checkbox" v-on:change="onChange()" v-model="capability.enabled"></md-checkbox>
                        </md-list-item>
                    </md-list>
                </md-card-content>
            </md-card>
        </md-layout>
        <md-button class="md-raised md-primary submit-permission-btn" v-on:click="openDialog('are_you_sure_modal')">Save</md-button>

        <md-dialog md-open-from="#fab" class="roles-modal" md-close-to="#fab" ref="are_you_sure_modal">
            <md-dialog-title>Save changes</md-dialog-title>
            <md-dialog-content>
                <p>Are you sure you want to save this changes?</p>
            </md-dialog-content>
            <md-dialog-actions>
                <md-button class="md-primary" @click="onPermissionsSubmit()">Yes</md-button>
                <md-button class="md-primary" @click="closeDialog('are_you_sure_modal')">No</md-button>
            </md-dialog-actions>
        </md-dialog>
        <md-dialog md-open-from="#fab" class="roles-modal" md-close-to="#fab" ref="success_modal">
            <md-dialog-title>Success</md-dialog-title>
            <md-dialog-content>
                <p>You have successfully updated changes</p>
            </md-dialog-content>
            <md-dialog-actions>
                <md-button class="md-primary" @click="closeDialog('success_modal')">Ok</md-button>
            </md-dialog-actions>
        </md-dialog>
        <md-dialog md-open-from="#fab" class="roles-modal" md-close-to="#fab" ref="error_modal">
            <md-dialog-title>Error</md-dialog-title>
            <md-dialog-content>
                <p>An error occurred</p>
            </md-dialog-content>
            <md-dialog-actions>
                <md-button class="md-primary" @click="closeDialog('error_modal')">Ok</md-button>
            </md-dialog-actions>
        </md-dialog>
    </div>
</template>

<script>
    let epicuri = require('../internal/epicuri')
    let _ = require('lodash')
    export default {
        data: () => {
            return {
                staffPermissionsData: [],
                enableSave: true
            }
        },
        mounted: function() {
          this.getStaffPermissions()
        },
        methods: {
            getStaffPermissions: function () {
                epicuri.getStaffPermissions(this.$http,
                    (response) => {
                        this.staffPermissionsData = response.body;
                    },
                    (response) => {
                        console.log("ERROR" + response)
                    }
                )
            },
            onChange: function() {
                this.enableSave = !this.enableSave
            },
            onPermissionsSubmit: function () {
                this.closeDialog('are_you_sure_modal')

                epicuri.setStaffPermissions(this.$http, this.staffPermissionsData,
                    (response) => {
                        console.log("SUCCESS" + response)
                        this.successDialog('success_modal')
                        this.getStaffPermissions()
                    },
                    (response) => {
                        this.errorModal('error_modal')
                        console.log("ERROR" + response)
                    }
                )
            },
            openDialog: function (ref) {
                this.$refs[ref].open();
            },
            closeDialog(ref) {
                this.$refs[ref].close();
            },
            successDialog(ref) {
                this.$refs[ref].open();
            },
            errorModal(ref) {
                this.$refs[ref].open();
            }
        }
    }
</script>

<style>
    .roles-list-item .md-list-item-container {
        padding: 0;
        font-size: 1em;
    }
    .roles-list {
        padding: 0;
    }
    .roles-checkbox {
        margin: 5px 5px 5px 10px;
    }
    .roles-header {
        margin-bottom: 0;
        margin-top: 0;
    }
    .md-list .roles-list-item .md-list-item-container {
        min-height: 35px;
    }
    .md-list .roles-list-item .md-list-item-container:hover {
        background-color: transparent!important;
    }
    .roles-card {
        margin-right: 10px;
        margin-bottom: 10px;
    }
    .submit-permission-btn {
        position: fixed;
        bottom: 30px;
        right: 10px;
        z-index: 2;
    }
    .menu-management-header {
        margin-bottom: 40px;
    }
</style>
