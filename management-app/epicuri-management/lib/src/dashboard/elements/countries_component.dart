import 'package:angular/angular.dart';
import 'package:angular_components/angular_components.dart';

@Component(
  selector: 'countries-component',
  templateUrl: 'countries_component.html',
  directives: const [
    CORE_DIRECTIVES,
    materialDirectives,
  ]
)
class CountriesComponent {

}