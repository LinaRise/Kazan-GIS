package sample;

import javafx.scene.shape.Circle;

import java.util.List;

class Helper {
  static double earthRadius = 6371210; //meters
  static double f = 298.257222100;

  static double getDistance(double x, double y, double x2, double y2) {


    double dLat = Math.toRadians(x2 - x);
    double dLng = Math.toRadians(y2 - y);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(x)) * Math.cos(Math.toRadians(x2)) *
                    Math.sin(dLng / 2) * Math.sin(dLng / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return (earthRadius * c);
  }


  static double getSquare(List<Circle> circlesList, double zoomLevel, double ratioCalculation) {
    double result = 0;
    for (int i = 0; i < circlesList.size() - 1; i++) {
      result += circlesList.get(i).getCenterX() * circlesList.get(i + 1).getCenterY();
    }
    result += circlesList.get(circlesList.size() - 1).getCenterX() * circlesList.get(0).getCenterY();
    for (int i = 1; i < circlesList.size(); i++) {
      result -= circlesList.get(i).getCenterX() * circlesList.get(i - 1).getCenterY();
    }
    result -= circlesList.get(0).getCenterX() * circlesList.get(circlesList.size() - 1).getCenterY();
    /*деление на 1000 для получения в км
     *
     */

    return Math.abs(result) / 2d / 22.5d / (zoomLevel * zoomLevel) * ratioCalculation / 1000d;
  }

}


