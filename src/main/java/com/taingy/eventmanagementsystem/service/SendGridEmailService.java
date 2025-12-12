package com.taingy.eventmanagementsystem.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.model.Registration;
import com.taingy.eventmanagementsystem.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * SendGrid Web API email service - works on cloud platforms that block SMTP ports
 * Uses HTTP API instead of SMTP, bypassing port 25/465/587 restrictions
 */
@Service
public class SendGridEmailService {

    private static final Logger logger = LoggerFactory.getLogger(SendGridEmailService.class);

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email:noreply@eventmanagement.com}")
    private String fromEmail;

    @Value("${sendgrid.from.name:Event Management System}")
    private String fromName;

    @Value("${app.name:Event Management System}")
    private String appName;

    @Async
    public void sendRegistrationConfirmation(Registration registration) {
        try {
            User user = registration.getUser();
            Event event = registration.getEvent();

            Email from = new Email(fromEmail, fromName);
            Email to = new Email(user.getEmail(), user.getFirstName() != null ? user.getFirstName() : user.getUsername());
            String subject = "Registration Confirmation - " + event.getTitle();

            String htmlContent = buildRegistrationEmailTemplate(user, event, registration);
            Content content = new Content("text/html", htmlContent);

            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("Registration confirmation email sent via SendGrid to {} for event {}",
                    user.getEmail(), event.getTitle());
            } else {
                logger.error("Failed to send email via SendGrid. Status: {}, Body: {}",
                    response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            logger.error("Failed to send registration confirmation email via SendGrid", e);
        }
    }

    @Async
    public void sendRegistrationCancellation(Registration registration) {
        try {
            User user = registration.getUser();
            Event event = registration.getEvent();

            Email from = new Email(fromEmail, fromName);
            Email to = new Email(user.getEmail(), user.getFirstName() != null ? user.getFirstName() : user.getUsername());
            String subject = "Registration Cancelled - " + event.getTitle();

            String htmlContent = buildCancellationEmailTemplate(user, event);
            Content content = new Content("text/html", htmlContent);

            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("Registration cancellation email sent via SendGrid to {} for event {}",
                    user.getEmail(), event.getTitle());
            } else {
                logger.error("Failed to send email via SendGrid. Status: {}, Body: {}",
                    response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            logger.error("Failed to send registration cancellation email via SendGrid", e);
        }
    }

    @Async
    public void sendOtpEmail(String email, String otpCode, String firstName) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(email, firstName != null && !firstName.isEmpty() ? firstName : "User");
            String subject = "Email Verification - " + appName;

            String htmlContent = buildOtpEmailTemplate(email, otpCode, firstName);
            Content content = new Content("text/html", htmlContent);

            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("OTP email sent via SendGrid to {}", email);
            } else {
                logger.error("Failed to send OTP email via SendGrid. Status: {}, Body: {}",
                    response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            logger.error("Failed to send OTP email via SendGrid", e);
        }
    }

    @Async
    public void sendPasswordResetOtpEmail(String email, String otpCode, String firstName) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(email, firstName != null && !firstName.isEmpty() ? firstName : "User");
            String subject = "Password Reset - " + appName;

            String htmlContent = buildPasswordResetOtpEmailTemplate(otpCode, firstName);
            Content content = new Content("text/html", htmlContent);

            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("Password reset OTP email sent via SendGrid to {}", email);
            } else {
                logger.error("Failed to send password reset OTP email via SendGrid. Status: {}, Body: {}",
                    response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            logger.error("Failed to send password reset OTP email via SendGrid", e);
        }
    }

    private String buildRegistrationEmailTemplate(User user, Event event, Registration registration) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        String eventDate = event.getEventDate() != null ? event.getEventDate().format(dateFormatter) : "TBA";
        String startTime = event.getStartTime() != null ? event.getStartTime().format(timeFormatter) : "TBA";
        String endTime = event.getEndTime() != null ? event.getEndTime().format(timeFormatter) : "TBA";
        String price = event.getPrice() != null ? String.format("$%.2f", event.getPrice()) : "Free";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                        .event-details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }
                        .detail-row { margin: 10px 0; }
                        .label { font-weight: bold; color: #555; }
                        .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Registration Confirmed!</h1>
                        </div>
                        <div class="content">
                            <p>Dear %s,</p>
                            <p>Thank you for registering for the following event:</p>

                            <div class="event-details">
                                <h2 style="margin-top: 0; color: #4CAF50;">%s</h2>
                                <div class="detail-row">
                                    <span class="label">Date:</span> %s
                                </div>
                                <div class="detail-row">
                                    <span class="label">Time:</span> %s - %s
                                </div>
                                <div class="detail-row">
                                    <span class="label">Location:</span> %s
                                </div>
                                <div class="detail-row">
                                    <span class="label">Price:</span> %s
                                </div>
                                %s
                            </div>

                            <p>We look forward to seeing you at the event!</p>
                            <p>If you need to cancel your registration, please contact the event organizer.</p>
                        </div>
                        <div class="footer">
                            <p>This email was sent by %s</p>
                            <p>Please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                user.getFirstName() != null ? user.getFirstName() : user.getUsername(),
                event.getTitle(),
                eventDate,
                startTime,
                endTime,
                event.getLocation() != null ? event.getLocation() : "TBA",
                price,
                registration.getNote() != null && !registration.getNote().isEmpty()
                    ? "<div class=\"detail-row\"><span class=\"label\">Note:</span> " + registration.getNote() + "</div>"
                    : "",
                appName
        );
    }

    private String buildCancellationEmailTemplate(User user, Event event) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

        String eventDate = event.getEventDate() != null ? event.getEventDate().format(dateFormatter) : "TBA";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #f44336; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                        .event-details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #f44336; }
                        .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Registration Cancelled</h1>
                        </div>
                        <div class="content">
                            <p>Dear %s,</p>
                            <p>Your registration for the following event has been cancelled:</p>

                            <div class="event-details">
                                <h2 style="margin-top: 0; color: #f44336;">%s</h2>
                                <p><strong>Date:</strong> %s</p>
                            </div>

                            <p>If this cancellation was made in error, please contact the event organizer.</p>
                        </div>
                        <div class="footer">
                            <p>This email was sent by %s</p>
                            <p>Please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                user.getFirstName() != null ? user.getFirstName() : user.getUsername(),
                event.getTitle(),
                eventDate,
                appName
        );
    }

    private String buildOtpEmailTemplate(String email, String otpCode, String firstName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                        .otp-box {
                            background-color: white;
                            padding: 20px;
                            margin: 20px 0;
                            border: 2px solid #4CAF50;
                            border-radius: 8px;
                            text-align: center;
                        }
                        .otp-code {
                            font-size: 32px;
                            font-weight: bold;
                            color: #4CAF50;
                            letter-spacing: 8px;
                            font-family: 'Courier New', monospace;
                        }
                        .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Email Verification</h1>
                        </div>
                        <div class="content">
                            <p>Dear %s,</p>
                            <p>Thank you for registering with %s. To complete your registration, please verify your email address using the OTP code below:</p>

                            <div class="otp-box">
                                <p style="margin: 0; font-size: 14px; color: #666;">Your verification code is:</p>
                                <div class="otp-code">%s</div>
                                <p style="margin: 10px 0 0 0; font-size: 12px; color: #999;">This code will expire in 10 minutes</p>
                            </div>

                            <p>Enter this code in the verification page to activate your account.</p>
                        </div>
                        <div class="footer">
                            <p>This email was sent by %s</p>
                            <p>Please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                firstName != null && !firstName.isEmpty() ? firstName : "User",
                appName,
                otpCode,
                appName
        );
    }

    private String buildPasswordResetOtpEmailTemplate(String otpCode, String firstName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #FF9800; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                        .otp-box {
                            background-color: white;
                            padding: 20px;
                            margin: 20px 0;
                            border: 2px solid #FF9800;
                            border-radius: 8px;
                            text-align: center;
                        }
                        .otp-code {
                            font-size: 32px;
                            font-weight: bold;
                            color: #FF9800;
                            letter-spacing: 8px;
                            font-family: 'Courier New', monospace;
                        }
                        .warning {
                            color: #f44336;
                            font-size: 14px;
                            margin-top: 20px;
                            padding: 15px;
                            background-color: #ffebee;
                            border-left: 4px solid #f44336;
                        }
                        .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Password Reset Request</h1>
                        </div>
                        <div class="content">
                            <p>Dear %s,</p>
                            <p>We received a request to reset your password for your %s account. Use the OTP code below to reset your password:</p>

                            <div class="otp-box">
                                <p style="margin: 0; font-size: 14px; color: #666;">Your password reset code is:</p>
                                <div class="otp-code">%s</div>
                                <p style="margin: 10px 0 0 0; font-size: 12px; color: #999;">This code will expire in 10 minutes</p>
                            </div>

                            <p>Enter this code along with your new password to complete the password reset process.</p>

                            <div class="warning">
                                <strong>Security Warning:</strong> If you did not request a password reset, please ignore this email and ensure your account is secure. Your password will not be changed unless you complete the reset process with this code.
                            </div>
                        </div>
                        <div class="footer">
                            <p>This email was sent by %s</p>
                            <p>Please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                firstName != null && !firstName.isEmpty() ? firstName : "User",
                appName,
                otpCode,
                appName
        );
    }
}
