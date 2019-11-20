<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 26/12/14
  Time: 11:34
  To change this template use File | Settings | File Templates.
--%>
<% session.setAttribute("nav_source", "help"); %>
<%@include file="header.jsp" %>
<%-- main help section --%>
<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Help</h3>
        </div>
        <div class="panel-body">
            IncaRNAfbinv offers an interactive environment for the inverse folding of RNA using a
            fragment-based design approach.<br>
            The algorithm implemented in our web server is a significant extension of two
            complementary methodologies: that described in Weinbrand et al. (Bioinformatics 2013,
            29(22): 2938-2940) called RNAfbinv, together with Reinharz et al. (Bioinformatics 2013,
            29(13): i308-i315) called incaRNAtion.<br>
            IncaRNAfbinv 2.0...<br><br>

            The server receives the desired secondary structure in dot bracket notation and additional parameters
            to allow the user to control specific aspects of the design. The maximum length allowed is 500 bases.
            The output includes the designed sequences and additional information such as structural distance to input,
            minimum free energy (based on the Turner model, 2004), neutrality and more.
            <br><br>

            <h3>Input</h3>
            <ul>
                <li>
                    <h4>Job name:</h4>
                    For personal use, can be used later on to search for old results (up to 1 week).
                    This parameter is optional.
                </li>
                <li>
                    <h4>e-mail:</h4>
                    Upon submission of the query form an e-mail will be sent to the given address which includes a
                    link
                    to the results page. Another mail will be sent again when the calculation is done and the results
                    are
                    ready for review.
                    Inserting your e-mail is optional but very much recommended for requests that include Mutational
                    robustness or require a large amount of designed sequences.
                </li>
            </ul>
            <div class="row">
                <div class="col-md-6">
                    <ul>
                        <li>
                            <h4>Target structure:</h4>
                            A sequence pattern based on the dot bracket notation (not including pseudoknots). This means
                            legal
                            characters are '.' to mark an unbounded base, '(' to mark first base in a base-pair and ')' to
                            mark
                            second base in a base pair (Or '<' and '>' respectively).
                            <h5>
                                Example:
                            </h5>
                            <pre class="monotd">((((((((...(.(((((.......))))).)........((((((.......))))))..))))))))</pre>
                            Structure of the Guanine-binding riboswitch aptamer (Kim and Breaker, Biol. Cell, 2008).
                        </li>
                        <li>
                            <h4>Target sequence:</h4>
                            A sequence pattern based on <a
                                href="https://en.wikipedia.org/wiki/Nucleic_acid_notation#IUPAC_notation"
                                target="_blank">IUPAC sequence notation</a> (not including 'x' and '-'). The sequence
                            constraint
                            is optional, if left empty then it will be replaced with 'N' x structure length. If used,
                            sequence constraints must have the same length as target structure. Result sequences must fit
                            this
                            sequence pattern. The sequence pattern is rigid and attached to an index.
                            <h4>
                                Example:
                            </h4>
                            The following sequence was constructed to match the structure above:
                            <pre class="monotd">NNNNNNNNUNNNNNNNNNNNNNNNNNNNNNNNNUNNNUNNNNNNNNNNNNNNNNNNNNNNYNNNNNNNN</pre>
                            Specific locations that are sequence conserved are constrained. Specifically these are the
                            nucleic acids that interact with the purine ligand.
                        </li>
                    </ul>
                </div>
                <div class="col-md-6 div-center-image">
                    <img src="${pageContext.request.contextPath}/img/Example_q1.jpg" class="img-fit-width"/>
                </div>
            </div>
            <ul>
                <li>
                    <h4>Target Energy: (Advanced Option)</h4>
                    Designed sequences will aim to fit the given minimum free energy. The calculation is done using RNAfold
                    From the <a href="http://www.tbi.univie.ac.at/RNA/">Vienna RNA Package</a> with the Turner energy
                    model, 2004. Target energy is an optional input.
                </li>
                <li>
                    <h4>Target Mutational robustness: (Advanced Option)</h4>
                    Designed sequences will aim to fit the given neutrality value [0,1]. Mutational robustness tests the base pair distance between the current
                    sequence to the fold of all the sequences that are a single point mutation away. This means that at
                    every iteration, to calculate this value, RNAfbinv must fold 3 * length(sequence) times. Using the
                    option slows down the calculation significantly and allows up to 300 max iterations and 50 output
                    sequences only.
                </li>
                <li>
                    <h4>Simulated Annealing Iterations: (Advanced Option)</h4>
                    The number of simulated annealing iterations done by RNAfbinv. By default 1000.
                </li>
                <li>
                    <h4>Consider sequence motifs (Advanced Option):</h4>
                    Considered consecutive lower case bases in the target sequence as a sequence motif. insertion and deletions
                    within a sequence motif incur increased penalties (See Design score -> Sequence alignment below).
                    The penalties are larger then single sequence deletion but are smaller then those that are connected to
                    structure. Note that sequence motif exist in the context of a single structural motif. This means that
                    a single consecutive lower case sequence spanning multiple motifs will be considered multiple sequence motifs.
                </li>
                <li>
                    <h4>Motif constraints:</h4>
                    Allows the user to select multiple motifs from the structure that will have a greater chance to
                    appear in the final result. The list of motifs will be filled upon insertion of a legal structure along side
                    an image of the structure generated by <a href="http://varna.lri.fr/">VARNA Visualization Applet for RNA</a>.
                    RNAfbinv 1.0 supports single motif constraint.
                </li>
                <li>
                    <h4>Varying size limit:</h4>
                    Avilable in RNAfbinv 2.0. The new motif comparison method allows for results of varying length.
                    This options is used to control the maximum and minimum length as query size &#177; varying length limit.
                </li>
                <li>
                    <h4>Seed generation method:</h4>
                    Any RNAfbinv run can start using a seed, the following methods are supported by the web-server.
                    <ul>
                        <li>
                            <h5>incaRNAtion</h5>
                            Unlike the original RNAfbinv, incaRNAtion uses a global search strategy. The adaptive sampling approach
                            simply generates sets of sequences by repeatedly running the stochastic backtrack algorithm.
                            incaRNAtion also allows the user to set a desired GC content distribution for the designed sequences.
                            Starting from incaRNAtion seeds allows RNAfbinv to reach the target structure in less iterations
                            and generates seeds with approximately the starting GC content.<br>

                            If selected the user must set the GC content of the seed sequences
                            (Advanced option). It is also possible to set a maximum GC content error from the selection.
                            The GC error option only effects the incaRNAtion seed content.
                        </li>
                        <li>
                            <h5>Random initial guess</h5>
                            RNAfbinv starts from a totally random sequence.
                        </li>
                        <li>
                            <h5>User Defined</h5>
                            RNAfbinv starts from a sequence given by the user. The sequence must be the same length of the structure and
                            in the <a href="https://en.wikipedia.org/wiki/Nucleic_acid_notation#IUPAC_notation"
                            target="_blank">IUPAC sequence notation</a>. The given sequence will be set as input for RNAfbinv
                            for all of the runs.
                        </li>
                    </ul>
                </li>
                <li>
                    <h4>Number of output sequences</h4>
                    Select the number of output designed sequences.
                </li>
            </ul>
            <h3>Examples</h3>
            We provide two simple examples. The examples are accessible in the selection box at the bottom of the input page.
            Once an example is selected, press the set button to apply it to the input form.
            <ul>
                <li>
                    <h4>Purine Riboswitch aptamer</h4>
                    Structure of the Guanine-binding riboswitch aptamer (Kim and Breaker, Biol. Cell, 2008).
                    <pre class="monotd">((((((((...(.(((((.......))))).)........((((((.......))))))..))))))))<br>NNNNNNNNUNNNNNNNNNNNNNNNNNNNNNNNNUNNNUNNNNNNNNNNNNNNNNNNNNNNYNNNNNNNN</pre>
                </li>
                <li>
                    <h4>miRNA-146 precursor</h4>
                    Structure of miRNA-146 precursor (Krol et al., J. Biol. Chem., 2004)
                    <pre class="monotd">((((..((((((((((((.((((((((............)))))))).)))))))))))).))))</pre>
                </li>
            </ul>
            <h3>Results</h3>
            The results section contains the designed pattern list with predicted structure and additional information
            stated below. The default sort is by Shapiro distance primary and BP distance secondary.
            The results can be downloaded in excel format for further analysis.<br>
            <ul>
                <li>
                    <h4>Run no:</h4>
                    The RNAfbinv run number. Only signifies the order of completion.
                </li>
                <li>
                    <h4>Sequence:</h4>
                    The resultant designed sequence with its folding predicted structure below it.
                </li>
                <li>
                    <h4>Shapiro structure: (Coarse grained representation)</h4>
                    Fragment based structure for the predicted fold. Hairpins, interior loops, bulges, multi-loops and
                    stems are represented by (H), (I), (B), (M) and (S) respectively (Shapiro B.A., 1988)
                </li>
                <li>
                    <h4>Energy score (dG):</h4>
                    Given the designed sequence and predicted structure we calculate the free energy using the Turner
                    energy model, 2004. This value is in kcal/mol. The value is calculated using functions from the
                    <a href="http://www.tbi.univie.ac.at/RNA/">Vienna RNA Package</a>.
                </li>
                <li>
                    <h4>Mutational Robustness</h4>
                    Mutational robustness tests the base pair distance between the current
                    sequence to the fold of all the sequences that are a single point mutation away. This means that at
                    every iteration, to calculate this value, RNAfbinv must fold 3 * length(seqeuence) times. Using the
                    option slows down the calculation significantly and allows up to 300 max iterations and 50 output
                    sequences only.
                </li>
                <li>
                    <h4>BP distance</h4>
                    The base pair distance between the structure of the predicted fold for the resultant sequence to the
                    target structure given in the input. The calculation counts the number of indexes where a mismatch exists.
                </li>
                <li>
                    <h4>Shapiro distance</h4>
                    The distance between the Shapiro structure tree-graph representation of the predicted fold of the
                    result sequence to the Shapiro tree-graph representation for the target structure given in the input.<br>
                    The calculation counts the number of insertion and deletions within the tree comparison.
                </li>
                <li>
                    <h4>Design Score</h4>
                    The design score RNAfbinv 2.0 generates for the resulted sequence.
                    <br>The design score is described by the following equations for target tree <i>T</i> and candidate tree <i>C</i>:<br>
                    <img src="${pageContext.request.contextPath}/img/score_help_of.png" /><br>
                    Where <i>TreeAlign(T,C)</i> is defined as:<br>
                    <img src="${pageContext.request.contextPath}/img/score_help_dp.png" /><br>
                    <i>ChildCombination</i> is the best <i>TreeAlign</i> over all ordered combinations of child motifs.
                    <i>Del</i> is the deletion cost of a single motif while &delta; is the deletion on the entire subtree (formula seen below)<br>
                    Alignment score values:
                    <ul>
                        <li>
                            <h5>Sequence alignment</h5>
                            Sequences are aligned per subsection (stem has 2, multi-loop has number of connected stems - 1, ect...)
                            1000 for deletion of non wild card ('N') nucleotide in target sequence, 1 for deletion of anything else.
                            Insertions are score with 1 like non 'N' deletion in target.<br>
                            When the sequence motif feature is active, insertion and deletion penalties are increased to 20
                            when they are done within lower case sequence regions in the target.<br>
                            The alignment objective function definition can be seen in the formula below:<br>
                            <img src="${pageContext.request.contextPath}/img/score_help_seqalign.png"/><br>
                        </li>
                        <li>
                            <h5>Motif deletion</h5>
                            Target: 1000 for conserved motif / 100 for normal motif + sequence alignment score
                            Design: 100 + sequence alignment score<br>
                            Deletion values are defined in the formula below:<br>
                            <img src="${pageContext.request.contextPath}/img/score_help_del.png"/><br>
                        </li>
                        <li>
                            <h5>Motif matching</h5>
                            Matching is allowed between different un-bounded motifs (Hairpin, buldge, multiloop, External sections and internal loop).
                            Bounded motifs (stems) are only matched to other bounded motifs. If a motif is set to be conserved it can only be compared to the exact same type.
                            The score matched the sequence alignment score for the two motifs.
                        </li>
                    </ul>
                </li>
                <li>
                    <h4>GC% content</h4>
                    The percentage of GC in the result sequence.
                </li>
                <li>
                    <h4>Additional Information:</h4>
                    <h5>Fold Image:</h5>
                    A secondary structure image of the designed sequence and its predicted fold.
                    RNAfbinv2.0 also marks nucleotides aligned to query sequence since they are compared to the proper
                    motif and not to a static index (as in RNAfbinv 1.0)
                    This image is generated by <a href="http://varna.lri.fr/">VARNA Visualization Applet for RNA</a>
                </li>
            </ul>
            <h3>Run Time</h3>
            The following table shows run times (Log10 seconds) for three different structures under five GC% contents. Tests were made with default options.
            The graph shows both seed generation times when using incaRNAtion seeds and RNAfbinv calculation, added together.
            <div class="col-md-12 div-center-image">
                <img src="${pageContext.request.contextPath}/img/Runtime.jpg" class="img-fit-width"/>
            </div>
            <h4>Structures:</h4>
            <ol>
                <li>
                    <h4>miRNA-146 precursor</h4>
                    65 bases.
                    <pre class="monotd">((((..((((((((((((.((((((((............)))))))).)))))))))))).))))</pre>
                </li>
                <li>
                    <h4>Purine Riboswitch aptamer</h4>
                    69 bases.
                    <pre class="monotd">((((((((...(.(((((.......))))).)........((((((.......))))))..))))))))</pre></li>

                <li>
                    <h4>Cobalamin Riboswitch aptamer</h4>
                    127 bases
                    <pre class="monotd">..((((((((......(((.......))).....((((......))))...........................(((((.......))))).....(((.......))).......))))))))..</pre>
                </li>
				<li>
					<h4>S14 Ribosomal RNA - Domain 2</h4>
					361 bases
					<pre class="monotd">..........(((((...(.((((.(.(((.(((((((.(((((((((((....(((((((.....)))))))...)))))))))..)))))))))...(((((((((..(((((((((..((((((((...(((......)))......))))))))..))....(..((....)))))))))).)))))).)))...))))..))))....((((((...((...((((.........))))...))))))))..........((((((..((((((((((((((.....))))))))))))))...((..)))).....)))))))))).(((......((((....))))....)))</pre>
				</li>
            </ol>
        </div>
    </div>
</div>
<%@ include file="Footer.jsp" %>
