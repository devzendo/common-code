/*
 * Copyright (C) 2008-2017 Matt Gumbley, DevZendo.org http://devzendo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devzendo.commoncode.prefs;

import org.devzendo.commoncode.string.StringUtils;

import java.io.File;

public class PrefsFactory {
    private final File absolutePrefsDir;
    private final File absolutePrefsFile;

    public PrefsFactory(final File homeDir, final String prefsSubDir, final String prefsFile) {
        absolutePrefsDir = new File(StringUtils.slashTerminate(homeDir.getAbsolutePath()) + prefsSubDir);
        absolutePrefsFile = new File(StringUtils.slashTerminate(absolutePrefsDir.getAbsolutePath()) + prefsFile);
    }

    public PrefsFactory(final String prefsSubDir, final String prefsFile) {
        this(new File(System.getProperty("user.home")), prefsSubDir, prefsFile);
    }

    public boolean prefsDirectoryExists() {
        return absolutePrefsDir.exists();
    }

    public boolean createPrefsDirectory() {
        return absolutePrefsDir.mkdir();
    }

    public File getPrefsDir() {
        return absolutePrefsDir;
    }

    public File getPrefsFile() {
        return absolutePrefsFile;
    }
}
