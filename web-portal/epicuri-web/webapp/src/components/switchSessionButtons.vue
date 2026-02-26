<template>
    <div class="md-pad-top lg-pad-bottom">
        <toggle-button :value="false"
                       :color="{'checked':'#EE6E09', 'unchecked':'#545454'}"
                       :labels="true"
                       :width="85"
                       :height="40"
                       v-model="radio" @change="reloadPage"
                       id="changed-font"
                       />
        <label class="switch-label md-pad-left">Group by session</label>
    </div>
</template>

<script>
    export default {
        name: 'RegularRadio',
        data: () => ({
            radio: false
        }),
        methods: {
            reloadPage: function(){
                this.$emit('switch-sort', this.radio)
            }
        }
    }
</script>

<style>
    .vue-js-switch#changed-font {
        font-size: 22px;
    }
    .md-pad-top{
        padding-top: 10px;
    }
    .lg-pad-bottom{
        padding-bottom: 15px;
    }
    .md-pad-left{
        padding-left: 10px;
    }
    .switch-label{
        font-size: 20px;
        line-height: 34px;
        font-weight: bold;
        color: #444444;
        vertical-align: middle;
    }
</style>