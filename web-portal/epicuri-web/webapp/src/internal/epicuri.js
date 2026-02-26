let models = require('../model/models');

const authCookie = "epicuri.co.uk.epa.current";
let authToken;

let options = {
   headers: {
      "X-Epicuri-API-Version": "1",
      "Content-type": "application/json",
      "X-Epicuri-Portal": "true"
   },
   timeout: 5000
};

//const targetUrl = "http://api-dev.epicuri.co.uk";
const targetUrl = "https://api-prod.epicuri.co.uk";

exports.login = (http, restaurantId, userName, password, onSuccess, onFail) => {
   let loginRequest = models.createLoginRequest(restaurantId, userName, password);
   http.post(targetUrl + "/Authentication/Login", loginRequest, options)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("Incorrect credentials or Venue ID");
              }
              this.setLocalToken(response.body.AuthKey);
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};

exports.getSessions = (http) => {
   console.log("Get sessions");
};

exports.getRestaurant = (http, onSuccess, onFail) => {
    this.getter(http,"Restaurant",onSuccess,onFail,"GET RESTAURANT ERROR")
};

exports.getPSTerminals = (http, onSuccess, onFail) => {
    this.getter(http,"External/PaymentSense/terminals",onSuccess,onFail,"GET PS TERMINALS ERROR")
};

exports.getPSReports = (http, onSuccess, onFail) => {
    this.getter(http,"External/PaymentSense/reports",onSuccess,onFail,"GET PS REPORTS ERROR")
};

exports.postPSEmailReport = (http, requestId, onSuccess, onFail) => {
    this.poster(http, "Reporting/paymentSense/email/" + requestId, {}, onSuccess, onFail, "EMAIL REPORT ERROR");
};

exports.postPSReportRequest = (http, tpi, type, onSuccess, onFail) => {
    this.poster(http,"External/PaymentSense/reports/" + tpi,{reportType: type},onSuccess,onFail,"POST PS REPORTS ERROR", 120000)
};

exports.getPrinters = (http, onSuccess, onFail) => {
    this.getter(http,"Printer",onSuccess,onFail,"GET PRINTERS ERROR");
};

exports.getMenus = (http, onSuccess, onFail) => {
    this.getter(http,"Menu",onSuccess,onFail,"GET MENUS ERROR");
};

exports.getTaxes = (http, onSuccess, onFail) => {
    this.getter(http,"TaxType",onSuccess,onFail,"GET TAXES ERROR");
};

exports.getMenuItems = (http, onSuccess, onFail) => {
    this.getter(http,"MenuItem",onSuccess,onFail,"GET ITEMS ERROR");
};

exports.getPreferences = (http, onSuccess, onFail) => {
    this.getter(http,"preferences",onSuccess,onFail,"GET PREFERENCES ERROR");
};

exports.getCourses = (http, onSuccess, onFail) => {
    this.getter(http,"Course",onSuccess,onFail,"GET COURSES ERROR");
};

exports.getServices = (http, onSuccess, onFail) => {
    this.getter(http,"Service",onSuccess,onFail,"GET SERVICES ERROR");
};

exports.getModifierGroups = (http, onSuccess, onFail) => {
    this.getter(http,"ModifierGroup",onSuccess,onFail,"GET MODIFIER GROUPS ERROR");
};

exports.getPaymentSenseReport = (http, onSuccess, onFail) => {
    this.getterWithAcceptBlob(http,"Reporting/paymentSense.xls",onSuccess,onFail,"GET REPORTS ERROR", "application/octet-stream");
};

exports.putMenuItem = (http, menuItem, onSuccess, onFail) => {
    this.putter(http, "MenuItem/" + menuItem.Id, menuItem, onSuccess, onFail);
};

exports.putMenuItems = (http, menuItems, onSuccess, onFail) => {
    this.putter(http, "MenuItem/multiple", menuItems, onSuccess, onFail);
};

exports.postMenuItem = (http, menuItem, onSuccess, onFail) => {
    this.poster(http, "MenuItem", menuItem, onSuccess, onFail);
};

exports.putMenu = (http, menu, onSuccess, onFail) => {
    this.putter(http, "Menu/" + menu.Id, menu, onSuccess, onFail);
};

exports.putCategory = (http, category, onSuccess, onFail) => {
    this.putter(http, "MenuCategory/" + category.Id, category, onSuccess, onFail);
};

exports.postCategory = (http, category, onSuccess, onFail) => {
    this.poster(http, "MenuCategory", category, onSuccess, onFail);
};

exports.postMenu = (http, menu, onSuccess, onFail) => {
    this.poster(http, "/Menu", menu, onSuccess, onFail);
};

exports.cloneMenu = (http, menuId, name, onSuccess, onFail) => {
    this.poster(http, "/Menu/clone/" + menuId, {Id:menuId, name:name}, onSuccess, onFail);
};

exports.cloneCategory = (http, categoryId, name, menuId, onSuccess, onFail) => {
    this.poster(http, "/MenuCategory/clone/" + categoryId, {Id:categoryId, name:name, menuId: menuId}, onSuccess, onFail);
};

exports.cloneGroup = (http, groupId, name, categoryId, onSuccess, onFail) => {
    this.poster(http, "/MenuGroup/clone/" + groupId, {Id:groupId, name:name, categoryId: categoryId}, onSuccess, onFail);
};

exports.putModifierGroup = (http, modifierGroup, onSuccess, onFail) => {
    this.putter(http, "/ModifierGroup/" + modifierGroup.Id, modifierGroup, onSuccess, onFail);
};

exports.postModifierGroup = (http, modifierGroup, onSuccess, onFail) => {
    this.poster(http, "/ModifierGroup", modifierGroup, onSuccess, onFail);
};

exports.deleteModifierGroup = (http, modifierGroupId, onSuccess, onFail) => {
    this.deleter(http, "/ModifierGroup/"+modifierGroupId, onSuccess, onFail);
};

exports.deleteMenu = (http, menuId, onSuccess, onFail) => {
    this.deleter(http, "Menu/"+menuId, onSuccess, onFail);
};

exports.deleteCategory = (http, categoryId, onSuccess, onFail) => {
    this.deleter(http, "MenuCategory/"+categoryId, onSuccess, onFail);
};

exports.deleteGroup = (http, groupId, onSuccess, onFail) => {
    this.deleter(http, "MenuGroup/"+groupId, onSuccess, onFail);
};

exports.deleteMenuItem = (http, menuItemId, onSuccess, onFail) => {
    let optionsAuth = {
        headers: {
            "X-Epicuri-API-Version": "1",
            "Accept": "application/json",
            "Content-Type": "application/json",
            "Authorization": this.findLocalToken(authCookie)
        },
        timeout: 5000
    };

    http.delete(targetUrl + "/MenuItem/" + menuItemId, optionsAuth)
        .then(
            response => {
                if (response.status >= 400) {
                    let failString = JSON.stringify(response.data);
                    console.log("Failed status: " + failString);
                    onFail(failString);
                }
                onSuccess(response);
            },
            error => {
                console.log("Error: " + JSON.stringify(error));
                onFail(error.status + ": " + error.body);
            });
};

exports.deleteMenuItems = (http, idArray, onSuccess, onFail) => {
    let optionsAuth = {
        headers: {
            "X-Epicuri-API-Version": "1",
            "Accept": "application/json",
            "Content-Type": "application/json",
            "Authorization": this.findLocalToken(authCookie)
        },
        timeout: 5000
    };

    http.post(targetUrl + "/MenuItem/multiple", {ids: idArray}, optionsAuth)
        .then(
            response => {
                if (response.status >= 400) {
                    let failString = JSON.stringify(response.data);
                    console.log("Failed status: " + failString);
                    onFail(failString);
                }
                onSuccess(response);
            },
            error => {
                console.log("Error: " + JSON.stringify(error));
                onFail(error.status + ": " + error.body);
            });
};

exports.putGroup = (http, group, onSuccess, onFail) => {
    this.putter(http, "MenuGroup/" + group.Id, group, onSuccess, onFail);
};

exports.postGroup = (http, group, onSuccess, onFail) => {
    this.poster(http, "MenuGroup", group, onSuccess, onFail);
};

exports.getStockControl = (http, onSuccess, onFail) => {
    this.getter(http, "StockControl", onSuccess, onFail);
};

exports.putStockControl = (http, stock, onSuccess, onFail) => {
    this.putter(http, "StockControl/"+stock.id, stock, onSuccess, onFail);
};

exports.postStockControl = (http, stock, onSuccess, onFail) => {
    this.poster(http, "StockControl", stock, onSuccess, onFail);
};

exports.deleteStockControl = (http, stock, onSuccess, onFail) => {
    this.deleter(http, "StockControl/"+stock.id, onSuccess, onFail, "");
};

exports.getIntegrations = (http, onSuccess, onFail) => {
    this.getter(http, "integrations", onSuccess, onFail, "");
};

exports.getXeroMappings = (http, onSuccess, onFail) => {
    this.getter(http, "xero/mappings", onSuccess, onFail, "");
};

exports.getXeroAuth = (http, onSuccess, onFail) => {
    this.getter(http, "xero", onSuccess, onFail, "");
};

exports.getXeroEnsureConnectionValidity = (http, onSuccess, onFail) => {
    this.getter(http, "xero/connection", onSuccess, onFail, "");
};

exports.getter = (http, endpoint, onSuccess, onFail, onFailString) => {
    this.getterWithAccept(http, endpoint, onSuccess, onFail, onFailString, "application/json");
};

exports.getterWithAccept = (http, endpoint, onSuccess, onFail, onFailString, acceptString) => {
    let optionsAuth = {
        headers: {
            "X-Epicuri-API-Version": "1",
            "Accept": acceptString,
            "Content-Type": "application/json",
            "Authorization": this.findLocalToken(authCookie)
        },
        timeout: 5000
    };

    http.get(targetUrl + "/" + endpoint, optionsAuth)
        .then(
            response => {
                if (response.status >= 400) {
                    console.log("Failed status: " + JSON.stringify(response.data));
                    onFail(onFailString);
                }
                onSuccess(response);
            },
            error => {
                console.log("Error: " + JSON.stringify(error));
                onFail(error.status + ": " + error.body);
            });
};

exports.getterWithAcceptBlob = (http, endpoint, onSuccess, onFail, onFailString, acceptString) => {
    let optionsAuth = {
        headers: {
            "X-Epicuri-API-Version": "1",
            "Accept": acceptString,
            "Content-Type": "application/json",
            "Authorization": this.findLocalToken(authCookie)
        },
        timeout: 5000,
        responseType: "Blob"
    };

    http.get(targetUrl + "/" + endpoint, optionsAuth)
        .then(
            response => {
                if (response.status >= 400) {
                    console.log("Failed status: " + JSON.stringify(response.data));
                    onFail(onFailString);
                }
                onSuccess(response);
            },
            error => {
                console.log("Error: " + JSON.stringify(error));
                onFail(error.status + ": " + error.body);
            });
};

exports.putter = (http, endpoint, payload, onSuccess, onFail, onFailString) => {
    let optionsAuth = {
        headers: {
            "X-Epicuri-API-Version": "1",
            "Accept": "application/json",
            "Content-Type": "application/json",
            "Authorization": this.findLocalToken(authCookie)
        },
        timeout: 5000
    };

    http.put(targetUrl + "/" + endpoint, payload, optionsAuth)
        .then(
            response => {
                if (response.status >= 400) {
                    console.log("Failed status: " + JSON.stringify(response.data));
                    onFail(onFailString);
                }
                onSuccess(response);
            },
            error => {
                console.log("Error: " + JSON.stringify(error));
                onFail(error.status + ": " + error.body);
            });
};

exports.poster = (http, endpoint, payload, onSuccess, onFail, onFailString, tm = 5000) => {
    let optionsAuth = {
        headers: {
            "X-Epicuri-API-Version": "1",
            "Accept": "application/json",
            "Content-Type": "application/json",
            "Authorization": this.findLocalToken(authCookie)
        },
        timeout: tm
    };

    http.post(targetUrl + "/" + endpoint, payload, optionsAuth)
        .then(
            response => {
                if (response.status >= 400) {
                    console.log("Failed status: " + JSON.stringify(response.data));
                    onFail(onFailString);
                }
                onSuccess(response);
            },
            error => {
                console.log("Error: " + JSON.stringify(error));
                onFail(error.status + ": " + error.body);
            });
};

exports.deleter = (http, endpoint, onSuccess, onFail, onFailString) => {
    let optionsAuth = {
        headers: {
            "X-Epicuri-API-Version": "1",
            "Accept": "application/json",
            "Content-Type": "application/json",
            "Authorization": this.findLocalToken(authCookie)
        },
        timeout: 5000
    };

    http.delete(targetUrl + "/" + endpoint, optionsAuth)
        .then(
            response => {
                if (response.status >= 400) {
                    console.log("Failed status: " + JSON.stringify(response.data));
                    onFail(onFailString);
                }
                onSuccess(response);
            },
            error => {
                console.log("Error: " + JSON.stringify(error));
                onFail(error.status + ": " + error.body);
            });
};

exports.getPrinterItems = (http, printerId, onSuccess, onFail, bool) => {

   let optionsAuth = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "application/json",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 5000
   };

  let boolFlag = (bool) ? true : false;

   http.get(targetUrl + "/Kitchen/Orders?printerId=" + printerId  + "&aggregateBySession=" + boolFlag, optionsAuth)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};

exports.getStaffPermissions = (http, onSuccess, onFail) => {

   let optionsAuth = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "application/json",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 5000
   };

   http.get(targetUrl + "/Staff/permissions", optionsAuth)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};
exports.setStaffPermissions = (http, staffData, onSuccess, onFail) => {

   let optionsAuth = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "application/json",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 5000
   };

   http.put(targetUrl + "/Staff/permissions", staffData, optionsAuth)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};

exports.markAsDone = (http, orderId, onSuccess, onFail) => {

   let optionsAuth = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "application/json",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 5000
   };

   http.put(targetUrl + "/Kitchen/Done", orderId, optionsAuth)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};


exports.markAsUnDone = (http, orderId, onSuccess, onFail) => {

   let optionsAuth = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "application/json",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 5000
   };

   http.put(targetUrl + "/Kitchen/Undone", orderId, optionsAuth)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};

exports.markAllAsDone = (http, sessionId, onSuccess, onFail) => {

   let optionsAuth = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "application/json",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 5000
   };

   http.put(targetUrl + "/Kitchen/AllDone", sessionId, optionsAuth)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};

exports.markAllAsUnDone = (http, sessionId, onSuccess, onFail) => {

   let optionsAuth = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "application/json",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 5000
   };

   http.put(targetUrl + "/Kitchen/AllUndone", sessionId, optionsAuth)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};

exports.getBIMetrics = (http, start, end, onSuccess, onFail) => {

   let optionsAuth = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "application/json",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 5000
   };

   http.get(targetUrl + "/BusinessIntelligence/Basic" + "?" + "start=" + start + "&end=" + end, optionsAuth)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};

exports.getReportingMetrics = (http, reportName, start, end, aggregateByPLU, onSuccess, onFail) => {

   let optionsAuth2 = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "text/csv",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 60000
   };

   http.get(targetUrl + "/Reporting/" + reportName + "?" + "start=" + start + "&end=" + end + "&byPLU=" + aggregateByPLU, optionsAuth2)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};

exports.getOpeningHours = (http, type, onSuccess, onFail) => {

   let optionsAuth = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "application/json",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 5000
   };

   http.get(targetUrl + "/Restaurant/OpeningHours?type=" + type, optionsAuth)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};

exports.getAbsoluteClosures = (http, type, onSuccess, onFail) => {

   let optionsAuth = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "application/json",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 5000
   };

   http.get(targetUrl + "/Restaurant/AbsoluteClosures?type=" + type, optionsAuth)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};

exports.sendOpeningHoursSlot = (http, type, payload, onSuccess, onFail) => {

   let optionsAuth = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "application/json",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 5000
   };

   http.put(targetUrl + "/Restaurant/OpeningHours?type=" + type, payload, optionsAuth)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};

exports.sendAbsoluteClosure = (http, type, payload, onSuccess, onFail) => {

   let optionsAuth = {
      headers: {
         "X-Epicuri-API-Version": "1",
         "Accept": "application/json",
         "Content-Type": "application/json",
         "Authorization": this.findLocalToken(authCookie)
      },
      timeout: 5000
   };

   http.put(targetUrl + "/Restaurant/AbsoluteClosures?type=" + type, payload, optionsAuth)
       .then(
           response => {
              if (response.status >= 400) {
                 console.log("Unauthorized access: " + JSON.stringify(response.data));
                 onFail("ERROR - OFFLINE?");
              }
              onSuccess(response);
           },
           error => {
              console.log("Error: " + JSON.stringify(error));
              onFail(error.status + ": " + error.body);
           });
};

exports.findLocalToken = (authCookie) => {
   let name = authCookie + "=";
   let decodedCookie = decodeURIComponent(document.cookie);
   let ca = decodedCookie.split(';');
   for (let i = 0; i < ca.length; i++) {
      let c = ca[i];
      while (c.charAt(0) === ' ') {
         c = c.substring(1);
      }
      if (c.indexOf(name) === 0) {
         return c.substring(name.length, c.length);
      }
   }
   return "";
};

exports.setLocalToken = (token) => {
   let d = new Date();
   d.setTime(d.getTime() + (28 * 24 * 60 * 60 * 1000));
   let expires = "expires=" + d.toUTCString();
   if (token) {
      document.cookie = authCookie + "=" + token + ";" + expires + ";path=/;";
      authToken = token;
   }
};

exports.eraseCookie = (name) => {
   document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:01 GMT;';
};
