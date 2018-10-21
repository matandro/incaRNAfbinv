package bgu.bioinf.rnaDesign.controller;

import bgu.bioinf.rnaDesign.Producers.ResultProducer;
import bgu.bioinf.rnaDesign.db.JobEntity;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by matan on 13/12/15.
 */
public class CheckJobReadyController extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Boolean isReady = false;
        String error = "";
        JobEntity.Status jobStatus = null;
        long amountReady = 0;
        String jobId = request.getParameter("jobId");

        if (jobId == null) {
            error = "Could not read job id in query.";
        } else {
            ResultProducer resultProducer = new ResultProducer(jobId);
            isReady = resultProducer.isResultsReady();
            error = resultProducer.getError();
            jobStatus = resultProducer.getJobStatus();
            if (error != null && "".equals(error)) {
                amountReady = resultProducer.getNoOfResults(false);
            }
        }

        if (error != null && !"".equals(error)) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(error);
        } else {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            JSONObject result = new JSONObject();
            result.put("jobStatus", jobStatus.name());
            result.put("amountReady", amountReady);
            result.put("isReady", isReady);
            result.writeJSONString(response.getWriter());
        }
    }
}
