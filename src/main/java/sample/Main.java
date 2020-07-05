package sample;

import com.sun.javafx.geom.Point2D;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main extends Application {
  private static double initx;
  private static double inity;
  private static int height;
  private static int width;
  private static Scene View;
  private static double offSetX;
  private static double offSetY;
  private static double zoomlvl = 1.0;
  private static double const_f;
  private static int const_height;
  private static int const_width;
  private static double distanceValue;

  private static List<Circle> marksList = new ArrayList<>();
  private static List<Double> marksListValue = new ArrayList<>();
  private static List<Line> linesList = new ArrayList<>();
  private static List<Double> linesListCoordinates = new ArrayList<>();

  private final static String pathToMap = "src/main/resources/map2.jpg";
  private final static String pathToMark = "src/main/resources/mark1.png";
  private final static String pathToAero = "src/main/resources/aero.jpg";

  private static ArrayList<Spec> places = new ArrayList<>();
  private static ArrayList<Spec> placesAero = new ArrayList<>();
  private static double oldValue = 1;

  private static int ratioCalculation = 500;


  private static final PseudoClass SELECTED_P_C = PseudoClass.getPseudoClass("selected");

  private final static ObjectProperty<Circle> selectedCircle = new SimpleObjectProperty<>();
  private static Circle firstCircle;

  private final static ObjectProperty<Point2D> selectedLocation = new SimpleObjectProperty<>();

  private static AnchorPane anchorPane = new AnchorPane();
  private static Label distanceValueLabel = new Label();
  private static Label zoomValue = new Label("1.0");

  static class Spec {
    ImageView image1;
    Point point;
    double x = -1;
    double y = -1;
    double offx;
    double offy;

    Spec(ImageView image1, Point point) {
      this.image1 = image1;
      this.point = point;
    }
  }

  //класс для информации о точках из бд
  public static class Point {
    double x;
    double y;
    String info;

    public Point(double x, double y) {
      this.x = x;
      this.y = y;
    }

    Point(double x, double y, String info) {
      this.x = x;
      this.y = y;
      this.info = info;
    }

    public double getX() {
      return x;
    }

    public void setX(double x) {
      this.x = x;
    }

    public double getY() {
      return y;
    }

    public void setY(double y) {
      this.y = y;
    }

    public String getInfo() {
      return info;
    }

    public void setInfo(String info) {
      this.info = info;
    }
  }

  //получение информации о точках из БД
  private static ObservableList<Point> getInfo() {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
      e.printStackTrace();
    }

    ObservableList<Point> newsData = FXCollections.observableArrayList();
    try {
      try (Connection connection = DriverManager.getConnection(Authentication.url, Authentication.user, Authentication.password);
           PreparedStatement preparedStatement = connection.prepareStatement(Authentication.SQL_SELECT)) {
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
          double x = resultSet.getDouble("x");
          double y = resultSet.getDouble("y");
          String info = resultSet.getString("information");
          newsData.add(new Point(x, y, info));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return newsData;
  }

  //получение информации о точках из БД
  private static ObservableList<Point> getMarks() {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
      e.printStackTrace();
    }

    ObservableList<Point> newsData = FXCollections.observableArrayList();
    try {
      try (Connection connection = DriverManager.getConnection(Authentication.url, Authentication.user, Authentication.password);
           PreparedStatement preparedStatement = connection.prepareStatement(Authentication.SQL_SELECT2)) {
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
          double x = resultSet.getDouble("x");
          double y = resultSet.getDouble("y");
          String name = resultSet.getString("name");
          String description = resultSet.getString("description");
          newsData.add(new Point(x, y, name + "<!>" + description));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return newsData;
  }


  private static Line createLine(double x1, double y1, double x2, double y2) {
    Line line = new Line();
    line.setStartX(x1);
    line.setStartY(y1);
    line.setEndX(x2);
    line.setEndY(y2);

    return line;
  }


  //создание точки
  private static Circle createCircle(double x, double y) {
    Circle circle = new Circle(x, y, 4, Color.RED);
    if (marksList.isEmpty()) firstCircle = circle;

    circle.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
              if (e.getClickCount() == 2) {
                selectedCircle.set(circle);

                if (circle == firstCircle && marksList.size() >= 3) {
                  distanceValue += Helper.getDistance(marksListValue.get(marksListValue.size() - 2), marksListValue.get(marksListValue.size() - 1), marksListValue.get(marksListValue.size() - 4), marksListValue.get(marksListValue.size() - 3)) / 1000;
                  //создание и добавление линии
                  Line line = createLine(linesListCoordinates.get(linesListCoordinates.size() - 2), linesListCoordinates.get(linesListCoordinates.size() - 1),
                          x, y);

                  anchorPane.getChildren().add(line);
                  linesList.add(line);
                  distanceValueLabel.setText(String.format(Locale.US, "%.2f", distanceValue) + " км");
                  selectedLocation.set(new Point2D((float) x, (float) y));
                  double square = Helper.getSquare(marksList, zoomlvl, ratioCalculation);
                  Alert alert = new Alert(Alert.AlertType.INFORMATION);
                  alert.setHeaderText("Площадь");
                  alert.setTitle("Вычисление площади ");
                  alert.setContentText("Площадь = " + String.format(Locale.US, "%.4f", square));
                  alert.showAndWait();
                  clean();

                }
              }
            }
    );

    return circle;
  }

  private static boolean isLayoutShown = false;

  private static void showLayout() {
    if (!isLayoutShown)
      for (int i = places.size() - 2; i < places.size(); i++) {
        places.get(i).image1.setVisible(true);
        isLayoutShown = true;
      }
    else {
      for (int i = places.size() - 2; i < places.size(); i++) {
        places.get(i).image1.setVisible(false);
        isLayoutShown = false;
      }
    }
  }

  private static void clean() {
    for (Circle c : marksList) {
      anchorPane.getChildren().remove(c);
    }
    for (Line l : linesList) {
      anchorPane.getChildren().remove(l);
    }
    marksList.removeAll(marksList);
    marksListValue.removeAll(marksListValue);
    linesList.removeAll(linesList);
    linesListCoordinates.removeAll(linesListCoordinates);
    distanceValue = 0;
    distanceValueLabel.setText("");
    firstCircle = null;
  }


  @Override
  public void start(Stage s) {


    selectedCircle.addListener((obs, oldSelection, newSelection) -> {
      if (oldSelection != null) {
        oldSelection.pseudoClassStateChanged(SELECTED_P_C, false);
      }
      if (newSelection != null) {
        newSelection.pseudoClassStateChanged(SELECTED_P_C, true);

      }
    });

    initView();
    s.setScene(View);
    s.show();
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Информация");
    alert.setHeaderText("Инструкция :");
    alert.setContentText("Для просмотра координаты точки, кликните  по карте 1 раз. Координаты отобразятся в верхней части окна.\n" +
            "Для просмотра информации о месте, выделенном меткой, кликните на метку 1 раз. Информация появится в всплывающем окне.\n" +
            "Для расчета расстояния между точками, выделите нужное количество точек двойным кликом по карте. " +
            "Расстояние отобразится в верхней части окна.\n" +
            "Для расчета площади фигуры, выделите нужное количество точек двойным кликом по карте " +
            "и дважды кликните на самую первую точку. Площадь отобразится в всплывающем окне.\n" +
            "Для очистки карты от точек нажмите кнопку \"Очистить\" в нижней части экрана.\n" +
            "Масштабирование производится посредстовм ползунка в нижней части окна.\n" +
            "Премещение по карте происходит как при помощи ползунков сверху и справа от карты, " +
            "так и при помощи зажатия карты кнопокой мышки и перетаскивания в нужном направлении.\n" +
            "Для показа дополнительного слоя нажмите кнопку \"Слой\"."+
            "\n");

    alert.showAndWait();

  }

  private static void initView() {


    VBox root = new VBox(20);
    anchorPane.getChildren().add(root);
    root.setAlignment(Pos.CENTER);


//label путь к картинке карты отображение
    Label title = new Label(pathToMap.substring(pathToMap.lastIndexOf("\\") + 1));
    //получаем картинки карты и метки
    Image source = null;
    Image sourceMark = null;
    Image sourceAero = null;
    try {
      source = new Image(new FileInputStream(pathToMap));
      sourceMark = new Image(new FileInputStream(pathToMark));
      sourceAero = new Image(new FileInputStream(pathToAero));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }


    ImageView imageView = new ImageView(source);
    double ratio = source.getWidth() / source.getHeight();

    //подгон картинки
    if (ratioCalculation / ratio < ratioCalculation) {
      width = ratioCalculation;
      height = (int) (ratioCalculation / ratio);
      const_f = source.getWidth() / ratioCalculation;
    } else if (ratioCalculation * ratio < ratioCalculation) {
      height = ratioCalculation;
      width = (int) (ratioCalculation * ratio);
      const_f = source.getHeight() / ratioCalculation;
    } else {
      height = ratioCalculation;
      width = ratioCalculation;
      const_f = source.getWidth() / ratioCalculation;
    }
    imageView.setPreserveRatio(false);
    imageView.setFitWidth(width);
    imageView.setFitHeight(height);
    height = (int) source.getHeight();
    width = (int) source.getWidth();
    const_height = height;
    const_width = width;
    HBox zoom = new HBox(10);
    zoom.setAlignment(Pos.CENTER);


    Slider zoomLvl = new Slider();
    zoomLvl.setMax(4);
    zoomLvl.setMin(1);
    zoomLvl.setMaxWidth(200);
    zoomLvl.setMinWidth(200);
    Label hint = new Label("Масштаб");


    offSetX = width / 2;
    offSetY = height / 2;


    Slider Hscroll = new Slider();
    Hscroll.setMin(0);
    Hscroll.setMax(width);
    Hscroll.setMaxWidth(imageView.getFitWidth());
    Hscroll.setMinWidth(imageView.getFitWidth());
    Hscroll.setTranslateY(-20);
    Slider Vscroll = new Slider();
    Vscroll.setMin(0);
    Vscroll.setMax(height);
    Vscroll.setMaxHeight(imageView.getFitHeight());
    Vscroll.setMinHeight(imageView.getFitHeight());
    Vscroll.setOrientation(Orientation.VERTICAL);
    Vscroll.setTranslateX(10);


    //бокс соторражением координат
    HBox coordinatesBox = new HBox(10);
    coordinatesBox.setAlignment(Pos.TOP_CENTER);


    Label distance = new Label("Расстояние: ");

    Label coordinatesAre = new Label("Координаты: ");
    Label xLabel = new Label();
    Label yLabel = new Label();
    coordinatesBox.getChildren().addAll(distance, distanceValueLabel, coordinatesAre, xLabel, yLabel);


    //кнопка очистки всех координат
    Button buttonRemoveAllCoordinates = new Button("Очистить");
    buttonRemoveAllCoordinates.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> clean());

    Button showNewLayout = new Button("Слой");
    showNewLayout.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> showLayout());

    zoom.getChildren().

            addAll(showNewLayout, hint, zoomLvl, zoomValue, buttonRemoveAllCoordinates);

    BorderPane borderPane = new BorderPane();
    BorderPane.setAlignment(Hscroll, Pos.CENTER);
    BorderPane.setAlignment(Vscroll, Pos.CENTER_LEFT);


    Hscroll.valueProperty().

            addListener(e ->

            {
              offSetX = Hscroll.getValue();
              zoomlvl = zoomLvl.getValue();
              double newValue = (double) ((int) (zoomlvl * 10)) / 10;
              zoomValue.setText(newValue + "");
//      чтобы картинка не двигалась дальше справа
              if (offSetX < (width / newValue) / 2) {
                offSetX = (width / newValue) / 2;
              }
              if (offSetX > width - ((width / newValue) / 2)) {
                offSetX = width - ((width / newValue) / 2);
              }

              //imageView.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2), offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
              //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
              imageView.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2), offSetY - ((height / newValue) / 2), width / newValue, height / newValue));

              for (Spec place : places) {
                if (place.x == -1 || place.y == -1) {
                  place.x = (place.image1.getX() - 38 - offSetX / const_f + 10);
                  place.y = (place.image1.getY() - 105 - offSetY / const_f + 30);
                  place.offx = offSetX;
                  place.offy = offSetY;
                }
              }
              for (Spec place : places) {
                double xx = (place.offx / const_f + (place.offx - offSetX) / const_f * newValue) + place.x * (newValue) + 38 - 10;
                place.image1.setX(xx);
                double yy = ((place.offy / const_f + (place.offy - offSetY) / const_f * newValue) + place.y * (newValue) + 105 - 30);
                place.image1.setY(yy);
              }

              clean();
            });


    Vscroll.valueProperty().

            addListener(e ->

            {
              offSetY = height - Vscroll.getValue();
              zoomlvl = zoomLvl.getValue();
              double newValue = (double) ((int) (zoomlvl * 10)) / 10;
              zoomValue.setText(newValue + "");
              //чтобы картинка не двигалась дальше справа
              if (offSetY < (height / newValue) / 2) {
                offSetY = (height / newValue) / 2;
              }
              //чтобы картинка не двигалась дальше справа
              //нижняя
              if (offSetY > height - ((height / newValue) / 2)) {
                offSetY = height - ((height / newValue) / 2);
              }
              //imageView.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2), offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
              imageView.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2), offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
              for (Spec place : places) {
                if (place.x == -1 || place.y == -1) {
                  place.x = (place.image1.getX() - 38 - offSetX / const_f + 10);
                  place.y = (place.image1.getY() - 105 - offSetY / const_f + 30);
                  place.offx = offSetX;
                  place.offy = offSetY;
                }
              }
              for (Spec place : places) {
                place.image1.setX((place.offx / const_f + (place.offx - offSetX) / const_f * newValue) + place.x * (newValue) + 38 - 10);
                place.image1.setY((place.offy / const_f + (place.offy - offSetY) / const_f * newValue) + place.y * (newValue) + 105 - 30);
              }

              clean();
            });


//установка в центр картинки
    borderPane.setCenter(imageView);
    borderPane.setTop(Hscroll);
    borderPane.setRight(Vscroll);
    zoomLvl.valueProperty().

            addListener(e ->

            {
              zoomlvl = zoomLvl.getValue();
              double newValue = (double) ((int) (zoomlvl * 10)) / 10;
              zoomValue.setText(newValue + "");
              if (offSetX < (width / newValue) / 2) {
                offSetX = (width / newValue) / 2;
              }
              if (offSetX > width - ((width / newValue) / 2)) {
                offSetX = width - ((width / newValue) / 2);
              }
              if (offSetY < (height / newValue) / 2) {
                offSetY = (height / newValue) / 2;
              }
              if (offSetY > height - ((height / newValue) / 2)) {
                offSetY = height - ((height / newValue) / 2);
              }
              Hscroll.setValue(offSetX);
              Vscroll.setValue(height - offSetY);

              imageView.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2), offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
              //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
              for (Spec place : places) {
                if (place.x == -1 || place.y == -1) {
                  place.x = (place.image1.getX() - 38 - offSetX / const_f + 10);
                  place.y = (place.image1.getY() - 105 - offSetY / const_f + 30);
                  place.offx = offSetX;
                  place.offy = offSetY;
                }
              }
              for (Spec place : places) {
                if ((newValue != oldValue)) {
                  double xx = (place.offx / const_f + (place.offx - offSetX) / const_f * newValue) + place.x * (newValue) + 38 - 10;
                  place.image1.setX(xx);
                  double yy = ((place.offy / const_f + (place.offy - offSetY) / const_f * newValue) + place.y * (newValue) + 105 - 30);
                  place.image1.setY(yy);
                }
              }
              oldValue = newValue;

              clean();
            });
    borderPane.setCursor(Cursor.DEFAULT);


    //при нажатии
    imageView.setOnMousePressed(e ->

    {
      initx = e.getSceneX();
      inity = e.getSceneY();
      borderPane.setCursor(Cursor.CROSSHAIR);
    });

    //мышка после нажатия
    imageView.setOnMouseReleased(e ->

            borderPane.setCursor(Cursor.DEFAULT));

    //при перетаскивании
    imageView.setOnMouseDragged(e ->

    {
      Hscroll.setValue(Hscroll.getValue() + (initx - e.getSceneX()));
      Vscroll.setValue(Vscroll.getValue() - (inity - e.getSceneY()));
      initx = e.getSceneX();
      inity = e.getSceneY();

//убираем метки при перемещении карты
      clean();

    });


    imageView.setOnMouseClicked(event ->

    {

      double center_x = imageView.getFitWidth() / 2;
      double center_y = imageView.getFitHeight() / 2;
      double delta_x = center_x - event.getX();
      double delta_y = center_y - event.getY();


      double real_coordinate_x = offSetX - delta_x * const_f / zoomLvl.getValue();
      double real_coordinate_y = offSetY - delta_y * const_f / zoomLvl.getValue();
      int startx_grad = 49;
      int starty_grad = 55;
      int startx_minute = 0;
      int starty_minute = 20;
      int startx_second = 0;
      int starty_second = 0;
      int intervalXminute = 60;
      int intervalYminute = 40;

      double sootX = real_coordinate_x / const_width;
      double sootY = ((const_height - real_coordinate_y) / const_height);

      double itogX = startx_grad * 3600 + startx_minute * 60 + startx_second;
      double itogY = starty_grad * 3600 + starty_minute * 60 + starty_second;

      itogX = (itogX + (intervalXminute * 60) * sootX);

      int xgrad = (int) (itogX / 3600);
      itogX = itogX % 3600;
      int xmin = (int) (itogX / 60);
      itogX = itogX % 60;
      int xsec = (int) (itogX * 0.6);

      itogY = (itogY + (intervalYminute * 60) * sootY);

      int ygrad = (int) (itogY / 3600);
      itogY = itogY % 3600;
      int ymin = (int) (itogY / 60);
      itogY = itogY % 60;
      int ysec = (int) (itogY * 0.6);


      String ygradCalculated = xgrad + "." + String.format(Locale.US, "%.4f", (double) xmin / 60 + (double) xsec / 3600).split("\\.")[1] + "°";
      String xgradCalculated = ygrad + "." + String.format(Locale.US, "%.4f", (double) ymin / 60 + (double) ysec / 3600).split("\\.")[1] + "°";


      double yForDistance = Double.parseDouble(xgrad + "." + String.format(Locale.US, "%.4f", (double) xmin / 60 + (double) xsec / 3600).split("\\.")[1]);
      double xForDistance = Double.parseDouble(ygrad + "." + String.format(Locale.US, "%.4f", (double) ymin / 60 + (double) ysec / 3600).split("\\.")[1]);


      xLabel.setText(xgradCalculated);
      yLabel.setText(ygradCalculated);


      //двойной клик стаит точку и начинает рассчитывать расстояния, а также идет прорисовка линий
      if (event.getClickCount() == 2) {
        //добавление кружка
        Circle c = createCircle(event.getSceneX(), event.getSceneY());
        anchorPane.getChildren().add(c);
        marksList.add(c);

        //координаты для рсчета дистанций
        marksListValue.add(xForDistance);
        marksListValue.add(yForDistance);

        //координаты для создания линиии между точками
        linesListCoordinates.add(event.getSceneX());
        linesListCoordinates.add(event.getSceneY());


        //если добавлено больше 1 точки, то начанием чертить линии и считать расстояние
        if (marksListValue.size() > 2) {
          //расчитываем дистанции
          distanceValue += Helper.getDistance(marksListValue.get(marksListValue.size() - 2), marksListValue.get(marksListValue.size() - 1), marksListValue.get(marksListValue.size() - 4), marksListValue.get(marksListValue.size() - 3)) / 1000;
          //создание и добавление линии
          Line line = createLine(linesListCoordinates.get(linesListCoordinates.size() - 4), linesListCoordinates.get(linesListCoordinates.size() - 3),
                  linesListCoordinates.get(linesListCoordinates.size() - 2), linesListCoordinates.get(linesListCoordinates.size() - 1));
          anchorPane.getChildren().add(line);
          linesList.add(line);
          distanceValueLabel.setText(String.format(Locale.US, "%.2f", distanceValue) + " км");
        }

      }

    });
    root.setLayoutX(35);
    root.getChildren().

            addAll(title, coordinatesBox, borderPane, zoom);


    ObservableList<Point> mapObjects = getInfo();
    for (
            int i = 0; i < mapObjects.size() - 2; i++) {
      ImageView imageViewMark = new ImageView(sourceMark);
      places.add(new Spec(imageViewMark, mapObjects.get(i)));
      anchorPane.getChildren().add(imageViewMark);
      imageViewMark.setFitHeight(15);
      imageViewMark.setFitHeight(15);
      imageViewMark.setPreserveRatio(true);
      imageViewMark.toFront();
      double center_x = imageView.getFitWidth() / 2 - 10;
      double center_y = imageView.getFitHeight() / 2 + 75;

      double yc = (mapObjects.get(i).x - 55.333333333333) / 0.666666666667 + 0.03;
      double xc = (mapObjects.get(i).y - 49.0) - 0.01;
      places.get(i).image1.setX(35 + xc * width / const_f);
      places.get(i).image1.setY(85 + (height / const_f - (yc * height / const_f)));
    }

    for (int i = mapObjects.size() - 2; i < mapObjects.size(); i++) {
      ImageView imageViewMark = new ImageView(sourceAero);
      places.add(new Spec(imageViewMark, mapObjects.get(i)));
      anchorPane.getChildren().add(imageViewMark);
      imageViewMark.setFitHeight(15);
      imageViewMark.setFitHeight(15);
      imageViewMark.setPreserveRatio(true);
      imageViewMark.toFront();
      imageViewMark.setVisible(false);
      double center_x = imageView.getFitWidth() / 2 - 10;
      double center_y = imageView.getFitHeight() / 2 + 75;
      double yc = (mapObjects.get(i).x - 55.333333333333) / 0.666666666667 + 0.03;
      double xc = (mapObjects.get(i).y - 49.0) - 0.01;
      places.get(i).image1.setX(35 + xc * width / const_f);
      places.get(i).image1.setY(85 + (height / const_f - (yc * height / const_f)));
    }


    //размещение меток из базы данных
    for (int i = 0; i < places.size(); i++) {
      int finalI = i;
      places.get(i).image1.setOnMouseClicked(event -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Описание");
        String[] lines = places.get(finalI).point.info.split("<!>");
        if (lines.length == 1) {
          alert.setHeaderText(places.get(finalI).point.info);
          alert.setContentText("\n" + "Координаты: " + places.get(finalI).point.x + "; " + places.get(finalI).point.y);
        }
        if (lines.length != 1) {
          alert.setHeaderText(lines[0]);
          alert.setContentText(lines[1] + "\n" + "Координаты: " + places.get(finalI).point.x + "; " + places.get(finalI).point.y);
        }

        xLabel.setText(String.valueOf(places.get(finalI).point.x));
        yLabel.setText(String.valueOf(places.get(finalI).point.y));
        alert.showAndWait();
      });

      places.get(i).image1.setCursor(Cursor.OPEN_HAND);
      places.get(i).image1.setOnMouseReleased(e -> imageView.setCursor(Cursor.DEFAULT));
    }

    View = new Scene(anchorPane, (imageView.getFitWidth()) + 70, (imageView.getFitHeight()) + 150);


  }


  public static void main(String[] args) {
    launch(args);
  }
}