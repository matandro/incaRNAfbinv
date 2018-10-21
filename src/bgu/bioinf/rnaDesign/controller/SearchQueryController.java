package bgu.bioinf.rnaDesign.controller;

import bgu.bioinf.rnaDesign.Producers.JobListProducer;
import bgu.bioinf.rnaDesign.model.JobInfoModel;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by matan on 23/12/15.
 */
public class SearchQueryController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jobId = request.getParameter("jid");
        // If we searched job id, try to load it via GetResults
        if (jobId != null && !"".equals(jobId)) {
            String urlStr = "GetResults.jsp?jid=" + jobId;
            String encodedURL = response.encodeRedirectURL(urlStr);
            response.sendRedirect(encodedURL);
            return;
        }
        // Otherwise search for relevant queries
        String jobName = request.getParameter("jobName");

        JobListProducer jobListProducer = new JobListProducer(jobName);
        List<JobInfoModel> results = jobListProducer.getJobList();

        if (jobName != null && !"".equals(jobName)) {
            request.setAttribute("jobName", jobName);
        }
        request.setAttribute("jobList", results);
        RequestDispatcher view = request.getRequestDispatcher("SearchQuery.jsp");
        view.forward(request, response);
    }
}
