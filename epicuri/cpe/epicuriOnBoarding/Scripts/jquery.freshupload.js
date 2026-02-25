/// <reference path="~/Scripts/jquery-1.5.1-vsdoc.js" />
/// <reference path="jquery.freshupload.silverlight.js" />
/// <reference path="jquery.freshupload.html5.js" />

(function ($) {

    // verify if XMLHttpRequest contains a method named upload
    function xhrUpload() {
        return "upload" in new window.XMLHttpRequest;
    }

    // verify if FormData object exists in the browser 
    function xhrForm() {
        return !!window.FormData;
    }

    // verify if local storage exists 
    function localStorageCheck() {
        try {
            return 'localStorage' in window && window['localStorage'] !== null;
        } catch (e) {
            return false;
        }
    }

    // verify the FileReader API is present, this is the most important
    function fileApiCheck() {
        return typeof window.FileReader != 'undefined';
    }

    // Start up point of the plugin
    $.extend($.fn, {
        freshupload: function (options) {
            if (!this.is(":input[type=file]"))
                throw new Error("Must be an input type=file");

            var uploader;
            if (xhrUpload() && xhrForm() && localStorageCheck() && fileApiCheck()) {
                uploader = this.createUploaderHtml5(options);
            }
            else {
                uploader = this.createUploaderSilverlight(options);
            }
            return uploader;
        }
    });

})(jQuery);