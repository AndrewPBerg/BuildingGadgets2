package com.direwolf20.buildinggadgets2.client.screen.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;

public class GuiTextFieldBase extends EditBox {
    private boolean suspended;
    private String valueDefault, valueOld;
    private BiConsumer<GuiTextFieldBase, String> postModification;
    private boolean numericOnly;

    public GuiTextFieldBase(Font fontRenderer, int x, int y, int width) {
        super(fontRenderer, x, y, width, 15, Component.empty());

        setMaxLength(50);
        valueOld = getValue();
        setResponder(this::onValueChanged);
    }

    private void onValueChanged(String value) {
        if (suspended) return;
        if (numericOnly && !isValidNumber(value)) {
            suspended = true;
            super.setValue(valueOld);
            suspended = false;
            return;
        }
        String previous = valueOld;
        valueOld = value;
        if (postModification != null) {
            suspended = true;
            postModification.accept(this, previous);
            suspended = false;
        }
    }

    private boolean isValidNumber(String value) {
        if (value == null || value.isEmpty() || "-".equals(value)) return true;
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public GuiTextFieldBase restrictToNumeric() {
        numericOnly = true;
        return this;
    }

    public int getInt() {
        try {
            return Integer.parseInt(getValue());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public GuiTextFieldBase setDefaultInt(int defaultInt) {
        return setDefaultValue(Integer.toString(defaultInt));
    }

    public GuiTextFieldBase setDefaultValue(String defaultValue) {
        this.valueDefault = defaultValue;
        return this;
    }
}
