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

import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * User: hansolo
 * Date: 22.10.18
 * Time: 20:43
 */
@DefaultProperty("children")
public class ColorSelector extends Region implements Toggle {
    private static final double                                  PREFERRED_WIDTH       = 70;
    private static final double                                  PREFERRED_HEIGHT      = 10;
    private static final double                                  MINIMUM_WIDTH         = 5;
    private static final double                                  MINIMUM_HEIGHT        = 5;
    private static final double                                  MAXIMUM_WIDTH         = 1024;
    private static final double                                  MAXIMUM_HEIGHT        = 1024;
    private static final PseudoClass                             SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private static final StyleablePropertyFactory<ColorSelector> FACTORY               = new StyleablePropertyFactory<>(Region.getClassCssMetaData());
    private static final CssMetaData<ColorSelector, Color>       SELECTION_COLOR       = FACTORY.createColorCssMetaData("-selection-color", s -> s.selectionColor, Color.web("#353535"), false);
    private        final StyleableProperty<Color>                selectionColor;
    private              double                                  size;
    private              double                                  width;
    private              double                                  height;
    private              Color                                   fill;
    private              Label                                   textLabel;
    private              Rectangle                               rectangle;
    private              HBox                                    pane;
    private              String                                  _text;
    private              StringProperty                          text;
    private              boolean                                 _selected;
    private              BooleanProperty                         selected;
    private              ToggleGroup                             _toggleGroup;
    private              ObjectProperty<ToggleGroup>             toggleGroup;
    private              ColorPicker                             colorPicker;
    private              List<ColorSelectorObserver>             observers;


    // ******************** Constructors **************************************
    public ColorSelector() {
        this("", Color.BLACK);
    }
    public ColorSelector(final String text, final Color fill) {
        getStylesheets().add(ColorSelector.class.getResource("colorselector.css").toExternalForm());
        this.fill      = fill;
        selectionColor = new SimpleStyleableObjectProperty<>(SELECTION_COLOR, this, "selectionColor");
        _text          = text;
        _selected      = false;
        _toggleGroup   = null;
        colorPicker    = new ColorPicker();
        observers      = new CopyOnWriteArrayList<>();

        setSelectionColor(SELECTION_COLOR.getInitialValue(ColorSelector.this));

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

        getStyleClass().setAll("color-selector");

        textLabel = new Label(getText());
        textLabel.setAlignment(Pos.CENTER_RIGHT);

        rectangle = new Rectangle(20, 10);

        colorPicker.setVisible(false);
        colorPicker.setManaged(false);

        pane = new HBox(5, textLabel, rectangle, colorPicker);
        pane.setAlignment(Pos.CENTER);

        pane.setPadding(new Insets(2));


        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        setOnMousePressed(e -> {
            if (null == getToggleGroup()) {
                setSelected(!isSelected());
            } else {
                getToggleGroup().selectToggle(ColorSelector.this);
            }
        });
        setOnMouseClicked(e -> { if (e.getClickCount() == 2) {
            colorPicker.setValue(getFill());
            colorPicker.show();
        } });
        selectedProperty().addListener((o, ov, nv) -> rectangle.setStroke(nv ? getSelectionColor() : Color.TRANSPARENT));

        colorPicker.valueProperty().addListener(o -> {
            setFill(colorPicker.getValue());
            fireColorSelectorEvent(new ColorSelectorEvent(ColorSelector.this, colorPicker.getValue()));
        });
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

    public Color getFill() { return fill; }
    public void setFill(final Color fill) {
        this.fill = fill;
        rectangle.setFill(fill);
    }

    public String getText() { return null == text ? _text : text.get(); }
    public void setText(final String text) {
        if (null == text) {
            _text = text;
            textLabel.setText(text);
        } else {
            this.text.set(text);
        }
    }
    public StringProperty textProperty() {
        if (null == text) {
            text = new StringPropertyBase(_text) {
                @Override protected void invalidated() { textLabel.setText(get()); }
                @Override public Object getBean() { return ColorSelector.this; }
                @Override public String getName() { return "text"; }
            };
            _text = null;
        }
        return text;
    }

    public Color getSelectionColor() { return selectionColor.getValue(); }
    public void setSelectionColor(final Color selectionColor) {
        this.selectionColor.setValue(selectionColor);
        this.colorPicker.setValue(selectionColor);
    }
    public ObjectProperty<Color> selectionColorProperty() { return (ObjectProperty<Color>) selectionColor; }

    @Override public ToggleGroup getToggleGroup() { return null == toggleGroup ? _toggleGroup : toggleGroup.get(); }
    @Override public void setToggleGroup(final ToggleGroup toggleGroup) {
        if (null == this.toggleGroup) {
            _toggleGroup = toggleGroup;
        } else {
            this.toggleGroup.set(toggleGroup);
        }
    }
    @Override public ObjectProperty<ToggleGroup> toggleGroupProperty() {
        if (null == toggleGroup) {
            toggleGroup = new ObjectPropertyBase<ToggleGroup>(_toggleGroup) {
                @Override public Object getBean() { return ColorSelector.this; }
                @Override public String getName() { return "toggleGroup"; }
            };
            _toggleGroup = null;
        }
        return toggleGroup;
    }

    @Override public boolean isSelected() { return null == selected ? _selected : selected.get(); }
    @Override public void setSelected(final boolean selected) {
        if (null == this.selected) {
            _selected = selected;

            ToggleGroup tg = getToggleGroup();
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
            if (tg != null) {
                if (selected) {
                    tg.selectToggle(ColorSelector.this);
                } else if (tg.getSelectedToggle() == ColorSelector.this) {
                    clearSelectedToggle();
                }
            }
            redraw();
        } else {
            this.selected.set(selected);
        }
    }
    @Override public BooleanProperty selectedProperty() {
        if (null == selected) {
            selected = new BooleanPropertyBase(_selected) {
                @Override protected void invalidated() {
                    ToggleGroup tg = getToggleGroup();
                    pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
                    notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
                    if (tg != null) {
                        if (get()) {
                            tg.selectToggle(ColorSelector.this);
                        } else if (tg.getSelectedToggle() == ColorSelector.this) {
                            clearSelectedToggle();
                        }
                    }
                }
                @Override public Object getBean() { return ColorSelector.this; }
                @Override public String getName() { return "selected";}
            };
        }
        return selected;
    }

    final void clearSelectedToggle() {
        if (!getToggleGroup().getSelectedToggle().isSelected()) {
            for (Toggle toggle: getToggleGroup().getToggles()) {
                if (toggle.isSelected()) { return; }
            }
        }
        getToggleGroup().selectToggle(null);
    }


    // ******************** Event handling ************************************
    public void addColorSelectorObserver(final ColorSelectorObserver observer) { if (!observers.contains(observer)) { observers.add(observer); }}
    public void removeColorSelectorObserver(final ColorSelectorObserver observer) { if (observers.contains(observer)) { observers.remove(observer); }}

    public void fireColorSelectorEvent(final ColorSelectorEvent evt) { for (ColorSelectorObserver observer : observers) { observer.onColorChanged(evt); }}


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            rectangle.setHeight(height * 0.5);

            redraw();
        }
    }

    private void redraw() {
        rectangle.setFill(getFill());
    }
}
