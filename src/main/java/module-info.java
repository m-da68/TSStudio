module ru.tsconnect.tsstudio {
    requires javafx.controls;
    requires javafx.fxml;
    requires jssc;
    requires java.prefs;
    requires com.install4j.runtime;


    opens ru.tsconnect.tsstudio to javafx.fxml;
    exports ru.tsconnect.tsstudio;
}