module fr.qmn.mamoucalendari {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.xerial.sqlitejdbc;
    requires tess4j;
    requires lept4j;


    opens fr.qmn.mamoucalendari to javafx.fxml;
    opens fr.qmn.mamoucalendari.controller.visual to javafx.fxml;
    opens fr.qmn.mamoucalendari.controller.calendar to javafx.fxml;
    opens fr.qmn.mamoucalendari.controller.tact to javafx.fxml;
    exports fr.qmn.mamoucalendari;
    exports fr.qmn.mamoucalendari.controller.visual;
    exports fr.qmn.mamoucalendari.controller.calendar;
    exports fr.qmn.mamoucalendari.controller.tact;
}