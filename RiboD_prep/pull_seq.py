import logging
from Bio import SeqIO, Entrez
from rnafbinv import vienna
from typing import Dict, List, Tuple
from tempfile import NamedTemporaryFile as NTF
import os
from subprocess import Popen, PIPE
from copy import deepcopy
import re
import time
import datetime


INFENRAL_PATH = "/opt/algorithm/infernal/bin/"
CMALIGN_EXE = "cmalign"
CMSEARCH_EXE = "cmsearch"
FASTA_LINE_LENGTH = 80
#SQL_INSERT = "INSERT INTO RNADesign.RiboD ('SeqId', 'Sequence', 'EnergyStructure', 'CmStructure') values"
SQL_INSERT = "INSERT INTO RNADesign.RiboD values"


cm_not_align_counter = 0


def generate_fasta(sequences: Dict[str, str]) -> NTF:
    tmp_file = NTF(mode='w+', dir='.', delete=False, encoding="utf-8")
    for topic, sequence in sequences.items():
        tmp_file.write('> {}\n'.format(topic))
        for fasta_line in [sequence[i:i+FASTA_LINE_LENGTH] for i in range(0, len(sequence), FASTA_LINE_LENGTH)]:
            tmp_file.write('{}\n'.format(fasta_line))
    tmp_file.close()
    return tmp_file


def analyze_stk(stk_file: str, name: str) -> Tuple[str, str, Dict[str, str]]:
    structure = ''
    sequence = ''
    tseq_ailgn = ''
    pp_align = ''
    struct_align = ''
    qseq_align = ''
    sel_name = name
    with open(stk_file, 'r') as stk_input:
        for line in stk_input:
            name_line = '#=GR {}'.format(name)
            if line.startswith(sel_name):
                # sel_name adds support for file with multiple alignment (take first which is best)
                sel_name = line.split(maxsplit=1)[0].strip()
                tseq_ailgn += line.split(maxsplit=1)[1].strip()
            elif line.startswith(name_line):
                pp_align += line.rsplit('PP', 1)[1].strip()
            elif line.startswith('#=GC SS_cons'):
                struct_align += line[12:].strip()
            elif line.startswith('#=GC RF'):
                qseq_align += line[7:].strip()
        origin_map_for = {}
        origin_map_rev = {}
        origin_new_map = {}
        new_origin_map = {}
        bracket_stack = []
        if pp_align == "":
            pp_align = '.' * len(tseq_ailgn)
        index = 0
        for tseq_char, pp_char, struct_char, qseq_char in zip(tseq_ailgn, pp_align, struct_align, qseq_align):
            if struct_char in '[{(<':
                bracket_stack.append(index)
            elif struct_char in ']})>':
                close_index = bracket_stack.pop()
                origin_map_for[close_index] = index
                origin_map_rev[index] = close_index
            if tseq_char.upper() in 'AGCU':
                if struct_char in '[]{}()<>':
                    origin_new_map[len(structure)] = index
                    new_origin_map[index] = len(structure)
                structure += struct_char
                sequence += tseq_char.upper()
            index += 1
        fix_structure = ''
        for index, struct_char in enumerate(structure):
            if struct_char in '[{(<':
                open_index = origin_new_map.get(index)
                close_index = origin_map_for.get(open_index)
                if new_origin_map.get(close_index) is None:
                    fix_structure += '.'
                else:
                    fix_structure += '('
            elif struct_char in ']})>':
                close_index = origin_new_map.get(index)
                open_index = origin_map_rev.get(close_index)
                if new_origin_map.get(open_index) is None:
                    fix_structure += '.'
                else:
                    fix_structure += ')'
            else:
                fix_structure += '.'
        structure = fix_structure
    align = {'tseq': tseq_ailgn, 'pp': pp_align, 'struct': struct_align, 'qseq': qseq_align}
    return structure, sequence, align


def repair_struct(structure: str) ->str:
    global cm_not_align_counter
    res = None
    if structure is not None:
        counter = 0
        balanced = True
        res = ''
        for char in structure:
            if char in '{[<(':
                counter += 1
                res += '('
            elif char in '}]>)':
                counter -= 1
                res += ')'
                if counter < 0:
                    balanced = False
            else:
                res += '.'
        if counter != 0:
            balanced = False
        if not balanced:
            cm_not_align_counter += 1
            logging.warning('Structure not balanced ({})\nstruct: '
                                '{}\nfinal: {}'.format(cm_not_align_counter, structure, res))
    return res



# CMALIGN
def align_sequences(sequences: Dict[str, str], cm_path: str, out_align_path: str) -> bool:
    result = False
    tmp_sequences_fasta = generate_fasta(sequences)
    try:
        param_list = [os.path.join(INFENRAL_PATH, CMALIGN_EXE), '-g', '-o', out_align_path, cm_path,
                      tmp_sequences_fasta.name]
        logging.info("Aligning sequences to CM file {}".format(cm_path))
        with Popen(param_list, stdout=PIPE, stdin=PIPE) as proc:
            ret_code = proc.wait()
            if ret_code < 0:
                raise Exception("cmalign ended with error code {}".format(ret_code))
        if os.path.exists(out_align_path):
            result = True
    except Exception as e:
        if os.path.exists(out_align_path):
            os.remove(out_align_path)
        logging.error("Failed to sequence to cm file {}. ERROR: {}".format(cm_path, e))
    finally:
        if tmp_sequences_fasta is not None and os.path.exists(tmp_sequences_fasta.name):
            os.remove(tmp_sequences_fasta.name)
    return result


def get_sequence(genome_id: str, start_index: int, end_index: int, strand: str) -> str:
    handle = None
    while handle is None:
        try:
            handle = Entrez.efetch(db="nucleotide", id=genome_id, rettype="gb", retmode="text", seq_start=start_index,
                                   seq_stop=end_index, strand=strand)
        except:
            print("Connection failure, sleep and retry")
            time.sleep(5)
    print("{} {}-{}({})".format(genome_id, start_index, end_index, strand))
    try:
        record = SeqIO.read(handle, "genbank")
        res = record.seq.transcribe()
    except:
        res = None
    handle.close()
    return res


def generate_cm(cm_file_path: str, sequence: str, seq_id: str) -> str:
    structure = ''
    stk_file = '{}.stk'.format(seq_id)
    try:
        align_sequences({'1': sequence}, cm_file_path, stk_file)
        structure, _, _ = analyze_stk(stk_file, '1')
    except Exception as e:
        logging.warning('Failed to get cm structure {}\n{}'.format(seq_id, e))
        structure = None
    finally:
        if os.path.exists(stk_file):
            os.remove(stk_file)
    return repair_struct(structure)


# CMSEARCH
def search_cm(cm_file_path: str, seqdb_path: str, sname: str, debug: bool=False) \
        -> Tuple[str, str]:
    structure = None
    sequence = None
    temp_out = None
    try:
        temp_out = NTF(dir='.', delete=False)
        temp_out.close()
        param_list = [os.path.join(INFENRAL_PATH, CMSEARCH_EXE), '--max', '--incE', '10', '-g', '-A', temp_out.name, cm_file_path, seqdb_path]
        logging.info("Starting cm search {}".format(param_list))
        with Popen(param_list, stdout=PIPE, stdin=PIPE) as proc:
            output, err = proc.communicate()
            ret_code = proc.wait()
            if ret_code < 0:
                raise Exception(err)
            name = None
            all_file = ""
            with open(temp_out.name, 'r') as test_file:
                for line in test_file:
                    all_file += line
                    print(line)
                    if len(line.strip()) > 1 and line[0] != '#' and name is None:
                        name = line.split()[0]
                        break
            if name is None:
                raise Exception("failed to figure new name\n{}".format(all_file))
            structure, sequence, align = analyze_stk(temp_out.name, name)
    except Exception as e:
        logging.error("Failed to search cm file {} on sequence db {}. ERROR: {}"
                      .format(cm_file_path, seqdb_path, e))
    finally:
        if temp_out is not None and os.path.exists(temp_out.name):
            if not debug:
                os.remove(temp_out.name)
            else:
                logging.info("Finished debug run on cm: {} output: {}".format(cm_file_path, temp_out.name))
    return structure, sequence


# general
class RiboD:
    def __init__(self,seq_id: str, sequence: str, energy_structure: str, cm_structure: str):
        self.seq_id = seq_id
        self.sequence = sequence
        self.energy_structure = energy_structure
        self.cm_structure = cm_structure

    def __str__(self):
        return "('{}','{}','{}','{}')".format(self.seq_id, self.sequence, self.energy_structure,
                                              self.cm_structure)


def read_existing(sql_input:str) -> Dict[str, str]:
    seen_map = {}
    if os.path.exists(sql_input):
        all_text = ''
        with open(sql_input, 'r') as output_read:
            for line in output_read:
                all_text += line
        entry = None
        in_quotes = False
        counter = 0
        for char in all_text[len(SQL_INSERT):]:
            if entry is None:
                if char == '(':
                    entry = char
                    in_quotes = False
            elif in_quotes:
                if char == "'":
                    in_quotes = False
                entry += char
            elif char == "'":
                in_quotes = True
                entry += char
            elif char == ')':
                identifier = entry[1:].split(',', 1)[0].strip("'")
                counter += 1
                seen_map[identifier] = '{})'.format(entry)
                entry = None
            else:
                entry += char
    return seen_map


def fill_seq(input_csv: str, output_sql: str):
    seen_map = read_existing(output_sql)
    now = datetime.datetime.now()
    with open(input_csv, 'r') as input_table, open(output_sql, 'a+') as insertion_sql, open("ERROR_LOG{}".format(now.strftime("%Y_%m_%d_%H_%M_%S")),'w') as err_log:
        header = dict([(item.strip(), index) for index, item in enumerate(input_table.readline().strip().split(','))])
        for index, line in enumerate(input_table):
            items = [item.strip() for item in line.strip().split(',')]
            if len(items) < len(header):
                logging.warning("Lines should have all information:\n{}".format(line))
                continue
            seq_id = items[header.get('Seq_ID')]
            if seq_id in seen_map:
                logging.info("Skipped {}, done before".format(seq_id))
                continue
            genome_id = items[header.get('Genome_ID')]
            riboswitch_class = items[header.get('Riboswitch_class')]
            start = int(items[header.get('Ribo_start')])
            end = int(items[header.get('Ribo_end')])
            strand = items[header.get('Riboswitch_strand')]
            sequence = get_sequence(genome_id, start, end, strand)
            if sequence is None:
                err_msg = "Failed to get sequence for {} {}-{}({})".format(genome_id, start, end, strand)
                err_log.write("{}\n".format(err_msg))
                err_log.flush()
                print(err_msg)
                continue
            cm_structure = generate_cm('{}.cm'.format(riboswitch_class), sequence, seq_id)
            min_structure = vienna.fold(sequence, structure_constraints=cm_structure)['MFE']
            insertion_sql.write('{} {};\n'.format(SQL_INSERT,
                                                  str(RiboD(seq_id, sequence, min_structure, cm_structure))))
            insertion_sql.flush()
        insertion_sql.write(':')


def merge_sql(sql_file: str):
    # merge for bug that retested sequences. Note that
    seen_map = read_existing(sql_file)
    with open(sql_file, 'w') as rewrite_sql:
        for key, insert_data in seen_map.items():
            if key != '' and key != 'SeqId':
                rewrite_sql.write("{} {};\n".format(SQL_INSERT, insert_data))


def read_class(input_csv: str) -> Dict[str, str]:
    class_map = {}
    with open(input_csv, 'r') as input_table:
        header = dict([(item.strip(), index) for index, item in enumerate(input_table.readline().strip().split(','))])
        for index, line in enumerate(input_table):
            items = [item.strip() for item in line.strip().split(',')]
            if len(items) < len(header):
                logging.warning("Lines should have all information:\n{}".format(line))
                continue
            seq_id = items[header.get('Seq_ID')]
            riboswitch_class = items[header.get('Riboswitch_class')]
            class_map[seq_id] = '{}.cm'.format(riboswitch_class)
    return class_map


def try_cmsearch(input_csv: str, sql_file: str):
    seen_map = read_existing(sql_file)
    seen_map_done = read_existing('Ribod_insertions_v2_cmsearch.sql')
    class_map = read_class(input_csv)
    with open('Ribod_insertions_v2_cmsearch.sql', 'a+') as write_cmserarch,\
         open('Ribod_insertions_v2_cmsearch_fail.sql', 'w') as write_fail_cmserarch:
        for name, value in seen_map.items():
            if name in seen_map_done:
                continue
            seq_db = None
            try:
                break_value = value[1:len(value)-1].split(',')
                ribod = RiboD(name, break_value[1].strip("'").upper(), break_value[2].strip("'"),
                              break_value[3].strip("'"))
                seq_db = generate_fasta({name: ribod.sequence})
                newstruct, newseq = search_cm(class_map.get(name), seq_db.name, name)
                if len(ribod.sequence) != len(newseq):
                    ribod_copy = deepcopy(ribod)
                    ribod.sequence = newseq.upper()
                    ribod.cm_structure = newstruct
                    ribod.energy_structure = vienna.fold(newseq, structure_constraints=ribod.cm_structure)['MFE']
                    logging.error("Sequence structure lenght differ")
                    write_fail_cmserarch.write("{};DIFF:{}\n".format(str(ribod_copy),
                                                                     len(ribod_copy.sequence) - len(newseq)))
                    write_cmserarch.write("{} {};\n".format(SQL_INSERT, str(ribod)))
                else:
                    ribod.sequence = newseq.upper()
                    ribod.cm_structure = newstruct
                    ribod.energy_structure = vienna.fold(newseq, structure_constraints=ribod.cm_structure)['MFE']
                    write_cmserarch.write("{} {};\n".format(SQL_INSERT, str(ribod)))
            except Exception as e:
                print("Error on ID: {}\n{}".format(name, e))
            finally:
                if seq_db is not None and os.path.exists(seq_db.name):
                    os.remove(seq_db.name)


def find_missing(sql1: str, sql2: str):
    seen_map1 = read_existing(sql1)
    seen_map2 = read_existing(sql2)
    with open('Ribod_insertions_cmsearch_v2_missing.sql', 'w') as missing:
        for key, value in seen_map1.items():
            if key not in seen_map2:
                break_value = value[1:len(value) - 1].split(',')
                ribod = RiboD(key, break_value[1].strip("'").upper(), break_value[2].strip("'"),
                              break_value[3].strip("'"))
                missing.write('{} {};\n'.format(SQL_INSERT, str(ribod)))


def find_missing_ids(inputcsv: str, outputsql: str):
    #seq_id = 'NC_015578.1:937418-937521'
    #sequence = get_sequence('NC_015578.1',937418,937521,'-')
    #cm_structure = generate_cm('TPP.cm', sequence, seq_id)
    #min_structure = vienna.fold(sequence, structure_constraints=cm_structure)['MFE']
    #print('{} {};\n'.format(SQL_INSERT, str(RiboD(seq_id, sequence, min_structure, cm_structure))))
    seen_map = read_existing(outputsql)
    class_map = read_class(inputcsv)
    for key, value in class_map.items():
        if key not in seen_map:
            print("Missing key: {}".format(key))


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    Entrez.email = "matandro@post.bgu.ac.il"
    # assumes Ribod_database.csv in same folder
    input = "Ribod_database_v2.csv"
    output = "Ribod_insertions_v2.sql"
    #fill_seq(input, output)
    #merge_sql(output)
    #try_cmsearch(input, output)
    #find_missing(output, 'FINAL_Ribod_insertions_cmsearch.sql')
    try_cmsearch(input, output)
    #find_missing_ids(input, output)

