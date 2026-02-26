import 'package:angular/angular.dart';
import 'package:angular_components/angular_components.dart';

@Component(
  selector: 'defaults-component',
  templateUrl: 'defaults_component.html',
  directives: const [
    CORE_DIRECTIVES,
    materialDirectives,
  ]
)
class DefaultsComponent {

}