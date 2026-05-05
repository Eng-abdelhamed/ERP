module com.erp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires org.apache.pdfbox;

    opens com.erp to javafx.graphics, javafx.fxml;
    opens controllers to javafx.fxml;
    opens models to javafx.base; // Required for TableView PropertyValueFactory
    opens services to javafx.base;
}