/*
 * Autopsy Forensic Browser
 *
 * Copyright 2019 Basis Technology Corp.
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
package org.sleuthkit.autopsy.communications;

import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.datamodel.BlackboardArtifactNode;
import org.sleuthkit.autopsy.datamodel.NodeProperty;
import org.sleuthkit.datamodel.BlackboardArtifact;
import static org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE.TSK_CONTACT;
import org.sleuthkit.datamodel.BlackboardAttribute;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_EMAIL;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_NAME;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_HOME;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_MOBILE;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_OFFICE;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_URL;
import static org.sleuthkit.datamodel.BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.DATETIME;
import org.sleuthkit.datamodel.Tag;
import org.sleuthkit.datamodel.TimeUtilities;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Extends BlackboardArtifactNode to override createSheet to create a contact
 * artifact specific sheet.
 */
final class ContactNode extends BlackboardArtifactNode {

    private static final Logger logger = Logger.getLogger(RelationshipNode.class.getName());

    @Messages({
        "ContactNode_Name=Name",
        "ContactNode_Phone=Phone Number",
        "ContactNode_Email=Email Address",
        "ContactNode_Mobile_Number=Mobile Number",
        "ContactNode_Office_Number=Office Number",
        "ContactNode_URL=URL",
        "ContactNode_Home_Number=Home Number",})

    ContactNode(BlackboardArtifact artifact) {
        super(artifact);

        setDisplayName(getAttributeDisplayString(artifact, TSK_NAME));
    }

    @Override
    protected Sheet createSheet() {
        final BlackboardArtifact artifact = getArtifact();
        BlackboardArtifact.ARTIFACT_TYPE fromID = BlackboardArtifact.ARTIFACT_TYPE.fromID(artifact.getArtifactTypeID());
        if (fromID != TSK_CONTACT) {
            return super.createSheet();
        }

        Sheet sheet = new Sheet();
        List<Tag> tags = getAllTagsFromDatabase();
        Sheet.Set sheetSet = sheet.get(Sheet.PROPERTIES);
        if (sheetSet == null) {
            sheetSet = Sheet.createPropertiesSet();
            sheet.put(sheetSet);
        }

        sheetSet.put(new NodeProperty<>("email", Bundle.ContactNode_Email(), "",
                getAttributeDisplayString(artifact, TSK_EMAIL))); //NON-NLS
        sheetSet.put(new NodeProperty<>("phone", Bundle.ContactNode_Phone(), "",
                getAttributeDisplayString(artifact, TSK_PHONE_NUMBER))); //NON-NLS
        sheetSet.put(new NodeProperty<>("mobile", Bundle.ContactNode_Mobile_Number(), "",
                getAttributeDisplayString(artifact, TSK_PHONE_NUMBER_MOBILE))); //NON-NLS
        sheetSet.put(new NodeProperty<>("home", Bundle.ContactNode_Home_Number(), "",
                getAttributeDisplayString(artifact, TSK_PHONE_NUMBER_HOME))); //NON-NLS
        sheetSet.put(new NodeProperty<>("office", Bundle.ContactNode_Office_Number(), "",
                getAttributeDisplayString(artifact, TSK_PHONE_NUMBER_OFFICE))); //NON-NLS
        sheetSet.put(new NodeProperty<>("url", Bundle.ContactNode_URL(), "",
                getAttributeDisplayString(artifact, TSK_URL))); //NON-NLS

        return sheet;
    }

    private static String getAttributeDisplayString(final BlackboardArtifact artifact, final BlackboardAttribute.ATTRIBUTE_TYPE attributeType) {
        try {
            BlackboardAttribute attribute = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.fromID(attributeType.getTypeID())));
            if (attribute == null) {
                return "";
            } else if (attributeType.getValueType() == DATETIME) {
                return TimeUtilities.epochToTime(attribute.getValueLong(),
                        TimeZone.getTimeZone(Utils.getUserPreferredZoneId()));
            } else {
                return attribute.getDisplayString();
            }
        } catch (TskCoreException tskCoreException) {
            logger.log(Level.WARNING, "Error getting attribute value.", tskCoreException); //NON-NLS
            return "";
        }
    }

    /**
     * Circumvent DataResultFilterNode's slightly odd delegation to
     * BlackboardArtifactNode.getSourceName().
     *
     * @return the displayName of this Node, which is the type.
     */
    @Override
    public String getSourceName() {
        return getDisplayName();
    }
}
