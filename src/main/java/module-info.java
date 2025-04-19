module kg.mega.demo9 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens kg.mega.demo9 to javafx.fxml;
    exports kg.mega.demo9;
}