<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 26/11/14
  Time: 15:29
  To change this template use File | Settings | File Templates.
--%>

<% session.setAttribute("nav_source", "index"); %>
<%@ include file="header.jsp" %>
<script language="JavaScript">
    function validateBPElement(element) {
        if (isNaN(element) || (element.value < 0 && element.value != -1)) {
            return false;
        }
        return true;
    }

    function validateStructure(structure) {
        var error = "";
        if (structure == null || (structure = structure.replace(/\s|\r?\n|\t|\r/g, "")) == "") {
            error = "Query structure is mandetory";
        } else if (structure.length > 500) {
            error = "Query structure length is too long (Max 500 nt).";
        } else {
            var bracket = 0;
            for (var i = 0; i < structure.length; ++i) {
                if (structure[i] == '(' || structure[i] == '<')
                    bracket++;
                else if (structure[i] == ')' || structure[i] == '>') {
                    if (--bracket < 0) {
                        error = "Query structure brackets are un balanced.";
                        break;
                    }
                } else if (structure[i] != '.') {
                    error = "Query structure may only contain '(', ')', '<', '>' or '.'.";
                    break;
                }
            }
            if (bracket > 0) {
                error = "Query structure brackets are un balanced.";
            }
        }
        return error;
    }

    function validateSequence(structure, sequence) {
        var error = "";
        if (structure != null) {
            structure = structure.replace(/\s|\r?\n|\t|\r/g, "");
        }
        if (sequence != null && (sequence = sequence.replace(/\s|\r?\n|\t|\r/g, "")) != "") {
            if (sequence.length != structure.length) {
                error = "Query sequence must be the same length as query structure.";
            } else if (!sequence.match(/[AGCTURYKMSWBDHVN]+/)) {
                error = "Query sequence may only contain legal fasta nucleic acid codes.";
            }
        }
        return error;
    }
    function resetForm(isReset) {
        if (isReset)
            document.getElementById('mainForm').reset();
        initMethod();
        initAdvanced();
        changeIgnore('target_energy');
        changeIgnore('target_mr');
        changeIgnore('gc_error');
        updateMotif(document.getElementById("query_structure"));
        document.getElementById('formSubmitBtn').disabled = false;
    }
    function validateForm() {
        // Test structure
        var structure = document.getElementById('query_structure').value;
        var error = validateStructure(structure);
        if (error != "") {
            alert(error);
            return;
        }

        var sequence = document.getElementById('query_sequence').value;
        error = validateSequence(structure, sequence);
        if (error != "") {
            alert(error);
            return;
        }

        if (document.getElementById('ignore_target_mr').checked) {
            if (document.getElementById('No_Iterations').value > 300) {
                alert("When target mutational robustness is active Number of iterations is capped at 300");
                return;
            }
            if (document.getElementById('output_amount').value > 50) {
                alert("When target mutational robustness is active Number of output sequences is capped at 50");
                return;
            }
        }

        if (document.getElementById('incaRNAtionRadio').checked) {
            // check incaRNAtion values
            if (document.getElementById('GC_content').value == null
                    || document.getElementById('GC_content').value > 100
                    || document.getElementById('GC_content').value < 0) {
                alert("GC% content is mandatory on incaRNAtion mode (values 0-100%).");
                return;
            }
        } else if (document.getElementById('customRadio').checked) {
            // Show custom seed, hide incaRNAtion
            var sequence = document.getElementById('seed_sequence').value;
            if (sequence == null && (sequence = sequence.replace(/\s|\r?\n|\t|\r/g, "")) == "") {
                alert("Seed sequence is mandatory when selecting custom seed.");
                return;
            }
            else {
                if (sequence.length != structure.length) {
                    alert("Seed sequence must be the same length as query structure.");
                    return;
                } else if (!sequence.match(/[AGCTU]+/)) {
                    alert("Seed sequence may only contain DNA or RNA letters.");
                    return;
                }
            }
        } else {
            // random mode, nothing to do
        }

        var element = document.getElementById("email");
        if (element.value == null || element.value == "") {
            element.disabled = true;
        }
        document.getElementById('formSubmitBtn').disabled = true;
        document.getElementById('mainForm').submit();
    }

    function changeIgnore(name) {
        if (document.getElementById('ignore_' + name).checked) {
            document.getElementById(name).disabled = false;
        } else {
            document.getElementById(name).disabled = true;
        }
    }

    function initMethod() {
        if (document.getElementById('incaRNAtionRadio').checked) {
            // show incaRNAtion setting, Hide custom seed sequence
            document.getElementById('incaRNAtion_options').style.display = 'block';
            document.getElementById('random_options').style.display = 'none';
            document.getElementById('custom_options').style.display = 'none';
        } else if (document.getElementById('customRadio').checked) {
            // Show custom seed, hide incaRNAtion
            document.getElementById('incaRNAtion_options').style.display = 'none';
            document.getElementById('random_options').style.display = 'none';
            document.getElementById('custom_options').style.display = 'block';
        } else {
            // Hide both
            document.getElementById('incaRNAtion_options').style.display = 'none';
            document.getElementById('random_options').style.display = 'block';
            document.getElementById('custom_options').style.display = 'none';
        }
    }

    function initAdvanced() {
        if (document.getElementById('showAdvanced').checked) {
            // Show advanced options
            document.getElementById('advanced_target').style.display = 'block';
            document.getElementById('advance_incaRNAtion').style.display = 'block';
        } else {
            // Hide advanced options
            document.getElementById('advanced_target').style.display = 'none';
            document.getElementById('advance_incaRNAtion').style.display = 'none';
        }
    }


    function setQuery() {
        var selectElement = document.getElementById('exampleQuerySelect');
        var structureElement = document.getElementById("query_structure");
        var sequenceElement = document.getElementById("query_sequence");

        var query = selectElement.options[selectElement.selectedIndex].value;
        if (query == "q1") {
            sequenceElement.value = "NNNNNNNNUNNNNNNNNNNNNNNNNNNNNNNNNUNNNUNNNNNNNNNNNNNNNNNNNNNNYNNNNNNNN";
            structureElement.value = "((((((((...(.(((((.......))))).)........((((((.......))))))..))))))))";
        } else if (query == "q2") {
            sequenceElement.value = "";
            structureElement.value = "((((..((((((((((((.((((((((............)))))))).)))))))))))).))))";
        }
        updateMotif(structureElement);
    }

    function imgError() {
        var imageElement = $('#motif_image');
        imageElement.attr('src', "${pageContext.request.contextPath}/img/NoImage.png");
        return true;
    }

    function pullSelected(select) {
        var result = ""
        var options = select && select.options;
        var opt;

        for (var i=0, iLen=options.length; i<iLen; i++) {
            opt = options[i];

            if (opt.selected) {
                result += (opt.value || opt.text) + ",";
            }
        }
        return result;
    }

    function updateMotif(callingElement) {
        var imageElement = $('#motif_image');
        var structureElement = document.getElementById('query_structure');
        var structure = structureElement.value;
        var motifReady = (validateStructure(structure) == "");
        // check structure
        if (motifReady) {
            // if still here structure is legal, generate correct call
            var isStructureChange = structureElement == callingElement;
            var sequence = document.getElementById('query_sequence').value;
            if (validateSequence(structure, sequence) != "") {
                sequence = "";
            }
            var motif = pullSelected(document.getElementById('motif_constraint'));
            $.ajaxSetup({cache: false});
            $.ajax({
                type: 'POST',
                url: "${pageContext.request.contextPath}/GetMotifs",
                data: {structure: structure, sequence: sequence, isStructureChange: isStructureChange, motif: motif,
                    version: 1},
                dataType: 'json',
                success: function (data) {
                    if (isStructureChange) {
                        // update motif selection if change is structural
                        var motifSelectElement = document.getElementById('motif_constraint');
                        motifSelectElement.options.length = 0;
                        motifSelectElement.options.add(new Option("", ""));
                        for (var i = 0; i < data.options.length; ++i) {
                            motifSelectElement.options.add(new Option(data.options[i], data.values[i]));
                        }
                    }
                    if (data.updated) {
                        // update image if new image created
                        imageElement.attr('src', "data:image/jpeg;base64," + data.img);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    motifReady = false;
                    if (jqXHR.readyState == 0 || jqXHR.status == 0)
                        return;
                    alert("Failed to retrieve motif information");
                }
            });
        }
        // if error in ajax or structure incorrect, reset image and motif
        if (!motifReady) {
            var motifSelectElement = document.getElementById('motif_constraint');
            motifSelectElement.options.length = 0;
            imageElement.attr('src', "${pageContext.request.contextPath}/img/NoImage.png");
        }
    }

</script>
<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Design Form - incaRNAfbinv 1.0</h3> <a href="index.jsp">Press to try the new incaRNAfbinv 2.0</a>
        </div>
        <div class="panel-body">
            <form action="SubmitJob.jsp" method="post" role="form" id="mainForm">
                <input type="hidden" id="version" name="version" value="1">
                <%-- 1) Job name and email --%>
                <div class="row">
                    <div class="form-group col-md-6">
                        <label class="control-label" for="query_name">
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="A name for the job.
Can be used to search for the job later on." tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                            </a>
                            Job Name <span class="text-muted">(Optional)</span>:
                        </label>
                        <input type="text" id="query_name" name="query_name" placeholder="Enter query name"
                               class="form-control">
                    </div>
                    <div class="form-group col-md-6">
                        <label class="control-label" for="email">
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="A link to the results will be sent by e-mail. The email will not be displayed at any page!"
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                            </a>
                            e-mail <span class="text-muted">(Optional)</span>:
                        </label>
                        <input type="email" id="email" name="email" placeholder="Enter e-mail" class="form-control">
                    </div>
                </div>
                <div class="row row-separate"></div>
                <%-- 1) Target structure 2) target sequence 3) target energy ,mutational robustness and iteration No--%>
                <h4>Target Information:</h4>
                <div class="row">
                    <div class="col-md-7">
                        <div class="row">
                            <div class="form-group col-md-12">
                                <label class="control-label" for="query_structure">
                                    <a href="#" data-toggle="tooltip" data-placement="top"
                                       title="Pattern of RNA structure.
Supports dot bracket notation &quot;(.)&quot; or &quot;<.>&quot;." tabindex="-1">
                                        <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                                    </a>Structure:
                                </label>
                        <textarea id="query_structure" name="query_structure" rows="4"
                                  class="form-control monotd" placeholder="Enter query structure"
                                  onchange="updateMotif(this)"></textarea>
                            </div>
                        </div>
                        <div class="row">
                            <div class="form-group col-md-12">
                                <label class="control-label" for="query_sequence">
                                    <a href="#" data-toggle="tooltip" data-placement="top"
                                       title="Pattern of RNA sequence constraints.
Supports IUPAC sequence notation." tabindex="-1">
                                        <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                                    </a>Sequence constraints <span class="text-muted">(Optional)</span>:
                                    <br>
                                    <small><a
                                            href="https://en.wikipedia.org/wiki/Nucleic_acid_notation#IUPAC_notation"
                                            target="_blank">IUPAC
                                        sequence notation</a></small>
                                </label>
                        <textarea id="query_sequence" name="query_sequence" rows="4"
                                  class="form-control monotd" placeholder="Enter query sequence"
                                  onchange="updateMotif(this)"></textarea>
                            </div>
                        </div>
                        <div id="advanced_target">
                            <h5>Advanced:</h5>
                            <div class="row">
                                <div class="form-inline col-md-3">
                                    <label class="control-label" for="target_energy">
                                        <a href="#" data-toggle="tooltip" data-placement="top"
                                           title="A target energy score, designed structures will approximate this value.
                                   Ignored unless checkbox marked." tabindex="-1">
                                            <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                                        </a>
                                        Target Energy (kcal/mol):
                                    </label>
                                    <div class="input-group">
                                <span class="input-group-addon">
                                    <input type="checkbox" id="ignore_target_energy" name="ignore_target_energy"
                                           onchange="changeIgnore('target_energy')"/>
                                </span>
                                        <input type="number" min="-999.0" max="100.0" name="target_energy"
                                               id="target_energy" class="form-control"/>
                                    </div>
                                </div>
                                <div class="form-inline col-md-4">
                                    <label class="control-label" for="target_mr">
                                        <a href="#" data-toggle="tooltip" data-placement="top"
                                           title="The target structure neutrality [0,1].
                                   Ignored unless checkbox marked. This option makes calculation slow." tabindex="-1">
                                            <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                                        </a>
                                        Target mutational robustness:
                                    </label>
                                    <div class="input-group">
                                <span class="input-group-addon">
                                    <input type="checkbox" id="ignore_target_mr" name="ignore_target_mr"
                                           onchange="changeIgnore('target_mr')"/>
                                </span>
                                        <input type="number" step="0.01" min="0.00" max="1.00" name="target_mr"
                                               id="target_mr" class="form-control"/>
                                    </div>
                                </div>
                                <div class="form-inline col-md-3">
                                    <label class="control-label" for="No_Iterations">
                                        <a href="#" data-toggle="tooltip" data-placement="top"
                                           title="Number of simulated annealing rounds done by RNAfbinv (per sequence) Up to 10k rounds"
                                           tabindex="-1">
                                            <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                                        </a>
                                        Simulated Annealing Iterations:
                                    </label>
                                    <input type="number" value="1000" min="1" max="10000" name="No_Iterations"
                                           id="No_Iterations" class="form-control"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-5">
                        <div class="row div-center-image">
                            <img src="${pageContext.request.contextPath}/img/NoImage.png" onError="imgError();"
                                 id="motif_image" name="motif_image" class="img-fit-width"/>
                        </div>
                        <div class="row">
                            <div class="form-inline col-md-12">
                                <label class="control-label" for="motif_constraint">
                                    <a href="#" data-toggle="tooltip" data-placement="top"
                                       title="Select a structural motif to be kept complete. May reduce number of results,
                                       In case of failure to preserve motif a result is denied."
                                       tabindex="-1">
                                        <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                                    </a>
                                    Motif selection (Optional):
                                </label>
                                <select class="form-control" id="motif_constraint" name="motif_constraint"
                                        onchange="updateMotif(this)">
                                </select>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row row-separate"></div>
                <%-- 1) Method 2) specific inputs --%>
                <h4>Seed Information:</h4>
                <div class="row" id="seed_method_row">
                    <div class="form-group col-md-12">
                        <label class="control-label">
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="The method used to generate seed sequences for RNAfbinv."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                            </a>Generation method:
                        </label>
                        <label class="radio-inline">
                            <input type="radio" name="methodRadio" id="incaRNAtionRadio"
                                   value="incaRNAtionRadio"
                                   onclick="initMethod()" checked>
                            <b>incaRNAtion</b>
                        </label>
                        <label class="radio-inline">
                            <input type="radio" name="methodRadio" id="radnomRadio"
                                   value="radnomRadio"
                                   onclick="initMethod()">
                            <b>Random initial guess</b>
                        </label>
                        <label class="radio-inline">
                            <input type="radio" name="methodRadio" id="customRadio"
                                   value="customRadio"
                                   onclick="initMethod()">
                            <b>User defined</b> <span class="text-muted">Single sequence for multiple runs</span>
                        </label>
                    </div>
                </div>
                <div id="incaRNAtion_options">
                    <div class="row">
                        <div class="form-inline col-md-12">
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="incaRNAtion will generate a sequence with an approximation of the given GC% content."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                            </a>
                            <label class="control-label" for="GC_content">
                                GC% content:
                            </label>
                            <input type="number" step="0.1" value="50.0" min="0.0" max="100.0" name="GC_content"
                                   id="GC_content" class="form-control"/>
                        </div>
                    </div>
                    <div id="advance_incaRNAtion">
                        <h5>Advanced:</h5>
                        <div class="row">
                            <div class="form-inline col-md-3">
                                <label class="control-label" for="gc_error">
                                    <a href="#" data-toggle="tooltip" data-placement="top"
                                       title="The maximum GC% error allowed in seeds.
                                   If not marked, default is 10%." tabindex="-1">
                                        <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                                    </a>
                                    GC% error:
                                </label>
                                <div class="input-group">
                                <span class="input-group-addon">
                                    <input type="checkbox" id="ignore_gc_error" name="ignore_gc_error"
                                           onchange="changeIgnore('gc_error')"/>
                                </span>
                                    <input type="number" min="0.0" max="100.0" name="gc_error"
                                           id="gc_error" value="10" class="form-control"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row" id="random_options"></div>
                <div class="row" id="custom_options">
                    <div class="form-group col-md-12">
                        <label class="control-label">
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="An RNA / DNA sequence, containing only A,G,C,T/U.
                               This sequence will be the initial sequence for RNAfbinv simulated annealing process."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                            </a>
                            Seed sequence:
                        </label>
                            <textarea id="seed_sequence" name="seed_sequence" rows="4"
                                      class="form-control monotd" placeholder="Enter seed sequence"></textarea>
                    </div>
                </div>
                <div class="row row-separate"></div>
                <%-- 1) No of outputs --%>
                <div class="row">
                    <div class="form-group col-md-12 form-inline">
                        <label class="control-label" for="output_amount">
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="Amount of sequences to generate. More sequences, longer calculation."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                            </a>No. output sequences:
                        </label>
                        <select class="form-control" id="output_amount" name="output_amount">
                            <option>1</option>
                            <option>10</option>
                            <option selected="selected">20</option>
                            <option>50</option>
                            <option>75</option>
                            <option>100</option>
                            <option>200</option>
                        </select>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-md-12">
                        <input type="checkbox" id="showAdvanced" name="showAdvanced" onchange="initAdvanced()"/>
                        <label class="control-label" for="showAdvanced">
                            Show Advanced Options
                        </label>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-md-12">
                        <input type="button" value="Submit job" onclick="validateForm()" class="btn btn-success"
                               id="formSubmitBtn">
                        <input type="button" value="Reset" onclick="resetForm(true)" class="btn btn-danger"
                               id="formResetBtn">
                    </div>
                </div>
            </form>
            <div class="row row-separate"></div>
            <%-- Example Section --%>
            <h3>Examples:</h3>
            <form class="form-inline">
                <div class="input-group">
                    <span class="input-group-addon">Queries:</span>
                    <select class="form-control" id="exampleQuerySelect">
                        <option></option>
                        <option value="q1">Guanine-binding riboswitch aptamer</option>
                        <option value="q2">miRNA-146 precursor</option>
                    </select>
                    <span class="input-group-btn">
                        <button class="btn btn-default" type="button" onclick="setQuery();">Set</button>
                    </span>
                </div>
            </form>
        </div>
    </div>
</div>
<script language="javascript">
    window.onload = resetForm(false);
</script>
<%@ include file="Footer.jsp" %>