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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * User: hansolo
 * Date: 22.10.18
 * Time: 19:50
 */
@DefaultProperty("children")
public class ColorChooser extends Region {
    private static final double                PREFERRED_WIDTH  = 250;
    private static final double                PREFERRED_HEIGHT = 250;
    private static final double                MINIMUM_WIDTH    = 50;
    private static final double                MINIMUM_HEIGHT   = 50;
    private static final double                MAXIMUM_WIDTH    = 1024;
    private static final double                MAXIMUM_HEIGHT   = 1024;
    private static final Pattern               HEX_PATTERN      = Pattern.compile("#?([A-Fa-f0-9]{2})");
    private static final Matcher               HEX_MATCHER      = HEX_PATTERN.matcher("");
    private static       double                aspectRatio;
    private              boolean               keepAspect;
    private              double                size;
    private              double                width;
    private              double                height;
    private              GridPane              grid;
    private              PaintSelector         fillCircle;
    private              PaintSelector         strokeCircle;
    private              ChoiceBox             colorModelChooser;
    private              ComboBox              opacityChooser;
    private              Label                 slider1Label;
    private              Slider                slider1;
    private              TextField             slider1Field;
    private              Label                 slider2Label;
    private              Slider                slider2;
    private              TextField             slider2Field;
    private              Label                 slider3Label;
    private              Slider                slider3;
    private              TextField             slider3Field;
    private              Canvas                canvas;
    private              GraphicsContext       ctx;
    private              TextField             colorField;
    private              Slider                opacitySlider;
    private              Circle                opacity0;
    private              Circle                opacity1;
    private              Pane                  pane;
    private              ObjectProperty<Paint> fill;
    private              ObjectProperty<Paint> stroke;
    private              double                xStep;
    private              double                yStep;


    // ******************** Constructors **************************************
    public ColorChooser() {
        getStylesheets().add(ColorChooser.class.getResource("colorchooser.css").toExternalForm());
        aspectRatio = PREFERRED_HEIGHT / PREFERRED_WIDTH;
        keepAspect  = false;
        fill        = new ObjectPropertyBase<Paint>(Color.BLACK) {
            @Override protected void invalidated() {
                fillCircle.setFill(get());
                fillCircle.setStroke(get());
                colorField.setText(get().toString().replace("0x", "#").substring(0, 7));
                //opacitySlider.setValue(((Color) get()).getOpacity());
                System.out.println(((Color) get()).getOpacity());
            }
            @Override public Object getBean() { return ColorChooser.this; }
            @Override public String getName() { return "fill"; }
        };
        stroke      = new ObjectPropertyBase<Paint>(Color.BLACK) {
            @Override protected void invalidated() {
                strokeCircle.setStroke(get());
                colorField.setText(get().toString().replace("0x", "#").substring(0, 7));
                //opacitySlider.setValue(((Color) get()).getOpacity());
                System.out.println(((Color) get()).getOpacity());
            }
            @Override public Object getBean() { return ColorChooser.this; }
            @Override public String getName() { return "stroke"; }
        };

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

        fillCircle = new PaintSelector(Color.BLACK, Color.BLACK);
        fillCircle.setFill(getFill());
        fillCircle.setStroke(getFill());
        fillCircle.setToggleGroup(fillStrokeGroup);
        fillCircle.setSelected(true);
        fillCircle.setMinSize(16, 16);
        fillCircle.setMaxSize(16, 16);
        fillCircle.setPrefSize(16, 16);

        strokeCircle = new PaintSelector(Color.TRANSPARENT, Color.BLACK);
        strokeCircle.setStroke(getStroke());
        strokeCircle.setFill(Color.TRANSPARENT);
        strokeCircle.setToggleGroup(fillStrokeGroup);
        strokeCircle.setMinSize(16, 16);
        strokeCircle.setMaxSize(16, 16);
        strokeCircle.setPrefSize(16, 16);

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
        opacity0.setStroke(Color.BLACK);
        opacity1 = new Circle(5);
        opacity1.setFill(Color.BLACK);
        opacity1.setStroke(Color.BLACK);
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

        grid.add(fillCircle, 0, 0);
        grid.add(strokeCircle, 1, 0);
        grid.add(colorModelChooser, 2, 0);
        grid.add(slider1Box, 0, 1);
        grid.add(slider2Box, 0, 2);
        grid.add(slider3Box, 0, 3);
        grid.add(colorField, 0, 4);
        grid.add(canvas, 0, 5);
        grid.add(opacityLabel, 0, 6);
        grid.add(opacityBox, 0, 7);

        GridPane.setFillWidth(fillCircle, true);
        GridPane.setFillWidth(strokeCircle, true);
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

        fillCircle.selectedProperty().addListener((o, ov, nv) -> {
            Color fillColor = (Color) fillCircle.getFill();
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
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
        });
        strokeCircle.selectedProperty().addListener((o, ov, nv) -> {
            Color strokeColor = (Color) strokeCircle.getStroke();
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
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
        });

        colorModelChooser.getSelectionModel().selectedIndexProperty().addListener((o, ov, nv) -> {
            Color color = fillCircle.isSelected() ? (Color) getFill() : (Color) getStroke();
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
                    slider1Field.setText(Integer.toHexString((int) (Math.round(color.getRed() * 255.0))));
                    slider1.setValue(Math.round(color.getRed() * 255.0));
                    slider2.setMax(255);
                    slider2Label.setText("G");
                    slider2Field.setText(Integer.toHexString((int) (Math.round(color.getGreen() * 255.0))));
                    slider2.setValue(Math.round(color.getGreen() * 255.0));
                    slider3.setMax(255);
                    slider3Label.setText("B");
                    slider3Field.setText(Integer.toHexString((int) (Math.round(color.getBlue() * 255.0))));
                    slider3.setValue(Math.round(color.getBlue() * 255.0));
                    break;
                case 2: // HSL
                    double[] hsl = Helper.toHSL(color);
                    slider1.setMax(360);
                    slider1Label.setText("H");
                    slider1Field.setText(Integer.toString((int) (hsl[0])));
                    slider1.setValue(hsl[0]);
                    slider2.setMax(100);
                    slider2Label.setText("S");
                    slider2Field.setText(Integer.toString((int) Math.round(hsl[1] * 100.0)));
                    slider2.setValue(Math.round(hsl[1] * 100.0));
                    slider3.setMax(100);
                    slider3Label.setText("L");
                    slider3Field.setText(Integer.toString((int) (Math.round(hsl[2] * 100.0))));
                    slider3.setValue(Math.round(hsl[2] * 100.0));
                    break;
            }
        });

        slider1.valueProperty().addListener((o, ov, nv) -> {
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
                case 0: // RGB
                    slider1Field.setText(Integer.toString((int) (slider1.getValue())));
                    if (fillCircle.isSelected()) {
                        fill.set(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    } else {
                        stroke.set(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    }
                    break;
                case 1: // RGB Hex
                    slider1Field.setText(Integer.toHexString((int) (slider1.getValue())));
                    if (fillCircle.isSelected()) {
                        fill.set(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    } else {
                        stroke.set(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    }
                    break;
                case 2: // HSL
                    slider1Field.setText(Integer.toString((int) (slider1.getValue())));
                    if (fillCircle.isSelected()) {
                        Color color = Helper.hslToRGB(slider1.getValue(), (slider2.getValue() / 100.0), (slider3.getValue() / 100.0), opacitySlider.getValue());
                        fill.set(color);
                    } else {
                        Color color = Helper.hslToRGB(slider1.getValue(), (slider2.getValue() / 100.0), (slider3.getValue() / 100.0), opacitySlider.getValue());
                        stroke.set(color);
                    }
                    break;
            }
        });
        slider2.valueProperty().addListener((o, ov, nv) -> {
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
                case 0: // RGB
                    slider2Field.setText(Integer.toString((int) (slider2.getValue())));
                    if (fillCircle.isSelected()) {
                        fill.set(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    } else {
                        stroke.set(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    }
                    break;
                case 1: // RGB Hex
                    slider2Field.setText(Integer.toHexString((int) (slider2.getValue())));
                    if (fillCircle.isSelected()) {
                        fill.set(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    } else {
                        stroke.set(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    }
                    break;
                case 2: // HSL
                    slider2Field.setText(Integer.toString((int) (slider2.getValue())));
                    if (fillCircle.isSelected()) {
                        Color color = Helper.hslToRGB(slider1.getValue(), (slider2.getValue() / 100.0), (slider3.getValue() / 100.0), opacitySlider.getValue());
                        fill.set(color);
                    } else {
                        Color color = Helper.hslToRGB(slider1.getValue(), (slider2.getValue() / 100.0), (slider3.getValue() / 100.0), opacitySlider.getValue());
                        stroke.set(color);
                    }
                    break;
            }
        });
        slider3.valueProperty().addListener((o, ov, nv) -> {
            switch(colorModelChooser.getSelectionModel().getSelectedIndex()) {
                case 0: // RGB
                    slider3Field.setText(Integer.toString((int) (slider3.getValue())));
                    if (fillCircle.isSelected()) {
                        fill.set(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    } else {
                        stroke.set(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    }
                    break;
                case 1: // RGB Hex
                    slider3Field.setText(Integer.toHexString((int) (slider3.getValue())));
                    if (fillCircle.isSelected()) {
                        fill.set(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    } else {
                        stroke.set(Color.rgb((int) slider1.getValue(), (int) slider2.getValue(), (int) slider3.getValue(), opacitySlider.getValue()));
                    }
                    break;
                case 2: // HSL
                    slider3Field.setText(Integer.toString((int) (slider3.getValue())));
                    if (fillCircle.isSelected()) {
                        Color color = Helper.hslToRGB(slider1.getValue(), (slider2.getValue() / 100.0), (slider3.getValue() / 100.0), opacitySlider.getValue());
                        fill.set(color);
                    } else {
                        Color color = Helper.hslToRGB(slider1.getValue(), (slider2.getValue() / 100.0), (slider3.getValue() / 100.0), opacitySlider.getValue());
                        stroke.set(color);
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

        colorField.textProperty().addListener(o -> {
            String hexColor = Helper.getHexColorFromString(colorField.getText());
            if (null == hexColor || hexColor.isEmpty()) { return; }
            updateSliders(Color.web(hexColor));
        });

        canvas.setOnMousePressed(e -> setColorByCanvas(e.getSceneX(), e.getSceneY()));
        canvas.setOnMouseDragged(e -> setColorByCanvas(e.getSceneX(), e.getSceneY()));

        opacitySlider.valueProperty().addListener((o, ov, nv) -> opacityChooser.getEditor().setText(String.format(Locale.US, "%.0f%%", (nv.doubleValue() * 100))));
        opacityChooser.getEditor().textProperty().addListener(o -> {
            double value = Helper.getNumberFromText(opacityChooser.getEditor().getText());
            value = Helper.clamp(0, 100, value);
            opacitySlider.setValue(value / 100);
            if (fillCircle.isSelected()) {
                setFill(Helper.getColorWithOpacity((Color) getFill(), opacitySlider.getValue()));
            } else {
                setStroke(Helper.getColorWithOpacity((Color) getStroke(), opacitySlider.getValue()));
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

    public Paint getFill() { return fill.get(); }
    public void setFill(final Paint fill) { this.fill.set(fill); }
    public ObjectProperty<Paint> fillProperty() { return fill; }

    public Paint getStroke() { return stroke.get(); }
    public void setStroke(final Paint stroke) { this.stroke.set(stroke); }
    public ObjectProperty<Paint> strokeProperty() { return stroke; }

    private TextField createSliderField(final String text) {
        TextField textField = new TextField(text);
        textField.setMinWidth(40);
        textField.setMaxWidth(40);
        textField.setPrefWidth(40);
        textField.setAlignment(Pos.CENTER_RIGHT);
        return textField;
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


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size  = width < height ? width : height;

        if (keepAspect) {
            if (aspectRatio * width > height) {
                width = 1 / (aspectRatio / height);
            } else if (1 / (aspectRatio / height) > width) {
                height = aspectRatio * width;
            }
        }

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
