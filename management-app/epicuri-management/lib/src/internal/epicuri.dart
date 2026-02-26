import 'dart:html';
import 'dart:async';

import 'models.dart';
import '../globals.dart' as globals;

class Epicuri {
  static const String _urlProd = "https://api-prod.epicuri.co.uk";
  static const String _urlDev = "http://api-dev.epicuri.co.uk";

  String mEnv;
  Epicuri(this.mEnv) {

  }

  static Epicuri PROD() {
    return new Epicuri(_urlProd);
  }

  static Epicuri DEV() {
    return new Epicuri(_urlDev);
  }

  void login(String userName, String password, Function onSuccess, Function onFailure) {
    HttpRequest request = _setUpRequest(onSuccess, onFailure);

    request.open("POST", mEnv + "/Authentication/LoginAdmin", async: true);
    request.setRequestHeader("content-type", "application/json");
    request.setRequestHeader("X-Epicuri-API-Version", "3");
    request.setRequestHeader("X-Epicuri-Admin-U", userName);
    request.setRequestHeader("Authorization", password);

    request.send();
  }

  HttpRequest _setUpRequest(Function onSuccess, Function onFailure) {
    HttpRequest request = new HttpRequest();

    request.onReadyStateChange.listen((_){
      if(request.readyState == HttpRequest.DONE && request.status < 400) {
        onSuccess(request.responseText);
      } else {
        onFailure(request.responseText);
      }
    });
    return request;
  }

  HttpRequest _setUpHeaders(HttpRequest request, [accept = "application/json", contentType = "application/json"]) {
    request.setRequestHeader("content-type", contentType);
    request.setRequestHeader("accept", accept);
    request.setRequestHeader("X-Epicuri-API-Version", "3");
    request.setRequestHeader("Authorization", globals.gToken);
    return request;
  }

  Future<RestResponse> get(String type, Function onSuccess) async {
    return await getX(type);
  }

  Future<RestResponse> getWithId(String type, String id) async {
    return await getX(type + "/" + id);
  }

  Future<RestResponse> getX(String className) async {
    HttpRequest request = new HttpRequest();
    request.open("GET", mEnv + "/Management/" + className);
    _setUpHeaders(request);
    request.send();

    await request.onLoadEnd.first;
    return new RestResponse(request.responseText, request.status);
  }
}