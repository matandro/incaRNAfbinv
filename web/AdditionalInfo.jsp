<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 08/04/15
  Time: 18:16
  To change this template use File | Settings | File Templates.
--%>
<% session.setAttribute("nav_source", "addinfo"); %>
<%@ include file="header.jsp" %>
<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Additional information</h3>
        </div>
        <div class="panel-body">
            This web server is based on two core applications:<br>

            <h4><b>RNAfbinv 2.0</b></h4>
            RNAfbinv 2.0 (RNA fragment base inverse) was developed in Danny Barash's lab, publication noted below.

            RNAfbinv 2.0 is a python 3 package available via pypi. It can be easily installed using <code>pip install rnafbinv</code>.
            The package is pypy 3 compatible for increased performance. The new version provides increased control over motif features.
            A GUI wrapper is available for download in the git repository: <a href="https://github.com/matandro/RNAsfbinv/">RNAfbinv 2.0 git</a><br>

            <h4><b>RNAfbinv 1.0</b></h4>
            RNAfbinv (RNA fragment base inverse) was developed in Danny Barash's lab, see article: L. Weinbrand, A. Avihoo, D. Barash.
            RNAfbinv: An Interactive Java Application for Fragment-Based Design of RNA Sequences. Bioinformatics, 29(22)
            , 2938-2940 (2013).<br>

            The tool uses a simulated annealing process to design sequences that fold as the target structure and/or its
            coarse grained tree-graph shape. RNAfbinv is focused on fragment based design, to allow the user a greater flexibility in the design.<br>
            RNAfbinv download link: <a href="http://www.cs.bgu.ac.il/~RNAexinv/RNAfbinv/">RNAfbinv webpage</a><br>

            <h4><b>incaRNAtion 1.0</b></h4>
			IncaRNAtion was developed in collaboration between J&#233;r&#244;me Waldisp&#252;hl's lab and Yann Ponty, see article:  V. Reinharz, Y. Ponty
			and J. Waldisp&#252;hl: A weighted sampling algorithm for the design of RNA sequences with targeted secondary structure and nucleotides distribution Bioinformatics 29 (13), i308-i315 (2013)
			The tool samples stochastically from the whole sequence space weighting in favour of sequences with high affinity with the target structure in linear time. 
			IncaRNAtion allows the control of the GC content and sequence constraints without biasing the sampling.<br>
			incaRNAtion download link: <a href="http://jwgitlab.cs.mcgill.ca/vreinharz/incarnation">incaRNAtion webpage</a><br>

            <h4><b>Implementation</b></h4>
            The web server runs on Tomcat 8. The backend is implemented using Java EE. The frontend generates JSP web pages.
            It is designed using the bootstrap css framework and utilizes JavaScript, JQuery, JSON and ajax.

            <h3>Citation</h3>
            <h4>Version 2.0</h4>
            Drory Retwitzer, M., Reinharz, V., Churkin, A., Ponty, Y., Waldisp&#252;hl, J., & Barash, D. (2019)
            incaRNAfbinv 2.0 - A webserver and software with motif control for fragment-based design of RNAs. In preparation.
            <h4>Version 1.0</h4>
            Drory Retwitzer, M., Reinharz, V., Ponty, Y., Waldisp&#252;hl, J., & Barash, D. (2016).
            incaRNAfbinv: a web server for the fragment-based design of RNA sequences. Nucleic acids research,
            44(W1), W308-W314.

            <h3>About us</h3>
            The web server was developed by Matan Drory et al. in Dr. Danny Barash's lab at Ben-Gurion University,
            Beer Sheva, Israel. For all relevant issues and suggestions please contact Matan Drory: <a
                href="mailto:matandro@cs.bgu.ac.il?Subject=incaRNAtion">matandro@cs.bgu.ac.il</a>
        </div>
    </div>
</div>
<%@ include file="Footer.jsp" %>
