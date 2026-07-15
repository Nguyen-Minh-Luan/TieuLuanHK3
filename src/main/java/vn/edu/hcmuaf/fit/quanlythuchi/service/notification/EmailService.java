package vn.edu.hcmuaf.fit.quanlythuchi.service.notification;

public interface EmailService {
    void send(String to, String subject, String htmlBody);
}
