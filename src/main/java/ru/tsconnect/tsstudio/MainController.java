package ru.tsconnect.tsstudio;

import javafx.application.Platform;
import javafx.animation.*;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    public Circle btnFullScreen;
    public Circle btnClose;
    public Button btnSettings;
    public BorderPane root;
    public Button btnMenuCameraView;
    public Button btnMenuControls;
    public ImageView cameraFrame;
    public Button btnSettingsPorts;
    public Button btnSettingsCamera;
    public Button btnSettingsWing;
    public AnchorPane paneWing;
    public AnchorPane paneCamera;
    public AnchorPane panePorts;
    public ChoiceBox settingsFieldCom;
    public ChoiceBox settingsFieldBaudrate;
    public ChoiceBox settingsFieldDatabits;
    public ChoiceBox settingsFieldStopbits;
    public ChoiceBox settingsFieldParity;
    public Button btnSettingsCheck;
    public Button btnSettingsSave;
    public CheckBox doHardwareSerialMode;
    public Button btnSettingsWingRefresh;
    public Button btnSettingsWingDFU;
    public TextField settingsFieldTextBlankMenu;
    public Button btnControlsSend;
    public TextField controlsFieldSendText;
    public ChoiceBox controlsFieldType;
    public AnchorPane settings;
    protected ArrayList<Button> menuBtns = new ArrayList<>();
    protected ArrayList<Button> settingsBtns = new ArrayList<>();
    public AnchorPane paneCameraView;
    public AnchorPane paneControls;
    public AnchorPane paneSettings;
    public ButtonBar navBtns;
    private double xOffset;
    private double yOffset;
    public BorderPane logo_bp;
    public static SerialBus serial;
    public static boolean doOpenSettings = false;
    public boolean doPortConfirm = false;

    protected ObservableList<String> portsAvailable = FXCollections.observableArrayList(serial.getPortList());

//    private final InvalidationListener listener = newValue -> {
//        if (newValue != null) {
//            enterSettingsBtn();
//            showPane(btnSettings);
//            btnSettings.heightProperty().removeListener(this.listener);
//        }
//    };

    @FXML
    public void initialize() {
        btnMenuCameraView.setUserData(0);
        btnMenuControls.setUserData(0);
        btnSettings.setUserData(0);
        btnSettingsPorts.setUserData(0);
        btnSettingsCamera.setUserData(0);
        btnSettingsWing.setUserData(0);
        btnSettingsCheck.setUserData(0);
        btnSettingsSave.setUserData(0);
        btnSettingsWingRefresh.setUserData(0);
        btnSettingsWingDFU.setUserData(0);
        btnControlsSend.setUserData(0);
        menuBtns.add(btnMenuCameraView);
        menuBtns.add(btnMenuControls);
        settingsBtns.add(btnSettingsPorts);
        settingsBtns.add(btnSettingsCamera);
        settingsBtns.add(btnSettingsWing);
        btnSettingsSave.setDisable(true);
        doHardwareSerialMode.setSelected(true);

        showPane(btnMenuCameraView);

        Platform.runLater(() -> {
            if (doOpenSettings) {
                btnMenuCameraView.setDisable(true);
                btnMenuControls.setDisable(true);
                btnSettingsWing.setDisable(true);
                btnSettingsCamera.setDisable(true);

                enterSettingsBtn();
                showPane(btnSettings);
            }
        });
        refreshPorts();
        for (String port : portsAvailable) {
            if (port.contains(serial.getCom())) {
                settingsFieldCom.setValue(port);
            }
        }

        controlsFieldType.setItems(FXCollections.observableArrayList("Пакет", "Команда"));
        controlsFieldType.getSelectionModel().selectFirst();

        settingsFieldBaudrate.setItems(FXCollections.observableArrayList(1200,2400,4800,9600,14400,19200,28800,38400,57600,115200,230400));
        settingsFieldBaudrate.setValue(115200);
        settingsFieldDatabits.setItems(FXCollections.observableArrayList(5,6,7,8));
        settingsFieldDatabits.setValue(8);
        settingsFieldStopbits.setItems(FXCollections.observableArrayList(1,2));
        settingsFieldStopbits.setValue(1);
        settingsFieldParity.setItems(FXCollections.observableArrayList("Нет","Нечётный","Чётный","Маркер","Пробел"));
        settingsFieldParity.getSelectionModel().selectFirst();


//        btnSettingsCamera.setDisable(true);
//        btnSettingsWing.setDisable(true);
        logo_bp.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node node = (Node) event.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                if (!stage.isFullScreen()) {
                    xOffset = stage.getX() - event.getScreenX();
                    yOffset = stage.getY() - event.getScreenY();
                }
            }
        });
        logo_bp.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node node = (Node) event.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                if (!stage.isFullScreen()) {
                    stage.setX(event.getScreenX() + xOffset);
                    stage.setY(event.getScreenY() + yOffset);
                }
            }
        });
        settingsFieldCom.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                refreshPorts();
                if (btnSettingsCheck.getUserData().equals(1)) {
                    btnSettingsCheck.setUserData(0);
                    exitMenuBtn(btnSettingsCheck);
                }
                btnSettingsSave.setDisable(true);
                btnSettingsCheck.setUserData(0);

                btnMenuCameraView.setDisable(true);
                btnMenuControls.setDisable(true);
                btnSettingsWing.setDisable(true);
                btnSettingsCamera.setDisable(true);
            }
        });
    }

    @FXML
    protected void close() throws SerialPortException {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        if (serial.isOpened()) {
            serial.sendSND("BYE");
            serial.sendCMD("BLANK");
            serial.close();
        }
        stage.close();
    }

    @FXML
    protected void fullScreen() throws SerialPortException {
        Stage stage = (Stage) btnFullScreen.getScene().getWindow();
        double old_w = stage.getWidth();
        double old_h = stage.getHeight();
        double wf = cameraFrame.getFitWidth();
        double hf = cameraFrame.getFitHeight();

//        serial.sendSND("FullScreen");

        if (stage.isFullScreen()) {
            stage.setFullScreen(false);
            logo_bp.setCursor(Cursor.MOVE);
            Rectangle rect = new Rectangle(900,600);
            rect.setArcHeight(16);
            rect.setArcWidth(16);
            stage.getScene().setFill(Color.TRANSPARENT);
            stage.getScene().getRoot().setClip(rect);
            cameraFrame.setFitWidth(stage.getWidth() - (old_w - wf));
            cameraFrame.setFitHeight(stage.getHeight() - (old_h - hf));

        } else {
            stage.setFullScreen(true);
            logo_bp.setCursor(Cursor.DEFAULT);
            stage.getScene().getRoot().setClip(null);

            cameraFrame.setFitWidth(wf + stage.getWidth() - old_w);
            cameraFrame.setFitHeight(hf + stage.getHeight() - old_h);
        }
    }

/*
    !=== Pain НАСТРОЙКИ ===!
 */

    protected void refreshPorts() {
        portsAvailable = FXCollections.observableArrayList(serial.getPortList());
        for (String port : portsAvailable) {
            if (port.contains(serial.getCom())) {
                portsAvailable.set(portsAvailable.indexOf(serial.getCom()), serial.getCom() + " (Wing)");
            }
        }
        settingsFieldCom.setItems(portsAvailable);
    }

    @FXML
    public void savePortSettings(ActionEvent actionEvent) {
        btnMenuCameraView.setDisable(false);
        btnMenuControls.setDisable(false);
        btnSettingsCamera.setDisable(false);
        for (String port : portsAvailable) {
            if (port.contains(serial.getCom())) {
                btnSettingsWing.setDisable(false);
            }
        }
    }

    @FXML
    protected void checkPortSettings(Event event) throws SerialPortException {
        if (btnSettingsCheck.getUserData().equals(1)) {
            btnSettingsCheck.setUserData(0);
            exitMenuBtn(btnSettingsCheck);
        }
        if (serial.isOpened()) serial.close();
        if (serial.open(serial.getCom((String) settingsFieldCom.getValue()))) {
            serial.settings(
                    (Integer) settingsFieldBaudrate.getValue(), (Integer) settingsFieldDatabits.getValue(),
                    (Integer) settingsFieldStopbits.getValue(),settingsFieldParity.getSelectionModel().getSelectedIndex()
            );
            serial.setHardwareMode(doHardwareSerialMode.isSelected());
            serial.addEventListener(new PortReaderSetCheck(), SerialPort.MASK_RXCHAR);
            serial.sendCMD("STATUS");

            new Timeline(new KeyFrame(
                    Duration.millis(2000),
                    ae -> {
                        if (!doPortConfirm) {
                            setStatusSettingsCheck(false);
                        }
                    }))
                    .play();
        } else setStatusSettingsCheck(false);
    }

    public void setStatusSettingsCheck(boolean status) {
        if (status) {
            btnSettingsSave.setDisable(false);
            btnSettingsCheck.setUserData(1);
        } else  {
            btnSettingsCheck.setUserData(1);
            btnSettingsSave.setDisable(true);
            new Timeline(new KeyFrame(
                    Duration.millis(100),
                    ae -> {
                        btnSettingsCheck.setStyle("-fx-background-color: #FF0048; -fx-border-color: #FF0048; -fx-text-fill: white; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
                    }))
                    .play();
        }
    }

    public void settingsWingRefresh(ActionEvent actionEvent) throws SerialPortException {
        serial.sendCMD("SHOW " + settingsFieldTextBlankMenu.getText());
    }

    public void wingDFU(ActionEvent actionEvent) throws SerialPortException {
        serial.sendCMD("DFU");
    }

    private class PortReaderSetCheck implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    String data = serial.readString(event.getEventValue());
                    if (!data.isEmpty() & !data.equals(" ") & !data.equals("\n")) {
                        if (data.contains("OK:TSTelemetry Command Wing by Dmitriy Gusev")) {
                            doPortConfirm = true;
                            new Timeline(new KeyFrame(
                                    Duration.millis(10),
                                    ae -> {
                                        setStatusSettingsCheck(true);
                                    }))
                                    .play();
                        } else {
                            new Timeline(new KeyFrame(
                                    Duration.millis(10),
                                    ae -> {
                                        setStatusSettingsCheck(false);
                                    }))
                                    .play();

                        }
                        try {
                            serial.removeEventListener();
                        } catch (SerialPortException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }
    }

/*
    !=== ANIMATIONS BEGIN ===!
 */

    @FXML
    protected void enterSettingsBtn() {
        if (btnSettings.getUserData().equals(0)) {
            KeyValue k1 = new KeyValue(btnSettings.prefHeightProperty(), btnSettings.getHeight());
            KeyValue kv = new KeyValue(btnSettings.prefHeightProperty(), btnSettings.getHeight() + 10);
            KeyFrame kf1 = new KeyFrame(Duration.millis(100), k1, kv);
            ObjectProperty<Color> baseColor = new SimpleObjectProperty<>();
            KeyValue keyValue1 = new KeyValue(baseColor, Color.WHITE);
            KeyFrame keyFrame1 = new KeyFrame(Duration.ZERO, keyValue1);
            KeyValue keyValue2 = new KeyValue(baseColor, Color.web("#569AFF"));
            KeyFrame keyFrame2 = new KeyFrame(Duration.millis(200), keyValue2);
            Timeline timeline = new Timeline(keyFrame1, keyFrame2);

            baseColor.addListener((obs, oldColor, newColor) -> {
                btnSettings.setStyle(String.format("-fx-background-color: #%s; -fx-border-color: #569AFF; -fx-text-fill: white; -fx-border-width: 2 2 0 2; -fx-border-radius: 10 10 0 0; -fx-background-radius: 10 10 0 0;",
                        (newColor.toString().split("x")[1])));
            });

            timeline.play();
            Timeline tl = new Timeline();
            tl.getKeyFrames().add(kf1);
            tl.play();
        }
    }

    @FXML
    protected void exitSettingsBtn() {
        if (btnSettings.getUserData().equals(0)) {
            KeyValue k1 = new KeyValue(btnSettings.prefHeightProperty(), btnSettings.getHeight());
            KeyValue kv = new KeyValue(btnSettings.prefHeightProperty(), 23);
            KeyFrame kf1 = new KeyFrame(Duration.millis(100), k1, kv);
            Timeline tl = new Timeline();
            tl.getKeyFrames().add(kf1);
            tl.play();

            ObjectProperty<Color> baseColor = new SimpleObjectProperty<>();
            KeyValue keyValue1 = new KeyValue(baseColor, Color.web("#569AFF"));
            KeyFrame keyFrame1 = new KeyFrame(Duration.ZERO, keyValue1);
            KeyValue keyValue2 = new KeyValue(baseColor, Color.WHITE);
            KeyFrame keyFrame2 = new KeyFrame(Duration.millis(200), keyValue2);
            Timeline timeline = new Timeline(keyFrame1, keyFrame2);

            baseColor.addListener((obs, oldColor, newColor) -> {
                btnSettings.setStyle(String.format("-fx-background-color: #%s; -fx-border-color: #569AFF; -fx-text-fill: black; -fx-border-width: 2 2 0 2; -fx-border-radius: 10 10 0 0; -fx-background-radius: 10 10 0 0;",
                        (newColor.toString().split("x")[1])));
            });

            timeline.play();
        }
    }

    @FXML
    protected void enterMenuBtn(MouseEvent event) {
        Node node = (Node) event.getSource();
        enterMenuBtn(node);
    }

    protected void enterMenuBtn(Node node) {
        if (node.getUserData().equals(0)) {
            ObjectProperty<Color> baseColor = new SimpleObjectProperty<>();
            KeyValue keyValue1 = new KeyValue(baseColor, Color.WHITE);
            KeyFrame keyFrame1 = new KeyFrame(Duration.ZERO, keyValue1);
            KeyValue keyValue2 = new KeyValue(baseColor, Color.web("#569AFF"));
            KeyFrame keyFrame2 = new KeyFrame(Duration.millis(200), keyValue2);
            Timeline timeline = new Timeline(keyFrame1, keyFrame2);

            baseColor.addListener((obs, oldColor, newColor) -> {
                node.setStyle(String.format("-fx-background-color: #%s; -fx-border-color: #569AFF; -fx-text-fill: white; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;",
                        (newColor.toString().split("x")[1])));
            });

            timeline.play();
        }
    }

    @FXML
    protected void exitMenuBtn(MouseEvent event) {
        Node node = (Node) event.getSource();
        exitMenuBtn(node);
    }

    protected void exitMenuBtn(Node node) {
        if (node.getUserData().equals(0)) {
            ObjectProperty<Color> baseColor = new SimpleObjectProperty<>();
            KeyValue keyValue1 = new KeyValue(baseColor, Color.web("#569AFF"));
            KeyFrame keyFrame1 = new KeyFrame(Duration.ZERO, keyValue1);
            KeyValue keyValue2 = new KeyValue(baseColor, Color.WHITE);
            KeyFrame keyFrame2 = new KeyFrame(Duration.millis(200), keyValue2);
            Timeline timeline = new Timeline(keyFrame1, keyFrame2);

            baseColor.addListener((obs, oldColor, newColor) -> {
                node.setStyle(String.format("-fx-background-color: #%s; -fx-border-color: #569AFF; -fx-text-fill: black; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;",
                        (newColor.toString().split("x")[1])));
            });

            timeline.play();
        }
    }

    @FXML
    protected void showPane(Event event) {
        Node node = (Node) event.getSource();
        showPane(node);
    }

    protected void showPane(Node node) {
        if (!node.getUserData().equals(1)) {
            if (btnSettings.getUserData().equals(1)) {
                btnSettings.setUserData(0);
                exitSettingsBtn();
            }
            if (node == btnMenuCameraView) {
                paneCameraView.toFront();
            } else if (node == btnMenuControls) {
                paneControls.toFront();
            } else if (node == btnSettings) {
                showSettingsPane(btnSettingsPorts);
                paneSettings.toFront();
                btnSettings.setUserData(1);
                btnSettings.setStyle("-fx-background-color: #569AFF; -fx-border-color: #569AFF; -fx-text-fill: white; -fx-border-width: 2 2 0 2; -fx-border-radius: 10 10 0 0; -fx-background-radius: 10 10 0 0;");
            }
            for (Button btn : menuBtns) {
                if (btn.getUserData().equals(1)) {
                    btn.setUserData(0);
                    exitMenuBtn(btn);
//                    btn.setStyle("-fx-background-color: white; -fx-border-color: #569AFF; -fx-text-fill: black; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
                }
            }
            if (node != btnSettings) {
                node.setStyle("-fx-background-color: #569AFF; -fx-border-color: #569AFF; -fx-text-fill: white; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
            }
            node.setUserData(1);
            navBtns.toFront();
        }
    }

    @FXML
    protected void showSettingsPane(Event event) {
        Node node = (Node) event.getSource();
        showSettingsPane(node);
    }

    protected void showSettingsPane(Node node) {
        if (!node.getUserData().equals(1)) {
            if (node == btnSettingsPorts) {
                panePorts.toFront();
            } else if (node == btnSettingsCamera) {
                paneCamera.toFront();
            } else if (node == btnSettingsWing) {
                paneWing.toFront();
            }
            for (Button btn : settingsBtns) {
                if (btn.getUserData().equals(1)) {
                    btn.setUserData(0);
                    exitMenuBtn(btn);
                }
//                btn.setStyle("-fx-background-color: white; -fx-border-color: #569AFF; -fx-text-fill: black; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
            }
            node.setStyle("-fx-background-color: #569AFF; -fx-border-color: #569AFF; -fx-text-fill: white; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
            node.setUserData(1);

        }
    }

/*
    !=== ANIMATIONS END ===!
 */

    public void controlsSend(ActionEvent actionEvent) throws SerialPortException {
        if (controlsFieldType.getValue().equals("Пакет")) serial.sendSND(controlsFieldSendText.getText());
        if (controlsFieldType.getValue().equals("Команда")) serial.sendCMD(controlsFieldSendText.getText());
    }




}