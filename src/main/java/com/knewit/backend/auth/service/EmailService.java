package com.knewit.backend.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String verificationLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        message.setSubject("Verify Your Account");

        String html = """
            <html>
            <body>
                <h2>Welcome!</h2>

                <p>Please verify your email by clicking the button below.</p>

                <a href="%s"
                   style="
                       background-color:#4CAF50;
                       color:white;
                       padding:12px 24px;
                       text-decoration:none;
                       border-radius:5px;
                       display:inline-block;
                   ">
                    Verify Email
                </a>

                <p>If the button doesn't work, use this link:</p>
                <p>%s</p>
            </body>
            </html>
            """.formatted(verificationLink, verificationLink);

        helper.setText(html, true);

        mailSender.send(message);
    }
}
