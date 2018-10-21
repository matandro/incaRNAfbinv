package bgu.bioinf.ribod.controller;


import bgu.bioinf.ribod.Producers.NoImageProducer;
import bgu.bioinf.ribod.Producers.RibodException;
import bgu.bioinf.ribod.db.RiboDEntity;
import bgu.bioinf.ribod.model.RiboDResult;
import bgu.bioinf.rnaDesign.Producers.ImageProducer;
import bgu.bioinf.rnaDesign.Producers.Utils;
import bgu.bioinf.rnaDesign.Producers.VarnaRNAImageProducer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;

public class ImageController extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        String requestedFile = request.getPathInfo();

        if (requestedFile == null) {
            throw new RibodException("Image file name missing (<SeqId>.jsp)");
        }

        String pathString = URLDecoder.decode(requestedFile, "UTF-8");
        System.out.println(pathString);
        pathString = pathString.substring(pathString.lastIndexOf('/') + 1, pathString.lastIndexOf('.'));
        int break_index = pathString.lastIndexOf('_');
        String type = null;
        if (break_index < 0 || (!"cm".equals(type = pathString.substring(break_index + 1)) && !"me".equals(type))) {
            throw new RibodException("Illegal format, Sould be <SeqID>_<cm/me>.jpg");
        }
        String seqId = pathString.substring(0, break_index);
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
        if ("me".equals(type)) {
            topic +=  "Minimum Energy";
            imageProducer = new VarnaRNAImageProducer(ribodResult.riboDEntity.getSequence(),
                    ribodResult.riboDEntity.getEnergyStructure(), topic);
        }
        else {
            topic += "Covarience Model alignment";
            if (Utils.verifyStructure(ribodResult.riboDEntity.getCmStructure())) {
                imageProducer = new VarnaRNAImageProducer(ribodResult.riboDEntity.getSequence(),
                        ribodResult.riboDEntity.getCmStructure(), topic);
            } else {
                generated_new = false;
                imageProducer = new NoImageProducer();
            }
        }

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
            throw new RibodException("Failed to retrieve image, Please try again later");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ignore) {

                }
            }
            if (imageFile != null && generated_new) {
                try {
                    imageFile.delete();
                } catch (Exception ignore) {

                }
            }
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        doPost(request, response);

    }
}
