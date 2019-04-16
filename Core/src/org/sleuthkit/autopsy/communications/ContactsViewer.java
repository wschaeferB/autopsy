/*
 * Autopsy Forensic Browser
 *
 * Copyright 2019 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obt ain a copy of the License at
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

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import static javax.swing.SwingUtilities.isDescendingFrom;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import static org.openide.explorer.ExplorerUtils.createLookup;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.corecomponents.TableFilterNode;
import org.sleuthkit.autopsy.directorytree.DataResultFilterNode;

/**
 * Visualization for contact nodes.
 *
 */
@ServiceProvider(service = RelationshipsViewer.class)
public final class ContactsViewer extends JPanel implements RelationshipsViewer, ExplorerManager.Provider, Lookup.Provider {

    private final ExplorerManager tableEM;
    private final Outline outline;
    private final ModifiableProxyLookup proxyLookup;
    private final PropertyChangeListener focusPropertyListener;
    private final ContactsChildNodeFactory nodeFactory;

    @NbBundle.Messages({
        "ContactsViewer_tabTitle=Contacts",
        "ContactsViewer_columnHeader_Name=Name",
        "ContactsViewer_columnHeader_Phone=Phone",
        "ContactsViewer_columnHeader_Email=Email",})

    /**
     * Visualization for contact nodes.
     */
    public ContactsViewer() {
        tableEM = new ExplorerManager();
        proxyLookup = new ModifiableProxyLookup(createLookup(tableEM, getActionMap()));
        nodeFactory = new ContactsChildNodeFactory(null);

        // See org.sleuthkit.autopsy.timeline.TimeLineTopComponent for a detailed
        // explaination of focusPropertyListener
        focusPropertyListener = (final PropertyChangeEvent focusEvent) -> {
            if (focusEvent.getPropertyName().equalsIgnoreCase("focusOwner")) {
                final Component newFocusOwner = (Component) focusEvent.getNewValue();

                if (newFocusOwner == null) {
                    return;
                }
                if (isDescendingFrom(newFocusOwner, contactPane)) {
                    //if the focus owner is within the MessageContentViewer (the attachments table)
                    proxyLookup.setNewLookups(createLookup(contactPane.getExplorerManager(), getActionMap()));
                } else if (isDescendingFrom(newFocusOwner, ContactsViewer.this)) {
                    //... or if it is within the Results table.
                    proxyLookup.setNewLookups(createLookup(tableEM, getActionMap()));

                }
            }
        };

        initComponents();

        outline = outlineView.getOutline();
        outlineView.setPropertyColumns(
                "email", Bundle.ContactsViewer_columnHeader_Email(),
                "phone", Bundle.ContactsViewer_columnHeader_Phone()
        );
        outline.setRootVisible(false);
        outline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ((DefaultOutlineModel) outline.getOutlineModel()).setNodesColumnLabel(Bundle.ContactsViewer_columnHeader_Name());

        tableEM.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                final Node[] nodes = tableEM.getSelectedNodes();

                if (nodes != null && nodes.length > 0) {
                    contactPane.setEnabled(true);
                    contactPane.setNode(nodes);
                }
            }
        });
        
        tableEM.setRootContext(new TableFilterNode(new DataResultFilterNode(new AbstractNode(Children.create(nodeFactory, true)), getExplorerManager()), true));
    }

    @Override
    public String getDisplayName() {
        return Bundle.ContactsViewer_tabTitle();
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    @Override
    public void setSelectionInfo(SelectionInfo info) {
        contactPane.setNode(new Node[]{new AbstractNode(Children.LEAF)});
        contactPane.setEnabled(false);
        
        nodeFactory.refresh(info);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return tableEM;
    }

    @Override
    public Lookup getLookup() {
        return proxyLookup;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        //add listener that maintains correct selection in the Global Actions Context
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addPropertyChangeListener("focusOwner", focusPropertyListener);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .removePropertyChangeListener("focusOwner", focusPropertyListener);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        outlineView = new org.openide.explorer.view.OutlineView();
        contactPane = new org.sleuthkit.autopsy.communications.ContactDetailsPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(outlineView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(contactPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(outlineView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(contactPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.sleuthkit.autopsy.communications.ContactDetailsPane contactPane;
    private org.openide.explorer.view.OutlineView outlineView;
    // End of variables declaration//GEN-END:variables
}
