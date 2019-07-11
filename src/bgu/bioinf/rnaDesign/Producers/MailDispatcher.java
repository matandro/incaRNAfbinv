package bgu.bioinf.rnaDesign.Producers;

import bgu.bioinf.rnaDesign.model.JobInfoModel;
import bgu.bioinf.brlab.credentials.EmailCredentials;
import sun.net.smtp.SmtpClient;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Created by matan on 27/12/14.
 */
public class MailDispatcher {
    private static final String FROM_RNAPATTMATCH = EmailCredentials.USER_NAME;
    private static final String SMTP_ADDRESS = EmailCredentials.SMTP_ADDRESS;

    private static final String PASSWORD_RNAPATTMATCH = EmailCredentials.PASSWORD;
    private static final String SITE_ADDRESS = "https://www.cs.bgu.ac.il/incaRNAfbinv";

    private static boolean sendMail(String bodyText, String subject, String recipient) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", SMTP_ADDRESS);
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_RNAPATTMATCH, PASSWORD_RNAPATTMATCH);
                    }
                });
        boolean success = false;
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_RNAPATTMATCH));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipient)
            );
            message.setSubject(subject);
            message.setText(bodyText);
            Transport.send(message);
            success = true;
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public static boolean submissionMail(JobInfoModel jobInformation) {
        SmtpClient client = null;
        if (jobInformation.getEmail() == null || "".equals(jobInformation.getEmail())) {
            return false;
        }

        StringBuilder message = new StringBuilder();
              String fromStr = "From: incaRNAtion + RNAfbinv" + (jobInformation.getVersion() == 2 ? "2.0" : "");
        message.append("Hello ");
        message.append(jobInformation.getEmail());
        message.append(",\nThank you for using our RNA design web server application.\n");
        message.append("The sequence design started, the Job ID is ");
        message.append(jobInformation.getJobId());
        message.append("\nYour results will be available in the following link:\n");
        message.append(SITE_ADDRESS);
        message.append("/GetResults.jsp?jid=");
        message.append(jobInformation.getJobId());
        message.append("\n\nResults will be removed after a week.\n\nThis is an automatic e-mail, " +
                "replays are ignored.\nHave a good day!\n");
        message.append("Have a good day!\nBarash lab, Ben Gurion University");
        return sendMail(message.toString(), "incaRNAfbinv" + (jobInformation.getVersion() == 2 ? "2.0" : "") +
                " confirmed submission, Query name: " + jobInformation.getQueryName(), jobInformation.getEmail());
    }
}
