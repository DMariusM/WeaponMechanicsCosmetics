/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.commands;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.skin.SkinList;

public class SkinCommand {

    public static void registerPermissions() {
        Configuration config = WeaponMechanics.getConfigurations();

        for (String weaponTitle : WeaponMechanics.getWeaponHandler().getInfoHandler().getSortedWeaponList()) {
            SkinList skins = config.getObject(weaponTitle + ".Skin", SkinList.class);

        }
    }

    public static void register() {

    }
}
