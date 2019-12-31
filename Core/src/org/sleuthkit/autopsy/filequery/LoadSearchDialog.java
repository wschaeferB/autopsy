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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.util.FileUtils;
import org.openide.windows.WindowManager;
import org.sleuthkit.autopsy.coreutils.PlatformUtil;

final class LoadSearchDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;
    private String fileName = null;
    private static final String SAVE_DIR = PlatformUtil.getUserDirectory() + File.separator + "discoveryFilterSaves";

    /**
     * Creates new form SaveSearchDialog
     */
    LoadSearchDialog() {
        super((JFrame) null, "Title here", true);
        initComponents();
        setResizable(false);
        jTable1.setDefaultEditor(Object.class, null);
        File folder = new File(SAVE_DIR);
        File[] files = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return FilenameUtils.getExtension(pathname.getName()).equalsIgnoreCase("dsf");
            }
        });
        if (files != null) {
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            for (final File savedSettings : files) {
                model.addRow(new Object[]{FilenameUtils.getBaseName(savedSettings.getName()), new Date(savedSettings.lastModified()).toString()});
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(400, 145));
        setResizable(false);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new String [][] {},
            new String [] {"Search Name", "Date Saved"}
        ));
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jTable1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(LoadSearchDialog.class, "LoadSearchDialog.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(LoadSearchDialog.class, "LoadSearchDialog.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(LoadSearchDialog.class, "LoadSearchDialog.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton1, jButton2, jButton3});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        fileName = jTable1.getValueAt(jTable1.getSelectedRow(), 0) + ".dsf";
        dispose();        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int selectedRow = jTable1.getSelectedRow();
        fileName = jTable1.getValueAt(selectedRow, 0) + ".dsf";
        int confirmed = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + fileName, "Confirm deletion", JOptionPane.OK_CANCEL_OPTION);
        if (confirmed == JOptionPane.OK_OPTION) {
            if (fileName != null) {
                FileUtils.delete(new File(SAVE_DIR + File.separator + fileName));
            }
            ((DefaultTableModel) jTable1.getModel()).removeRow(jTable1.convertRowIndexToModel(selectedRow));
            jTable1.repaint();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * Display the Search Other Cases dialog.
     */
    void display() {
        this.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        pack();
        setVisible(true);
    }

    SearchFilterSave getSearch() throws FileNotFoundException, IOException {
        //get name of selected search
        if (fileName != null) {
            try (FileInputStream is = new FileInputStream(SAVE_DIR + File.separator + fileName); InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                GsonBuilder gsonBuilder = new GsonBuilder()
                        .setPrettyPrinting();
                Gson gson = gsonBuilder.create();
                return gson.fromJson(reader, SearchFilterSave.class);
            }
        }
        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
