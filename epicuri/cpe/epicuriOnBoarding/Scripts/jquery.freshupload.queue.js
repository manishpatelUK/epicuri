/// <reference path="~/Scripts/jquery-1.5.1-vsdoc.js" />
// Plugin providing queue feature to freshupload 
// Author: Abel Pérez Martínez
// Email: abelperezok@gmail.com

(function ($) {
    var globalIndex = Math.round(Math.random() * 1000000000); //Global element index, it's different for each post.
    var queue = [];
    var guidmap = {};
    var plugin;
    var working = 0;
    var currentFreshUpload = null;

    function queueIsEmpty() {
        for (var p in queue) {
            return false;
        }
        return true;
    }

    function getPeek() {
        for (var p in queue) {
            return queue[p];
        }
        return null;
    }

    // Internal function that is responsible for add a new instance to the queue
    function addFile(freshupload) {
        var op = plugin.options;
        var currentIndex = ++globalIndex;
        var fname = freshupload.getFileName();
        var fsize = freshupload.getFileSize();

        // invoke the action AddFile with the file info
        var finfo = { FileName: fname, FileSize: fsize };
        op.fnAddFile(plugin, freshupload.trackId, finfo);

        if (finfo.Cancel) return;

        freshupload.getDOMElement().hide();
        queue[freshupload.trackId] = freshupload;

        var inp = createInputFile(currentIndex);
        inp.appendTo($('div.filecont', plugin));
        currentFreshUpload = createfupload(inp, currentIndex);

        // if AutoStart flag is set then start upload inmediately
        if (finfo.AutoStart) startUpload();
    }

    // Removes the internal block corresponding to 'index' and calls fnRemoveFile handler for custom client code.
    function removeFile(index, reason) {
        delete queue[index];
        plugin.options.fnRemoveFile(plugin, index, reason, guidmap[index]);
    }

    // Cancels a job, removes it from the queue and launches the next upload 
    function cancelJob(index) {
        if (!queue[index]) return;
        
        queue[index].cancelUpload();
        removeFile(index, "canceled");
        if (working == 1)
            startUpload();
    }

    // Creates the Cancel link for each file in the list for upload
    function createCancelLink(pl, index) {
        var cancel = $('<a href="javascript:">' + pl.options.textCancel + '</a>')
        .click(function () { pl.options.cancelJob(index); });
        return cancel;
    }

    // Adds a new Visual element (basic) for visual feedback to the user
    // May be overriden by the user via parameter fnAddFile in plugin constructor 
    function addVisualElement(pl, index, finfo) {
        var cancel = pl.options.createCancelLink(pl, index);
        $('<div class="list-item" data-freshupload-index="' + index + '"></div>')
            .appendTo(pl)
            .append(cancel)
            .append('<span>' + finfo.FileName + '</span>')
            .append('<div data-freshupload-role="progress"></div>');
    }

    // Removes a new Visual element (basic) for visual feedback to the user
    // May be overriden by the user via parameter fnRemoveFile in plugin constructor 
    function removeVisualElement(pl, index, reason, guid) {
        var ln = (reason == "finished") ? "underline" : "line-through";
        $('div[data-freshupload-index=' + index + ']').css("text-decoration", ln);
    }

    // Internal function that creates an input type=file  
    function createInputFile(id) {
        var input = $('<input id="freshuploadinput' + id + '" type="file" name="freshuploadinput' + id + '" />');
        return input;
    }

    // Internal function that creates a freshupload instance 
    function createfupload(input, index) {
        var freshupload = input.freshupload({
            postDataUrl: plugin.options.postDataUrl,
            packetSize: plugin.options.packetSize,
            xapLocation: plugin.options.xapLocation,
            onInitPacketArrived: onInitPacketArrived,
            onDataPacketArrived: onDataPacketArrived,
            onFileUploaded: onFileUploaded,
            trackId: index
        });
        return freshupload;
    }

    // Receives the notifycation from uploader that a the initial packet has been arrived
    function onInitPacketArrived(pl, guid) {
        // stores interanlly the guid for each file
        guidmap[pl.trackId] = guid;
    }

    // Receives the notifycation from uploader that a packet has been arrived
    // Fires the fnShowProgress event so the UI can be updated with the percent
    function onDataPacketArrived(pl, ack, total) {
        var percent = Math.round(ack.Packet / total * 100);
        plugin.options.fnShowProgress(plugin, pl.trackId, percent, ack);
    }

    // Shows the current progress for the default visual interface
    // May be overriden by the user via parameter fnShowProgress in plugin constructor 
    function fnShowProgress(pl, index, percent, ack) {
        $('div[data-freshupload-index=' + index + '] div[data-freshupload-role=progress]').html(percent + " %");
    }

    // starts the upload process following the order in the queue
    function startUpload() {
        working = 0;
        if (queueIsEmpty()) return;

        var peek = getPeek();
        if (peek != null) {
            working = 1;
            peek.startUpload();
        }
    }

    // handles the FileUploaded event, removes the job finished and launches the next upload  
    function onFileUploaded(pl, ack, total) {
        removeFile(pl.trackId, "finished");
        startUpload();
    }

    // Start up point of the plugin
    $.fn.freshupload_queue = function (options) {
        this.options = $.extend({}, $.fn.freshupload_queue.defaults, options);
        plugin = this;
        if (plugin.length > 0) {

            plugin.empty().append('<div class="filecont"></div>');

            var inp = createInputFile(globalIndex);
            inp.appendTo($('div.filecont', plugin));

            currentFreshUpload = createfupload(inp, globalIndex);

            var btnAdd = $(plugin.options.selectorButtonAdd);
            if (!btnAdd.length) {
                btnAdd = $('<a href="javascript:" class="add">' + plugin.options.textAdd + '</a>');
                btnAdd.appendTo(plugin);
            }
            btnAdd.click(function () {
                if (!currentFreshUpload.hasFile()) return;
                if (!plugin.options.fnValidateBeforeAdd(plugin)) return;
                addFile(currentFreshUpload);
            });

            var btnUpload = $(plugin.options.selectorButtonUpload);
            if (!btnUpload.length) {
                btnUpload = $('<a href="javascript:" class="upload">' + plugin.options.textUpload + '</a>');
                btnUpload.appendTo(plugin);
            }
            btnUpload.click(function () {
                if (plugin.options.fnValidateBeforeUpload(plugin))
                    startUpload();
            });
        }
        return plugin;
    };

    // Defaults for the plugin (works well if using the module's defaults).
    $.fn.freshupload_queue.defaults = {
        postDataUrl: "",                                // (Required) Path to handler/page/Controller who is responsible for receive the data.
        xapLocation: "",                                // (Required) Path to xap Sliverlight source data 
        packetSize: 4 * 1024 * 1024,                    // Size to split the file by

        containerId: "freshuploadContainer",            // Just an id for HTML container (hidden).
        fnAddFile: addVisualElement,                    // Function that adds visual elements (DOM) for the corresponding hidden elements at the bottom of the page.
        fnShowProgress: fnShowProgress,                 // Function that is called when the server sends a status chance notifycation.
        textAdd: "Add",                                 // Text for the link/button Add, may be overriden using the parameter selectorButtonAdd.
        textUpload: "Upload",                           // Text for the link/button Upload, may be overriden using the parameter selectorButtonUpload.
        textCancel: "Cancel",                           // Text for the link/button Cancel, may be overriden using the parameter createCancelLink which is a function.
        selectorButtonAdd: "",                          // jQuery selector indicating how to get the DOM element that will click for the add action.
        selectorButtonUpload: "",                       // jQuery selector indicating how to get the DOM element that will click for the upload action.
        createCancelLink: createCancelLink,             // Function responsible for creating the cancel link for every file in the list.
        cancelJob: cancelJob,                           // 
        fnRemoveFile: removeVisualElement,              // Function that is called when a file finish the copy on the server.
        fnValidateBeforeAdd: function (pl) { return true; }, // Function that may execute some validation code, for example: validate the filename in one input text.
        fnUploadComplete: function (pl) { },            // Function that is called when all uploads were done, may be used for visual feedback in the client.
        fnValidateBeforeUpload: function (pl) { return true; }
    };

})(jQuery);


