package com.paulganly.sikuli.mapper.ui;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.paulganly.sikuli.mapper.main.MainApp;
import com.paulganly.sikuli.mapper.model.GuiElementSelectorItems;
import com.paulganly.sikuli.mapper.model.GuiElementSizeAndPosition;
import com.paulganly.sikuli.mapper.model.Options;

import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class UiController {

	static Logger logger = Logger.getLogger(UiController.class);

	private static final int NUMBER_OF_GRID_ROWS_FOR_SELECTOR_ITEMS = 10;
	private static final int GUI_ITEMS_PIXELS_HEIGHT_IN_SCROLLBAR = 350;
	@SuppressWarnings("unused")
	private MainApp mainApp;
	private ScreenShotSelector screenShotSelector;
	private ScreenShotProcessor screenShotProcessor = new ScreenShotProcessor();
	private int gridPaneNextRow = 0;
	private int elementNumber = 1;
	private List<GuiElementSelectorItems> selectorItems = new ArrayList<>();
	private Insets guiSelectorItemPadding = new Insets(5, 0, 5, 5);

	@FXML
	private TextField guiCreateFolder;
	@FXML
	private Label validationMessage;
	@FXML
	private ImageView screenGrabView;
	@FXML
	private Group imageGroup;
	@FXML
	private TextField pageAreaName;
	@FXML
	private TextField pageAreaTimeout;
	@FXML
	private CheckBox pageAreaHasHeader;
	@FXML
	private Button selectPageHeaderBtn;
	@FXML
	private ImageView pageAreaImage;
	@FXML
	private ImageView pageAreaHeader;
	@FXML
	private GridPane inputsGrid;
	@FXML
	private AnchorPane guiItemsAnchorPane;

	@FXML
	private void initialize() {
		validationMessage.setTextFill(Color.FIREBRICK);
		validationMessage.setFont(Font.font(null, FontWeight.BOLD, 15.0));
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	public void initialiseOptions() {
		this.guiCreateFolder.setText(Options.getGuiCreateFolder());
	}

	@FXML
	private void handleUpdateProperties() {
		Properties properties = new Properties();
		properties.setProperty("GuiCreateFolder", guiCreateFolder.getText());
		Options.saveOptions(properties);

	}

	@FXML
	private void handleResetProperties() {
		initialiseOptions();
	}

	/**
	 * A method that handles the actions to carry out when the user clicks the button to load the
	 * screenshot into the main page area.
	 *
	 * @throws AWTException
	 * @throws IOException
	 */
	@FXML
	public void handleTakeScreenShot() throws AWTException, IOException {

		logger.info("Loading an image from the clipboard to main screenshot");

		BufferedImage clipboardContents = screenShotProcessor.getImageFromClipboard();

		if(clipboardContents != null){

			logger.info("Loading image");

			Image newImg = SwingFXUtils.toFXImage(screenShotProcessor.getImageFromClipboard(), null);
			screenGrabView.setFitWidth(newImg.getWidth());
			screenGrabView.setFitHeight(newImg.getHeight());
			screenGrabView.setImage(newImg);
			validationMessage.setText("");
		}
		else{

			logger.info("Clipboard does not contain an image");
			validationMessage.setText("Clipboard does not contain an image");
		}
	}

	/**
	 * A method that handles the actions to carry out when the user clicks the button to generate the
	 * page area. All the image files, the page.properties file and the java class file are written to
	 * the output folder.
	 *
	 * @throws IOException
	 */
	@FXML
	public void handleGeneratePageArea() throws IOException {

		logger.info("Attempting to generate the page area");

		ArrayList<String> pageProperties = new ArrayList<>();
		File elementOutputFolder = new File(Options.getGuiCreateFolder() + "\\" + pageAreaName.getText());

		if(!elementOutputFolder.exists()){

			logger.info("Making the page area output folder");
			elementOutputFolder.mkdirs();

		}

		try{
			if(pageAreaHasHeader.isSelected() && pageAreaHeader != null){

				logger.info("Writing the page header image to the output folder");

				writeImageToFile(pageAreaHeader.getImage(), elementOutputFolder + "\\" + "pageAreaHeader.png");

			}

			for(GuiElementSelectorItems guiItem : selectorItems){

				logger.info("Writing each of the GUI items images to be created to file");

				if(guiItem.getDirectlySelectedElementButton().isSelected()){

					logger.info("Writing directly selected element :" + guiItem.getElementNameTextField().getText() + " to file");

					writeImageToFile(
					    guiItem.getElementAreaImage().getImage(), elementOutputFolder + "\\" + guiItem.getElementNameTextField().getText() + ".png");
				}
				else{

					logger.info("Writing referenced element :" + guiItem.getElementNameTextField().getText() + " to file");

					writeReferencedElementImagesToFile(guiItem, elementOutputFolder);
					pageProperties.add(guiItem.getElementNameTextField().getText() + "=" + generateReferencedPositions(guiItem));
				}
			}

		} catch(IOException e){
			e.printStackTrace();
		}

		writePageAreaClassFile(elementOutputFolder);
		writeReferencedElementPageProperties(pageProperties, elementOutputFolder);

	}

	/**
	 * A method used to write the lines of the auto generated java class file.
	 *
	 * @param elementOutputFolder the folder to write the class file into
	 * @throws IOException
	 */
	private void writePageAreaClassFile(File elementOutputFolder) throws IOException {

		logger.info("Writing the lines of the auto-generated java class to file");

		List<String> lines = new ArrayList<>();
		lines.add("public class " + pageAreaName.getText() + " implements SikuliPage {");
		lines.add("");
		lines.add("    public static final String IMAGES_FOLDER = \"imgs/xxx\";");
		if(pageAreaHasHeader.isSelected()){
			lines.add("    public static final String PAGE_HEADER = IMAGES_FOLDER + \"pageAreaHeader.png\";");
		}
		lines.add("");
		lines.add("    private SikuliLocatorMap locatorMap;");
		lines.add("");
		lines.add("    public " + pageAreaName.getText() + "() { ");
		lines.add("        locatorMap = new SikuliLocatorMap(IMAGES_FOLDER + \"page.properties\");");
		lines.add("    }");
		if(pageAreaHasHeader.isSelected()){
			lines.add("");
			lines.add("    public Region get" + pageAreaName.getText() + "Screen() {");
			lines.add(
			    "        return SikuliDriver.findRegionInParent(SikuliDriver.getFullscreenRegion(), PAGE_HEADER, " + pageAreaTimeout.getText() + ".0);");
			lines.add("    }");
		}

		for(GuiElementSelectorItems item : selectorItems){
			if(item.getReferencedElementButton().isSelected()){
				lines.add("");
				lines.add("    public Region get" + item.getReferenceAreaNameTextField().getText() + "() {");
				lines.add(
				    "        return SikuliDriver.findRegionOnScrollablePage(PAGE_HEADER, IMAGES_FOLDER + \"" + item.getReferenceAreaNameTextField().getText()
				        + ".png\", 10);");
				lines.add("    }");
				lines.add("");
				lines.add("    public Region get" + item.getElementNameTextField().getText() + "() {");
				lines.add(
				    "        return SikuliDriver.createRelativeRegion(this, locatorMap.getElementPropertiesMap(\"" + item.getElementNameTextField().getText()
				        + "\"));");
				lines.add("    }");
			}
		}

		for(GuiElementSelectorItems item : selectorItems){
			if(item.getDirectlySelectedElementButton().isSelected()){
				lines.add("");
				lines.add("    public Region get" + item.getElementNameTextField().getText() + "() {");
				lines.add(
				    "        return SikuliDriver.findRegionOnScrollablePage(PAGE_HEADER, IMAGES_FOLDER + \"" + item.getElementNameTextField().getText()
				        + ".png\", 10);");
				lines.add("    }");
			}
		}

		lines.add("");
		lines.add("    public Region getReferenceRegionByName(String referenceRegion) { ");
		lines.add("        Region returnedRegion = null; ");
		lines.add("        switch(referenceRegion){ ");
		for(GuiElementSelectorItems item : selectorItems){
			if(item.getReferencedElementButton().isSelected()){
				lines.add("            case \"" + item.getReferenceAreaNameTextField().getText() + "\":");
				lines.add("	               returnedRegion = get" + item.getReferenceAreaNameTextField().getText() + "();");
				lines.add("                break; ");
			}
		}
		lines.add("        }");
		lines.add("        return returnedRegion; ");
		lines.add("    }");
		lines.add("}");

		Files.write(Paths.get(elementOutputFolder + "\\" + pageAreaName.getText() + ".java"), lines);
	}

	/**
	 * A method used to write the page properties file. This file will contain details for all elements that are
	 * selected by referencing other objects on the screen.
	 *
	 * @param properties
	 * @param elementOutputFolder
	 * @throws IOException
	 */
	private void writeReferencedElementPageProperties(List<String> properties, File elementOutputFolder) throws IOException {
		Files.write(Paths.get(elementOutputFolder + "\\" + "page.properties"), properties);
	}

	/**
	 * Creates the colon separated string that will have the position values for the referenced GUI item.
	 * The String will be comprised of:
	 * 1. The reference area name
	 * 2. The width of the area to be created
	 * 3. The distance from the right of the reference area that the created area lies.
	 * 4. The distance from the left of the reference area that the created area lies.
	 * 5. The distance from the top of the reference area that the created area lies.
	 * 6. The distance from the bottom of the reference area that the created area lies.
	 *
	 * @param guiItem
	 * @return the string to be added to the page properties file
	 */
	private String generateReferencedPositions(GuiElementSelectorItems guiItem) {

		logger.info("Position values for page properties file");

		Double width = guiItem.getElementSizeAndPosition().getWidth();
		Double height = guiItem.getElementSizeAndPosition().getHeight();
		Double distanceFromRightOfReference = getDistanceFromRightOfReference(guiItem);
		Double distanceFromLeftOfReference = getDistanceFromLeftOfReference(guiItem);
		Double distanceAboveReferenceRegion = getDistanceAboveReferenceRegion(guiItem);
		Double distanceBelowReferenceRegion = getDistanceBelowReferenceRegion(guiItem);

		StringBuilder sb = new StringBuilder();

		sb.append(guiItem.getReferenceAreaNameTextField().getText()).append(":")
		.append(width.intValue()).append(":")
		.append(height.intValue()).append(":")
		.append(distanceFromRightOfReference.intValue()).append(":")
		.append(distanceFromLeftOfReference.intValue()).append(":")
		.append(distanceAboveReferenceRegion.intValue()).append(":")
		.append(distanceBelowReferenceRegion.intValue());

		logger.info("Generated page properties for element " + guiItem.getElementNameTextField().getText() + " = " + sb.toString());

		return sb.toString();
	}

	/**
	 * Calculates the distance in pixels that the region to be created is above the reference region.
	 *
	 * @param guiItem
	 * @return the distance in pixels
	 */
	private Double getDistanceAboveReferenceRegion(GuiElementSelectorItems guiItem) {
		Double distanceAboveReferenceRegion = 0.0;
		if(!elementIsBelowTheReferenceRegion(guiItem)){
			distanceAboveReferenceRegion = guiItem.getReferencedElementSizeAndPosition().getyCoords()
			    - (guiItem.getElementSizeAndPosition().getyCoords() + guiItem.getElementSizeAndPosition().getHeight());
		}
		return Math.max(0, distanceAboveReferenceRegion);
	}

	/**
	 * Calculates the distance in pixels that the region to be created is below the reference region.
	 *
	 * @param guiItem
	 * @return the distance in pixels
	 */
	private Double getDistanceBelowReferenceRegion(GuiElementSelectorItems guiItem) {
		Double distanceBelowReferenceRegion = 0.0;
		if(elementIsBelowTheReferenceRegion(guiItem)){
			distanceBelowReferenceRegion = guiItem.getElementSizeAndPosition().getyCoords()
			    - (guiItem.getReferencedElementSizeAndPosition().getyCoords() + guiItem.getReferencedElementSizeAndPosition().getHeight());
		}
		return Math.max(0, distanceBelowReferenceRegion);
	}

	/**
	 * Calculates the distance in pixels that the region to be created is to the left of the reference region.
	 *
	 * @param guiItem
	 * @return the distance in pixels
	 */
	private Double getDistanceFromLeftOfReference(GuiElementSelectorItems guiItem) {
		Double distanceFromLeftOfReference = 0.0;
		if(!elementIsToTheRightOfReferenceRegion(guiItem)){
			distanceFromLeftOfReference = guiItem.getReferencedElementSizeAndPosition().getxCoords()
			    - (guiItem.getElementSizeAndPosition().getxCoords() + guiItem.getElementSizeAndPosition().getWidth());
		}
		return Math.max(0, distanceFromLeftOfReference);
	}

	/**
	 * Calculates the distance in pixels that the region to be created is to the right of the reference region.
	 *
	 * @param guiItem
	 * @return the distance in pixels
	 */
	private Double getDistanceFromRightOfReference(GuiElementSelectorItems guiItem) {
		Double distanceFromRightOfReference = 0.0;
		if(elementIsToTheRightOfReferenceRegion(guiItem)){
			distanceFromRightOfReference = guiItem.getElementSizeAndPosition().getxCoords()
			    - (guiItem.getReferencedElementSizeAndPosition().getxCoords() + guiItem.getReferencedElementSizeAndPosition().getWidth());
		}
		return Math.max(0, distanceFromRightOfReference);
	}

	/**
	 * A check to see whether the element region is below the reference region.
	 *
	 * @param guiItem
	 * @return true if the element is below the reference region.
	 */
	private boolean elementIsBelowTheReferenceRegion(GuiElementSelectorItems guiItem) {
		return guiItem.getReferencedElementSizeAndPosition().getyCoords() < guiItem.getElementSizeAndPosition().getyCoords();
	}

	/**
	 * A check to see whether the element region is to the right the reference region.
	 *
	 * @param guiItem
	 * @return true if the element is to the right of the reference region.
	 */
	public boolean elementIsToTheRightOfReferenceRegion(GuiElementSelectorItems guiItem) {
		return guiItem.getReferencedElementSizeAndPosition().getxCoords() < guiItem.getElementSizeAndPosition().getxCoords();
	}

	/**
	 *
	 *
	 * @param guiItem
	 * @param elementOutputFolder
	 * @throws IOException
	 */
	private void writeReferencedElementImagesToFile(GuiElementSelectorItems guiItem, File elementOutputFolder) throws IOException {
		writeImageToFile(guiItem.getElementAreaImage().getImage(), elementOutputFolder + "\\" + guiItem.getElementNameTextField().getText() + ".png");
		writeImageToFile(guiItem.getReferenceAreaImage().getImage(), elementOutputFolder + "\\" + guiItem.getReferenceAreaNameTextField().getText() + ".png");
	}

	/**
	 * A method to handle the actions to be carried out when the user chooses to select or deselect the
	 * page area has header checkbox.
	 *
	 * Selecting will enable the select page area header button.
	 * Deselecting will disable the button and remove any image already selected.
	 *
	 */
	@FXML
	public void handlePageAreaHasHeaderCheckbox() {
		logger.info("Handling page area header checkbox click");

		if(pageAreaHasHeader.isSelected()){

			logger.info("Page area has header");
			selectPageHeaderBtn.setDisable(false);
		}
		else{

			logger.info("Page area doesn't have header");
			selectPageHeaderBtn.setDisable(true);
			pageAreaHeader.setImage(null);
		}
	}

	/**
	 * A method to handle the actions to be carried out when the user clicks the select page header button
	 */
	@FXML
	public void handleSelectPageHeader() {
		logger.info("Handling page area header image selection");

		selectAreaFromScreenshot(pageAreaHeader, Color.RED);
	}

	/**
	 * A method to handle the actions to be carried out when the user clicks the add GUI element button.
	 *
	 * The scrollbar is resized to accommodate the new fields/buttons etc. and all the new fields/buttons etc. are
	 * added to a gridpane within the scrollbar. The number of elements is incremented.
	 */
	@FXML
	public void handleAddGuiElement() {

		logger.info("Adding GUI Element Selector Items");

		GuiElementSelectorItems items = new GuiElementSelectorItems(elementNumber);
		setTheScrollbarHeightToNumberOfElements(items.getElementNumber());
		addGuiItemNumberLabel(items);
		addElementNameField(items);
		addRadioButtonsToGrid(items);
		addSelectElementAreaButton(items);
		addElementAreaImage(items);
		addReferenceAreaNameField(items);
		addSelectReferenceAreaButton(items);
		addReferenceAreaImage(items);
		addSeperatingLine(items);
		selectorItems.add(items);
		elementNumber++;

		logger.info("Added new Gui Element Selector Items :"
						+ " Element Number : " + items.getElementNumber()
						+ ", Element Index : " + items.getElementIndex()
						+ ", Selector Items Size : " + selectorItems.size()
						+ ", Next element number will be : " + elementNumber);
	}

	/**
	 * Method to add the element number label to the GUI Element Selector Input gridpane
	 *
	 * @param items the group of Gui Element Selector Items
	 * @param elementNumber
	 */
	private void addGuiItemNumberLabel(GuiElementSelectorItems items) {

		logger.info("Adding Item number label for Gui Item : " + items.getElementNumber());

		items.getGuiItemNumberLabel().setText("Gui Item " + items.getElementNumber());
		items.getGuiItemNumberLabel().setFont(Font.font(null, FontWeight.BOLD, 12.0));
		inputsGrid.addRow(gridPaneNextRow, items.getGuiItemNumberLabel());
		GridPane.setMargin(items.getGuiItemNumberLabel(), guiSelectorItemPadding);
		gridPaneNextRow++;
	}

	/**
	 * Method to add a separating line between GUI Element Selector Inputs on the gridpane
	 *
	 * @param items the group of Gui Element Selector Items
	 */
	private void addSeperatingLine(GuiElementSelectorItems items) {

		logger.info("Adding Seperating Line for Gui Item : " + items.getElementNumber());

		items.setSeperatingLine(new Line(0, 25, 200, 25));
		items.getSeperatingLine().setStrokeWidth(3.0);
		items.getSeperatingLine().setStroke(Paint.valueOf(Color.DARKGRAY.toString()));
		inputsGrid.addRow(gridPaneNextRow, items.getSeperatingLine());
		gridPaneNextRow++;
	}

	/**
	 * Method to add the button the user clicks to select the element area to the GUI Element Selector Inputs
	 * on the gridpane
	 *
	 * @param items the group of Gui Element Selector Items
	 */
	private void addSelectElementAreaButton(final GuiElementSelectorItems items) {

		logger.info("Adding Select Element Area Button for Gui Item : " + items.getElementNumber());

		items.getSelectElementAreaButton().setText("Select Element");
		items.getSelectElementAreaButton().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
			public void handle(Event event) {
				selectElementArea(items.getElementIndex());
			}
		});
		inputsGrid.addRow(gridPaneNextRow, items.getSelectElementAreaButton());
		GridPane.setMargin(items.getSelectElementAreaButton(), guiSelectorItemPadding);
		gridPaneNextRow++;
	}

	/**
	 * A method select the element main area from the larger screenshot.
	 *
	 * @param elementIndex
	 */
	protected void selectElementArea(Integer elementIndex) {
		selectorItems.get(elementIndex).setElementSizeAndPosition(selectAreaFromScreenshot(selectorItems.get(elementIndex).getElementAreaImage(), Color.BLUE));
	}

	/**
	 * Method to add the image view for the element area to the GUI Element Selector Inputs
	 * on the gridpane
	 *
	 * @param items the group of Gui Element Selector Items
	 */
	private void addElementAreaImage(GuiElementSelectorItems items) {

		logger.info("Adding Element Area Image for Gui Item : " + items.getElementNumber());

		items.getElementAreaImageContainer().setMinWidth(180);
		items.getElementAreaImageContainer().setMaxWidth(180);
		items.getElementAreaImageContainer().setMinHeight(50);
		items.getElementAreaImageContainer().setMaxHeight(50);
		items.getElementAreaImage().setFitWidth(1280);
		items.getElementAreaImage().setFitHeight(960);
		items.getElementAreaImageContainer().setContent(items.getElementAreaImage());
		inputsGrid.addRow(gridPaneNextRow, items.getElementAreaImageContainer());
		GridPane.setMargin(items.getElementAreaImageContainer(), guiSelectorItemPadding);
		gridPaneNextRow++;
	}

	/**
	 * Method to add the image view for the reference area to the GUI Element Selector Inputs
	 * on the gridpane
	 *
	 * @param items the group of Gui Element Selector Items
	 */
	private void addReferenceAreaImage(GuiElementSelectorItems items) {

		logger.info("Adding Reference Area Image for Gui Item : " + items.getElementNumber());

		items.getReferenceAreaImageContainer().setMinWidth(180);
		items.getReferenceAreaImageContainer().setMaxWidth(180);
		items.getReferenceAreaImageContainer().setMinHeight(50);
		items.getReferenceAreaImageContainer().setMaxHeight(50);
		items.getReferenceAreaImage().setFitWidth(1280);
		items.getReferenceAreaImage().setFitHeight(960);
		items.getReferenceAreaImageContainer().setContent(items.getReferenceAreaImage());
		items.getReferenceAreaImage().setVisible(false);
		items.getReferenceAreaImageContainer().setVisible(false);
		items.getSelectReferenceAreaButton().setVisible(false);
		items.getReferenceAreaNameTextField().setVisible(false);
		inputsGrid.addRow(gridPaneNextRow, items.getReferenceAreaImageContainer());
		GridPane.setMargin(items.getReferenceAreaImageContainer(), guiSelectorItemPadding);
		gridPaneNextRow++;

	}

	/**
	 * Method to add the button the user clicks to select the reference area to the GUI Element Selector Inputs
	 * on the gridpane
	 *
	 * @param items the group of Gui Element Selector Items
	 */
	private void addSelectReferenceAreaButton(final GuiElementSelectorItems items) {

		logger.info("Adding Select Reference Area Button for Gui Item : " + items.getElementNumber());

		items.getSelectReferenceAreaButton().setText("Select Reference Area");
		items.getSelectReferenceAreaButton().setDisable(true);
		items.getSelectReferenceAreaButton().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
			public void handle(Event event) {
				selectReferenceArea(items.getElementIndex());
			}
		});
		inputsGrid.addRow(gridPaneNextRow, items.getSelectReferenceAreaButton());
		GridPane.setMargin(items.getSelectReferenceAreaButton(), guiSelectorItemPadding);
		gridPaneNextRow++;
	}

	/**
	 * A method select the reference image area from the larger screenshot.
	 *
	 * @param elementIndex
	 */
	protected void selectReferenceArea(int elementIndex) {
		selectorItems.get(elementIndex)
		    .setReferencedElementSizeAndPosition(selectAreaFromScreenshot(selectorItems.get(elementIndex).getReferenceAreaImage(), Color.GREEN));
	}

	/**
	 * Method to add the reference area name field to the GUI Element Selector Inputs on the gridpane
	 *
	 * @param items
	 */
	private void addReferenceAreaNameField(GuiElementSelectorItems items) {

		logger.info("Adding Reference Area Name field for Gui Item : " + items.getElementNumber());

		items.getReferenceAreaNameTextField().setPromptText("Reference Area Name");
		items.getReferenceAreaNameTextField().setDisable(true);
		inputsGrid.addRow(gridPaneNextRow, items.getReferenceAreaNameTextField());
		GridPane.setMargin(items.getReferenceAreaNameTextField(), guiSelectorItemPadding);
		gridPaneNextRow++;
	}

	/**
	 * Method to add the reference area name field to the GUI Element Selector Inputs on the gridpane
	 *
	 * @param items
	 */
	private void addElementNameField(GuiElementSelectorItems items) {

		logger.info("Adding Element Name field for Gui Item : " + items.getElementNumber());

		items.getElementNameTextField().setPromptText("Element Name");
		inputsGrid.addRow(gridPaneNextRow, items.getElementNameTextField());
		GridPane.setMargin(items.getElementNameTextField(), new Insets(10, 0, 5, 5));
		gridPaneNextRow++;
	}

	/**
	 *
	 *
	 * @param items
	 */
	private void addRadioButtonsToGrid(final GuiElementSelectorItems items) {
		final ToggleGroup group = new ToggleGroup();
		items.getDirectlySelectedElementButton().setText("Direct");
		items.getDirectlySelectedElementButton().setToggleGroup(group);
		items.getDirectlySelectedElementButton().setSelected(true);
		items.getDirectlySelectedElementButton().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
			public void handle(Event event) {
				if(items.getDirectlySelectedElementButton().isSelected()){
					disableReferenceAreaInputs(items);
				}
			}
		});

		items.getReferencedElementButton().setText("Referenced");
		items.getReferencedElementButton().setToggleGroup(group);
		items.getReferencedElementButton().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
			public void handle(Event event) {
				if(items.getReferencedElementButton().isSelected()){
					enableReferenceAreaInputs(items);
				}
			}
		});

		inputsGrid.addRow(gridPaneNextRow, items.getDirectlySelectedElementButton());
		GridPane.setMargin(items.getDirectlySelectedElementButton(), guiSelectorItemPadding);
		gridPaneNextRow++;
		inputsGrid.addRow(gridPaneNextRow, items.getReferencedElementButton());
		GridPane.setMargin(items.getReferencedElementButton(), guiSelectorItemPadding);
		gridPaneNextRow++;
	}

	protected void enableReferenceAreaInputs(GuiElementSelectorItems items) {
		items.getReferenceAreaImage().setVisible(true);
		items.getReferenceAreaImageContainer().setVisible(true);
		items.getSelectReferenceAreaButton().setVisible(true);
		items.getReferenceAreaNameTextField().setVisible(true);
		items.getSelectReferenceAreaButton().setDisable(false);
		items.getReferenceAreaNameTextField().setDisable(false);
	}

	protected void disableReferenceAreaInputs(GuiElementSelectorItems items) {
		items.getReferenceAreaImage().setImage(null);
		items.getReferenceAreaNameTextField().setText(null);
		items.getReferenceAreaImage().setVisible(false);
		items.getReferenceAreaImageContainer().setVisible(false);
		items.getSelectReferenceAreaButton().setVisible(false);
		items.getReferenceAreaNameTextField().setVisible(false);
		items.getSelectReferenceAreaButton().setDisable(true);
		items.getReferenceAreaNameTextField().setDisable(true);
	}

	@FXML
	public void handleRemoveGuiElement() {

		logger.info("Removing Elements");

		int lastAddedElementNumber = elementNumber - 1;
		int lastAddedElementIndex = elementNumber - 2;

		if(lastAddedElementNumber > 0){
			setTheScrollbarHeightToNumberOfElements(lastAddedElementIndex);
			inputsGrid.getChildren().remove(selectorItems.get(lastAddedElementIndex).getGuiItemNumberLabel());
			inputsGrid.getChildren().remove(selectorItems.get(lastAddedElementIndex).getDirectlySelectedElementButton());
			inputsGrid.getChildren().remove(selectorItems.get(lastAddedElementIndex).getReferencedElementButton());
			inputsGrid.getChildren().remove(selectorItems.get(lastAddedElementIndex).getElementNameTextField());
			inputsGrid.getChildren().remove(selectorItems.get(lastAddedElementIndex).getSelectReferenceAreaButton());
			inputsGrid.getChildren().remove(selectorItems.get(lastAddedElementIndex).getReferenceAreaImageContainer());
			inputsGrid.getChildren().remove(selectorItems.get(lastAddedElementIndex).getReferenceAreaImage());
			inputsGrid.getChildren().remove(selectorItems.get(lastAddedElementIndex).getSelectElementAreaButton());
			inputsGrid.getChildren().remove(selectorItems.get(lastAddedElementIndex).getElementAreaImageContainer());
			inputsGrid.getChildren().remove(selectorItems.get(lastAddedElementIndex).getElementAreaImage());
			inputsGrid.getChildren().remove(selectorItems.get(lastAddedElementIndex).getReferenceAreaNameTextField());
			inputsGrid.getChildren().remove(selectorItems.get(lastAddedElementIndex).getSeperatingLine());
			gridPaneNextRow = gridPaneNextRow - NUMBER_OF_GRID_ROWS_FOR_SELECTOR_ITEMS;
			selectorItems.remove(lastAddedElementIndex);
			elementNumber--;
		}

		logger.info("Removing last Gui Element Selector Items :"
				+ " Last added element Number : " + lastAddedElementNumber
				+ ", Last added element Index : " + lastAddedElementIndex
				+ ", GridPane next row : " + gridPaneNextRow
				+ ", New element number : " + elementNumber
				+ ", Items left in selector items list : " + selectorItems.size());

	}

	/**
	 *
	 * @param numberOfElements
	 */
	public void setTheScrollbarHeightToNumberOfElements(int numberOfElements) {
		System.out.println(GUI_ITEMS_PIXELS_HEIGHT_IN_SCROLLBAR * (elementNumber));
		guiItemsAnchorPane.setPrefHeight(GUI_ITEMS_PIXELS_HEIGHT_IN_SCROLLBAR * (elementNumber));
		guiItemsAnchorPane.setMaxHeight(GUI_ITEMS_PIXELS_HEIGHT_IN_SCROLLBAR * (elementNumber));
		guiItemsAnchorPane.setMinHeight(GUI_ITEMS_PIXELS_HEIGHT_IN_SCROLLBAR * (elementNumber));
	}

	private GuiElementSizeAndPosition selectAreaFromScreenshot(final ImageView uiLocation, Color selectorColor) {
		ObservableList<Node> childrenList = imageGroup.getChildren();
		final GuiElementSizeAndPosition sizeAndPosition = new GuiElementSizeAndPosition();
		childrenList.clear();
		childrenList.add(screenGrabView);
		screenShotSelector = new ScreenShotSelector(imageGroup, selectorColor);

		// create context menu and menu items
		final ContextMenu contextMenu = new ContextMenu();

		MenuItem cropMenuItem = new MenuItem("Select");
		cropMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				Bounds selectionBounds = screenShotSelector.getBounds();
				System.out.println("Selected area: " + selectionBounds);

				int width = (int) selectionBounds.getWidth();
				int height = (int) selectionBounds.getHeight();

				SnapshotParameters parameters = new SnapshotParameters();
				parameters.setFill(Color.TRANSPARENT);
				parameters.setViewport(new Rectangle2D(selectionBounds.getMinX(), selectionBounds.getMinY(), width, height));
				sizeAndPosition.setWidth(Double.valueOf(width));
				sizeAndPosition.setHeight(Double.valueOf(height));
				sizeAndPosition.setxCoords(selectionBounds.getMinX());
				sizeAndPosition.setyCoords(selectionBounds.getMinY());

				WritableImage wi = new WritableImage(width, height);
				screenGrabView.snapshot(parameters, wi);

				BufferedImage bufImage = SwingFXUtils.fromFXImage(wi, null);
				screenShotProcessor.setImageToClipboard(bufImage);

				BufferedImage clipboardContents = screenShotProcessor.getImageFromClipboard();
				if(clipboardContents != null){
					Image newImg = SwingFXUtils.toFXImage(screenShotProcessor.getImageFromClipboard(), null);
					uiLocation.setFitWidth(width);
					uiLocation.setFitHeight(height);
					setImageToUiLocaton(uiLocation, newImg);
				}
				else{
					validationMessage.setText("Clipboard does not contain an image");
				}

			}

		});
		contextMenu.getItems().add(cropMenuItem);

		// set context menu on image layer
		imageGroup.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(event.isSecondaryButtonDown()){
					contextMenu.show(imageGroup, event.getScreenX(), event.getScreenY());
				}
			}
		});

		return sizeAndPosition;
	}

	private void setImageToUiLocaton(ImageView uiLocation, Image newImg) {
		uiLocation.setImage(newImg);
	}

	private void writeImageToFile(Image image, String fileName) throws IOException {
		BufferedImage bufImg = SwingFXUtils.fromFXImage(image, null);
		ImageIO.write(bufImg, "png", new File(fileName));
	}

	public List<GuiElementSelectorItems> getSelectorItems() {
		return selectorItems;
	}

	public void setSelectorItems(List<GuiElementSelectorItems> selectorItems) {
		this.selectorItems = selectorItems;
	}

}
