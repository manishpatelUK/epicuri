/// <reference path="~/Scripts/jquery-1.5.1-vsdoc.js" />

(function ($) {

    // default handler for event PreparingUpload
    function onPreparingUpload(pl, fname, mime, size) {
        pl.settings.logger("ufname = [" + fname + "] umime = [" + mime + "] usize = [" + size + "]");
        return true;
    }

    // default handler for event InitPacketArrived
    function onInitPacketArrived(pl, guid) {
        pl.settings.logger("guid = [" + guid + "]");
    }

    // default handler for event DataPacketArrived
    function onDataPacketArrived(pl, ack, total) {
        pl.settings.logger("ACK [" + ack.Guid + "] packet = [" + ack.Packet + "] total = [" + total + "]");
    }

    // default handler for event FileUploaded
    function onFileUploaded(pl, ack, total) {
        pl.settings.logger("File finished!!!");
    }

    $.extend($.fn, {
        createUploaderHtml5: function (options) {
            if (!this.is(":input[type=file]"))
                throw new Error("Must be an input type=file");

            var uploader = new $.uploaderHtml5(options, this);
            return uploader;
        }
    });

    // constructor for uploader
    $.uploaderHtml5 = function (options, input) {
        this.settings = $.extend({}, $.uploaderHtml5.defaults, options);
        this.input = input;
        this.ufname = "";
        this.umime = "";
        this.usize = "";
        this.ufile = null;
        this.totalPackages = 0;
        this.lastReceived = 0;
        this.fileGuid = "";
        this.state = 0; // 0 - stoped  1 - working
        this.trackId = this.settings.trackId;
        this.init();
    };


    $.extend($.uploaderHtml5, {
        defaults: {
            postDataUrl: "",                                // (Required) Path to handler/page/Controller as data receiver.
            packetSize: 4 * 1024 * 1024,                    // Size to split the file by
            onPreparingUpload: onPreparingUpload,           // Event raised before send the initial packet (cancelable)
            onInitPacketArrived: onInitPacketArrived,       // Event raised after initial packet is received
            onDataPacketArrived: onDataPacketArrived,       // Event raised each time a data packet is received
            logger: function (msg) { console.log(msg); },   // Function resposible of logging i.e Debugger console.
            onFileUploaded: onFileUploaded,                 // Event raised when the last packet is arrived, so the file is finished
            trackId: ""
        },

        prototype: {
            init: function () {
                var obj = this;
                this.input.change(function () {
                    obj.ufile = this.files[0];
                    obj.ufname = obj.ufile.name;
                    obj.umime = obj.ufile.type || "application/octet-stream";
                    obj.usize = obj.ufile.size;
                });
            },

            getDOMElement: function () {
                return this.input;
            },

            hasFile: function () {
                return this.ufile != null;
            },

            getFileName: function () {
                return this.ufname;
            },
            
            getFileSize: function () {
                return this.usize;
            },

            cancelUpload: function () {
                this.state = 0; // set the stoped state so the next packet won't be sent
                localStorage.removeItem(this.fileId); // remove from cache
            },

            startUpload: function () {
                // if there's no file do nothing
                if (this.ufile == null) return;

                // if user cancel by returning false then do nothing
                if (!this.settings.onPreparingUpload(this, this.ufname, this.umime, this.usize)) return;

                // initialize the global variables
                this.totalPackages = Math.ceil(this.usize / this.settings.packetSize);
                this.lastReceived = 0;
                this.fileGuid = "";
                this.state = 1; // working

                // check first in the cache
                this.fileId = this.ufname + "|" + this.umime + "|" + this.usize;
                var fileData = localStorage[this.fileId];
                if (fileData) {
                    var parts = fileData.split("|");
                    this.fileGuid = parts[0];
                    this.lastReceived = parseInt(parts[1]);

                    // start upload file parts
                    this.uploadDataPacket();
                } else {
                    // submit initial packet information to server
                    this.postInitialPacket();
                }
            },

            // post initial data, just metadata
            postInitialPacket: function () {
                // if someone canceled then do nothing
                if (this.state == 0) return;

                var formData = new window.FormData();
                formData.append('ufname', this.ufname);
                formData.append('umime', this.umime);
                formData.append('usize', this.usize);

                var obj = this;
                var xhr = new window.XMLHttpRequest();
                xhr.open('POST', this.settings.postDataUrl, true);
                xhr.onload = function (e) {
                    obj.onInitPacketArrival(this);
                };
                xhr.send(formData);
            },

            // receive the data on init packet sent and start sending data packets
            onInitPacketArrival: function (xhr) {

                var response = JSON.parse(xhr.responseText);
                this.fileGuid = response.Guid;

                this.settings.onInitPacketArrived(this, this.fileGuid);

                // if someone canceled then do nothing
                if (this.state == 0) return;

                // store in cache
                localStorage[this.fileId] = this.fileGuid + "|" + 0;

                // start upload file parts
                this.uploadDataPacket();
            },

            uploadDataPacket: function () {
                if (this.state == 0) return;

                var packetNumber = (this.lastReceived + 1);
                if (packetNumber > this.totalPackages) return;

                var xhr = new window.XMLHttpRequest();
                var url = this.settings.postDataUrl + "?guid=" + this.fileGuid + "&packet=" + packetNumber + "&total=" + this.totalPackages;
                var obj = this;
                xhr.open('POST', url, true);
                xhr.onprogress = function (e) {
                    //updateTotalProgress(fileDetails, e.position);
                };

                xhr.onload = function (e) {
                    obj.onDataPacketArrival(this);
                };
                xhr.send(this.getDataPacket(packetNumber));
            },

            getDataPacket: function (packetNumber) {

                var startByte = (packetNumber - 1) * this.settings.packetSize;
                var endByte = startByte + this.settings.packetSize, packet;

                // the HTML5 dirty part, FileReader API is not yet fully implemented
                //packet = this.file.slice(startByte, endByte);

                if ('mozSlice' in this.ufile) {
                    // mozilla
                    packet = this.ufile.mozSlice(startByte, endByte);
                } else if ('webkitSlice' in this.ufile) {
                        // webkit
                    packet = this.ufile.webkitSlice(startByte, endByte);
                } else {
                    // IE 10
                    packet = this.ufile.slice(startByte, endByte);
                }
                return packet;
            },

            onDataPacketArrival: function (xhr) {
                // if someone canceled then do nothing
                if (this.state == 0) return;

                var response = JSON.parse(xhr.responseText);

                this.settings.onDataPacketArrived(this, response, this.totalPackages);

                // if packet == -1 that means no retry possible
                if (response.Packet == -1) {
                    this.fileGuid = "";
                    this.lastReceived = 0;

                    localStorage.removeItem(this.fileId);
                    this.postInitialPacket();
                    return;
                }

                this.fileGuid = response.Guid;
                this.lastReceived = response.Packet;

                // store in cache if not canceled

                localStorage[this.fileId] = this.fileGuid + "|" + this.lastReceived;

                // upload next part
                this.uploadDataPacket();

                // if last packet was received OK
                if (this.lastReceived == this.totalPackages) {
                    localStorage.removeItem(this.fileId);

                    // fire event finished the file upload
                    this.settings.onFileUploaded(this, response, this.totalPackages);
                    this.state = 0; // stoped
                }
            }
        }
    });

})(jQuery);
