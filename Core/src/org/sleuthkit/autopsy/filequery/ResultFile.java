/*
 * Autopsy Forensic Browser
 *
 * Copyright 2019-2020 Basis Technology Corp.
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

import org.sleuthkit.autopsy.filequery.FileSearchData.FileType;
import org.sleuthkit.datamodel.AbstractFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.corecomponents.DataResultViewerTable;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.ContentTag;
import org.sleuthkit.datamodel.HashUtility;
import org.sleuthkit.datamodel.Tag;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;

/**
 * Container for files that holds all necessary data for grouping and sorting
 */
class ResultFile {

    private final static Logger logger = Logger.getLogger(ResultFile.class.getName());
    private FileSearchData.Frequency frequency;
    private final List<String> keywordListNames;
    private final List<String> hashSetNames;
    private final List<String> tagNames;
    private final List<String> interestingSetNames;
    private final List<String> objectDetectedNames;
    private final List<AbstractFile> instances = new ArrayList<>();
    private DataResultViewerTable.Score currentScore = DataResultViewerTable.Score.NO_SCORE;
    private String scoreDescription = null;
    private boolean deleted = false;
    private FileType fileType;

    /**
     * Create a ResultFile from an AbstractFile
     *
     * @param abstractFile
     */
    ResultFile(AbstractFile abstractFile) {
        try {
            //call get uniquePath to cache the path
            abstractFile.getUniquePath();
        } catch (TskCoreException ignored) {
            //path wasnt cached will likely be called on EDT later JIRA-5972
        }
        //store the file the ResultFile was created for as the first value in the instances list
        instances.add(abstractFile);
        if (abstractFile.isDirNameFlagSet(TskData.TSK_FS_NAME_FLAG_ENUM.UNALLOC)) {
            deleted = true;
        }
        updateScoreAndDescription(abstractFile);
        this.frequency = FileSearchData.Frequency.UNKNOWN;
        keywordListNames = new ArrayList<>();
        hashSetNames = new ArrayList<>();
        tagNames = new ArrayList<>();
        interestingSetNames = new ArrayList<>();
        objectDetectedNames = new ArrayList<>();
        fileType = FileType.fromMIMEtype(abstractFile.getMIMEType());
    }

    /**
     * Get the frequency of this file in the central repository
     *
     * @return The Frequency enum
     */
    FileSearchData.Frequency getFrequency() {
        return frequency;
    }

    /**
     * Set the frequency of this file from the central repository
     *
     * @param frequency The frequency of the file as an enum
     */
    void setFrequency(FileSearchData.Frequency frequency) {
        this.frequency = frequency;
    }

    /**
     * Add an AbstractFile to the list of files which are instances of this
     * file.
     *
     * @param duplicate The abstract file to add as a duplicate.
     */
    void addDuplicate(AbstractFile duplicate) {
        if (deleted && !duplicate.isDirNameFlagSet(TskData.TSK_FS_NAME_FLAG_ENUM.UNALLOC)) {
            deleted = false;
        }
        if (fileType == FileType.OTHER) {
            fileType = FileType.fromMIMEtype(duplicate.getMIMEType());
        }
        updateScoreAndDescription(duplicate);
        try {
            //call get uniquePath to cache the path
            duplicate.getUniquePath();
        } catch (TskCoreException ignored) {
            //path wasnt cached will likely be called on EDT later JIRA-5972
        }
        instances.add(duplicate);
    }

    /**
     * Get the aggregate score of this ResultFile. Calculated as the highest
     * score among all instances it represents.
     *
     * @return The score of this ResultFile.
     */
    DataResultViewerTable.Score getScore() {
        return currentScore;
    }

    /**
     * Get the description for the score assigned to this item.
     *
     * @return The score description of this ResultFile.
     */
    String getScoreDescription() {
        return scoreDescription;
    }

    /**
     * Get the aggregate deleted status of this ResultFile. A file is identified
     * as deleted if all instances of it are deleted.
     *
     * @return The deleted status of this ResultFile.
     */
    boolean isDeleted() {
        return deleted;
    }

    /**
     * Get the list of AbstractFiles which have been identified as instances of
     * this file.
     *
     * @return The list of AbstractFiles which have been identified as instances
     *         of this file.
     */
    List<AbstractFile> getAllInstances() {
        return Collections.unmodifiableList(instances);
    }

    /**
     * Get the file type.
     *
     * @return The FileType enum.
     */
    FileType getFileType() {
        return fileType;
    }

    /**
     * Add a keyword list name that matched this file.
     *
     * @param keywordListName
     */
    void addKeywordListName(String keywordListName) {
        if (!keywordListNames.contains(keywordListName)) {
            keywordListNames.add(keywordListName);
        }

        // Sort the list so the getKeywordListNames() will be consistent regardless of the order added
        Collections.sort(keywordListNames);
    }

    /**
     * Get the keyword list names for this file
     *
     * @return the keyword list names that matched this file.
     */
    List<String> getKeywordListNames() {
        return Collections.unmodifiableList(keywordListNames);
    }

    /**
     * Add a hash set name that matched this file.
     *
     * @param hashSetName
     */
    void addHashSetName(String hashSetName) {
        if (!hashSetNames.contains(hashSetName)) {
            hashSetNames.add(hashSetName);
        }

        // Sort the list so the getHashHitNames() will be consistent regardless of the order added
        Collections.sort(hashSetNames);
    }

    /**
     * Get the hash set names for this file
     *
     * @return The hash set names that matched this file.
     */
    List<String> getHashSetNames() {
        return Collections.unmodifiableList(hashSetNames);
    }

    /**
     * Add a tag name that matched this file.
     *
     * @param tagName
     */
    void addTagName(String tagName) {
        if (!tagNames.contains(tagName)) {
            tagNames.add(tagName);
        }

        // Sort the list so the getTagNames() will be consistent regardless of the order added
        Collections.sort(tagNames);
    }

    /**
     * Get the tag names for this file
     *
     * @return the tag names that matched this file.
     */
    List<String> getTagNames() {
        return Collections.unmodifiableList(tagNames);
    }

    /**
     * Add an interesting file set name that matched this file.
     *
     * @param interestingSetName
     */
    void addInterestingSetName(String interestingSetName) {
        if (!interestingSetNames.contains(interestingSetName)) {
            interestingSetNames.add(interestingSetName);
        }

        // Sort the list so the getInterestingSetNames() will be consistent regardless of the order added
        Collections.sort(interestingSetNames);
    }

    /**
     * Get the interesting item set names for this file
     *
     * @return the interesting item set names that matched this file.
     */
    List<String> getInterestingSetNames() {
        return Collections.unmodifiableList(interestingSetNames);
    }

    /**
     * Add an object detected in this file.
     *
     * @param objectDetectedName
     */
    void addObjectDetectedName(String objectDetectedName) {
        if (!objectDetectedNames.contains(objectDetectedName)) {
            objectDetectedNames.add(objectDetectedName);
        }

        // Sort the list so the getObjectDetectedNames() will be consistent regardless of the order added
        Collections.sort(objectDetectedNames);
    }

    /**
     * Get the objects detected for this file
     *
     * @return the objects detected in this file.
     */
    List<String> getObjectDetectedNames() {
        return Collections.unmodifiableList(objectDetectedNames);
    }

    /**
     * Get the AbstractFile
     *
     * @return the AbstractFile object
     */
    AbstractFile getFirstInstance() {
        return instances.get(0);
    }

    @Override
    public String toString() {
        return getFirstInstance().getName() + "(" + getFirstInstance().getId() + ") - "
                + getFirstInstance().getSize() + ", " + getFirstInstance().getParentPath() + ", "
                + getFirstInstance().getDataSourceObjectId() + ", " + frequency.toString() + ", "
                + String.join(",", keywordListNames) + ", " + getFirstInstance().getMIMEType();
    }

    @Override
    public int hashCode() {
        if (this.getFirstInstance().getMd5Hash() == null
                || HashUtility.isNoDataMd5(this.getFirstInstance().getMd5Hash())
                || !HashUtility.isValidMd5Hash(this.getFirstInstance().getMd5Hash())) {
            return super.hashCode();
        } else {
            //if the file has a valid MD5 use the hashcode of the MD5 for deduping files with the same MD5
            return this.getFirstInstance().getMd5Hash().hashCode();
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ResultFile)
                || this.getFirstInstance().getMd5Hash() == null
                || HashUtility.isNoDataMd5(this.getFirstInstance().getMd5Hash())
                || !HashUtility.isValidMd5Hash(this.getFirstInstance().getMd5Hash())) {
            return super.equals(obj);
        } else {
            //if the file has a valid MD5 compare use the MD5 for equality check
            return this.getFirstInstance().getMd5Hash().equals(((ResultFile) obj).getFirstInstance().getMd5Hash());
        }
    }

    /**
     * Get all tags from the case database that are associated with the file
     *
     * @return a list of tags that are associated with the file
     */
    private List<ContentTag> getContentTagsFromDatabase(AbstractFile file) {
        List<ContentTag> tags = new ArrayList<>();
        try {
            tags.addAll(Case.getCurrentCaseThrows().getServices().getTagsManager().getContentTagsByContent(file));
        } catch (TskCoreException | NoCurrentCaseException ex) {
            logger.log(Level.SEVERE, "Failed to get tags for file " + file.getName(), ex);
        }
        return tags;
    }

    @NbBundle.Messages({
        "ResultFile.score.notableFile.description=At least one instance of the file was recognized as notable.",
        "ResultFile.score.interestingResult.description=At least one instance of the file has an interesting result associated with it.",
        "ResultFile.score.taggedFile.description=At least one instance of the file has been tagged.",
        "ResultFile.score.notableTaggedFile.description=At least one instance of the file is tagged with a notable tag."})
    private void updateScoreAndDescription(AbstractFile file) {
        if (currentScore == DataResultViewerTable.Score.NOTABLE_SCORE) {
            //already notable can return
            return;
        }
        if (file.getKnown() == TskData.FileKnown.BAD) {
            currentScore = DataResultViewerTable.Score.NOTABLE_SCORE;
            scoreDescription = Bundle.ResultFile_score_notableFile_description();
            return;
        }
        try {
            if (currentScore == DataResultViewerTable.Score.NO_SCORE && !file.getArtifacts(BlackboardArtifact.ARTIFACT_TYPE.TSK_INTERESTING_FILE_HIT).isEmpty()) {
                currentScore = DataResultViewerTable.Score.INTERESTING_SCORE;
                scoreDescription = Bundle.ResultFile_score_interestingResult_description();
            }
        } catch (TskCoreException ex) {
            logger.log(Level.WARNING, "Error getting artifacts for file: " + file.getName(), ex);
        }
        List<ContentTag> tags = getContentTagsFromDatabase(file);
        if (!tags.isEmpty()) {
            currentScore = DataResultViewerTable.Score.INTERESTING_SCORE;
            scoreDescription = Bundle.ResultFile_score_taggedFile_description();
            for (Tag tag : tags) {
                if (tag.getName().getKnownStatus() == TskData.FileKnown.BAD) {
                    currentScore = DataResultViewerTable.Score.NOTABLE_SCORE;
                    scoreDescription = Bundle.ResultFile_score_notableTaggedFile_description();
                    return;
                }
            }
        }
    }
}
