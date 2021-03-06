/*
 * Copyright (c) 2018 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.colorchooser;

import eu.hansolo.fx.colorchooser.tool.Helper;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * User: hansolo
 * Date: 22.10.18
 * Time: 19:50
 */
@DefaultProperty("children")
public class ColorChooser extends Region {
    private static final double                     PREFERRED_WIDTH  = 250;
    private static final double                     PREFERRED_HEIGHT = 215;
    private static final double                     MINIMUM_WIDTH    = 50;
    private static final double                     MINIMUM_HEIGHT   = 50;
    private static final double                     MAXIMUM_WIDTH    = 1024;
    private static final double                     MAXIMUM_HEIGHT   = 1024;
    private static final Pattern                    HEX_PATTERN      = Pattern.compile("#?([A-Fa-f0-9]{2})");
    private static final Matcher                    HEX_MATCHER      = HEX_PATTERN.matcher("");
    private static final Color                      DARK_COLOR       = Color.BLACK;
    private static final Color                      BRIGHT_COLOR     = Color.web("#dbdbdb");
    private              double                     size;
    private              double                     width;
    private              double                     height;
    private              GridPane                   grid;
    private              ColorSelector              fillSelector;
    private              ColorSelectorObserver      fillSelectorObserver;
    private              ColorSelector              strokeSelector;
    private              ColorSelectorObserver      strokeSelectorObserver;
    private              ChoiceBox                  colorModelChooser;
    private              ComboBox                   opacityChooser;
    private              Label                      slider1Label;
    private              Slider                     slider1;
    private              TextField                  slider1Field;
    private              Label                      slider2Label;
    private              Slider                     slider2;
    private              TextField                  slider2Field;
    private              Label                      slider3Label;
    private              Slider                     slider3;
    private              TextField                  slider3Field;
    private              Canvas                     canvas;
    private              GraphicsContext            ctx;
    private              TextField                  colorField;
    private              Slider                     opacitySlider;
    private              Circle                     opacity0;
    private              Circle                     opacity1;
    private              Pane                       pane;
    private              Color                      _fill;
    private              ObjectProperty<Color>      fill;
    private              Color                      _stroke;
    private              ObjectProperty<Color>      stroke;
    private              double                     xStep;
    private              double                     yStep;
    private              List<ColorChooserObserver> observers;


    // ******************** Constructors **************************************
    public ColorChooser() {
        getStylesheets().add(ColorChooser.class.getResource("colorchooser.css").toExternalForm());
        _fill                  = Color.BLACK;
        _stroke                = Color.BLACK;
        fillSelectorObserver   = e -> setSliders(e.getSelectedColor());
        strokeSelectorObserver = e -> setSliders(e.getSelectedColor());
        observers              = new CopyOnWriteArrayList<>();

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
            Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        getStyleClass().add("color-chooser");

        grid = new GridPane();
        grid.setVgap(3);
        grid.getStyleClass().add("area-background");

        ToggleGroup fillStrokeGroup = new ToggleGroup();

        fillSelector = new ColorSelector("Fill", Color.BLACK);
        fillSelector.setSelectionColor(BRIGHT_COLOR);
        fillSelector.setFill(getFill());
        fillSelector.setToggleGroup(fillStrokeGroup);
        fillSelector.setSelected(true);

        strokeSelector = new ColorSelector("Stroke", Color.BLACK);
        strokeSelector.setSelectionColor(BRIGHT_COLOR);
        strokeSelector.setFill(getStroke());
        strokeSelector.setToggleGroup(fillStrokeGroup);

        String    colorModels[]     = { "RGB", "RGB Hex", "HSL" };
        colorModelChooser = new ChoiceBox(FXCollections.observableArrayList(colorModels));
        colorModelChooser.getSelectionModel().select(0);
        colorModelChooser.setMinWidth(70);
        colorModelChooser.setMaxWidth(70);
        colorModelChooser.setPrefWidth(70);

        slider1Label = new Label("R");
        slider1 = new Slider(0, 255, 0);
        slider1.setFocusTraversable(false);
        slider1Field = createSliderField("0");
        HBox slider1Box = new HBox(5, slider1Label, slider1, slider1Field);
        HBox.setHgrow(slider1, Priority.ALWAYS);
        slider1Box.setAlignment(Pos.CENTER_RIGHT);

        slider2Label = new Label("G");
        slider2 = new Slider(0, 255, 0);
        slider2.setFocusTraversable(false);
        slider2Field = createSliderField("0");
        HBox slider2Box = new HBox(5, slider2Label, slider2, slider2Field);
        HBox.setHgrow(slider2, Priority.ALWAYS);
        slider2Box.setAlignment(Pos.CENTER_RIGHT);

        slider3Label = new Label("B");
        slider3 = new Slider(0, 255, 0);
        slider3.setFocusTraversable(false);
        slider3Field = createSliderField("0");
        HBox slider3Box = new HBox(5, slider3Label, slider3, slider3Field);
        HBox.setHgrow(slider3, Priority.ALWAYS);
        slider3Box.setAlignment(Pos.CENTER_RIGHT);

        colorField = new TextField("#000000");
        colorField.setMaxWidth(70);
        colorField.setAlignment(Pos.CENTER_RIGHT);

        canvas = new Canvas(250, 48);
        ctx    = canvas.getGraphicsContext2D();
        drawColorCanvas();

        Label opacityLabel = new Label("Opacity");
        opacitySlider = new Slider(0, 1, 1);
        opacity0 = new Circle(5);
        opacity0.setFill(Color.TRANSPARENT);
        opacity0.setStroke(BRIGHT_COLOR);
        opacity1 = new Circle(5);
        opacity1.setFill(BRIGHT_COLOR);
        opacity1.setStroke(BRIGHT_COLOR);
        String opacities[] = { "100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%", "0%" };
        opacityChooser = new ComboBox(FXCollections.observableArrayList(opacities));
        opacityChooser.setMinWidth(70);
        opacityChooser.setMaxWidth(70);
        opacityChooser.setPrefWidth(70);
        opacityChooser.setEditable(true);
        opacityChooser.getSelectionModel().select(0);
        opacityChooser.getEditor().setAlignment(Pos.CENTER_RIGHT);
        HBox opacityBox = new HBox(5, opacity0, opacitySlider, opacity1, opacityChooser);
        HBox.setHgrow(opacitySlider, Priority.ALWAYS);
        opacityBox.setAlignment(Pos.CENTER_RIGHT);

        grid.add(fillSelector, 0, 0);
        grid.add(strokeSelector, 1, 0);
        grid.add(colorModelChooser, 2, 0);
        grid.add(slider1Box, 0, 1);
        grid.add(slider2Box, 0, 2);
        grid.add(slider3Box, 0, 3);
        grid.add(colorField, 0, 4);
        grid.add(canvas, 0, 5);
        grid.add(opacityLabel, 0, 6);
        grid.add(opacityBox, 0, 7);

        GridPane.setFillWidth(fillSelector, true);
        GridPane.setFillWidth(strokeSelector, true);
        GridPane.setHalignment(colorModelChooser, HPos.RIGHT);
        GridPane.setColumnSpan(slider1Box, 3);
        GridPane.setColumnSpan(slider2Box, 3);
        GridPane.setColumnSpan(slider3Box, 3);
        GridPane.setColumnSpan(colorField, 3);
        GridPane.setColumnSpan(canvas, 3);
        GridPane.setColumnSpan(opacityBox, 3);

        GridPane.setHalignment(colorField, HPos.RIGHT);

        pane = new Pane(grid);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());

        fillSelector.selectedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                Color fillColor = fillSelector.getFill();
                switch (colorModelChooser.getSelectionModel().getSelectedIndex()) {
                    case 0: // RGB
                        slider1.setValue(fillColor.getRed() * 255);
                        slider2.setValue(fillColor.getGreen() * 255);
                        slider3.setValue(fillColor.getBlue() * 255);
                        opacitySlider.setValue(fillColor.getOpacity());
                        break;
                    case 1: // RGB Hex
                        slider1.setValue(fillColor.getRed() * 255);
                        slider2.setValue(fillColor.getGreen() * 255);
                        slider3.setValue(fillColor.getBlue() * 255);
                        opacitySlider.setValue(fillColor.getOpacity());
                        break;
                    case 2: // HSL
                        double[] hsl = Helper.toHSL(fillColor);
                        slider1.setValue(hsl[0]);
                        slider2.setValue(hsl[1] * 100.0);
                        slider3.setValue(hsl[2] * 100.0);
                        opacitySlider.setValue(fillColor.getOpacity());
                        break;
                }
            }
        });
        strokeSelector.selectedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                Color strokeColor = strokeSelector.getFill();
                switch (colorModelChooser.getSelectionModel().getSelectedIndex()) {
                    case 0: // RGB
                        slider1.setValue(strokeColor.getRed() * 255);
                        slider2.setValue(strokeColor.getGreen() * 255);
                        slider3.setValue(strokeColor.getBlue() * 255);
                        opacitySlider.setValue(strokeColor.getOpacity());
                        break;
                    case 1: // RGB Hex
                        slider1.setValue(strokeColor.getRed() * 255);
                        slider2.setValue(strokeColor.getGreen() * 255);
                        slider3.setValue(strokeColor.getBlue() * 255);
                        opacitySlider.setValue(strokeColor.getOpacity());
                        break;
                    case 2: // HSL
                        double[] hsl = Helper.toHSL(strokeColor);
                        slider1.setValue(hsl[0]);
                        slider2.setValue(hsl[1] * 100.0);
                        slider3.setValue(hsl[2] * 100.0);
                        opacitySlider.setValue(strokeColor.getOpacity());
                        break;
                }
            }
        });

        fillSelector.addColorSelectorObserver(fillSelectorObserver);
        strokeSelector.addColorSelectorObserver(strokeSelectorObserver);

        colorModelChooser.getSelectionModel().selectedIndexProperty().addListener((o, ov, nv) -> {
            Color color = fillSelector.isSelected() ? getFill() : getStroke();
            switch(nv.intValue()) {
                case 0: // RGB
                    slider1.setMax(255);
                    slider1Label.setText("R");
                    slider1Field.setText(Integer.toString((int) (Math.round(color.getRed() * 255.0))));
                    slider1.setValue(Math.round(color.getRed() * 255.0));
                    slider2.setMax(255);
                    slider2Label.setText("G");
                    slider2Field.setText(Integer.toString((int) (Math.round(color.getGreen() * 255.0))));
                    slider2.setValue(Math.round(color.getGreen() * 255.0));
                    slider3.setMax(255);
                    slider3Label.setText("B");
                    slider3Field.setText(Integer.toString((int) (Math.round(color.getBlue() * 255.0))));
                    slider3.setValue(Math.round(color.getBlue() * 255.0));
                    break;
                case 1: // RGB Hex
                    slider1.setMax(255);
                    slider1Label.setText("R");
                    slider1Field.setText(String.format("%02X", (int) (Math.round(color.getRed() * 255.0))));
                    slider1.setValue(Math.round(color.getRed() * 255.0));
                    slider2.setMax(255);
                    slider2Label.setText("G");
                    slider2Field.setText(String.format("%02X", (int) (Math.round(color.getGreen() * 255.0))));
                    slider2.setValue(Math.round(color.getGreen() * 255.0));
                    slider3.setMax(255);
                    slider3Label.setText("B");
                    slider3Field.setText(String.format("%02X", (int) (Math.round(color.getBlue() * 255.0))));
                    slider3.setValue(Math.round(color.getBlue() * 255.0));
                    break;
                case 2: // HSL
                    double[] hsl = Helper.toHSL(color);
                    double hue        = hsl[0];
                    double saturation = hsl[1] * 100;
                    double lightness  = hsl[2] * 100;
                    slider1.setMax(360);
                    slider1Label.setText("H");

                    slider2.setMax(100);
                    slider2Label.setText("S");

                    slider3.setMax(100);
                    slider3Label.setText("L");

                    slider3.setValue(lightness);
                    slider2.setValue(saturation);
                    slider1.setValue(hue);
                    break;
            }
        });

        slider1.valueProperty().addListener((o, ov, nv) -> {
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
                case 0: // RGB
                    slider1Field.setText(Integer.toString((int) (slider1.getValue())));
                    if (fillSelector.isSelected()) {
                        setFill(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    } else {
                        setStroke(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    }
                    break;
                case 1: // RGB Hex
                    slider1Field.setText(String.format("%02X", (int) (slider1.getValue())));
                    if (fillSelector.isSelected()) {
                        setFill(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    } else {
                        setStroke(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    }
                    break;
                case 2: // HSL
                    slider1Field.setText(Integer.toString((int) (slider1.getValue())));
                    if (fillSelector.isSelected()) {
                        Color color = Helper.hslToRGB(slider1.getValue(), (slider2.getValue() / 100.0), (slider3.getValue() / 100.0), opacitySlider.getValue());
                        setFill(color);
                    } else {
                        Color color = Helper.hslToRGB(slider1.getValue(), (slider2.getValue() / 100.0), (slider3.getValue() / 100.0), opacitySlider.getValue());
                        setStroke(color);
                    }
                    break;
            }
        });
        slider2.valueProperty().addListener((o, ov, nv) -> {
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
                case 0: // RGB
                    slider2Field.setText(Integer.toString((int) (slider2.getValue())));
                    if (fillSelector.isSelected()) {
                        setFill(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    } else {
                        setStroke(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    }
                    break;
                case 1: // RGB Hex
                    slider2Field.setText(String.format("%02X", (int) (slider2.getValue())));
                    if (fillSelector.isSelected()) {
                        setFill(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    } else {
                        setStroke(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    }
                    break;
                case 2: // HSL
                    slider2Field.setText(Integer.toString((int) (slider2.getValue())));
                    if (fillSelector.isSelected()) {
                        Color color = Helper.hslToRGB(slider1.getValue(), (slider2.getValue() / 100.0), (slider3.getValue() / 100.0), opacitySlider.getValue());
                        setFill(color);
                    } else {
                        Color color = Helper.hslToRGB(slider1.getValue(), (slider2.getValue() / 100.0), (slider3.getValue() / 100.0), opacitySlider.getValue());
                        setStroke(color);
                    }
                    break;
            }
        });
        slider3.valueProperty().addListener((o, ov, nv) -> {
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
                case 0: // RGB
                    slider3Field.setText(Integer.toString((int) (slider3.getValue())));
                    if (fillSelector.isSelected()) {
                        setFill(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    } else {
                        setStroke(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    }
                    break;
                case 1: // RGB Hex
                    slider3Field.setText(String.format("%02X", (int) (slider3.getValue())));
                    if (fillSelector.isSelected()) {
                        setFill(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    } else {
                        setStroke(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    }
                    break;
                case 2: // HSL
                    slider3Field.setText(Integer.toString((int) (slider3.getValue())));
                    if (fillSelector.isSelected()) {
                        Color color = Helper.hslToRGB(slider1.getValue(), (slider2.getValue() / 100.0), (slider3.getValue() / 100.0), opacitySlider.getValue());
                        setFill(color);
                    } else {
                        Color color = Helper.hslToRGB(slider1.getValue(), (slider2.getValue() / 100.0), (slider3.getValue() / 100.0), opacitySlider.getValue());
                        setStroke(color);
                    }
                    break;
            }
        });

        slider1Field.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv) {
                int value;
                switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
                    case 0:
                        value = Helper.clamp(0, 255, (int) Helper.getNumberFromText(slider1Field.getText()));
                        slider1Field.setText(Integer.toString(value));
                        slider1.setValue(value);
                        break;
                    case 1:
                        HEX_MATCHER.reset(slider1Field.getText());
                        String result = "";
                        try { while (HEX_MATCHER.find()) { result = HEX_MATCHER.group(0); } } catch (IllegalStateException ex) { result = "00"; }
                        value = Integer.parseInt(result, 16);
                        slider1Field.setText(result);
                        slider1.setValue(value);
                        break;
                    case 2:
                        value = Helper.clamp(0, 360, (int) Helper.getNumberFromText(slider1Field.getText()));
                        slider1Field.setText(Integer.toString(value));
                        slider1.setValue(value);
                        break;
                }
            }
        });
        slider2Field.focusedProperty().addListener((o, ov, nv) -> {
            int value;
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
                case 0:
                    value = Helper.clamp(0, 255, (int) Helper.getNumberFromText(slider2Field.getText()));
                    slider2Field.setText(Integer.toString(value));
                    slider2.setValue(value);
                    break;
                case 1:
                    HEX_MATCHER.reset(slider2Field.getText());
                    String result = "";
                    try { while (HEX_MATCHER.find()) { result = HEX_MATCHER.group(0); } } catch (IllegalStateException ex) { result = "00"; }
                    value = Integer.parseInt(result, 16);
                    slider2Field.setText(result);
                    slider2.setValue(value);
                    break;
                case 2:
                    value = Helper.clamp(0, 100, (int) Helper.getNumberFromText(slider2Field.getText()));
                    slider2Field.setText(Integer.toString(value));
                    slider2.setValue(value);
                    break;
            }
        });
        slider3Field.focusedProperty().addListener((o, ov, nv) -> {
            int value;
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
                case 0:
                    value = Helper.clamp(0, 255, (int) Helper.getNumberFromText(slider3Field.getText()));
                    slider3Field.setText(Integer.toString(value));
                    slider3.setValue(value);
                    break;
                case 1:
                    HEX_MATCHER.reset(slider3Field.getText());
                    String result = "";
                    try { while (HEX_MATCHER.find()) { result = HEX_MATCHER.group(0); } } catch (IllegalStateException ex) { result = "00"; }
                    value = Integer.parseInt(result, 16);
                    slider3Field.setText(result);
                    slider3.setValue(value);
                    break;
                case 2:
                    value = Helper.clamp(0, 100, (int) Helper.getNumberFromText(slider3Field.getText()));
                    slider3Field.setText(Integer.toString(value));
                    slider3.setValue(value);
                    break;
            }
        });

        slider1Field.setOnAction(e -> {
            int value;
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
                case 0:
                    value = Helper.clamp(0, 255, (int) Helper.getNumberFromText(slider1Field.getText()));
                    slider1Field.setText(Integer.toString(value));
                    slider1.setValue(value);
                    break;
                case 1:
                    HEX_MATCHER.reset(slider1Field.getText());
                    String result = "";
                    try { while (HEX_MATCHER.find()) { result = HEX_MATCHER.group(0); } } catch (IllegalStateException ex) { result = "00"; }
                    value = Integer.parseInt(result, 16);
                    slider1Field.setText(result);
                    slider1.setValue(value);
                    break;
                case 2:
                    value = Helper.clamp(0, 360, (int) Helper.getNumberFromText(slider1Field.getText()));
                    slider1Field.setText(Integer.toString(value));
                    slider1.setValue(value);
                    break;
            }
        });
        slider2Field.setOnAction(e -> {
            int value;
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
                case 0:
                    value = Helper.clamp(0, 255, (int) Helper.getNumberFromText(slider2Field.getText()));
                    slider2Field.setText(Integer.toString(value));
                    slider2.setValue(value);
                    break;
                case 1:
                    HEX_MATCHER.reset(slider2Field.getText());
                    String result = "";
                    try { while (HEX_MATCHER.find()) { result = HEX_MATCHER.group(0); } } catch (IllegalStateException ex) { result = "00"; }
                    value = Integer.parseInt(result, 16);
                    slider2Field.setText(result);
                    slider2.setValue(value);
                    break;
                case 2:
                    value = Helper.clamp(0, 100, (int) Helper.getNumberFromText(slider2Field.getText()));
                    slider2Field.setText(Integer.toString(value));
                    slider2.setValue(value);
                    break;
            }
        });
        slider3Field.setOnAction(e -> {
            int value;
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
                case 0:
                    value = Helper.clamp(0, 255, (int) Helper.getNumberFromText(slider3Field.getText()));
                    slider3Field.setText(Integer.toString(value));
                    slider3.setValue(value);
                    break;
                case 1:
                    HEX_MATCHER.reset(slider3Field.getText());
                    String result = "";
                    try { while (HEX_MATCHER.find()) { result = HEX_MATCHER.group(0); } } catch (IllegalStateException ex) { result = "00"; }
                    value = Integer.parseInt(result, 16);
                    slider3Field.setText(result);
                    slider3.setValue(value);
                    break;
                case 2:
                    value = Helper.clamp(0, 100, (int) Helper.getNumberFromText(slider3Field.getText()));
                    slider3Field.setText(Integer.toString(value));
                    slider3.setValue(value);
                    break;
            }
        });

        colorField.setOnKeyPressed(evt -> {
            if (KeyCode.ENTER.equals(evt.getCode())) {
                String hexColor = Helper.getHexColorFromString(colorField.getText());
                if (null == hexColor || hexColor.isEmpty()) { return; }
                updateSliders(Color.web(hexColor));
            }
        });
        colorField.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv) {
                String hexColor = Helper.getHexColorFromString(colorField.getText());
                if (null == hexColor || hexColor.isEmpty()) { return; }
                updateSliders(Color.web(hexColor));
            }
        });

        canvas.setOnMousePressed(e -> setColorByCanvas(e.getSceneX(), e.getSceneY()));
        canvas.setOnMouseDragged(e -> setColorByCanvas(e.getSceneX(), e.getSceneY()));

        opacitySlider.valueProperty().addListener((o, ov, nv) -> {
            String percentage = String.format(Locale.US, "%.0f%%", (nv.doubleValue() * 100));
            opacityChooser.getEditor().setText(percentage);
            if (fillSelector.isSelected()) {
                setFill(Helper.getColorWithOpacity(getFill(), nv.doubleValue()));
            } else {
                setStroke(Helper.getColorWithOpacity(getStroke(), nv.doubleValue()));
            }
        });
        opacityChooser.setOnAction(e -> {
            double value = Helper.getNumberFromText(opacityChooser.getEditor().getText());
            value = Helper.clamp(0, 100, value);
            opacitySlider.setValue(value / 100);
            if (fillSelector.isSelected()) {
                setFill(Helper.getColorWithOpacity(getFill(), opacitySlider.getValue()));
            } else {
                setStroke(Helper.getColorWithOpacity(getStroke(), opacitySlider.getValue()));
            }
        });
        opacityChooser.getEditor().focusedProperty().addListener((o, ov, nv) -> {
            double value = Helper.getNumberFromText(opacityChooser.getEditor().getText());
            value = Helper.clamp(0, 100, value);
            opacitySlider.setValue(value / 100);
            if (fillSelector.isSelected()) {
                setFill(Helper.getColorWithOpacity(getFill(), opacitySlider.getValue()));
            } else {
                setStroke(Helper.getColorWithOpacity(getStroke(), opacitySlider.getValue()));
            }
        });
    }

    private void setColorByCanvas(final double sceneX, final double sceneY) {
        Bounds colorCanvasBounds = canvas.localToScene(canvas.getBoundsInLocal());
        double x                 = sceneX - colorCanvasBounds.getMinX();
        double y                 = sceneY - colorCanvasBounds.getMinY();
        Color  color             = Helper.hslToRGB(x * xStep, 1, 1 - y * yStep);
        updateSliders(color);
    }


    // ******************** Methods *******************************************
    @Override public void layoutChildren() {
        super.layoutChildren();
    }

    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }

    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public Color getFill() { return null == fill ? _fill : fill.get(); }
    public void setFill(final Color fill) {
        if (null == this.fill) {
            _fill = fill;
            if (fillSelector.isSelected() && fill instanceof Color) { opacitySlider.setValue(fill.getOpacity()); }
            fillSelector.setFill(fill);
            colorField.setText(fill.toString().replace("0x", "#").substring(0, 7));
            fireColorChooserEvent(new ColorChooserEvent(ColorChooser.this, ColorChooserEventType.FILL));
        } else {
            this.fill.set(fill);
        }
    }
    public ObjectProperty<Color> fillProperty() {
        if (null == fill) {
            fill = new ObjectPropertyBase<Color>(_fill) {
                @Override protected void invalidated() {
                    Color fill = get();
                    if (fillSelector.isSelected()) { opacitySlider.setValue(fill.getOpacity()); }
                    fillSelector.setFill(fill);
                    colorField.setText(fill.toString().replace("0x", "#").substring(0, 7));
                    fireColorChooserEvent(new ColorChooserEvent(ColorChooser.this, ColorChooserEventType.FILL));
                }
                @Override public Object getBean() { return ColorChooser.this; }
                @Override public String getName() { return "fill"; }
            };
            _fill = null;
        }
        return fill;
    }

    public Color getStroke() { return null == stroke ? _stroke : stroke.get(); }
    public void setStroke(final Color stroke) {
        if (null == this.stroke) {
            _stroke = stroke;
            if (strokeSelector.isSelected()) { opacitySlider.setValue(stroke.getOpacity()); }
            strokeSelector.setFill(stroke);
            colorField.setText(stroke.toString().replace("0x", "#").substring(0, 7));
            fireColorChooserEvent(new ColorChooserEvent(ColorChooser.this, ColorChooserEventType.STROKE));
        } else {
            this.stroke.set(stroke);
        }
    }
    public ObjectProperty<Color> strokeProperty() {
        if (null == stroke) {
            stroke = new ObjectPropertyBase<Color>(_stroke) {
                @Override protected void invalidated() {
                    Color stroke = get();
                    if (strokeSelector.isSelected()) { opacitySlider.setValue(stroke.getOpacity()); }
                    strokeSelector.setFill(stroke);
                    colorField.setText(stroke.toString().replace("0x", "#").substring(0, 7));
                    fireColorChooserEvent(new ColorChooserEvent(ColorChooser.this, ColorChooserEventType.STROKE));
                }
                @Override public Object getBean() { return ColorChooser.this; }
                @Override public String getName() { return "stroke"; }
            };
            _stroke = null;
        }
        return stroke;
    }

    public boolean isFillSelected() { return fillSelector.isSelected(); }
    public boolean isStrokeSelected() { return strokeSelector.isSelected(); }

    public void setSelectionColor(final Color selectionColor) {
        fillSelector.setSelectionColor(selectionColor);
        strokeSelector.setSelectionColor(selectionColor);
    }

    private TextField createSliderField(final String text) {
        TextField textField = new TextField(text);
        textField.setMinWidth(40);
        textField.setMaxWidth(40);
        textField.setPrefWidth(40);
        textField.setAlignment(Pos.CENTER_RIGHT);
        return textField;
    }

    private void setSliders(final Color color) {
        switch (colorModelChooser.getSelectionModel().getSelectedIndex()) {
            case 0: // RGB
                slider1.setValue(color.getRed() * 255);
                slider2.setValue(color.getGreen() * 255);
                slider3.setValue(color.getBlue() * 255);
                opacitySlider.setValue(color.getOpacity());
                break;
            case 1: // RGB Hex
                slider1.setValue(color.getRed() * 255);
                slider2.setValue(color.getGreen() * 255);
                slider3.setValue(color.getBlue() * 255);
                opacitySlider.setValue(color.getOpacity());
                break;
            case 2: // HSL
                double[] hsl = Helper.toHSL(color);
                slider1.setValue(hsl[0]);
                slider2.setValue(hsl[1] * 100.0);
                slider3.setValue(hsl[2] * 100.0);
                opacitySlider.setValue(color.getOpacity());
                break;
        }
    }

    private void updateSliders(final Color color) {
        double red   = color.getRed()   * 255;
        double green = color.getGreen() * 255;
        double blue  = color.getBlue()  * 255;
        switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
            case 0:
            case 1:
                slider1.setValue(red);
                slider2.setValue(green);
                slider3.setValue(blue);
                break;
            case 2:
                double[] hsl = Helper.toHSL(color);
                slider1.setValue(hsl[0]);
                slider2.setValue(hsl[1] * 100);
                slider3.setValue(hsl[2] * 100);
                break;
        }
    }

    private void updateSliderFromTextField(final TextField field, final Slider slider) {
        int value;
        switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
            case 0:
                value = Helper.clamp(0, 255, (int) Helper.getNumberFromText(field.getText()));
                field.setText(Integer.toString(value));
                slider.setValue(value);
                break;
            case 1:
                HEX_MATCHER.reset(field.getText());
                String result = "";
                try { while (HEX_MATCHER.find()) { result = HEX_MATCHER.group(0); } } catch (IllegalStateException ex) { result = "00"; }
                value = Integer.parseInt(result, 16);
                field.setText(result);
                slider.setValue(value);
                break;
            case 2:
                value = Helper.clamp(0, 100, (int) Helper.getNumberFromText(field.getText()));
                field.setText(Integer.toString(value));
                slider.setValue(value);
                break;
        }
    }


    // ******************** Event handling ************************************
    public void addColorChooserObserver(final ColorChooserObserver observer) { if (!observers.contains(observer)) { observers.add(observer); }}
    public void removeColorChooserObserver(final ColorChooserObserver observer) { if (observers.contains(observer)) { observers.remove(observer); }}

    public void fireColorChooserEvent(final ColorChooserEvent evt) { for (ColorChooserObserver observer : observers) { observer.onColorChooserEvent(evt); }}


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);
            pane.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            grid.setPrefSize(width, height);

            canvas.setWidth(width);
            canvas.setHeight(width / 5);

            redraw();
        }
    }

    private void redraw() {
        drawColorCanvas();
    }

    private void drawColorCanvas() {
        xStep = 360 / canvas.getWidth();
        yStep = 1 / canvas.getHeight();
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (int y = 0; y < canvas.getHeight() ; y++) {
            for (int x = 0; x < canvas.getWidth() ; x++) {
                ctx.setStroke(Helper.hslToRGB(x * xStep, 1, 1 - y * yStep));
                ctx.strokeLine(x, y, x, y);
            }
        }
    }
}
