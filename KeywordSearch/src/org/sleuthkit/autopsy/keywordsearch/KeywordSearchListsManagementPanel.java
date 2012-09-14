/*
 * Autopsy Forensic Browser
 * 
 * Copyright 2011 Basis Technology Corp.
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

/*
 * KeywordSearchListImportExportForm.java
 *
 * Created on Feb 10, 2012, 4:04:13 PM
 */
package org.sleuthkit.autopsy.keywordsearch;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dfickling
 */
class KeywordSearchListsManagementPanel extends javax.swing.JPanel {

    private Logger logger = Logger.getLogger(KeywordSearchListsManagementPanel.class.getName());
    private KeywordListTableModel tableModel;
    
    //private static KeywordSearchListsManagementPanel instance = null;
    
    /** Creates new form KeywordSearchListImportExportForm */
    KeywordSearchListsManagementPanel() {
        tableModel = new KeywordListTableModel();
        initComponents();
        customizeComponents();
    }
    
    /*public static synchronized KeywordSearchListsManagementPanel getDefault() {
        if (instance == null) {
            instance = new KeywordSearchListsManagementPanel();
        }
        return instance;
    }*/
    
    private void customizeComponents() {


        listsTable.setAutoscrolls(true);
        listsTable.setTableHeader(null);
        listsTable.setShowHorizontalLines(false);
        listsTable.setShowVerticalLines(false);

        listsTable.getParent().setBackground(listsTable.getBackground());

        listsTable.setCellSelectionEnabled(false);
        listsTable.setRowSelectionAllowed(true);
        tableModel.resync();
        
        KeywordSearchListsXML.getCurrent().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(KeywordSearchListsXML.ListsEvt.LIST_ADDED.toString())) {
                    tableModel.resync();
                    for(int i = 0; i<listsTable.getRowCount(); i++) {
                            String name = (String) listsTable.getValueAt(i, 0);
                            if(((String) evt.getNewValue()).equals(name)) {
                                listsTable.getSelectionModel().setSelectionInterval(i, i);
                            }
                    }
                } else if (evt.getPropertyName().equals(KeywordSearchListsXML.ListsEvt.LIST_DELETED.toString())) {
                    tableModel.resync();
                    if(listsTable.getRowCount() > 0) {
                        listsTable.getSelectionModel().setSelectionInterval(0, 0);
                    } else {
                        listsTable.getSelectionModel().clearSelection();
                    }
                }
            }
        });

    }
    
    void reload() {
        listsTable.clearSelection();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        listsTable = new javax.swing.JTable();
        newListButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(200, 0));
        setPreferredSize(new java.awt.Dimension(250, 492));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 402));

        listsTable.setModel(tableModel);
        listsTable.setShowHorizontalLines(false);
        listsTable.setShowVerticalLines(false);
        listsTable.getTableHeader().setReorderingAllowed(false);
        listsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                listsTableKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(listsTable);

        newListButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/keywordsearch/new16.png"))); // NOI18N
        newListButton.setText(org.openide.util.NbBundle.getMessage(KeywordSearchListsManagementPanel.class, "KeywordSearchListsManagementPanel.newListButton.text")); // NOI18N
        newListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newListButtonActionPerformed(evt);
            }
        });

        importButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/keywordsearch/import16.png"))); // NOI18N
        importButton.setText(org.openide.util.NbBundle.getMessage(KeywordSearchListsManagementPanel.class, "KeywordSearchListsManagementPanel.importButton.text")); // NOI18N
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(KeywordSearchListsManagementPanel.class, "KeywordSearchListsManagementPanel.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(newListButton, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(importButton, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newListButton)
                    .addComponent(importButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newListButtonActionPerformed
        KeywordSearchListsXML writer = KeywordSearchListsXML.getCurrent();
        String listName = (String) JOptionPane.showInputDialog(null, "New keyword list name:", "New Keyword List", JOptionPane.PLAIN_MESSAGE, null, null, "");
        if (listName == null || listName.trim().equals("")) {
            return;
        }
        boolean shouldAdd = false;
        if (writer.listExists(listName)) {
            if (writer.getList(listName).isLocked() ) {
                boolean replace = KeywordSearchUtil.displayConfirmDialog("New Keyword List", "Keyword List <" + listName 
                        + "> already exists as a read-only list. Do you want to replace it for the duration of the program (the change will not be persistent).", KeywordSearchUtil.DIALOG_MESSAGE_TYPE.WARN);
                if (replace) {
                    shouldAdd = true;
                }
            }
            else {
                boolean replace = KeywordSearchUtil.displayConfirmDialog("New Keyword List", "Keyword List <" + listName + "> already exists, do you want to replace it?", KeywordSearchUtil.DIALOG_MESSAGE_TYPE.WARN);
                if (replace) {
                    shouldAdd = true;
                }
            }
        } else {
            shouldAdd = true;
        }
        if (shouldAdd) {
            writer.addList(listName, new ArrayList<Keyword>());
        }
        for (int i = 0; i < listsTable.getRowCount(); i++) {
            if (listsTable.getValueAt(i, 0).equals(listName)) {
                listsTable.getSelectionModel().addSelectionInterval(i, i);
            }
        }
    }//GEN-LAST:event_newListButtonActionPerformed

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed

        final String FEATURE_NAME = "Keyword List Import";

        JFileChooser chooser = new JFileChooser();
        final String[] EXTENSION = new String[]{"xml", "txt"};
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Keyword List File", EXTENSION);
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selFile = chooser.getSelectedFile();
            if (selFile == null) {
                return;
            }

            //force append extension if not given
            String fileAbs = selFile.getAbsolutePath();
            
            final KeywordSearchListsAbstract reader;
            
            if(KeywordSearchUtil.isXMLList(fileAbs)) {
                reader = new KeywordSearchListsXML(fileAbs);
            } else {
                reader = new KeywordSearchListsEncase(fileAbs);
            }
            
            if (!reader.load()) {
                KeywordSearchUtil.displayDialog(FEATURE_NAME, "Error importing keyword list from file " + fileAbs, KeywordSearchUtil.DIALOG_MESSAGE_TYPE.ERROR);
                return;
            }

            List<KeywordSearchList> toImport = reader.getListsL();
            List<KeywordSearchList> toImportConfirmed = new ArrayList<KeywordSearchList>();

            final KeywordSearchListsXML writer = KeywordSearchListsXML.getCurrent();

            for (KeywordSearchList list : toImport) {
                //check name collisions
                if (writer.listExists(list.getName())) {
                    Object[] options = {"Yes, overwrite",
                        "No, skip",
                        "Cancel import"};
                    int choice = JOptionPane.showOptionDialog(this,
                            "Keyword list <" + list.getName() + "> already exists locally, overwrite?",
                            "Import list conflict",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                    if (choice == JOptionPane.OK_OPTION) {
                        toImportConfirmed.add(list);
                    } else if (choice == JOptionPane.CANCEL_OPTION) {
                        break;
                    }

                } else {
                    //no conflict
                    toImportConfirmed.add(list);
                }

            }

            if (toImportConfirmed.isEmpty()) {
                return;
            }

            if (!writer.writeLists(toImportConfirmed)) {
                KeywordSearchUtil.displayDialog(FEATURE_NAME, "Keyword list not imported", KeywordSearchUtil.DIALOG_MESSAGE_TYPE.INFO);
            }

        }
    }//GEN-LAST:event_importButtonActionPerformed

    private void listsTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listsTableKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_DELETE) {
            int[] selected = listsTable.getSelectedRows();
            if(selected.length == 0) {
                return;
            }
            KeywordSearchListsXML deleter = KeywordSearchListsXML.getCurrent();
            String listName = deleter.getListNames().get(selected[0]);
            KeywordSearchListsXML.getCurrent().deleteList(listName);
        }
    }//GEN-LAST:event_listsTableKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton importButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable listsTable;
    private javax.swing.JButton newListButton;
    // End of variables declaration//GEN-END:variables

    
    private class KeywordListTableModel extends AbstractTableModel {
        //data

        private KeywordSearchListsXML listsHandle = KeywordSearchListsXML.getCurrent();

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return listsHandle.getNumberLists(false);
        }

        @Override
        public String getColumnName(int column) {
            return "Name";
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return listsHandle.getListNames(false).get(rowIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            throw new UnsupportedOperationException("Editing of cells is not supported");
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        //delete selected from handle, events are fired from the handle
        void deleteSelected(int[] selected) {
            List<String> toDel = new ArrayList<String>();
            for(int i = 0; i < selected.length; i++){
                toDel.add((String) getValueAt(0, selected[i]));
            }
            for (String del : toDel) {
                listsHandle.deleteList(del);
            }
        }

        //resync model from handle, then update table
        void resync() {
            fireTableDataChanged();
        }
    }
    
    void addListSelectionListener(ListSelectionListener l) {
        listsTable.getSelectionModel().addListSelectionListener(l);
    }
}
