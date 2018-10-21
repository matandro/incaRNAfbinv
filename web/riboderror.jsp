<%@ page isErrorPage="true" contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE HTML>
<html>

<head>
    <title>RiboswitchDB</title>
    <link rel="icon" href="http://ribod.iiserkol.ac.in/iiserk.png" type="image/jpg"/>
    <meta name="description" content="website description" />
    <meta name="keywords" content="website keywords, website keywords" />
    <meta http-equiv="content-type" content="text/html; charset=windows-1252" />
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/ribod_style.css" /></head>

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
        <b><font color="red">Error: <%=exception.getMessage() %></font></b>
    </div>
    <div id="content_footer"></div>
    <div id="footer">
        <p>Copyright &copy; <a href="http://www.iiserkol.ac.in">IISER kolkata </a></p>
        <p> Department of Physical Sciences, Indian Institute of Science Education and Research, Kolkata </p>
    </div>
</div>
</body>
</html>
