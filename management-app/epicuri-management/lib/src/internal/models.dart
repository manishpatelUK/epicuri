import 'dart:convert';

class RestResponse {
  String responseText;
  int code;
  RestResponse(this.responseText, this.code){}
}

class IdPojo {
  String id;

  IdPojo.fromJson(Map json) {
    id = json["Id"];
  }
}

abstract class IdAble implements EpicuriModel {
}

class EpicuriModel {
  EpicuriModel clone(Map json) => null;
}

class AdjustmentType extends IdAble {
  String name;
  String shortCode;
  String type;
  bool supportsChange;
  bool visible;

  @override
  EpicuriModel clone(Map json) {
    AdjustmentType adjustmentType = new AdjustmentType();
    adjustmentType.name = json['name'];
    adjustmentType.shortCode = json['shortCode'];
    adjustmentType.type = json['type'];
    adjustmentType.supportsChange = json['supportsChange'];
    adjustmentType.visible = json['visible'];
    return adjustmentType;
  }
}

class Types {
  static const String ADJUSTMENT = "uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType";
}