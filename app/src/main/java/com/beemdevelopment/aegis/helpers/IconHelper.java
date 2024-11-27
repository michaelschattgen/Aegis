package com.beemdevelopment.aegis.helpers;

import com.beemdevelopment.aegis.icons.IconPack;
import com.beemdevelopment.aegis.icons.IconPackManager;
import com.beemdevelopment.aegis.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class IconHelper {

    public static byte[] readIcon(File file) {
        try (FileInputStream inStream = new FileInputStream(file)) {
            return IOUtils.readFile(inStream);
        } catch (IOException e) {
            throw new RuntimeException("Error reading icon file", e);
        }
    }

    public static List<IconPack.Icon> getSuggestedIcons(IconPackManager iconPackManager, String issuer) {
        Optional<IconPack> firstIconPack = iconPackManager.getIconPacks().stream()
                .sorted(Comparator.comparing(IconPack::getName))
                .findFirst();

        if (!firstIconPack.isPresent()) {
            throw new RuntimeException("No icon packs found");
        }

        return firstIconPack.map(iconPack -> iconPack.getSuggestedIcons(issuer))
                .orElse(Collections.emptyList());
    }

    public static IconPack.Icon getSuggestedIcon(IconPackManager iconPackManager, String issuer) {
        List<IconPack.Icon> icons = getSuggestedIcons(iconPackManager, issuer);

        if (!icons.isEmpty()) {
            return icons.get(0);
        }

        return null;
    }
}
