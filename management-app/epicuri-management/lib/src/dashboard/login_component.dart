import 'dart:convert';

import 'package:angular/angular.dart';
import 'package:angular_components/angular_components.dart';
import 'package:angular_forms/angular_forms.dart';

import '../globals.dart' as globals;
import '../internal/models.dart';

@Component(
  selector: 'login-component',
  templateUrl: 'login_component.html',
  directives: const [
    CORE_DIRECTIVES,
    materialDirectives,
    formDirectives
  ],
  providers: const [materialProviders],
)
class LoginComponent {
  bool showSpinner = false;
  bool showErrorPopup = false;
  String errorString;
  String userName;
  String password;

  LoginComponent() {
    reset();
  }

  void reset([dynamic event]) {
    showSpinner = false;
    showErrorPopup = false;
    errorString = null;
    userName = null;
    password = null;
  }

  void login(dynamic event) {
    showSpinner = true;
    globals.epicuriEnv.login(userName, password, loginSuccess, loginFailure);
  }

  void loginSuccess(String message) {
    showErrorPopup = false;
    showSpinner = false;
    IdPojo idPojo = new IdPojo.fromJson(JSON.decode(message));
    globals.gToken = idPojo.id;
  }

  void loginFailure(String message) {
    showSpinner = false;
    showErrorPopup = true;
    errorString = message;
  }
}