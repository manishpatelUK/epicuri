<template>
    <div class="content-wrapper">
        <div class="login-bg">
            <img class="login-logo" src="../images/epicuri-logo.jpg"/>
            <form novalidate @submit.stop.prevent="submit">
                <md-input-container>
                    <label>Venue ID</label>
                    <md-input required v-model="restaurantId" :disabled=spinner></md-input>
                    <!--<span class="md-error">Validation message</span>-->
                </md-input-container>
                <md-input-container>
                    <label>User Name</label>
                    <md-input required v-model="userName" :disabled=spinner></md-input>
                </md-input-container>
                <md-input-container md-has-password>
                    <label>Password</label>
                    <md-input required type="password" v-model="pw" :disabled=spinner></md-input>
                </md-input-container>
            </form>
            <a class="help-link" href="https://epicuri.freshdesk.com/helpdesk" target="_blank">Help</a>
            <md-button class="login-button" @click.native="onLogin" :disabled=spinner>Login</md-button>
            <p></p>
        </div>
        <md-spinner :md-size="50" md-indeterminate v-if="spinner" class="md-warn"></md-spinner>

        <md-dialog-alert
                :md-title="title"
                :md-content-html="contentHtml"
                ref="dialog">
        </md-dialog-alert>
    </div>
</template>

<script>
    let epicuri = require('../internal/epicuri');
    let queryparams = require('../internal/queryparams');

    export default {
        data() {
            return {
                restaurantId: "",
                userName: "",
                pw: "",
                spinner: false,
                title: "Oops!",
                contentHtml: "Error"
            }
        },
        mounted: function () {
            let token = queryparams.getAllUrlParams().Auth;
            let printerId = queryparams.getAllUrlParams().Printer;

            if (token) {
                epicuri.setLocalToken(token);
                sessionStorage.setItem("printerBar", printerId);
                this.$router.push({name: "kitchen_app", query: {printerId: printerId}});
                this.loggedIn = true;
            } else {
                this.loggedIn = !!epicuri.findLocalToken();
            }

            //todo check if there is "kitchen=true" to redirect to kitchen view
        },
        methods: {
            onLogin: function () {
                this.spinner = true;
                epicuri.login(GLOBAL_HTTP, this.restaurantId, this.userName, this.pw,
                    (response) => {
                        this.spinner = false;
                        this.$emit('login-success');
                        this.$router.push({name: 'dashboard'});
                    },
                    (response) => {
                        this.spinner = false;
                        this.contentHtml = "Could not log you in: <strong>" + "Credentials not recognised or your account is disabled for portal access." + "</strong>";
                        this.openDialog("dialog");
                    });
            },
            openDialog: function (ref) {
                this.$refs[ref].open();
            }
        }
    }
</script>

<style>
    .md-spinner {
        padding: 0;
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        margin: auto;
        bottom: 0;
    }

    .md-input-focused input,
    .md-input-focused label {
        color: #EE6E09 !important;
    }

    .md-input-focused .md-icon:not(.md-icon-delete) {
        color: #EE6E09 !important;
    }

    .md-input-focused:after {
        background-color: #EE6E09 !important;
    }

    .content-wrapper {
        height: 100vh;
        width: 100%;
        background-image: url('../images/splash-background.png');

    }

    .login-bg {
        margin: 0 auto 50px auto;
        padding-top: 15vh;
        display: block;
        width: 30%;
        background-color: rgba(255, 255, 255, 0.7);
    }

    .login-button {
        background-color: #EE6E09;
        color: #FFFFFF;
        width: 100%;
        height: 50px;
        margin: 20px 0 0 0;
    }

    .login-button:hover {
        background-color: #FFF !important;
        color: #EE6E09;
        border: 1px solid #EE6E09;
    }

    .login-logo {
        margin: 0 auto 10vh auto;
    }

    .help-link {
        color: #EE6E09 !important;
        font-size: 18px;
    }

</style>
