import 'package:angular/angular.dart';
import 'package:angular_components/angular_components.dart';

@Component(
  selector: 'restaurants-component',
  templateUrl: 'restaurants_component.html',
  directives: const [
    CORE_DIRECTIVES,
    materialDirectives,
  ]
)
class RestaurantsComponent {

}