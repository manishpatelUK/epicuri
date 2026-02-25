/// <reference path="~/Scripts/jquery-1.5.1-vsdoc.js" />

(function ($) {

    var globalIndex = Math.round(Math.random() * 1000000000);

    // declare the function onSilverlightError as child of window so the silverlight plugin can see it
    window.onSilverlightError = function (sender, args) {
        var appSource = "";
        if (sender != null && sender != 0) {
            appSource = sender.getHost().Source;
        }

        var errorType = args.ErrorType;
        var iErrorCode = args.ErrorCode;

        if (errorType == "ImageError" || errorType == "MediaError") {
            return;
        }

        var errMsg = "Unhandled Error in Silverlight Application " + appSource + "\n";

        errMsg += "Code: " + iErrorCode + "    \n";
        errMsg += "Category: " + errorType + "       \n";
        errMsg += "Message: " + args.ErrorMessage + "     \n";

        if (errorType == "ParserError") {
            errMsg += "File: " + args.xamlFile + "     \n";
            errMsg += "Line: " + args.lineNumber + "     \n";
            errMsg += "Position: " + args.charPosition + "     \n";
        }
        else if (errorType == "RuntimeError") {
            if (args.lineNumber != 0) {
                errMsg += "Line: " + args.lineNumber + "     \n";
                errMsg += "Position: " + args.charPosition + "     \n";
            }
            errMsg += "MethodName: " + args.methodName + "     \n";
        }

        throw new Error(errMsg);
    };

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
        createUploaderSilverlight: function (options) {
            if (!this.is(":input[type=file]"))
                throw new Error("Must be an input type=file");

            var uploader = new $.uploaderSilverlight(options, this);
            return uploader;
        }
    });

    // constructor for uploader
    $.uploaderSilverlight = function (options, input) {
        this.settings = $.extend({}, $.uploaderSilverlight.defaults, options);
        this.input = input;
        this.trackId = this.settings.trackId;
        // increase the global index
        globalIndex++;
        // generate the silverlight markup
        this.input.hide().after(
            "<div id=\"silverlightControlHost\" style=\"width:100px;height: 28px; border: 1px solid black; overflow:hidden\">" +
                "<object id=\"silverlight_SLupload\" data=\"data:application/x-silverlight-2,\" type=\"application/x-silverlight-2\" width=\"100%\" height=\"100%\">" +
            		"<param name=\"source\" value=\"" + this.settings.xapLocation + "\"/>" +
                    "<param name=\"onError\" value=\"onSilverlightError\" />" +
                    "<param name=\"background\" value=\"white\" />" +
                    "<param name=\"minRuntimeVersion\" value=\"4.0.60310.0\" />" +
            		"<param name=\"autoUpgrade\" value=\"true\" />" +
                    "<param name=\"onLoad\" value=\"pluginLoaded" + globalIndex + "\" />" +
                    "<a href=\"http://go.microsoft.com/fwlink/?LinkID=149156&v=4.0.60310.0\" style=\"text-decoration:none\">" +
                        //"<img src=\"http://go.microsoft.com/fwlink/?LinkId=161376\" alt=\"Get Microsoft Silverlight\" style=\"border-style:none\"/>"+
                    "</a>" +
                "</object>" +
                "<iframe id=\"_sl_historyFrame\" style=\"visibility:hidden;height:0px;width:0px;border:0px\"></iframe>" +
            "</div>");

        // the internal silverlight uploader
        this.uploader = null;

        var obj = this;
        // Catch the load event
        // declare the function pluginLoaded as child of window so the silverlight plugin can see it
        window["pluginLoaded" + globalIndex] = function (sender, args) {
            var slobject = sender.getHost();
            obj.uploader = slobject.Content.uploader;
            obj.init();
        };
    };

    $.extend($.uploaderSilverlight, {
        defaults: {
            xapLocation: "",                                // (Required) Path to xap Sliverlight source data 
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
                // set the PostDataUrl according to external options
                this.uploader.PostDataUrl = this.settings.postDataUrl;

                var obj = this;
                // fire the onPreparingUpload event
                this.uploader.PreparingUpload = function (s, e) {
                    obj.settings.onPreparingUpload(obj, e.FileName, e.ContentType, e.FileSize);
                };

                // fire the onInitPacketArrived event
                this.uploader.InitPacketArrived = function (s, e) {
                    obj.settings.onInitPacketArrived(obj, e.Guid);
                };

                // fire the onDataPacketArrived event
                this.uploader.DataPacketArrived = function (s, e) {
                    var ack = { Guid: e.Guid, Packet: e.Packet };
                    obj.settings.onDataPacketArrived(obj, ack, e.TotalPackages);
                };

                // fire the onFileUploaded event
                this.uploader.FileUploaded = function (s, e) {
                    var ack = { Guid: e.Guid, Packet: e.Packet };
                    obj.settings.onFileUploaded(obj, ack, e.TotalPackages);
                };
            },

            getDOMElement: function () {
                return this.input.next();
            },

            hasFile: function () {
                return this.uploader.HasFile();
            },

            getFileName: function () {
                return this.uploader.GetFileName();
            },

            getFileSize: function () {
                return this.uploader.GetFileSize();
            },

            cancelUpload: function () {
                this.uploader.CancelUpload();
            },

            startUpload: function () {
                this.uploader.StartUpload();
            }
        }
    });

})(jQuery);
