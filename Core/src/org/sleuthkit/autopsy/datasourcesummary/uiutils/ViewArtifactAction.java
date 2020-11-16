/*
 * Autopsy Forensic Browser
 *
 * Copyright 2020 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
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
package org.sleuthkit.autopsy.datasourcesummary.uiutils;

import org.sleuthkit.autopsy.directorytree.DirectoryTreeTopComponent;
import org.sleuthkit.datamodel.BlackboardArtifact;

/**
 * Action that navigates to an artifact in a tree.
 */
public class ViewArtifactAction implements Runnable {

    private final BlackboardArtifact artifact;

    /**
     * Main constructor for this action.
     *
     * @param artifact The artifact that will be displayed in tree
     * DirectoryTreeTopComponent.
     */
    public ViewArtifactAction(BlackboardArtifact artifact) {
        this.artifact = artifact;
    }

    @Override
    public void run() {
        final DirectoryTreeTopComponent dtc = DirectoryTreeTopComponent.findInstance();

        // Navigate to the source context artifact.
        if (dtc != null && artifact != null) {
            dtc.viewArtifact(artifact);
        }
    }

}
