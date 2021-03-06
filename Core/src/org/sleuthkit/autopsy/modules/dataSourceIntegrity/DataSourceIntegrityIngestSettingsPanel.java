/*
 * Central Repository
 *
 * Copyright 2018 Basis Technology Corp.
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
package org.sleuthkit.autopsy.modules.dataSourceIntegrity;

import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettingsPanel;

/**
 * Ingest job settings panel for the Correlation Engine module.
 */
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
final class DataSourceIntegrityIngestSettingsPanel extends IngestModuleIngestJobSettingsPanel {

    /**
     * Creates new form DataSourceIntegrityIngestSettingsPanel
     */
    public DataSourceIntegrityIngestSettingsPanel(DataSourceIntegrityIngestSettings settings) {
        initComponents();
        customizeComponents(settings);
    }

    /**
     * Update components with values from the ingest job settings.
     *
     * @param settings The ingest job settings.
     */
    private void customizeComponents(DataSourceIntegrityIngestSettings settings) {
        computeHashesCheckbox.setSelected(settings.shouldComputeHashes());
        verifyHashesCheckbox.setSelected(settings.shouldVerifyHashes());
    }
    
    @Override
    public IngestModuleIngestJobSettings getSettings() {
        return new DataSourceIntegrityIngestSettings(computeHashesCheckbox.isSelected(), verifyHashesCheckbox.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        computeHashesCheckbox = new javax.swing.JCheckBox();
        verifyHashesCheckbox = new javax.swing.JCheckBox();
        ingestSettingsLabel = new javax.swing.JLabel();
        noteLabel = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(300, 155));

        org.openide.awt.Mnemonics.setLocalizedText(computeHashesCheckbox, org.openide.util.NbBundle.getMessage(DataSourceIntegrityIngestSettingsPanel.class, "DataSourceIntegrityIngestSettingsPanel.computeHashesCheckbox.text")); // NOI18N
        computeHashesCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                computeHashesCheckboxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(verifyHashesCheckbox, org.openide.util.NbBundle.getMessage(DataSourceIntegrityIngestSettingsPanel.class, "DataSourceIntegrityIngestSettingsPanel.verifyHashesCheckbox.text")); // NOI18N

        ingestSettingsLabel.setFont(ingestSettingsLabel.getFont().deriveFont(ingestSettingsLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(ingestSettingsLabel, org.openide.util.NbBundle.getMessage(DataSourceIntegrityIngestSettingsPanel.class, "DataSourceIntegrityIngestSettingsPanel.ingestSettingsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(noteLabel, org.openide.util.NbBundle.getMessage(DataSourceIntegrityIngestSettingsPanel.class, "DataSourceIntegrityIngestSettingsPanel.noteLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(noteLabel)
                    .addComponent(verifyHashesCheckbox)
                    .addComponent(computeHashesCheckbox)
                    .addComponent(ingestSettingsLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ingestSettingsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(computeHashesCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(verifyHashesCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(noteLabel)
                .addContainerGap(53, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void computeHashesCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_computeHashesCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_computeHashesCheckboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox computeHashesCheckbox;
    private javax.swing.JLabel ingestSettingsLabel;
    private javax.swing.JLabel noteLabel;
    private javax.swing.JCheckBox verifyHashesCheckbox;
    // End of variables declaration//GEN-END:variables

}
