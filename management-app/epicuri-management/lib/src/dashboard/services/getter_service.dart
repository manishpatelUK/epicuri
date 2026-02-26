import 'package:angular/angular.dart';
import 'dart:async';
import 'dart:convert';

import '../../internal/models.dart';
import '../../globals.dart' as globals;

@Injectable()
class RESTService<T extends EpicuriModel> {
  Future<List<T>> getListOf(String type, EpicuriModel template) async {
    RestResponse response = await globals.epicuriEnv.getX(type);
    List<T> data = new List();

    if(response.code >= 400) {
      print("ERROR: " + response.code.toString() + "=>" + response.responseText);
      return data;
    }

    List array  = JSON.decode(response.responseText);
    for(var element in array) {
      data.add(template.clone(element));
    }

    return data;
  }
}