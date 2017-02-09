package com.paulganly.sikuli.mapper.main;

import java.io.IOException;

import com.paulganly.sikuli.mapper.model.Options;
import com.paulganly.sikuli.mapper.ui.UiController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application {

	private Stage primaryStage;
	private AnchorPane rootLayout;
	private Options options;

	public MainApp() {
		options = new Options();
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Sikulix Page Mapper");

		initAppLayout();
	}


	public void initAppLayout() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/SikuliMapperAppView.fxml"));
			rootLayout = (AnchorPane) loader.load();

			final UiController controller = loader.getController();
			controller.setMainApp(this);
			controller.initialiseOptions();

			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		launch(args);
	}

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

}
