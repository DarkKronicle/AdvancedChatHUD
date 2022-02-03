/*
 * Copyright (C) 2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.util;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class FileUtil {
    // https://mkyong.com/java/how-to-find-files-with-certain-extension-only/
    public List<Path> getFilesWithExtension(Path directory, String extension) throws IOException {
        if (!directory.toFile().isDirectory()) {
            throw new IllegalArgumentException("Directory has to be directory!");
        }

        List<Path> paths;

        try (Stream<Path> walk = Files.walk(directory)) {
            paths = walk
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.toString().endsWith(extension))
                    .collect(Collectors.toList());
        }
        return paths;
    }

    public Optional<List<Path>> getFilesWithExtensionCaught(Path directory, String extension) {
        try {
            return Optional.of(getFilesWithExtension(directory, extension));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
