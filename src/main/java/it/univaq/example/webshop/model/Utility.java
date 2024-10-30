package it.univaq.example.webshop.model;

import java.time.LocalDateTime;
import java.util.Properties;

import it.univaq.example.webshop.business.NotificationResourceDB;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.ServletContext;

public class Utility {
    public static void sendNotification(User user, String message, NotificationTypeEnum type, String link) {
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setCreationDate(LocalDateTime.now());
        notification.setMessage(message);
        notification.setType(type);
        notification.setLink(link);
        NotificationResourceDB.setNotification(notification);
    }

    public static void sendMail(ServletContext sc, String email, String text) {
        if(Boolean.parseBoolean(sc.getInitParameter("sendEmail"))) {
            String sender = sc.getInitParameter("emailSender");
            String securityCode = sc.getInitParameter("securityCode");
            String to = email;
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");

            Session session = Session.getDefaultInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication(){
                    return new PasswordAuthentication(sender, securityCode);
                }
            });
            //compose message
            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(email));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                message.setSubject("WebMarket");
                message.setText(text);

                Transport.send(message);
                System.out.println("Message sent successfully");
            } catch (MessagingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
}
