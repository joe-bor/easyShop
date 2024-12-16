package org.yearup.services;

import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.yearup.models.ShoppingCartItem;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.Collection;

@Service
@AllArgsConstructor
public class EmailService {

    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String body) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
        try {
            mimeMessageHelper.setFrom("jborlongan@appdev.yearup.org");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        javaMailSender.send(mimeMessage);
        System.err.println("Email Sent");
    }

    public String createBody(Collection<ShoppingCartItem> shoppingCartItemList) {
        StringBuilder stringBuilder = new StringBuilder();
        BigDecimal total = BigDecimal.ZERO;

        stringBuilder.append("<html>");
        stringBuilder.append("<body>");
        stringBuilder.append("<h2>Order Summary</h2>");

        // Start the table
        stringBuilder.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
        stringBuilder.append("<thead>");
        stringBuilder.append("<tr>");
        stringBuilder.append("<th style='text-align: left;'>Product Name</th>");
        stringBuilder.append("<th style='text-align: right;'>Quantity</th>");
        stringBuilder.append("<th style='text-align: right;'>Unit Price</th>");
        stringBuilder.append("<th style='text-align: right;'>Discount (%)</th>");
        stringBuilder.append("<th style='text-align: right;'>Line Total</th>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</thead>");
        stringBuilder.append("<tbody>");

        // add 1 row per item
        for (ShoppingCartItem shoppingCartItem : shoppingCartItemList) {
            BigDecimal lineTotal = shoppingCartItem.getLineTotal();
            total = total.add(lineTotal);

            stringBuilder.append("<tr>");
            stringBuilder.append(String.format(
                    "<td style='text-align: left;'>%s</td>" +
                            "<td style='text-align: right;'>%d</td>" +
                            "<td style='text-align: right;'>%.2f</td>" +
                            "<td style='text-align: right;'>%.2f</td>" +
                            "<td style='text-align: right;'>%.2f</td>",
                    shoppingCartItem.getProduct().getName(),
                    shoppingCartItem.getQuantity(),
                    shoppingCartItem.getProduct().getPrice(),
                    shoppingCartItem.getDiscountPercent().multiply(BigDecimal.valueOf(100)),
                    shoppingCartItem.getLineTotal()
            ));
            stringBuilder.append("</tr>");
        }

        // row for the total
        stringBuilder.append("<tr style='font-weight: bold;'>");
        stringBuilder.append("<td colspan='4' style='text-align: right;'>Total</td>");
        stringBuilder.append(String.format("<td style='text-align: right;'>%.2f</td>", total));
        stringBuilder.append("</tr>");

        // closing tags
        stringBuilder.append("</tbody>");
        stringBuilder.append("</table>");
        stringBuilder.append("</body>");
        stringBuilder.append("</html>");

        return stringBuilder.toString();
    }
}
