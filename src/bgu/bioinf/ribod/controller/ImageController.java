package bgu.bioinf.ribod.controller;


import bgu.bioinf.ribod.Producers.NoImageProducer;
import bgu.bioinf.ribod.Producers.RibodException;
import bgu.bioinf.ribod.db.RiboDEntity;
import bgu.bioinf.ribod.model.RiboDResult;
import bgu.bioinf.rnaDesign.Producers.ImageProducer;
import bgu.bioinf.rnaDesign.Producers.Utils;
import bgu.bioinf.rnaDesign.Producers.VarnaRNAImageProducer;
import bgu.bioinf.rnaDesign.controller.ResultImageController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;

public class ImageController extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        String requestedFile = request.getPathInfo();

        if (requestedFile == null) {
            throw new RibodException("Image file name missing (<SeqId>.jsp)");
        }

        String pathString = URLDecoder.decode(requestedFile, "UTF-8");
        System.out.println(pathString);
        String seqId = pathString.substring(pathString.lastIndexOf('/') + 1, pathString.lastIndexOf('.'));
        RiboDResult ribodResult = null;
        try {
            ribodResult = RiboDEntity.retrieveDbObject(seqId);
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        if (ribodResult == null || ribodResult.error != null) {
            throw new RibodException(ribodResult.error);
        }

        String topic = seqId + " - ";
        ImageProducer imageProducer = null;
        boolean generated_new = true;
        String mfeStructure = ribodResult.riboDEntity.getEnergyStructure();
        String cmStructure = ribodResult.riboDEntity.getCmStructure();
        String sequence = ribodResult.riboDEntity.getSequence();
        Map<Integer, Integer> cmbps = new HashMap<>();
        String imageStructure;
        if (Utils.verifyStructure(ribodResult.riboDEntity.getCmStructure())) {
            imageStructure = mfeStructure;
            Stack<Integer> bracketStack = new Stack<>();
            boolean noMfe = false;
            for (int i=0; i < sequence.length(); ++i) {
                char cm = cmStructure.charAt(i);
                char mfe = mfeStructure.charAt(i);
                if (cm == '(') {
                    bracketStack.push(i);
                    if (mfe != '(')
                        noMfe = true;
                } else if (cm == ')') {
                    cmbps.put(bracketStack.pop() + 1, i + 1);
                    if (mfe != ')')
                        noMfe = true;
                }
            }
            if (noMfe) {
                imageStructure = cmStructure;
                topic += "could not match minimum energy structure";
            } else {
                topic += "combined";
            }
        } else {
            imageStructure = mfeStructure;
            topic += "no covarience model";
        }
        imageProducer = new VarnaRNAImageProducer(ribodResult.riboDEntity.getSequence(),
                imageStructure, topic, cmbps);

        String error = ResultImageController.writeImage(response, imageProducer);
        if (!"".equals(error))
            throw new RibodException("Failed to retrieve image, Please try again later");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        doPost(request, response);

    }
}
