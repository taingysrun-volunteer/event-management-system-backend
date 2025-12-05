# SendGrid Email Setup for Render

## Why SendGrid Web API?

Render's **free tier blocks outbound SMTP ports** (25, 465, 587) to prevent abuse. This means traditional email services using SMTP won't work.

**Solution**: Use SendGrid's HTTP-based Web API instead of SMTP.

---

## Setup Steps

### 1. Create SendGrid Account

1. Go to https://signup.sendgrid.com/
2. Sign up (free tier: 100 emails/day)
3. Verify your email address

### 2. Verify Sender Email

1. In SendGrid dashboard: **Settings** → **Sender Authentication**
2. Click **Verify a Single Sender**
3. Enter your email: `taingysrun.volunteer@rnd4impact.com`
4. Check your email and click the verification link
5. ⚠️ **Important**: You can only send emails FROM verified addresses

### 3. Create API Key

1. Go to **Settings** → **API Keys**
2. Click **Create API Key**
3. Name: `Event Management System - Production`
4. Choose **Restricted Access**
5. Enable **Mail Send** permission only
6. Click **Create & View**
7. **Copy the API key immediately** (you won't see it again!)
   - Format: `SG.xxxxxxxxxxxxxxx.yyyyyyyyyyyyyyy`

### 4. Configure Render Environment Variables

In your Render dashboard:

1. Go to your service
2. Click **Environment** tab
3. Add these variables:

```
SENDGRID_API_KEY=SG.your-api-key-here
SENDGRID_FROM_EMAIL=taingysrun.volunteer@rnd4impact.com
SENDGRID_FROM_NAME=Event Management System
SPRING_PROFILES_ACTIVE=prod
```

### 5. Deploy

```bash
git add .
git commit -m "feat: implement SendGrid Web API for email notifications"
git push
```

Render will automatically redeploy your application.

---

## How It Works

### Production (Render with `prod` profile)
- Uses **SendGrid Web API** (HTTP-based)
- No SMTP ports needed
- Works on Render free tier ✅

### Local Development (with `local` profile)
- Uses **SMTP** (JavaMailSender)
- Falls back to traditional email sending

### Code Implementation

The system automatically chooses the right email service:

```java
// In RegistrationService
private void sendConfirmationEmail(Registration registration) {
    if (sendGridEmailService != null) {  // prod profile
        sendGridEmailService.sendRegistrationConfirmation(registration);
    } else {  // local profile
        emailService.sendRegistrationConfirmation(registration);
    }
}
```

---

## Testing

### 1. Check Logs

After deploying, monitor Render logs for:

```
Registration confirmation email sent via SendGrid to user@example.com for event XYZ
```

### 2. Test Registration

1. Create a new event
2. Register for the event
3. Check the registered user's email
4. You should receive a confirmation email

### 3. If Emails Don't Arrive

**Check these:**

1. ✅ Sender email is verified in SendGrid
2. ✅ API key is correct in Render environment variables
3. ✅ `SPRING_PROFILES_ACTIVE=prod` is set
4. ✅ Check spam folder
5. ✅ Check SendGrid activity: https://app.sendgrid.com/email_activity

---

## Troubleshooting

### Error: "Forbidden"
- Your API key doesn't have Mail Send permission
- Create a new API key with the correct permissions

### Error: "From email not verified"
- You must verify your sender email in SendGrid
- Go to Settings → Sender Authentication

### Emails go to spam
- Add SPF/DKIM records (SendGrid provides these)
- Settings → Sender Authentication → Authenticate Your Domain

### Still not working?
- Check Render logs for detailed error messages
- Verify all environment variables are set correctly
- Make sure you're using the `prod` profile

---

## Cost

- **SendGrid**: Free tier (100 emails/day) - sufficient for most use cases
- **Render**: Free tier works perfectly with SendGrid Web API
- **Total**: $0/month for small applications

If you need more than 100 emails/day, upgrade SendGrid to paid plan ($15/month for 40,000 emails).
