package com.example.app;

import android.content.Context;
import java.io.File;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailSender {

    public static void sendMailWithAttachment(
            final Context context,
            final String userEmail,
            final String appPassword,
            final String toEmail,
            final String subject,
            final String body,
            final File fileAttachment
    ) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Properties props = new Properties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.port", "587");

                    Session session = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(userEmail, appPassword);
                        }
                    });

                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(userEmail));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
                    message.setSubject(subject);

                    // Cuerpo del mensaje
                    MimeBodyPart messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setText(body);

                    // Adjuntar archivo
                    MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(fileAttachment);
                    attachmentBodyPart.setDataHandler(new DataHandler(source));
                    attachmentBodyPart.setFileName(fileAttachment.getName());

                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(messageBodyPart);
                    multipart.addBodyPart(attachmentBodyPart);

                    message.setContent(multipart);

                    Transport.send(message);

                    // Puedes notificar éxito aquí usando un handler o similar
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> android.widget.Toast.makeText(context, "Correo enviado exitosamente", android.widget.Toast.LENGTH_LONG).show());

                } catch (final MessagingException e) {
                    e.printStackTrace();
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> android.widget.Toast.makeText(context, "Error al enviar correo: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
                    // Puedes notificar error aquí usando un handler o similar
                }
            }
        }).start();
    }
}
