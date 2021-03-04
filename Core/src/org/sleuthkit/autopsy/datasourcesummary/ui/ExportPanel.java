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
package org.sleuthkit.autopsy.datasourcesummary.ui;

/**
 * The panel that provides options for exporting data source summary data.
 */
public class ExportPanel extends javax.swing.JPanel {

    private Runnable xlsxExportAction;

    /**
     * Creates new form ExportPanel
     */
    public ExportPanel() {
        initComponents();
    }

    /**
     * Returns the action that handles exporting to excel.
     *
     * @return The action that handles exporting to excel.
     */
    public Runnable getXlsxExportAction() {
        return xlsxExportAction;
    }

    /**
     * Sets the action that handles exporting to excel.
     *
     * @param onXlsxExport The action that handles exporting to excel.
     */
    public void setXlsxExportAction(Runnable onXlsxExport) {
        this.xlsxExportAction = onXlsxExport;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JButton xlsxExportButton = new javax.swing.JButton();
        javax.swing.JLabel xlsxExportMessage = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(xlsxExportButton, org.openide.util.NbBundle.getMessage(ExportPanel.class, "ExportPanel.xlsxExportButton.text")); // NOI18N
        xlsxExportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xlsxExportButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(xlsxExportMessage, org.openide.util.NbBundle.getMessage(ExportPanel.class, "ExportPanel.xlsxExportMessage.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(xlsxExportMessage)
                    .addComponent(xlsxExportButton))
                .addContainerGap(62, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(xlsxExportMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xlsxExportButton)
                .addContainerGap(250, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void xlsxExportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xlsxExportButtonActionPerformed
        if (this.xlsxExportAction != null) {
            xlsxExportAction.run();
        }
    }//GEN-LAST:event_xlsxExportButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
