package bgu.bioinf.rnaDesign.controller;

import bgu.bioinf.rnaDesign.Producers.JobProducer;
import bgu.bioinf.rnaDesign.Producers.Utils;
import bgu.bioinf.rnaDesign.model.JobInfoModel;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by matan on 15/10/15.
 */
public class SubmitJobController extends HttpServlet {
    private static final String METHOD_INCARNATION = "incaRNAtionRadio";
    private static final String METHOD_CUSTOM = "customRadio";

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String error = "";

        JobInfoModel jobInformation = new JobInfoModel();
        jobInformation.setQueryName(request.getParameter("query_name"));
        jobInformation.setEmail(request.getParameter("email"));
        try {
            jobInformation.setQueryStructure(Utils.replaceBracketType(Utils.removeAllWhitespaces(request.getParameter("query_structure"))));
        } catch (Exception e) {
            jobInformation.setQueryStructure("");
        }
        String sequence = Utils.removeAllWhitespaces(request.getParameter("query_sequence"));
        if (sequence == null || (sequence = sequence.toUpperCase()).equals("")) {
            sequence = Utils.createRepeat("N", jobInformation.getQueryStructure().length());
        }
        jobInformation.setQuerySequence(Utils.verifySequence(sequence, true));
        jobInformation.setOutputAmount(Integer.valueOf(request.getParameter("output_amount")));
        jobInformation.setTargetEnergy(-1000.0f);
        try {
            jobInformation.setTargetEnergy(Float.valueOf(request.getParameter("target_energy")));
        } catch (Exception ignore) {
        }
        jobInformation.setTargetMR(-1000.0f);
        try {
            jobInformation.setTargetMR(Float.valueOf(request.getParameter("target_mr")));
        } catch (Exception ignore) {
        }
        jobInformation.setNoIterations(1000);
        try {
            jobInformation.setNoIterations(Integer.valueOf(request.getParameter("No_Iterations")));
        } catch (Exception ignore) {
        }
        jobInformation.setVersion(2);
        try {
            jobInformation.setVersion(Integer.valueOf(request.getParameter("version")));
        } catch (Exception ignore) {
        }
        try {
            jobInformation.setVaryingSize(Integer.valueOf(request.getParameter("Varying_size")));
        } catch (Exception ignore) {

        }
        jobInformation.setMotifConstraint(request.getParameterValues("motif_constraint"));

        if (!"".equals(error)) {
            // No need for additional test, error is not empty
        } else if ("".equals(jobInformation.getQueryStructure())) {
            error = "Must insert structure";
        } else if (jobInformation.getQuerySequence() == null) {
            error = "Query sequence may only contain IUPAC nucleotide codes.";
        } else if (jobInformation.getQuerySequence().length() != jobInformation.getQueryStructure().length()) {
            error = "Query sequence and query structure length must be the same";
        } else if (!Utils.verifyStructure(jobInformation.getQueryStructure())) {
            error = "illeagal structure (Must be balanced and in dot bracket notation)";
        } else if (jobInformation.getQuerySequence() == null) {
            error = "Ilegal sequence (Must fit FASTA nucleic acid codes)";
        } else {
            String method = request.getParameter("methodRadio");
            if (METHOD_INCARNATION.equals(method)) {
                // TODO: Maybe check if constraints fit GC content if not throw an error - could be complicated
                jobInformation.setGcContent(50.0f);
                try {
                    String gcContent = request.getParameter("GC_content");
                    Float gcContentFloat = Float.valueOf(gcContent);
                    jobInformation.setGcContent(gcContentFloat);
                } catch (Exception ignore) {
                }
                jobInformation.setGcError(10.0f);
                try {
                    String gcErrorStr = request.getParameter("gc_error");
                    Float gcError = Float.valueOf(gcErrorStr);
                    jobInformation.setGcError(gcError);
                } catch (Exception ignore) {
                }
            } else if (METHOD_CUSTOM.equals(method)) {
                // TODO: check if seed sequence agree with query sequence
                String seedSequence = Utils.removeAllWhitespaces(request.getParameter("seed_sequence"));
                if (seedSequence == null || (seedSequence = seedSequence.toUpperCase()).equals("")) {
                    error = "Seed sequence is mandetory when selecting custom seed sequence.";
                } //else if ((seedSequence = Utils.verifySequence(seedSequence, false)) == null) {
                else if ((seedSequence = Utils.verifySequence(seedSequence,
                        jobInformation.getVersion() == 2)) == null) {
                    error = "Seed sequence may only contain DNA or RNA letters";
                } else if (seedSequence.length() != jobInformation.getQueryStructure().length()) {
                    error = "Seed sequence and query structure length must be the same";
                } else if (!Utils.isSequenceAgreement(jobInformation.getQuerySequence(), seedSequence)) {
                    error = "Seed sequence and Query sequence do not agree.";
                } else {
                    jobInformation.setSeedSequence(seedSequence);
                }
            }
        }


        if ("".equals(error)) {
            JobProducer jobProducer = new JobProducer(jobInformation);
            boolean isRunning = jobProducer.resolveJob();

            if (isRunning) {
                // Go to result page
                boolean emailSent = jobProducer.sendEmail();
                String urlStr = "GetResults.jsp?jid=" + jobInformation.getJobId();
                if (!emailSent) {
                    urlStr += "&emailErr=1";
                }
                String encodedURL = response.encodeRedirectURL(urlStr);
                response.sendRedirect(encodedURL);
                return;
            }

            error = jobProducer.getError();
        }

        request.setAttribute("error", error);
        RequestDispatcher view = request.getRequestDispatcher("SubmissionError.jsp");
        view.forward(request, response);
    }
}
