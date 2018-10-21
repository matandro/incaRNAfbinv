<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE HTML>
<html>

<head>
    <title>RiboswitchDB</title>
    <link rel="icon" href="http://ribod.iiserkol.ac.in/iiserk.png" type="image/jpg"/>
    <meta name="description" content="website description" />
    <meta name="keywords" content="website keywords, website keywords" />
    <meta http-equiv="content-type" content="text/html; charset=windows-1252" />
    <link rel="stylesheet" type="text/css" href="http://ribod.iiserkol.ac.in/style/style.css" />
    <script type="text/javascript" src="http://gc.kis.v2.scr.kaspersky-labs.com/1E221B15-F960-BF48-8E21-6D43A677E93C/main.js" charset="UTF-8"></script>
</head>

<body>
<div id="main">
    <div id="header">
        <div id="logo">
            <div id="logo_text">

                <img src="http://ribod.iiserkol.ac.in/pic/ribod_logo2.png" alt="logo" height="125" width="880" />
            </div>
        </div>

    </div>

    <div id="content_header"></div>
    <div id="site_content">
        <div id="sidebar_container">
        </div>
        <table width="100%" align="center" style="overflow:scroll;">
            <tr>
                <th align="center">Minimum energy structure</th>
                <th align="center">Covariance model alignment structure</th>
            </tr>
            <tr>
                <td align="center"><img src="${pageContext.request.contextPath}/ribod/img/<c:out value="${seqId}"/>_me.jpg"
                         alt="Minimum energy structure" style="width:100%; max-width:580px;"></td>
                <c:choose>
                    <c:when test="${hasCm}">
                        <td align="center"><img src="${pageContext.request.contextPath}/ribod/img/<c:out value="${seqId}"/>_cm.jpg"
                                                alt="Covarience model alignment structure" style="width:100%; max-width:580px;"></td>
                    </c:when>
                    <c:otherwise>
                        <td align="center"><img src="${pageContext.request.contextPath}/img/ribod_nocm.jpg"
                                                alt="Covarience model alignment structure" style="width:100%; max-width:580px;"></td>
                    </c:otherwise>
                </c:choose>
            </tr>
        </table>

    </div>
    <div id="content_footer"></div>
    <div id="footer">
        <p>Copyright &copy; <a href="http://www.iiserkol.ac.in">IISER kolkata </a></p>
        <p> Department of Physical Sciences, Indian Institute of Science Education and Research, Kolkata </p>
    </div>
</div>
</body>
</html>
