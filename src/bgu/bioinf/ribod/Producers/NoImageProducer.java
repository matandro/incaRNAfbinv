package bgu.bioinf.ribod.Producers;

import bgu.bioinf.rnaDesign.Producers.ImageProducer;

public class NoImageProducer implements ImageProducer {
    /*
    private String locationURL;

    public NoImageProducer(String locationURL) {
        this.locationURL = locationURL;
    }*/

    @Override
    public String getImage() {
        return "img/ribod_nocm.jpg";
    }
}
