package com.example.attendance.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    public void sendResetEmail(String to, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("ATI Attendance Password Reset");
        message.setText(
                "Click this link to reset your password:\n\n" + link
        );
        mailSender.send(message);
        System.out.println("Email Sent Successfully!");
    }
}