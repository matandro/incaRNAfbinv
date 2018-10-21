package bgu.bioinf.rnaDesign.controller;

import bgu.bioinf.rnaDesign.Listeners.WebappContextListener;
import bgu.bioinf.rnaDesign.Producers.*;
import bgu.bioinf.rnaDesign.Runners.RNAfoldRunner;
import bgu.bioinf.rnaDesign.model.MotifData;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by matan on 27/12/15.
 */
public class MotifListController extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String structure = Utils.replaceBracketType(Utils.removeAllWhitespaces(request.getParameter("structure")));
        String sequence = Utils.removeAllWhitespaces(request.getParameter("sequence"));
        String motifStr = request.getParameter("motif");
        List<Integer> marked = null;
        boolean isStructureChange = Boolean.valueOf(request.getParameter("isStructureChange"));

        if (structure == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if ("".equals(sequence)) {
            sequence = new String(new char[structure.length()]).replaceAll("\0", "N");
        }

        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        ShapiroGenerator shapiroGenerator = new ShapiroGenerator(structure);
        JSONObject data = new JSONObject();

        if (isStructureChange || !"".equals(motifStr)) {
            // Load fragments any way, Need to get indexs
            FragmentParser fragmentParser = new FragmentParser(shapiroGenerator);
            Map<Integer, MotifData> motifs = fragmentParser.getMotifs();
            List<String> values = new ArrayList<String>(motifs.size());
            for (Map.Entry<Integer, MotifData> motif : motifs.entrySet()) {
                String val = motif.getKey() + "_" + motif.getValue().getValue();
                values.add(val);

                if (val.equals(motifStr)) {
                    marked = shapiroGenerator.getIndexByMotif(motif.getValue());
                }
            }
            // Attach to data
            if (isStructureChange) {
                List<String> options = new ArrayList<String>(motifs.size());
                for (MotifData motif : motifs.values()) {
                    options.add(motif.getName() + " - " + motif.getValue());
                }
                data.put("options", options);
                data.put("values", values);
            }
        }

        File imageFile = null;
        try {
            ImageProducer im = new VarnaRNAImageProducer(sequence, structure, "", marked);
            imageFile = new File(im.getImage());
            if (imageFile == null) {
                imageFile = new File(WebappContextListener.getWarPath() + "/img/NoImage.jpg");
            }
            byte[] imgData = Utils.fileIntoByteArray(imageFile);
            if (imgData != null) {
                byte[] imageDataBytes = Base64.encodeBase64(imgData);
                String imageDataString = new String(imageDataBytes);
                data.put("img", imageDataString);
            }
            data.put("updated", true);
        } catch (Exception e) {
            Utils.log("SEVERE", e, "MotifListController.doPost");

        } finally {
            if (imageFile != null) {
                imageFile.delete();
            }
        }

        data.writeJSONString(response.getWriter());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
