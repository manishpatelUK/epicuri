import 'dart:async';
import 'package:angular/angular.dart';
import 'package:angular_components/angular_components.dart';
import 'package:angular_forms/angular_forms.dart';

import '../../internal/models.dart';
import '../services/getter_service.dart';

@Component(
  selector: 'adjustments-component',
  templateUrl: 'adjustment_types_component.html',
  styleUrls: const ['../css/common.css','../css/adjustment_types.css'],
  directives: const [
    CORE_DIRECTIVES,
    materialDirectives,
    formDirectives
  ],
  providers: const [RESTService, materialProviders]
)
class AdjustmentsComponent implements OnInit {
  final RESTService _restService;
  List<AdjustmentType> adjustmentTypes;
  AdjustmentType selected;

  AdjustmentsComponent(this._restService){}

  Future<Null> scheduleGet() async {
    adjustmentTypes = await _restService.getListOf(Types.ADJUSTMENT, new AdjustmentType());
  }

  bool adjustmentSelected() {
    return selected != null;
  }

  @override
  ngOnInit() {
    scheduleGet();
  }
}