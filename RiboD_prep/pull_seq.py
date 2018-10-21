import logging
from Bio import SeqIO, Entrez
from rnafbinv import vienna
from typing import Dict, List
from tempfile import NamedTemporaryFile as NTF
import os
from subprocess import Popen, PIPE
import re


INFENRAL_PATH = "/opt/algorithm/infernal/bin/"
CMALIGN_EXE = "cmalign"
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
    handle = Entrez.efetch(db="nucleotide", id=genome_id, rettype="gb", retmode="text", seq_start=start_index,
                           seq_stop=end_index, strand=strand)
    record = SeqIO.read(handle, "genbank")
    handle.close()
    res = record.seq.transcribe()
    return res


def generate_cm(cm_file_path: str, sequence: str, seq_id: str) -> str:
    def repair_struct() ->str:
        global cm_not_align_counter
        if structure is not None:
            counter = 0
            balanced = True
            res = ''
            for char in structure:
                if char in '{[<':
                    counter += 1
                    res += '('
                elif char in '}]>':
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
                logging.warning('Structure not balanced ({})\nseq: {}\nstruct: '
                                '{}\nfinal: {}'.format(cm_not_align_counter, seq_ailgn, struct_align, res))
            return res
        return None
    structure = ''
    stk_file = '{}.stk'.format(seq_id)
    try:
        align_sequences({'1': sequence}, cm_file_path, stk_file)
        with open(stk_file, 'r') as stk_input:
            seq_ailgn = ''
            pp_align = ''
            struct_align = ''
            for line in stk_input:
                if line[0] == '1':
                    seq_ailgn += line.split(maxsplit=1)[1].strip()
                elif line.startswith('#=GR 1 PP'):
                    pp_align += line[9:].strip()
                elif line.startswith('#=GC SS_cons'):
                    struct_align += line[12:].strip()
            for seq_char, pp_char, struct_char in zip(seq_ailgn, pp_align, struct_align):
                if seq_char.upper() in 'AGCU':
                    structure += struct_char
    except Exception as e:
        logging.warning('Failed to get cm structure {}\n{}'.format(seq_id, e))
        structure = None
    finally:
        if os.path.exists(stk_file):
            os.remove(stk_file)
    return repair_struct()


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
    with open(input_csv, 'r') as input_table, open(output_sql, 'a+') as insertion_sql:
        if len(seen_map) == 0:
            first = True
        else:
            first = False
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
            min_structure = vienna.fold(sequence)['MFE']
            cm_structure = generate_cm('{}.cm'.format(riboswitch_class), sequence, seq_id)
            if first:
                first = False
            else:
                insertion_sql.write(', ')
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


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    Entrez.email = "matandro@post.bgu.ac.il"
    # assumes Ribod_database.csv in same folder
    input = "Ribod_database.csv"
    output = "ribod_insertions.sql"
    fill_seq(input, output)
    #merge_sql(output)
