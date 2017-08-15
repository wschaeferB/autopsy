# Requires python3

import re
import sqlite3
import subprocess
import shutil
import os
import codecs
import datetime
import sys

class TskDbDiff(object):
    """Compares two TSK/Autospy SQLite databases.

    Attributes:
        gold_artifacts:
        autopsy_artifacts:
        gold_attributes:
        autopsy_attributes:
        gold_objects:
        autopsy_objects:
        artifact_comparison:
        attribute_comparision:
        report_errors: a listof_listof_String, the error messages that will be
        printed to screen in the run_diff method
        passed: a boolean, did the diff pass?
        autopsy_db_file:
        gold_db_file:
    """
    def __init__(self, output_db, gold_db, output_dir=None, gold_bb_dump=None, gold_dump=None, verbose=False):
        """Constructor for TskDbDiff.

        Args:
            output_db_path: path to output database (non-gold standard)
            gold_db_path: path to gold database
            output_dir: (optional) Path to folder where generated files will be put.
            gold_bb_dump: (optional) path to file where the gold blackboard dump is located
            gold_dump: (optional) path to file where the gold non-blackboard dump is located
            verbose: (optional) a boolean, if true, diff results are sent to stdout. 
        """

        self.output_db_file = output_db
        self.gold_db_file = gold_db
        self.output_dir = output_dir
        self.gold_bb_dump = gold_bb_dump
        self.gold_dump = gold_dump
        self._generate_gold_dump = False        
        self._generate_gold_bb_dump = False
        self._bb_dump_diff = ""
        self._dump_diff = ""
        self._bb_dump = ""
        self._dump = ""
        self.verbose = verbose

        if self.gold_bb_dump is None:
            self._generate_gold_bb_dump = True
        if self.gold_dump is None:
            self._generate_gold_dump = True

    def run_diff(self):
        """Compare the databases.

        Raises:
            TskDbDiffException: if an error occurs while diffing or dumping the database
        """

        self._init_diff()

        # generate the gold database dumps if necessary     
        if self._generate_gold_dump:       
            TskDbDiff._dump_output_db_nonbb(self.gold_db_file, self.gold_dump)     
        if self._generate_gold_bb_dump:        
            TskDbDiff._dump_output_db_bb(self.gold_db_file, self.gold_bb_dump)

        # generate the output database dumps (both DB and BB)
        TskDbDiff._dump_output_db_nonbb(self.output_db_file, self._dump)
        TskDbDiff._dump_output_db_bb(self.output_db_file, self._bb_dump)

        # Compare non-BB
        dump_diff_pass = self._diff(self._dump, self.gold_dump, self._dump_diff)

        # Compare BB
        bb_dump_diff_pass = self._diff(self._bb_dump, self.gold_bb_dump, self._bb_dump_diff)

        self._cleanup_diff()
        return dump_diff_pass, bb_dump_diff_pass


    def _init_diff(self):
        """Set up the necessary files based on the arguments given at construction"""
        if self.output_dir is None:
            # No stored files
            self._bb_dump = TskDbDiff._get_tmp_file("BlackboardDump", ".txt")
            self._bb_dump_diff = TskDbDiff._get_tmp_file("BlackboardDump-Diff", ".txt")
            self._dump = TskDbDiff._get_tmp_file("DBDump", ".txt")
            self._dump_diff = TskDbDiff._get_tmp_file("DBDump-Diff", ".txt")
        else:
            self._bb_dump = os.path.join(self.output_dir, "BlackboardDump.txt")
            self._bb_dump_diff = os.path.join(self.output_dir, "BlackboardDump-Diff.txt")
            self._dump = os.path.join(self.output_dir, "DBDump.txt")
            self._dump_diff = os.path.join(self.output_dir, "DBDump-Diff.txt")

        # Sorting gold before comparing (sort behaves differently in different environments)
        new_bb = TskDbDiff._get_tmp_file("GoldBlackboardDump", ".txt")
        new_db = TskDbDiff._get_tmp_file("GoldDBDump", ".txt")
        if self.gold_bb_dump is not None:
            srtcmdlst = ["sort", self.gold_bb_dump, "-o", new_bb]
            subprocess.call(srtcmdlst)
            srtcmdlst = ["sort", self.gold_dump, "-o", new_db]
            subprocess.call(srtcmdlst)
        self.gold_bb_dump = new_bb
        self.gold_dump = new_db


    def _cleanup_diff(self):
        if self.output_dir is None:
            #cleanup temp files
            os.remove(self._dump)
            os.remove(self._bb_dump)
            if os.path.isfile(self._dump_diff):
                os.remove(self._dump_diff)
            if os.path.isfile(self._bb_dump_diff):
                os.remove(self._bb_dump_diff)

        if self.gold_bb_dump is None:
            os.remove(self.gold_bb_dump)
            os.remove(self.gold_dump)


    def _diff(self, output_file, gold_file, diff_path):
        """Compare two text files.

        Args:
            output_file: a pathto_File, the latest text file
            gold_file: a pathto_File, the gold text file
            diff_path: The file to write the differences to
        Returns False if different
        """

        if (not os.path.isfile(output_file)):
            return False

        if (not os.path.isfile(gold_file)):
            return False

        # It is faster to read the contents in and directly compare
        output_data = codecs.open(output_file, "r", "utf_8").read()
        gold_data = codecs.open(gold_file, "r", "utf_8").read()
        if (gold_data == output_data):
            return True

        # If they are different, invoke 'diff'
        diff_file = codecs.open(diff_path, "wb", "utf_8")
        # Gold needs to be passed in as 1st arg and output as 2nd
        dffcmdlst = ["diff", gold_file, output_file]
        subprocess.call(dffcmdlst, stdout = diff_file)

        # create file path for gold files inside output folder. In case of diff, both gold and current run files
        # are available in the report output folder. Prefix Gold- is added to the filename.
        gold_file_in_output_dir = output_file[:output_file.rfind("/")] + "/Gold-" + output_file[output_file.rfind("/")+1:]
        shutil.copy(gold_file, gold_file_in_output_dir)

        return False


    def _dump_output_db_bb(db_file, bb_dump_file):
        """Dumps sorted text results to the given output location.

        Smart method that deals with a blackboard comparison to avoid issues
        with different IDs based on when artifacts were created.

        Args:
            db_file: a pathto_File, the output database.
            bb_dump_file: a pathto_File, the sorted dump file to write to
        """

        unsorted_dump = TskDbDiff._get_tmp_file("dump_data", ".txt")
        conn = sqlite3.connect(db_file)
        conn.text_factory = lambda x: x.decode("utf-8", "ignore")
        conn.row_factory = sqlite3.Row

        artifact_cursor = conn.cursor()

        # Get the list of all artifacts (along with type and associated file)
        # @@@ Could add a SORT by parent_path in here since that is how we are going to later sort it.
        artifact_cursor.execute("SELECT tsk_files.parent_path, tsk_files.name, blackboard_artifact_types.display_name, blackboard_artifacts.artifact_id FROM blackboard_artifact_types INNER JOIN blackboard_artifacts ON blackboard_artifact_types.artifact_type_id = blackboard_artifacts.artifact_type_id INNER JOIN tsk_files ON tsk_files.obj_id = blackboard_artifacts.obj_id")
        database_log = codecs.open(unsorted_dump, "wb", "utf_8")
        row = artifact_cursor.fetchone()
        appnd = False
        counter = 0
        artifact_count = 0
        artifact_fail = 0

        # Cycle through artifacts
        try:
            while (row != None):

                # File Name and artifact type
                if(row["parent_path"] != None):
                    database_log.write(row["parent_path"] + row["name"] + ' <artifact type="' + row["display_name"] + '" > ')
                else:
                    database_log.write(row["name"] + ' <artifact type="' + row["display_name"] + '" > ')

                # Get attributes for this artifact
                attribute_cursor = conn.cursor()
                looptry = True
                artifact_count += 1
                try:
                    art_id = ""
                    art_id = str(row["artifact_id"])
                    attribute_cursor.execute("SELECT blackboard_attributes.source, blackboard_attribute_types.display_name, blackboard_attributes.value_type, blackboard_attributes.value_text, blackboard_attributes.value_int32, blackboard_attributes.value_int64, blackboard_attributes.value_double FROM blackboard_attributes INNER JOIN blackboard_attribute_types ON blackboard_attributes.attribute_type_id = blackboard_attribute_types.attribute_type_id WHERE artifact_id =? ORDER BY blackboard_attributes.source, blackboard_attribute_types.display_name, blackboard_attributes.value_type, blackboard_attributes.value_text, blackboard_attributes.value_int32, blackboard_attributes.value_int64, blackboard_attributes.value_double", [art_id])
                    attributes = attribute_cursor.fetchall()
                
                    # Print attributes
                    if (len(attributes) == 0):
                       # @@@@ This should be </artifact> 
                       database_log.write(' <artifact/>\n')
                       row = artifact_cursor.fetchone()
                       continue

                    src = attributes[0][0]
                    for attr in attributes:
                        numvals = 0
                        for x in range(3, 6):
                            if(attr[x] != None):
                                numvals += 1
                        if(numvals > 1):
                            msg = "There were too many values for attribute type: " + attr["display_name"] + " for artifact with id #" + str(row["artifact_id"]) + ".\n"

                        if(not attr["source"] == src):
                            msg = "There were inconsistent sources for artifact with id #" + str(row["artifact_id"]) + ".\n"

                        try:
                            if attr["value_type"] == 0:
                                attr_value_as_string = str(attr["value_text"])                        
                            elif attr["value_type"] == 1:
                                attr_value_as_string = str(attr["value_int32"])                        
                            elif attr["value_type"] == 2:
                                attr_value_as_string = str(attr["value_int64"])                        
                            elif attr["value_type"] == 3:
                                attr_value_as_string = str(attr["value_double"])                        
                            elif attr["value_type"] == 4:
                                attr_value_as_string = "bytes"                        
                            elif attr["value_type"] == 5:
                                attr_value_as_string = str(attr["value_int64"])                        
                            if attr["display_name"] == "Associated Artifact":
                                attr_value_as_string = getAssociatedArtifactType(db_file, attr_value_as_string)                            
                            patrn = re.compile("[\n\0\a\b\r\f]")
                            attr_value_as_string = re.sub(patrn, ' ', attr_value_as_string)
                            database_log.write('<attribute source="' + attr["source"] + '" type="' + attr["display_name"] + '" value="' + attr_value_as_string + '" />')
                        except IOError as e:
                            print("IO error")
                            raise TskDbDiffException("Unexpected IO error while writing to database log." + str(e))

                except sqlite3.Error as e:
                    msg = "Attributes in artifact id (in output DB)# " + str(row["artifact_id"]) + " encountered an error: " + str(e) +" .\n"
                    print("Attributes in artifact id (in output DB)# ", str(row["artifact_id"]), " encountered an error: ", str(e))
                    print() 
                    looptry = False
                    artifact_fail += 1
                    database_log.write('Error Extracting Attributes')
                    database_log.close()
                    raise TskDbDiffException(msg)
                finally:
                    attribute_cursor.close()

               
                # @@@@ This should be </artifact> 
                database_log.write(' <artifact/>\n')
                row = artifact_cursor.fetchone()

            if(artifact_fail > 0):
                msg ="There were " + str(artifact_count) + " artifacts and " + str(artifact_fail) + " threw an exception while loading.\n"
        except Exception as e:
            raise TskDbDiffException("Unexpected error while dumping blackboard database: " + str(e))
        finally:
            database_log.close()
            artifact_cursor.close()
            conn.close()
        
        # Now sort the file
        srtcmdlst = ["sort", unsorted_dump, "-o", bb_dump_file]
        subprocess.call(srtcmdlst)


    def _dump_output_db_nonbb(db_file, dump_file):
        """Dumps a database to a text file.

        Does not dump the artifact and attributes.

        Args:
            db_file: a pathto_File, the database file to dump
            dump_file: a pathto_File, the location to dump the non-blackboard database items
        """

        # Make a copy that we can modify
        backup_db_file = TskDbDiff._get_tmp_file("tsk_backup_db", ".db")
        shutil.copy(db_file, backup_db_file)
        # We sometimes get situations with messed up permissions
        os.chmod (backup_db_file, 0o777)

        conn = sqlite3.connect(backup_db_file)
        id_files_table = build_id_files_table(conn.cursor())
        id_vs_parts_table = build_id_vs_parts_table(conn.cursor())
        id_vs_info_table = build_id_vs_info_table(conn.cursor())
        id_fs_info_table = build_id_fs_info_table(conn.cursor())
        id_objects_table = build_id_objects_table(conn.cursor())
        id_artifact_types_table = build_id_artifact_types_table(conn.cursor())
        id_obj_path_table = build_id_obj_path_table(id_files_table, id_objects_table, id_artifact_types_table)

        conn.text_factory = lambda x: x.decode("utf-8", "ignore")

        # Delete the blackboard tables
        conn.execute("DROP TABLE blackboard_artifacts")
        conn.execute("DROP TABLE blackboard_attributes")

        # Write to the database dump
        with codecs.open(dump_file, "wb", "utf_8") as db_log:
            for line in conn.iterdump():
                line = normalize_db_entry(line, id_obj_path_table, id_vs_parts_table, id_vs_info_table, id_fs_info_table, id_objects_table)
                db_log.write('%s\n' % line)
            # Now sort the file    
            
        srtcmdlst = ["sort", dump_file, "-o", dump_file]
        subprocess.call(srtcmdlst)

        conn.close()
        # cleanup the backup
        os.remove(backup_db_file)


    def dump_output_db(db_file, dump_file, bb_dump_file):
        """Dumps the given database to text files for later comparison.

        Args:
            db_file: a pathto_File, the database file to dump
            dump_file: a pathto_File, the location to dump the non-blackboard database items
            bb_dump_file: a pathto_File, the location to dump the blackboard database items
        """
        TskDbDiff._dump_output_db_nonbb(db_file, dump_file)
        TskDbDiff._dump_output_db_bb(db_file, bb_dump_file)


    def _get_tmp_file(base, ext):
        time = datetime.datetime.now().time().strftime("%H%M%f")
        return os.path.join(os.environ['TMP'], base + time + ext)


class TskDbDiffException(Exception):
    pass

def normalize_db_entry(line, files_table, vs_parts_table, vs_info_table, fs_info_table, objects_table):
    """ Make testing more consistent and reasonable by doctoring certain db entries.

    Args:
        line: a String, the line to remove the object id from.
        files_table: a map from object ids to file paths.
    """

    files_index = line.find('INSERT INTO "tsk_files"')
    path_index = line.find('INSERT INTO "tsk_files_path"')
    object_index = line.find('INSERT INTO "tsk_objects"')
    report_index = line.find('INSERT INTO "reports"')
    layout_index = line.find('INSERT INTO "tsk_file_layout"')
    data_source_info_index = line.find('INSERT INTO "data_source_info"')
    ingest_job_index = line.find('INSERT INTO "ingest_jobs"')

    parens = line[line.find('(') + 1 : line.rfind(')')]
    fields_list = parens.replace(" ", "").split(',')
    
    # remove object ID
    if (files_index != -1):
        obj_id = fields_list[0]
        path = files_table[int(obj_id)]
        newLine = ('INSERT INTO "tsk_files" VALUES(' + ', '.join(fields_list[1:]) + ');') 
        return newLine
    # remove object ID
    elif (path_index != -1):
        obj_id = int(fields_list[0])
        objValue = files_table[obj_id]
        # remove the obj_id from ModuleOutput/EmbeddedFileExtractor directory
        idx_pre = fields_list[1].find('EmbeddedFileExtractor') + len('EmbeddedFileExtractor')
        if idx_pre > -1:
            idx_pos =  fields_list[1].find('\\', idx_pre + 2)
            dir_to_replace = fields_list[1][idx_pre + 1 : idx_pos] # +1 to skip the file seperator
            dir_to_replace = dir_to_replace[0:dir_to_replace.rfind('_')]
            pathValue = fields_list[1][:idx_pre+1] + dir_to_replace + fields_list[1][idx_pos:]
        else:
            pathValue = fields_list[1]
        newLine = ('INSERT INTO "tsk_files_path" VALUES(' + objValue + ', ' + pathValue + ', ' + ', '.join(fields_list[2:]) + ');') 
        return newLine
    # remove object ID
    elif (layout_index != -1):
        obj_id = fields_list[0]
        path= files_table[int(obj_id)]
        newLine = ('INSERT INTO "tsk_file_layout" VALUES(' + path + ', ' + ', '.join(fields_list[1:]) + ');') 
        return newLine
    # remove object ID
    elif (object_index != -1):
        obj_id = fields_list[0]
        parent_id = fields_list[1]
        newLine = 'INSERT INTO "tsk_objects" VALUES('
        path = None
        parent_path = None

        #if obj_id or parent_id is invalid literal, we simple return the values as it is 
        try:
            obj_id = int(obj_id)
            parent_id = int(parent_id) 
        except Exception as e:
            return line

        if obj_id in files_table.keys():
            path = files_table[obj_id]
        elif obj_id in vs_parts_table.keys():
            path = vs_parts_table[obj_id]
        elif obj_id in vs_info_table.keys():
            path = vs_info_table[obj_id]
        elif obj_id in fs_info_table.keys():
            path = fs_info_table[obj_id]
        
        if parent_id in files_table.keys():
            parent_path = files_table[parent_id]
        elif parent_id in vs_parts_table.keys():
            parent_path = vs_parts_table[parent_id]
        elif parent_id in vs_info_table.keys():
            parent_path = vs_info_table[parent_id]
        elif parent_id in fs_info_table.keys():
            parent_path = fs_info_table[parent_id]
        

        if path and parent_path:
            return newLine + path + ', ' + parent_path + ', ' + ', '.join(fields_list[2:]) + ');'
        else:
            return line 
    # remove time-based information, ie Test_6/11/14 -> Test    
    elif (report_index != -1):
        fields_list[1] = "AutopsyTestCase"
        fields_list[2] = "0"
        newLine = ('INSERT INTO "reports" VALUES(' + ','.join(fields_list) + ');')
        return newLine
    elif (data_source_info_index != -1):
        fields_list[1] = "{device id}"
        newLine = ('INSERT INTO "data_source_info" VALUES(' + ','.join(fields_list) + ');')
        return newLine
    elif (ingest_job_index != -1):
        fields_list[2] = "{host_name}"
        start_time = int(fields_list[3])
        end_time = int(fields_list[4])
        if (start_time <= end_time):
            fields_list[3] = "0"
            fields_list[4] = "0"
        newLine = ('INSERT INTO "injest_jobs" VALUES(' + ','.join(fields_list) + ');')
        return newLine
    else:
        return line

def getAssociatedArtifactType(db_file, artifact_id):
    # Make a copy that we can modify
    backup_db_file = TskDbDiff._get_tmp_file("tsk_backup_db", ".db")
    shutil.copy(db_file, backup_db_file)
    # We sometimes get situations with messed up permissions
    os.chmod (backup_db_file, 0o777)

    conn = sqlite3.connect(backup_db_file)
    cur = conn.cursor()
    #artifact_cursor.execute("SELECT display_name FROM blackboard_artifact_types WHERE artifact_id=?",[artifact_id])
    cur.execute("SELECT tsk_files.parent_path, blackboard_artifact_types.display_name FROM blackboard_artifact_types INNER JOIN blackboard_artifacts ON blackboard_artifact_types.artifact_type_id = blackboard_artifacts.artifact_type_id INNER JOIN tsk_files ON tsk_files.obj_id = blackboard_artifacts.obj_id WHERE artifact_id=?",[artifact_id])
    info = cur.fetchone()
    
    conn.close()
    # cleanup the backup
    os.remove(backup_db_file)

    return "File path: " + info[0] + " Artifact Type: " + info[1]

def build_id_files_table(db_cursor):
    """Build the map of object ids to file paths.

    Args:
        db_cursor: the database cursor
    """
    # for each row in the db, take the object id, parent path, and name, then create a tuple in the dictionary
    # with the object id as the key and the full file path (parent + name) as the value
    mapping = dict([(row[0], str(row[1]) + str(row[2])) for row in db_cursor.execute("SELECT obj_id, parent_path, name FROM tsk_files")])
    return mapping

def build_id_vs_parts_table(db_cursor):
    """Build the map of object ids to vs_parts.

    Args:
        db_cursor: the database cursor
    """
    # for each row in the db, take the object id, addr, and start, then create a tuple in the dictionary
    # with the object id as the key and (addr + start) as the value
    mapping = dict([(row[0], str(row[1]) + '_' + str(row[2])) for row in db_cursor.execute("SELECT obj_id, addr, start FROM tsk_vs_parts")])
    return mapping

def build_id_vs_info_table(db_cursor):
    """Build the map of object ids to vs_info.

    Args:
        db_cursor: the database cursor
    """
    # for each row in the db, take the object id, vs_type, and img_offset, then create a tuple in the dictionary
    # with the object id as the key and (vs_type + img_offset) as the value
    mapping = dict([(row[0], str(row[1]) + '_' + str(row[2])) for row in db_cursor.execute("SELECT obj_id, vs_type, img_offset FROM tsk_vs_info")])
    return mapping

     
def build_id_fs_info_table(db_cursor):
    """Build the map of object ids to fs_info.

    Args:
        db_cursor: the database cursor
    """
    # for each row in the db, take the object id, img_offset, and fs_type, then create a tuple in the dictionary
    # with the object id as the key and (img_offset + fs_type) as the value
    mapping = dict([(row[0], str(row[1]) + '_' + str(row[2])) for row in db_cursor.execute("SELECT obj_id, img_offset, fs_type FROM tsk_fs_info")])
    return mapping

def build_id_objects_table(db_cursor):
    """Build the map of object ids to par_id.

    Args:
        db_cursor: the database cursor
    """
    # for each row in the db, take the object id, par_obj_id, then create a tuple in the dictionary
    # with the object id as the key and par_obj_id, type as the value
    mapping = dict([(row[0], [row[1], row[2]]) for row in db_cursor.execute("SELECT * FROM tsk_objects")])
    return mapping

def build_id_artifact_types_table(db_cursor):
    """Build the map of object ids to artifact ids.

    Args:
        db_cursor: the database cursor
    """
    # for each row in the db, take the object id, par_obj_id, then create a tuple in the dictionary
    # with the object id as the key and artifact type as the value
    mapping = dict([(row[0], row[1]) for row in db_cursor.execute("SELECT blackboard_artifacts.artifact_obj_id, blackboard_artifact_types.type_name FROM blackboard_artifacts INNER JOIN blackboard_artifact_types ON blackboard_artifact_types.artifact_type_id = blackboard_artifacts.artifact_type_id ")])
    return mapping


def build_id_obj_path_table(files_table, objects_table, artifacts_table):
    """Build the map of object ids to artifact ids.

    Args:
        files_table: obj_id, path
        objects_table: obj_id, par_obj_id, type
        artifacts_table: obj_id, artifact_type_name
    """
    # make a copy of files_table and updated it with new data from artifats_table
    mapping = files_table.copy()
    for k, v in objects_table.items():
        if k not in mapping.keys(): # If the mapping table doesn't have data for obj_id(k), we use it's par_obj_id's path+name plus it's artifact_type name as path
            if k in artifacts_table.keys():
                par_obj_id = v[0]
                path = mapping[par_obj_id] 
                mapping[k] = path + "/" + artifacts_table[k]
        elif v[0] not in mapping.keys():
            if v[0] in artifacts_table.keys():
                par_obj_id = objects_table[v[0]]
                path = mapping[par_obj_id] 
                mapping[k] = path + "/" + artifacts_table[v[0]]
    return mapping

def main():
    try:
        sys.argv.pop(0)
        output_db = sys.argv.pop(0)
        gold_db = sys.argv.pop(0)
    except:
        print("usage: tskdbdiff [OUPUT DB PATH] [GOLD DB PATH]")
        sys.exit(1)

    db_diff = TskDbDiff(output_db, gold_db, output_dir=".") 
    dump_passed, bb_dump_passed = db_diff.run_diff()

    if dump_passed and bb_dump_passed:
        print("Database comparison passed.")
    if not dump_passed:
        print("Non blackboard database comparison failed.")
    if not bb_dump_passed:
        print("Blackboard database comparison failed.")

    sys.exit(0)


if __name__ == "__main__":
    if sys.hexversion < 0x03000000:
        print("Python 3 required")
        sys.exit(1)

    main()

