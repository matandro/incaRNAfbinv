<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 27/11/14
  Time: 23:49

  show resutlss
--%>

<% session.setAttribute("nav_source", "results"); %>
<%@ include file="header.jsp" %>
<c:set var="jobId" value="${jobInfoModel.jobId}"/>
<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Job: <c:out value="${jobInfoModel.queryName}"/></h3>
        </div>
        <div class="panel-body">
            <div class="row col-md-12">
                <c:if test="${emailErr == true}">
                    <h3>
                    <span class="label label-warning">Could not send mail with the job ID,
                        Please write your job ID for later use: <c:out value="${jobId}"/>
                    </span>
                    </h3>
                </c:if>
                <table class="table">
                    <tr>
                        <th>
                            Job ID:
                        </th>
                        <th>
                            Design target:
                        </th>
                        <th>
                            Target Energy:
                        </th>
                        <th>
                            Target Mutational Robustness:
                        </th>
                        <c:choose>
                            <c:when test="${jobInfoModel.gcContent != null}">
                                <th>
                                    GC content:
                                </th>
                            </c:when>
                            <c:when test="${jobInfoModel.seedSequence != null}">
                                <th>
                                    Seed sequence:
                                </th>
                            </c:when>
                        </c:choose>
                        <th>
                            Submission time:
                        </th>
                    </tr>
                    <tr>
                        <td>
                            <c:out value="${jobId}"/>
                        </td>
                        <td class="monotd">
                            <c:out value="${jobInfoModel.queryStructure}"/><br>
                            <c:out value="${jobInfoModel.querySequence}"/>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${jobInfoModel.targetEnergy == -1000}">
                                    N/A
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${jobInfoModel.targetEnergy}"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${jobInfoModel.targetMR == -1000}">
                                    N/A
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${jobInfoModel.targetMR}"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <c:choose>
                            <c:when test="${jobInfoModel.gcContent != null}">
                                <td>
                                    <c:out value="${jobInfoModel.gcContent}"/>%
                                </td>
                            </c:when>
                            <c:when test="${jobInfoModel.seedSequence != null}">
                                <td class="monotd">
                                    <c:out value="${jobInfoModel.seedSequence}"/>
                                </td>
                            </c:when>
                        </c:choose>
                        <td>
                            <c:out value="${jobInfoModel.formattedStartTime}"/>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="row col-md-12">
                <c:choose>
                    <%-- When the job isn't completed (still calculating) --%>
                <c:when test="${not jobInfoModel.jobReady}">
                    <br>

                    <h3><span class="label label-info">Still calculating job
                        (<span id="progress_div">Checking progress...</span>)</span></h3>
                    <span id="countdown"></span>
                </c:when>
                    <%-- When the job had an error while running (algorithm issue) --%>
                <c:when test="${not empty jobInfoModel.jobError}">
                    <br>

                    <h3><span class="label label-danger">Error running job:</span></h3>
                    <pre><c:out value="${jobInfoModel.jobError}"/></pre>
                </c:when>
                    <%-- When the job is ready and so are the results --%>
                <c:when test="${empty resultsModel.results}">
                    <br>

                    <h3><span class="label label-danger">Error running job:</span></h3>
                    <pre>Failed to generate sequences</pre>
                </c:when>
                <c:otherwise>
                <a href="${pageContext.request.contextPath}/Excel/<c:out value="${jobId}"/>.xlsx">Download
                    excel summary</a><br>

                <form class="form-inline" role="form">
                    <div class="col-md-11">
                        <label class="control-label" for="maxEnergy">
                            Maximum energy score:
                        </label>
                        <input type="number" class="form-control" id="maxEnergy"
                               value="<c:out value="${resultsModel.filters[0]}"/>">&nbsp;&nbsp;&nbsp;&nbsp;
                        <label for="maxBpDistance">
                            Maximum bp distance:
                        </label>
                        <input type="number" class="form-control" id="maxBpDistance"
                               value="<c:out value="${resultsModel.filters[1]}"/>">&nbsp;&nbsp;&nbsp;&nbsp;
                        <label for="maxShapiroDistance">
                            Maximum Shapiro distance:
                        </label>
                        <input type="number" class="form-control" id="maxShapiroDistance"
                               value="<c:out value="${resultsModel.filters[2]}"/>">&nbsp;&nbsp;&nbsp;&nbsp;
                    </div>
                    <div class="col-md-1">
                        <button type="button" class="btn btn-default" onclick="submitFilter();">Filter</button>
                    </div>
                    </button>
                </form>
                <c:set var="allResults" value="${resultsModel.results}"/>
                    <%-- We have results! --%>
                <c:set var="startResult"
                       value="${resultsModel.page * resultsModel.maxResultsInPage}"/>
                <c:set var="endResult"
                       value="${startResult + Math.min(resultsModel.maxResultsInPage,resultsModel.totalNoOfResults)}"/>
            </div>
                <%--
                <c:if test="${jobInfoModel.motifConstraint == null || ''.equals(jobInfoModel.motifConstraint)}">
                    <div class="row col-md-12">

                        <div class="row col-md-12">
                            <h3>
                        <span class="label label-warning">
                            Using motif constraints may reduce number of results.
                        </span>
                            </h3>
                        </div>
                    </div>
                </c:if>
                --%>
            <!-- ACTUAL DATA: HEADER START -->
            <c:set var="sortBy" value="${resultsModel.sortBy}"/>
            <div class="row col-md-12">
                <table class="table table-striped">
                    <tr>
                        <th>
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="Run number."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png"
                                     class="help">
                            </a>
                            <c:choose>
                                <c:when test="${sortBy.startsWith('resultNo')}">
                                    <c:choose>
                                        <c:when test="${sortBy.endsWith('ASC')}">
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'resultNo_DESC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                Run no.
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/desc.gif">
                                        </c:when>
                                        <c:otherwise>
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'resultNo_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                Run no.
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/asc.gif">
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out
                                                value="${'resultNo_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                        Run no.
                                        <img src="${pageContext.request.contextPath}/img/bg.gif">
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </th>
                        <th>
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="The generated sequence and an aligned structure prediction below."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png"
                                     class="help">
                            </a>
                            <c:choose>
                                <c:when test="${sortBy.startsWith('resultSequence')}">
                                    <c:choose>
                                        <c:when test="${sortBy.endsWith('ASC')}">
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'resultSequence_DESC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                Sequence
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/desc.gif">
                                        </c:when>
                                        <c:otherwise>
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'resultSequence_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                Sequence
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/asc.gif">
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out
                                                value="${'resultSequence_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                        Sequence
                                    </a>
                                    <img src="${pageContext.request.contextPath}/img/bg.gif">
                                </c:otherwise>
                            </c:choose>
                        </th>
                        <th>
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="The shapiro form of the predicted structure below."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png"
                                     class="help">
                            </a>
                            <c:choose>
                                <c:when test="${sortBy.startsWith('shapiroStructure')}">
                                    <c:choose>
                                        <c:when test="${sortBy.endsWith('ASC')}">
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'shapiroStructure_DESC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                Shapiro structure
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/desc.gif">
                                        </c:when>
                                        <c:otherwise>
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'shapiroStructure_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                Shapiro structure
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/asc.gif">
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out
                                                value="${'shapiroStructure_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                        Shapiro structure
                                    </a>
                                    <img src="${pageContext.request.contextPath}/img/bg.gif">
                                </c:otherwise>
                            </c:choose>
                        </th>
                        <th>
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="Energy of given match calculated using the Turner model, 2004."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png"
                                     class="help">
                            </a>
                            <c:choose>
                                <c:when test="${sortBy.startsWith('energyScore')}">
                                    <c:choose>
                                        <c:when test="${sortBy.endsWith('ASC')}">
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'energyScore_DESC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                Energy score (dG)
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/desc.gif">
                                        </c:when>
                                        <c:otherwise>
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'energyScore_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                Energy score (dG)
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/asc.gif">
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out
                                                value="${'energyScore_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                        Energy score (dG)
                                    </a>
                                    <img src="${pageContext.request.contextPath}/img/bg.gif">
                                </c:otherwise>
                            </c:choose>
                        </th>
                        <th>
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="The target structure neutrality [0,1]."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png"
                                     class="help">
                            </a>
                            <c:choose>
                                <c:when test="${sortBy.startsWith('mutationalRobustness')}">
                                    <c:choose>
                                        <c:when test="${sortBy.endsWith('ASC')}">
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'mutationalRobustness_DESC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                Mutational Robustness
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/desc.gif">
                                        </c:when>
                                        <c:otherwise>
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'mutationalRobustness_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                Mutational Robustness
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/asc.gif">
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out
                                                value="${'mutationalRobustness_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                        Mutational Robustness
                                    </a>
                                    <img src="${pageContext.request.contextPath}/img/bg.gif">
                                </c:otherwise>
                            </c:choose>
                        </th>
                        <th>
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="Base pair distance between query structure to sequence predicted structure."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png"
                                     class="help">
                            </a>
                            <c:choose>
                                <c:when test="${sortBy.startsWith('structureDistance')}">
                                    <c:choose>
                                        <c:when test="${sortBy.endsWith('ASC')}">
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'structureDistance_DESC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                BP distance
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/desc.gif">
                                        </c:when>
                                        <c:otherwise>
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'structureDistance_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                BP distance
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/asc.gif">
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out
                                                value="${'structureDistance_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                        BP distance
                                    </a>
                                    <img src="${pageContext.request.contextPath}/img/bg.gif">
                                </c:otherwise>
                            </c:choose>
                        </th>
                        <th>
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="Shapiro distance between query structure to sequence predicted structure."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png"
                                     class="help">
                            </a>
                            <c:choose>
                                <c:when test="${sortBy.startsWith('shapiroDistance')}">
                                    <c:choose>
                                        <c:when test="${sortBy.endsWith('ASC')}">
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'shapiroDistance_DESC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                Shapiro distance
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/desc.gif">
                                        </c:when>
                                        <c:otherwise>
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'shapiroDistance_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                Shapiro distance
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/asc.gif">
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out
                                                value="${'shapiroDistance_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                        Shapiro distance
                                    </a>
                                    <img src="${pageContext.request.contextPath}/img/bg.gif">
                                </c:otherwise>
                            </c:choose>
                        </th>
                        <th>
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="GC% content in designed sequence."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png"
                                     class="help">
                            </a>
                            <c:choose>
                                <c:when test="${sortBy.startsWith('gcContent')}">
                                    <c:choose>
                                        <c:when test="${sortBy.endsWith('ASC')}">
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'gcContent_DESC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                GC% content
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/desc.gif">
                                        </c:when>
                                        <c:otherwise>
                                            <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'gcContent_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                                GC% content
                                            </a>
                                            <img src="${pageContext.request.contextPath}/img/asc.gif">
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out
                                                value="${'gcContent_ASC'}"/><c:out value="${resultsModel.filterURL}"/>">
                                        GC% content
                                    </a>
                                    <img src="${pageContext.request.contextPath}/img/bg.gif">
                                </c:otherwise>
                            </c:choose>
                        </th>

                        <th>
                            <a href="#" data-toggle="tooltip" data-placement="top"
                               title="Additional information on the current match."
                               tabindex="-1">
                                <img src="${pageContext.request.contextPath}/img/help.png"
                                     class="help">
                            </a>
                            Additional Information
                        </th>
                    </tr>
                    <!-- ACTUAL DATA: HEADER END, START DATA -->
                    <c:forEach items="${allResults}" var="result">
                        <tr>
                            <td>
                                <c:out value="${result.resultNo}"/>
                            </td>
                            <td class="monotd">
                                <c:out value="${result.resultSequence}"/>
                                <br><c:out value="${result.resultStructure}"/>
                            </td>
                            <td class="monotd">
                                <c:out value="${result.resultShapiroCoarse}"/>
                            </td>
                            <td>
                                <c:if test="${result.energyScore != null}">
                                    <c:out value="${result.energyScore}"/>
                                </c:if>
                            </td>
                            <td>
                                <c:if test="${result.mutationalRobustness != null}">
                                    <c:out value="${result.mutationalRobustness}"/>
                                </c:if>
                            </td>
                            <td>
                                <c:if test="${result.bpDistance != null}">
                                    <c:out value="${result.bpDistance}"/>
                                </c:if>
                            </td>
                            <td>
                                <c:if test="${result.shapiroDistance != null}">
                                    <c:out value="${result.shapiroDistance}"/>
                                </c:if>
                            </td>
                            <td>
                                <c:if test="${result.gcContent != null}">
                                    <c:out value="${result.gcContentPrintable}"/>%
                                </c:if>
                            </td>
                            <td>
                                <ol>
                                    <li>
                                        <a href="${pageContext.request.contextPath}/img/result/<c:out value="${jobId}"/>_<c:out value="${result.resultNo}"/>.jpg"
                                           target="_blank"
                                           onclick="return windowpop(this.href, 545, 433)">
                                            Fold Image
                                        </a>
                                    </li>
                                </ol>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </div>
            <!-- ACTUAL DATA: DATA END -->
            <div class="row col-md-12">
                <h3>
                    Results <c:out value="${startResult + 1}"/>-<c:out
                        value="${endResult}"/> /
                    <c:out value="${resultsModel.totalNoOfResults}"/><br></h3>
                    <%-- More then one page of results, insert links --%>
                <c:if test="${resultsModel.totalPages > 1}">
                    <c:if test="${resultsModel.page > 0}">
                        <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                        value="${resultsModel.page - 1}"/>&sortBy=<c:out
                                        value="${sortBy}"/><c:out value="${resultsModel.filterURL}"/>">
                            <<
                        </a>
                    </c:if>
                    <a
                            <c:if test="${resultsModel.page != 0}">
                                href="GetResults.jsp?jid=<c:out
                                    value="${jobId}"/>&page=0&sortBy=<c:out
                                    value="${sortBy}"/><c:out value="${resultsModel.filterURL}"/>"
                            </c:if>
                    >First</a>
                    <c:forEach var="pages" begin="1"
                               end="${(resultsModel.totalPages - 2)}">
                        <a
                                <c:if test="${resultsModel.page != pages}">
                                    href="GetResults.jsp?jid=<c:out
                                        value="${jobId}"/>&page=<c:out
                                        value="${pages}"/>&sortBy=<c:out
                                        value="${sortBy}"/><c:out value="${resultsModel.filterURL}"/>"
                                </c:if>
                        ><c:out value="${pages + 1}"/></a>
                    </c:forEach>
                    <a
                            <c:if test="${resultsModel.page != (resultsModel.totalPages - 1)}">
                                href="GetResults.jsp?jid=<c:out
                                    value="${jobId}"/>&page=<c:out
                                    value="${resultsModel.totalPages - 1}"/>&sortBy=<c:out
                                    value="${sortBy}"/><c:out value="${resultsModel.filterURL}"/>"
                            </c:if>
                    >Last</a>
                    <c:if test="${resultsModel.page != (resultsModel.totalPages - 1)}">
                        <a href="GetResults.jsp?jid=<c:out value="${jobId}"/>&page=<c:out
                                        value="${resultsModel.page + 1}"/>&sortBy=<c:out
                                        value="${sortBy}"/><c:out value="${resultsModel.filterURL}"/>">
                            >>
                        </a>
                    </c:if>
                </c:if>
                </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>
<script language="JavaScript">
    function windowpop(url, width, height) {
        var leftPosition, topPosition;
        //Allow for borders.
        leftPosition = (window.screen.width / 2) - ((width / 2) + 10);
        //Allow for title and status bars.
        topPosition = (window.screen.height / 2) - ((height / 2) + 50);
        //Open the window.
        window.open(url, "Fold Image", "status=no,height=" + height + ",width=" + width + ",resizable=yes,left=" + leftPosition + ",top=" + topPosition + ",screenX=" + leftPosition + ",screenY=" + topPosition + ",toolbar=no,menubar=no,scrollbars=no,location=no,directories=no");
    }

    var times = 0;
    var timeLeft = 1;
    var updateInterval = null;

    function submitFilter() {
        var newURL = "GetResults.jsp?jid=<c:out value="${jobId}"/>";
        <c:if test="${emailErr == true}">
        newURL += "&emailErr=1";
        </c:if>
        // reset to page 0 on reset
        newURL += "&page=0&sortBy=<c:out value="${resultsModel.sortBy}"/>";
        filter = document.getElementById("maxEnergy");
        if (filter.value != null && filter.value != "")
            newURL += "&maxEnergy=" + filter.value;
        filter = document.getElementById("maxBpDistance");
        if (filter.value != null && filter.value != "")
            newURL += "&maxBpDistance=" + filter.value;
        filter = document.getElementById("maxShapiroDistance");
        if (filter.value != null && filter.value != "")
            newURL += "&maxShapiroDistance=" + filter.value;

        window.location = newURL
    }

    function checkReady() {
        if (updateInterval != null) {
            clearInterval(updateInterval);
            updateInterval = null;
        }
        var countdown = document.getElementById("countdown");
        if (countdown == null) return;

        timeLeft--;
        if (timeLeft <= 0) {
            countdown.innerHTML = "Checking state.";
            times++;
            $.ajax({
                type: 'GET',
                url: "${pageContext.request.contextPath}/IsJobReady",
                data: {jobId: "<c:out value="${jobId}"/>"},
                dataType: 'json',
                success: function (data) {
                    if (data.isReady) {
                        var newURL = "GetResults.jsp?jid=<c:out value="${jobId}"/>";
                        <c:if test = "${emailErr == true}" >
                        newURL += "&emailErr=1";
                        </c:if>
                        clearInterval(updateInterval);
                        window.location = newURL;
                    } else {
                        if(data.jobStatus == 'QUEUED')
                            $('#progress_div').html('Job in queue');
                        else if (data.jobStatus == 'PREP_SEED')
                            $('#progress_div').html('Preparing seeds');
                        else if (data.jobStatus == 'GENERATE_SEQ')
                            $('#progress_div').html(data.amountReady + ' out of <c:out value="${jobInfoModel.outputAmount}"/> ready');
                        timeLeft = Math.min(5 * Math.ceil(times / 2), 30);
                        countdown.innerHTML = "Checking state in " + timeLeft + " seconds.";
                        updateInterval = setInterval(checkReady, 1000);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    clearInterval(updateInterval);
                    alert("Error: " + jqXHR.responseText);
                    countdown.innerHTML = "Please try refreshing later or wait for e-mail.";
                }
            });
        } else {
            countdown.innerHTML = "Checking state in " + timeLeft + " seconds.";
            updateInterval = setInterval(checkReady, 1000);
        }
    }
    // once all is ready call for check in 1 second
    updateInterval = setInterval(checkReady, 1);
</script>
<%@ include file="Footer.jsp" %>