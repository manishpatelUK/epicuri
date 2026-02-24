class Printer {
  String id;
  String ip;
  String name;
  String restaurantId;
  String redirectTo;
  String duplicateTo;
  PrinterType printerType;
}

enum PrinterType {
  NONE, STAR_TSP_X, AURES
}