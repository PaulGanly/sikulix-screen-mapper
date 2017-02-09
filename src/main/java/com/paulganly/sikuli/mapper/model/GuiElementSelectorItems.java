package com.paulganly.sikuli.mapper.model;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;

public class GuiElementSelectorItems {

	private Integer elementNumber;
	private Integer elementIndex;
	private Label guiItemNumberLabel;
	private RadioButton directlySelectedElementButton;
	private RadioButton referencedElementButton;
	private TextField elementNameTextField;
	private Button selectReferenceAreaButton;
	private TextField referenceAreaNameTextField;
	private ScrollPane referenceAreaImageContainer;
	private ImageView referenceAreaImage;
	private Button selectElementAreaButton;
	private ScrollPane elementAreaImageContainer;
	private ImageView elementAreaImage;
	private GuiElementSizeAndPosition referencedElementSizeAndPosition;
	private GuiElementSizeAndPosition elementSizeAndPosition;
	private Line seperatingLine;

	public GuiElementSelectorItems(Integer elementNumber) {
		super();
		this.elementNumber = elementNumber;
		this.elementIndex = (elementNumber-1);
		guiItemNumberLabel = new Label();
		directlySelectedElementButton = new RadioButton();
		referencedElementButton = new RadioButton();
		elementNameTextField = new TextField();
		selectReferenceAreaButton = new Button();
		referenceAreaNameTextField = new TextField();
		referenceAreaImageContainer = new ScrollPane();
		referenceAreaImage = new ImageView();
		selectElementAreaButton = new Button();
		elementAreaImageContainer = new ScrollPane();
		elementAreaImage = new ImageView();
		seperatingLine = new Line();
	}

	public RadioButton getDirectlySelectedElementButton() {
		return directlySelectedElementButton;
	}

	public void setDirectlySelectedElementButton(RadioButton directlySelectedElementButton) {
		this.directlySelectedElementButton = directlySelectedElementButton;
	}

	public RadioButton getReferencedElementButton() {
		return referencedElementButton;
	}

	public void setReferencedElementButton(RadioButton referencedElementButton) {
		this.referencedElementButton = referencedElementButton;
	}

	public TextField getElementNameTextField() {
		return elementNameTextField;
	}

	public void setElementNameTextField(TextField elementNameTextField) {
		this.elementNameTextField = elementNameTextField;
	}

	public Button getSelectReferenceAreaButton() {
		return selectReferenceAreaButton;
	}

	public void setSelectReferenceAreaButton(Button selectReferenceAreaButton) {
		this.selectReferenceAreaButton = selectReferenceAreaButton;
	}

	public ImageView getReferenceAreaImage() {
		return referenceAreaImage;
	}

	public void setReferenceAreaImage(ImageView referenceAreaImage) {
		this.referenceAreaImage = referenceAreaImage;
	}

	public Button getSelectElementAreaButton() {
		return selectElementAreaButton;
	}

	public void setSelectElementAreaButton(Button selectElementAreaButton) {
		this.selectElementAreaButton = selectElementAreaButton;
	}

	public ImageView getElementAreaImage() {
		return elementAreaImage;
	}

	public void setElementAreaImage(ImageView elementAreaImage) {
		this.elementAreaImage = elementAreaImage;
	}

	public Integer getElementNumber() {
		return elementNumber;
	}

	public void setElementNumber(Integer elementNumber) {
		this.elementNumber = elementNumber;
	}

	public GuiElementSizeAndPosition getElementSizeAndPosition() {
		return elementSizeAndPosition;
	}

	public void setElementSizeAndPosition(GuiElementSizeAndPosition elementSizeAndPosition) {
		this.elementSizeAndPosition = elementSizeAndPosition;
	}

	public GuiElementSizeAndPosition getReferencedElementSizeAndPosition() {
		return referencedElementSizeAndPosition;
	}

	public void setReferencedElementSizeAndPosition(GuiElementSizeAndPosition referencedElementSizeAndPosition) {
		this.referencedElementSizeAndPosition = referencedElementSizeAndPosition;
	}

	public TextField getReferenceAreaNameTextField() {
	  return referenceAreaNameTextField;
  }

	public void setReferenceAreaNameTextField(TextField referenceAreaNameTextField) {
	  this.referenceAreaNameTextField = referenceAreaNameTextField;
  }

	public Line getSeperatingLine() {
		return seperatingLine;
	}

	public void setSeperatingLine(Line seperatingLine) {
		this.seperatingLine = seperatingLine;
	}

	public ScrollPane getReferenceAreaImageContainer() {
		return referenceAreaImageContainer;
	}

	public void setReferenceAreaImageContainer(ScrollPane referenceAreaImageContainer) {
		this.referenceAreaImageContainer = referenceAreaImageContainer;
	}

	public ScrollPane getElementAreaImageContainer() {
		return elementAreaImageContainer;
	}

	public void setElementAreaImageContainer(ScrollPane elementAreaImageContainer) {
		this.elementAreaImageContainer = elementAreaImageContainer;
	}

	public Label getGuiItemNumberLabel() {
		return guiItemNumberLabel;
	}

	public void setGuiItemNumberLabel(Label guiItemNumberLabel) {
		this.guiItemNumberLabel = guiItemNumberLabel;
	}

	public Integer getElementIndex() {
		return elementIndex;
	}

	public void setElementIndex(Integer elementIndex) {
		this.elementIndex = elementIndex;
	}

}
