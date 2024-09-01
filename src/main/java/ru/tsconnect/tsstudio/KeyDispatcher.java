package ru.tsconnect.tsstudio;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import jssc.SerialPortException;

public class KeyDispatcher {
    private static boolean W_pressed = false;
    private static boolean S_pressed = false;
    private static boolean A_pressed = false;
    private static boolean D_pressed = false;
    private static boolean E_pressed = false;
    private static boolean Q_pressed = false;
    public static void start(MainController controller) {
        Scene scene = controller.root.getScene();
        SerialBus serial = MainController.serial;
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                if (!serial.isOpened()) return;
                switch (ke.getCode()) {
                    case SPACE:
                        System.out.println("HARD STOP");
                        try {
                            serial.sendSND("HS");
                        } catch (SerialPortException e) {
                            throw new RuntimeException(e);
                        }

                        new Timeline(new KeyFrame(
                                Duration.millis(400),
                                ae -> {
                                    System.out.println("HARD STOP Repeat");
                                    try {
                                        serial.sendSND("HS");
                                    } catch (SerialPortException e) {
                                        throw new RuntimeException(e);
                                    }
                                }))
                                .play();
                        break;

                    case W:
                        if (W_pressed) return;
                        controller.setMovStatus(true);
                        System.out.println("movFORWARD");
                        try {
                            serial.sendSND("mF");
                            W_pressed = true;
                        } catch (SerialPortException e) {
                            throw new RuntimeException(e);
                        }
                        break;

                    case S:
                        if (S_pressed) return;
                        controller.setMovStatus(true);
                        System.out.println("movBACK");
                        try {
                            serial.sendSND("mB");
                            S_pressed = true;
                        } catch (SerialPortException e) {
                            throw new RuntimeException(e);
                        }
                        break;



                    case E:
                        if (E_pressed) return;
                        controller.setMovStatus(true);
                        System.out.println("servoRIGHT");
                        try {
                            serial.sendSND("sR");
                            E_pressed = true;
                        } catch (SerialPortException e) {
                            throw new RuntimeException(e);
                        }
                        break;

                    case Q:
                        if (Q_pressed) return;
                        controller.setMovStatus(true);
                        System.out.println("servoLEFT");
                        try {
                            serial.sendSND("sL");
                            Q_pressed = true;
                        } catch (SerialPortException e) {
                            throw new RuntimeException(e);
                        }
                        break;



                    ////

                    case A:
                        if (A_pressed) return;
                        controller.setMovStatus(true);
                        System.out.println("turnLEFT");
                        try {
                            serial.sendSND("tL");
                            A_pressed = true;
                        } catch (SerialPortException e) {
                            throw new RuntimeException(e);
                        }
                        break;

                    case D:
                        if (D_pressed) return;
                        controller.setMovStatus(true);
                        System.out.println("turnRIGHT");
                        try {
                            serial.sendSND("tR");
                            D_pressed = true;
                        } catch (SerialPortException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                }
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                if (!serial.isOpened()) return;
                switch (ke.getCode()) {
                    case W, S:
                        if (!(W_pressed | S_pressed)) return;
                        controller.setMovStatus(false);
                        System.out.println("movSTOP");
                        try {
                            serial.sendSND("mS");
                            W_pressed = false;
                            S_pressed = false;
                        } catch (SerialPortException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case A, D:
                        if (!(A_pressed | D_pressed)) return;
                        controller.setMovStatus(false);
                        System.out.println("turnRESET");
                        try {
                            serial.sendSND("tS");
                            A_pressed = false;
                            D_pressed = false;
                        } catch (SerialPortException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case E, Q:
                        if (!(E_pressed | Q_pressed)) return;
                        controller.setMovStatus(false);
                        System.out.println("servoSTOP");
                        try {
                            serial.sendSND("ss");
                            E_pressed = false;
                            Q_pressed = false;
                        } catch (SerialPortException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                }
            }
        });
//        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
//
//            @Override
//            public boolean dispatchKeyEvent(KeyEvent ke) {
//                synchronized (KeyDispatcher.class) {
//                    System.out.println("n");
//                    if (!serial.isOpened()) return false;
//                    System.out.println("u");
//                    switch (ke.getID()) {
//                        case KeyEvent.KEY_PRESSED:
//                            if (ke.getKeyCode() == KeyEvent.VK_W) {
//                                try {
//                                    System.out.println("mov UP");
//                                    serial.sendSND("mov UP");
//                                } catch (SerialPortException e) {
//                                    throw new RuntimeException(e);
//                                }
//                            }
//                            break;
//
//                        case KeyEvent.KEY_RELEASED:
//                            if (ke.getKeyCode() == KeyEvent.VK_W) {
//                                try {
//                                    System.out.println("mov STOP");
//                                    serial.sendSND("mov STOP");
//                                } catch (SerialPortException e) {
//                                    throw new RuntimeException(e);
//                                }
//                            }
//                            break;
//                        default:
//                            return false;
//                    }
//                    return true;
//                }
//            }
//        });
    }
}
