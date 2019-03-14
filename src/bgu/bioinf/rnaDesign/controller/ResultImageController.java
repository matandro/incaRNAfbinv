package bgu.bioinf.rnaDesign.controller;

import bgu.bioinf.rnaDesign.Producers.ImageProducer;
import bgu.bioinf.rnaDesign.Producers.ResultProducer;
import bgu.bioinf.rnaDesign.Producers.VarnaRNAImageProducer;
import bgu.bioinf.rnaDesign.model.SingleResultModel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;

/**
 * Created by matan on 03/12/15.
 */
public class ResultImageController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String requestedFile = request.getPathInfo();
        String error = "";

        if (requestedFile == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image file name missing (<JobId_RunNo>.png)");
            return;
        }

        String pathString = URLDecoder.decode(requestedFile, "UTF-8");
        pathString = pathString.substring(pathString.lastIndexOf('/') + 1, pathString.lastIndexOf('.'));
        String[] idString = pathString.split("_");
        String jobId = idString[0];
        Integer runNo;
        try {
            runNo = Integer.valueOf(idString[1]);
        } catch (Exception e) {
            runNo = -1;
        }

        ResultProducer resultProducer = new ResultProducer(jobId);
        SingleResultModel singleResultModel;
        if (!resultProducer.isResultsReady()) {
            error = resultProducer.getError();
            if ("".equals(error)) {
                error = "Could not find job " + jobId;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND, error);
            return;
        } else if ((singleResultModel = resultProducer.getSingleResult(runNo)) == null) {
            error = resultProducer.getError();
            if ("".equals(error)) {
                error = "Could not find run " + runNo + " for job " + jobId;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND, error);
            return;
        } else {
            String topic = resultProducer.getJobInformation().getQueryName()
                    + " - Run No: " + singleResultModel.getResultNo();
            ImageProducer imageProducer = new VarnaRNAImageProducer(singleResultModel.getResultSequence(),
                    singleResultModel.getResultStructure(), topic);

            error = writeImage(response, imageProducer);
        }

        if (!"".equals(error)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, error);
        }

        return;
    }

    public static String writeImage(HttpServletResponse response, ImageProducer imageProducer) {
        String error = "";
        File imageFile = null;
        OutputStream out = null;
        try {
            response.setContentType("image/jpg");
            imageFile = new File(imageProducer.getImage());
            InputStream in = new FileInputStream(imageFile);
            out = response.getOutputStream();
            byte[] data = new byte[1024];
            int read;
            while ((read = in.read(data)) != -1) {
                out.write(data, 0, read);
            }
            out.flush();
        } catch (Exception e) {
            response.reset();
            e.printStackTrace();
            error = "Failed to retrieve image, Please try again later";
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ignore) {

                }
            }
            if (imageFile != null) {
                try {
                    imageFile.delete();
                } catch (Exception ignore) {

                }
            }
        }
        return error;
    }
}