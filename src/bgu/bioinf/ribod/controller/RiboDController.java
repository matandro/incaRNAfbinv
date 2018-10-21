package bgu.bioinf.ribod.controller;

import bgu.bioinf.ribod.Producers.RibodException;
import bgu.bioinf.ribod.db.RiboDEntity;
import bgu.bioinf.ribod.model.RiboDResult;
import bgu.bioinf.rnaDesign.Producers.Utils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

public class RiboDController extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestedFile = request.getPathInfo();
        String error = "";

        if (requestedFile == null) {
            throw new RibodException("Image file name missing (<SeqId>.jsp)");
            /*
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image file name missing (<SeqId>.jsp)");
            return;
            */
        }

        String pathString = URLDecoder.decode(requestedFile, "UTF-8");
        pathString = pathString.substring(pathString.lastIndexOf('/') + 1, pathString.lastIndexOf('.'));
        RiboDResult ribodResult = null;
        try {
            ribodResult = RiboDEntity.retrieveDbObject(pathString);
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        if (ribodResult == null || ribodResult.error != null) {
            throw new RibodException(ribodResult.error);
            /*
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ribodResult.error);
            return;
            */
        }
        boolean hasCm = Utils.verifyStructure(ribodResult.riboDEntity.getCmStructure());

        request.setAttribute("hasCm", hasCm);
        request.setAttribute("seqId", pathString);
        RequestDispatcher view = request.getRequestDispatcher("/ribod.jsp");
        view.forward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
