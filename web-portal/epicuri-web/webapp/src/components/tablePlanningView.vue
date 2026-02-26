<template>
    <div>
        <h1>Coming Soon</h1>
        <!--<div id="main">
            <div class="drag">
                <h2>Draggable</h2>
                <draggable :list="list" class="dragArea">
                    <div v-for="element in list">{{element.name}}</div>
                </draggable>
            </div>
            <div class="normal">
                <h2>Normal v-for</h2>
                <div class="dragArea">
                    <div v-for="element in list">{{element.name}}</div>
                </div>
            </div>

            <button @click="add">Add</button>
            <button @click="replace">Replace</button>

            <draggable>
                <div v-for="element in tasks">
                    {{element.id}}
                </div>
            </draggable>

        </div>-->
    </div>
</template>

<script>
    import draggable from 'vuedraggable';
    import moment from 'moment';

    export default {
        data: function () {
            return {
                list: [{name: "John"},
                    {name: "Joao"},
                    {name: "Jean"}],
                tasks: [
                    {
                        id: 'Task 1',
                        name: 'Redesign website',
                        start: '2016-12-28',
                        end: '2016-12-31',
                        progress: 20,
                        dependencies: 'Task 2, Task 3'
                    },
                    {
                        id: 'Task 2',
                        name: 'Redesign website',
                        start: '2016-12-28',
                        end: '2016-12-31',
                        progress: 20,
                        dependencies: 'Task 2, Task 3'
                    },
                    {
                        id: 'Task 3',
                        name: 'Redesign website',
                        start: '2016-12-28',
                        end: '2016-12-31',
                        progress: 20,
                        dependencies: 'Task 2, Task 3'
                    },
                ]
            }
        },
        components: {
            draggable,
            moment
        },
        mounted: function () {
            console.log("TABLE BRE")
        },
        methods: {
            add: function () {
                this.list.push({name: 'Juan'});
            },
            replace: function () {
                this.list = [{name: 'Edgard'}]
            }
        }
    }
</script>

<style>
    .normal {
        background-color: grey;
    }

    .drag {
        background-color: green;
    }

    .dragArea {
        min-height: 10px;
    }
</style>
