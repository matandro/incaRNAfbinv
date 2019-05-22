package bgu.bioinf.rnaDesign.Producers;

import bgu.bioinf.rnaDesign.model.JobInfoModel;
import sun.net.smtp.SmtpClient;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by matan on 17/11/15.
 */
public class MailDispatcher {
    private static final String FROM_RNAPATTMATCH = "incaRNAfbinv@cs.bgu.ac.il";
    private static final String SMTP_ADDRESS = "smtp.bgu.ac.il";
    private static final String SITE_ADDRESS = "https://www.cs.bgu.ac.il/incaRNAfbinv";


    public static boolean submissionMail(JobInfoModel jobInformation) {
        SmtpClient client = null;
        if (jobInformation.getEmail() == null || "".equals(jobInformation.getEmail())) {
            return false;
        }

        boolean success = false;
        try {
            client = new SmtpClient(SMTP_ADDRESS);
            client.from(FROM_RNAPATTMATCH);
            client.to(jobInformation.getEmail());
            PrintStream message = client.startMessage();
            String fromStr = "From: incaRNAtion + RNAfbinv" + (jobInformation.getVersion() == 2 ? "2.0" : "");
            message.println(fromStr);
            message.println("To: " + jobInformation.getEmail());
            message.println("Subject: Results for query " + jobInformation.getQueryName());
            message.print("Hello ");
            message.println(jobInformation.getEmail() + ",");
            message.println("Thank you for using our RNA design web server application.");
            message.println("The sequence design started, the Job ID is " + jobInformation.getJobId());
            message.println("Your results will be available in the following link:");
            message.println();
            message.println(SITE_ADDRESS + "/GetResults.jsp?jid=" + jobInformation.getJobId());
            message.println();
            message.println("Results will be removed after a week.");
            message.println("Have a good day!");
            message.println();
            message.flush();
            message.close();
            client.closeServer();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    client.closeServer();
                } catch (IOException ignore) {
                }
            }
        }
        return success;
    }
}
