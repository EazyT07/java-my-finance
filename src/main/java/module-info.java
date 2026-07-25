module MyFinance {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.prefs;

    exports com.financeapp;
    opens  com.financeapp to javafx.graphics, javafx.fxml;
}