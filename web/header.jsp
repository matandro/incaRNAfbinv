<%--
  Created by IntelliJ IDEA.
  User: alex
  Date: 21/12/14
  Time: 11:41
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <!-- Bootstrap -->
    <link href="${pageContext.request.contextPath}/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/bootstrap-theme.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">

    <!-- jquery and bootstrap -->
    <script src="${pageContext.request.contextPath}/js/jquery.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/bootstrap.js"></script>

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    <title>incaRNAtion & RNAfbinv</title>
</head>

<body>
<header class="container" id="top-container">
    <!-- Static navbar -->
    <nav class="navbar navbar-default">
        <div class="container-fluid">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar"
                        aria-expanded="false" aria-controls="navbar">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="${pageContext.request.contextPath}" tabindex="-1">
                    <img src="${pageContext.request.contextPath}/img/Site-logo.png" alt="bgu-mcgill" id="logo"/>

                </a>
                <a class="navbar-brand" href="${pageContext.request.contextPath}/index.jsp">
                    incaRNAfbinv
                </a>
            </div>
            <div id="navbar" class="navbar-collapse collapse">
                <ul class="nav navbar-nav">
                    <li
                            <% if ("index".equals(session.getAttribute("nav_source"))) {%>
                            class="active"
                            <%}%>><a href="${pageContext.request.contextPath}/index.jsp">Design Form</a></li>
                    <li<% if ("results".equals(session.getAttribute("nav_source"))) {%>
                            class="active"
                            <%}%>><a href="${pageContext.request.contextPath}/SearchQuery.jsp">Search Result</a></li>
                    <li<% if ("help".equals(session.getAttribute("nav_source"))) {%>
                            class="active"
                            <%}%>><a href="${pageContext.request.contextPath}/Help.jsp">Help</a></li>
                    <li
                            <% if ("addinfo".equals(session.getAttribute("nav_source"))) {%>
                            class="active"
                            <%}%>><a href="${pageContext.request.contextPath}/AdditionalInfo.jsp">About</a></li>
                </ul>
            </div>
            <!--/.nav-collapse -->
        </div>
        <!--/.container-fluid -->
    </nav>
</header>