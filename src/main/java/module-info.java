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
    opens com.localgreengrocer.controllers to javafx.fxml;
    opens com.localgreengrocer.models to javafx.base;

    exports com.greengrocer;
    exports com.localgreengrocer.controllers;
    exports com.localgreengrocer.models;
    exports com.localgreengrocer.dao;
    exports com.localgreengrocer.utils;
}
