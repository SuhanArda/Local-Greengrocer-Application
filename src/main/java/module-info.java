module com.greengrocer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires MaterialFX;
    requires java.sql;
    requires kernel;
    requires layout;
    requires io;

    opens com.greengrocer to javafx.fxml;
    opens com.greengrocer.controllers to javafx.fxml;
    opens com.greengrocer.models to javafx.base;

    exports com.greengrocer;
    exports com.greengrocer.controllers;
    exports com.greengrocer.models;
    exports com.greengrocer.dao;
    exports com.greengrocer.utils;
}
