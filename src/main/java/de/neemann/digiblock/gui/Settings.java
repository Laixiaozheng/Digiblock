/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digiblock.gui;

import de.neemann.digiblock.core.element.ElementAttributes;
import de.neemann.digiblock.core.element.Key;
import de.neemann.digiblock.core.element.Keys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Settings of Digital
 * <p>
 * Created by Helmut.Neemann on 11.05.2016.
 */
public final class Settings extends SettingsBase {

    private static final class SettingsHolder {
        static final Settings INSTANCE = new Settings();
    }

    /**
     * Returns the settings instance
     *
     * @return the Settings
     */
    public static Settings getInstance() {
        return SettingsHolder.INSTANCE;
    }

    private Settings() {
        super(createKeyList(), ".digiblock.cfg");
    }

    private static List<Key> createKeyList() {
        List<Key> intList = new ArrayList<>();
        intList.add(Keys.SETTINGS_IEEE_SHAPES);
        intList.add(Keys.SETTINGS_LANGUAGE);
        intList.add(Keys.SETTINGS_EXPRESSION_FORMAT);
        intList.add(Keys.SETTINGS_DEFAULT_TREESELECT);
        intList.add(Keys.SETTINGS_GRID);
        intList.add(Keys.SETTINGS_SHOW_WIRE_BITS);
        intList.add(Keys.SETTINGS_NOTOOLTIPS);
        intList.add(Keys.SETTINGS_WIRETOOLTIP);
        intList.add(Keys.SETTINGS_LIBRARY_PATH);
        intList.add(Keys.SETTINGS_JAR_PATH);
        intList.add(Keys.SETTINGS_ATF1502_FITTER);
        intList.add(Keys.SETTINGS_ATMISP);
        intList.add(Keys.SETTINGS_GHDL_PATH);
        intList.add(Keys.SETTINGS_IVERILOG_PATH);
        intList.add(Keys.SETTINGS_GTKWAVE_PATH);
        intList.add(Keys.SETTINGS_TOOLCHAIN_CONFIG);
        intList.add(Keys.SETTINGS_FONT_SCALING);
        intList.add(Keys.SETTINGS_MAC_MOUSE);
        intList.add(Keys.SETTINGS_USE_EQUALS_KEY);
        intList.add(Keys.SETTINGS_SHOW_TUNNEL_RENAME_DIALOG);

        return Collections.unmodifiableList(intList);
    }

    /**
     * Returns true if the given modification requires a restart.
     *
     * @param modified the modified settings
     * @return true if the modification requires a restart
     */
    public boolean requiresRestart(ElementAttributes modified) {
        for (Key<?> key : getKeys())
            if (key.getRequiresRestart() && !getAttributes().equalsKey(key, modified))
                return true;

        return false;
    }

}
