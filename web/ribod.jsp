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
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/ribod_style.css">
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
        <table width="100%" style="overflow:scroll;">
            <tr>
                <th>Minimum energy structure over covarience model constraints</th>
            </tr>
            <tr>
                <td><img src="${pageContext.request.contextPath}/ribod/img/<c:out value="${seqId}"/>.jpg"
                         alt="Minimum energy structure over covarience model constraints" style="width:100%; max-width:580px;"></td>
            </tr>
            <tr>
                <td>
                    <p>Minimum energy structure constrained by base pairs in the covarience model alignment<br>
                    Basepairs from the covarience model alignment are marked in red</p>
                </td>
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
