/*
 * Copyright (C) 2015 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.playonlinux.utils;

import com.playonlinux.app.PlayOnLinuxException;
import net.sf.jmimemagic.*;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class MimeType {
    private MimeType() {
        // Utility class
    }

    public static String getMimetype(File inputFile) throws PlayOnLinuxException {
        Path path = Paths.get(inputFile.getAbsolutePath());

        try {
            byte[] data = Files.readAllBytes(path);
            MagicMatch match = Magic.getMagicMatch(data);
            return match.getMimeType();
        } catch (MagicMatchNotFoundException e) {
            final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            return mimeTypesMap.getContentType(inputFile);
        } catch (MagicException | MagicParseException | IOException e) {
            throw new PlayOnLinuxException("Unable to detect mimetype of the file", e);
        }
    }
}
