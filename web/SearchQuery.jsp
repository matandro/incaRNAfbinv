<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 27/11/14
  Time: 23:49
  To change this template use File | Settings | File Templates.
--%>
<%--
<jsp:useBean id="jobList" scope="request" type="java.util.List"/>
<jsp:useBean id="jobName" scope="request" type="java.lang.String"/>
--%>
<% session.setAttribute("nav_source", "results"); %>
<%@ include file="header.jsp" %>
<div class="container">
    <div class="panel panel-default">
        <c:choose>
            <%-- Showing search results --%>
            <c:when test="${jobList != null && not empty jobList}">
                <div class="panel-heading">
                    <h3 class="panel-title">Jobs containing "<c:out value="${jobName}"/>" in name</h3>
                </div>
                <div class="panel-body">
                    <table class="table table-striped">
                        <tr>
                            <th>Job id</th>
                            <th>Job name</th>
                            <th>Target</th>
                            <th>Seed type</th>
                            <th>Submission time</th>
                            <th>Is ready?</th>
                        </tr>
                        <c:forEach items="${jobList}" var="jobInfoModel">
                            <tr>
                                <td>
                                    <a href="GetResults.jsp?jid=<c:out value="${jobInfoModel.jobId}"/>"><c:out
                                            value="${jobInfoModel.jobId}"/></a>
                                </td>
                                <td>
                                    <c:out value="${jobInfoModel.queryName}"/>
                                </td>
                                <td class="monotd">
                                    <c:out value="${jobInfoModel.querySequence}"/>
                                    <br><c:out value="${jobInfoModel.queryStructure}"/>
                                </td>
                                <td>
                                    <c:out value="${jobInfoModel.seedTypeString}"/>
                                </td>
                                <td>
                                    <c:out value="${jobInfoModel.formattedStartTime}"/>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${jobInfoModel.jobReady}">
                                            Yes
                                        </c:when>
                                        <c:otherwise>
                                            No
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <%-- No job results because... --%>
                <div class="panel-heading">
                    <h3 class="panel-title">Enter Job name or id:<c:out value="${jobName}"/></h3>
                </div>
                <div class="panel-body">
                    <form name="queryLocForm" action="GetJobs.jsp" method="get" role="form"
                          class="form-horizontal">
                        <div class="form-group">
                            <label class="control-label col-sm-2" for="jobName">Job Name: </label></td>
                            <div class="col-sm-10">
                                <input type="text" name="jobName" id="jobName" class="form-control">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-sm-2" for="jid">Job ID: </label>

                            <div class="col-sm-10">
                                <input type="text" name="jid" id="jid" class="form-control">
                            </div>
                        </div>
                        <input type="submit" value="Submit" class="button" class="btn btn-default">
                    </form>
                    <br>
                    <c:choose>
                        <%-- No query submitted yet --%>
                        <c:when test="${jobList == null && empty jobName && empty jid}">
                            <%-- Leave empty, just went in with nothing --%>
                        </c:when>
                        <%-- Failed to load job list caused by error --%>
                        <c:when test="${jobList == null}">
                            <h3><span class="label label-danger">
                            Failed to load jobs with query name <c:out value="${jobName}"/>, Please try again
                            later.
                        </span></h3>
                        </c:when>
                        <%-- no results found for the query name --%>
                        <c:otherwise>
                            <h3><span class="label label-danger">
                            No jobs found containing "<c:out value="${jobName}"/>" in name.
                        </span></h3>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
<%@ include file="Footer.jsp" %>