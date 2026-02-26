import 'package:cookie/cookie.dart' as cookie;

import 'internal/epicuri.dart';

Epicuri epicuriEnv = Epicuri.DEV();

set gToken(String token) {
  cookie.set('epicuri.co.uk.admin', token, expires: 1);
}

String get gToken {
  return _getToken();
}


String _getToken() {
  return cookie.get('epicuri.co.uk.admin');
}