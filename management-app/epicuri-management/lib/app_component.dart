import 'package:angular/angular.dart';
import 'package:angular_components/angular_components.dart';

import 'src/dashboard/dashboard_component.dart';
import 'src/dashboard/login_component.dart';

import 'src/global_aware.dart';

// AngularDart info: https://webdev.dartlang.org/angular
// Components info: https://webdev.dartlang.org/components

@Component(
  selector: 'management-app',
  styleUrls: const ['app_component.css'],
  templateUrl: 'app_component.html',
  directives: const [CORE_DIRECTIVES, materialDirectives, DashboardComponent, LoginComponent],
  providers: const [materialProviders],
)
class AppComponent extends GlobalAware{
  // Nothing here yet. All logic is in TodoListComponent.
}
