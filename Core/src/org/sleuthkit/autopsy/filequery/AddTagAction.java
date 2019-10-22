/*
 * Autopsy
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
package org.sleuthkit.autopsy.filequery;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.sleuthkit.autopsy.actions.GetTagNameAndCommentDialog;
import org.sleuthkit.autopsy.actions.GetTagNameDialog;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.services.TagsManager;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.ContentTag;
import org.sleuthkit.datamodel.TagName;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;

/**
 * Instances of this Action allow users to apply tags to content.
 */
public class AddTagAction extends AbstractAction {

    private static final Logger LOGGER = Logger.getLogger(AddTagAction.class.getName());

    private final Set<AbstractFile> selectedFileIDs;
    private final TagName tagName;

    AddTagAction(TagName tagName, Set<AbstractFile> selectedFileIDs, String notableString) {
        super(tagName.getDisplayName() + notableString);
        this.selectedFileIDs = selectedFileIDs;
        this.tagName = tagName;
    }

    static public JMenu getTagMenu(Set<AbstractFile> selectedFiles) {
        return new TagMenu(selectedFiles, Case.getCurrentCase().getServices().getTagsManager());
    }

    private void addTagWithComment(String comment) {
        addTagsToFiles(tagName, comment, selectedFileIDs);
    }

    @NbBundle.Messages({"# {0} - fileID",
        "AddDrawableTagAction.addTagsToFiles.alert=Unable to tag file {0}."})
    private void addTagsToFiles(TagName tagName, String comment, Set<AbstractFile> selectedFiles) {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                // check if the same tag is being added for the same abstract file.
                TagsManager tagsManager = Case.getCurrentCase().getServices().getTagsManager();
                for (AbstractFile content : selectedFiles) {
                    try {
                        LOGGER.log(Level.INFO, "tagging {0} with {1} and comment {2}", new Object[]{content.getName(), tagName.getDisplayName(), comment}); //NON-NLS

                        List<ContentTag> contentTags = tagsManager.getContentTagsByContent(content);
                        Optional<TagName> duplicateTagName = contentTags.stream()
                                .map(ContentTag::getName)
                                .filter(tagName::equals)
                                .findAny();

                        if (duplicateTagName.isPresent()) {
                            LOGGER.log(Level.INFO, "{0} already tagged as {1}. Skipping.", new Object[]{content.getName(), tagName.getDisplayName()}); //NON-NLS
                        } else {
                            LOGGER.log(Level.INFO, "Tagging {0} as {1}", new Object[]{content.getName(), tagName.getDisplayName()}); //NON-NLS
                            tagsManager.addContentTag(content, tagName, comment);
                        }

                    } catch (TskCoreException tskCoreException) {
                        LOGGER.log(Level.SEVERE, "Error tagging file", tskCoreException); //NON-NLS
                        Platform.runLater(()
                                -> new Alert(Alert.AlertType.ERROR, Bundle.AddDrawableTagAction_addTagsToFiles_alert(content.getId())).show()
                        );
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                super.done();
                try {
                    get();
                } catch (InterruptedException | ExecutionException ex) {
                    LOGGER.log(Level.SEVERE, "unexpected exception while tagging files", ex); //NON-NLS
                }
            }
        }.execute();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        addTagWithComment("");
    }

    @NbBundle.Messages({"AddTagAction.menuItem.quickTag=Quick Tag",
        "AddTagAction.menuItem.noTags=No tags",
        "AddTagAction.menuItem.newTag=New Tag...",
        "AddTagAction.menuItem.tagAndComment=Tag and Comment...",
        "AddDrawableTagAction.displayName.plural=Tag Files",
        "AddDrawableTagAction.displayName.singular=Tag File"})
    private final static class TagMenu extends JMenu {

        TagMenu(Set<AbstractFile> selectedFiles, TagsManager manager) {
            setText(selectedFiles.size() > 1
                    ? Bundle.AddDrawableTagAction_displayName_plural()
                    : Bundle.AddDrawableTagAction_displayName_singular());

            // Create a "Quick Tag" sub-menu.
            JMenu quickTagMenu = new JMenu(Bundle.AddTagAction_menuItem_quickTag());
            add(quickTagMenu);

            /*
             * Each non-Category tag name in the current set of tags gets its
             * own menu item in the "Quick Tags" sub-menu. Selecting one of
             * these menu items adds a tag with the associated tag name.
             */
            Collection<TagName> tagNames;
            try {
                tagNames = manager.getAllTagNames();
            } catch (TskCoreException ex) {
                tagNames = Collections.EMPTY_SET;
            }
            if (tagNames.isEmpty()) {
                JMenuItem empty = new JMenuItem(Bundle.AddTagAction_menuItem_noTags());
                empty.setEnabled(false);
                add(empty);
            } else {
                for (TagName tName : tagNames) {
                    String notableString = tName.getKnownStatus() == TskData.FileKnown.BAD ? TagsManager.getNotableTagLabel() : "";
                    quickTagMenu.add(new AddTagAction(tName, selectedFiles, notableString));
                }

            }

            /*
             * The "Quick Tag" menu also gets an "New Tag..." menu item.
             * Selecting this item initiates a dialog that can be used to create
             * or select a tag name and adds a tag with the resulting name.
             */
            JMenuItem newTagMenuItem = new JMenuItem(new AbstractAction(Bundle.AddTagAction_menuItem_newTag()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        TagName tName = GetTagNameDialog.doDialog(getDiscoveryWindow());
                        if (tName != null) {
                            String notableString = tName.getKnownStatus() == TskData.FileKnown.BAD ? TagsManager.getNotableTagLabel() : "";
                            new AddTagAction(tName, selectedFiles, notableString).actionPerformed(e);
                        }
                    });
                }
            });

            quickTagMenu.add(newTagMenuItem);

            /*
             * Create a "Tag and Comment..." menu item. Selecting this item
             * initiates a dialog that can be used to create or select a tag
             * name with an optional comment and adds a tag with the resulting
             * name.
             */
            JMenuItem tagAndCommentItem = new JMenuItem(new AbstractAction(Bundle.AddTagAction_menuItem_tagAndComment()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        GetTagNameAndCommentDialog.TagNameAndComment tagNameAndComment = GetTagNameAndCommentDialog.doDialog(getDiscoveryWindow());
                        if (tagNameAndComment != null) {
                            String notableString = tagNameAndComment.getTagName().getKnownStatus() == TskData.FileKnown.BAD ? TagsManager.getNotableTagLabel() : "";
                            new AddTagAction(tagNameAndComment.getTagName(), selectedFiles, notableString).addTagWithComment(tagNameAndComment.getComment());
                        }
                    });
                }
            });
            
            add(tagAndCommentItem);
        }
    }

    static private Window getDiscoveryWindow() {
        TopComponent etc = DiscoveryTopComponent.getTopComponent();
        return SwingUtilities.getWindowAncestor(etc);
    }
}
