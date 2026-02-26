import 'dart:async';

import 'package:angular/angular.dart';
import 'package:angular_components/angular_components.dart';

import 'dashboard_service.dart';

import '../global_aware.dart' as globals;
import 'elements/elements_lib.dart';

@Component(
  selector: 'dashboard-component',
  styleUrls: const ['package:angular_components/app_layout/layout.scss.css','dashboard_component.css'],
  templateUrl: 'dashboard_component.html',
  directives: const [
    CORE_DIRECTIVES,
    materialDirectives,
    AdjustmentsComponent, BookingStaticsComponent, CountriesComponent, CuisinesComponent, DefaultsComponent, PreferencesComponent, RestaurantsComponent, TaxesComponent
  ],
  providers: const [DashboardService],
)
class DashboardComponent extends globals.GlobalAware implements OnInit {
  final DashboardService todoListService;

  List<String> items = [];
  String newTodo = '';

  DashboardComponent(this.todoListService);

  @override
  Future<Null> ngOnInit() async {
    items = await todoListService.getTodoList();
  }

  void add() {
    items.add(newTodo);
    newTodo = '';
  }

  String remove(int index) => items.removeAt(index);
  void onReorder(ReorderEvent e) =>
      items.insert(e.destIndex, items.removeAt(e.sourceIndex));
}
