package bgu.bioinf.rnaDesign.controller;

import bgu.bioinf.rnaDesign.Producers.ResultProducer;
import bgu.bioinf.rnaDesign.model.JobInfoModel;
import bgu.bioinf.rnaDesign.model.JobResultModel;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matan on 01/12/15.
 */
public class ResultController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_RESULTS_PER_PAGE = 20;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String error = "";
        boolean emailErr = ("1".equals(request.getParameter("emailErr")));
        String sortBy = request.getParameter("sortBy");

        Integer pageNo = 0;
        try {
            pageNo = Integer.valueOf(request.getParameter("page"));
        } catch (Exception ignore) {
        }

        // Setup filters
        List<Float> filters = new ArrayList<Float>(3);
        for (int i = 0 ; i < JobResultModel.FILTER_NAMES.length ; ++i) {
            Float filter = null;
            try {
                filter = Float.valueOf(request.getParameter(JobResultModel.FILTER_NAMES[i]));
            } catch (Exception ignore) {
            }
            filters.add(filter);
        }

        // retrieve job ID from request
        String jobId = request.getParameter("jid");
        ResultProducer resultProducer = new ResultProducer(jobId, pageNo, sortBy, filters, MAX_RESULTS_PER_PAGE);
        // check if job exists / ready
        if (!resultProducer.isResultsReady()) {
            error = resultProducer.getError();
        } else {
            // job is ready take results
            resultProducer.initiateResults();
        }

        // initiate proper page
        if ("".equals(error)) {
            JobInfoModel jobInfoModel = resultProducer.getJobInformation();
            JobResultModel jobResultModel = resultProducer.getJobResults();
            jobResultModel.setTotalNoOfResults(resultProducer.getNoOfResults(jobInfoModel.isJobReady()));
            // Setup attribute and forward to view
            request.setAttribute("resultsModel", jobResultModel);
            request.setAttribute("jobInfoModel", jobInfoModel);
            request.setAttribute("emailErr", emailErr);
            RequestDispatcher view = request.getRequestDispatcher("ShowResults.jsp");
            view.forward(request, response);
        } else {
            request.setAttribute("error", error);
            RequestDispatcher view = request.getRequestDispatcher("SubmissionError.jsp");
            view.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
