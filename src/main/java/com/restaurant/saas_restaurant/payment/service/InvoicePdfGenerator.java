package com.restaurant.saas_restaurant.payment.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.restaurant.saas_restaurant.order.entity.CustomerOrder;
import com.restaurant.saas_restaurant.order.entity.OrderItem;
import com.restaurant.saas_restaurant.order.entity.OrderItemSupplement;
import com.restaurant.saas_restaurant.payment.entity.Payment;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.table.entity.RestaurantTable;

@Component
public class InvoicePdfGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // prices are tax-inclusive; the invoice shows the VAT contained in the total
    @Value("${app.invoice.vat-rate:4}")
    private BigDecimal vatRate;

    @Value("${app.invoice.currency:MAD}")
    private String currency;

    private final Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
    private final Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private final Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private final Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);

    /** Must be called inside a transaction (the order's lines are lazy-loaded). */
    public byte[] generate(Payment payment) {
        CustomerOrder order = payment.getOrder();
        Restaurant restaurant = order.getRestaurant();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, out);
        document.open();

        // --- header ---
        document.add(new Paragraph(restaurant.getName(), titleFont));
        document.add(new Paragraph(restaurant.getAddress(), normalFont));
        document.add(new Paragraph("Tel: " + restaurant.getPhone() + "  |  " + restaurant.getEmail(), normalFont));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("INVOICE " + payment.getInvoiceNumber(), boldFont));
        document.add(new Paragraph("Date: " + payment.getCreated_at().format(DATE_FORMAT), normalFont));
        document.add(new Paragraph("Order #" + order.getId(), normalFont));
        RestaurantTable table = order.getTable();
        String where = table != null
            ? "Zone: " + table.getZone().getName() + "  |  Table: " + table.getTableNumber()
            : "Takeaway";
        document.add(new Paragraph(where, normalFont));
        document.add(new Paragraph(" "));

        // --- lines ---
        PdfPTable lines = new PdfPTable(new float[] { 5f, 2f, 1f, 2f });
        lines.setWidthPercentage(100);
        lines.addCell(headerCell("Item", Element.ALIGN_LEFT));
        lines.addCell(headerCell("Unit price", Element.ALIGN_RIGHT));
        lines.addCell(headerCell("Qty", Element.ALIGN_CENTER));
        lines.addCell(headerCell("Total", Element.ALIGN_RIGHT));

        for (OrderItem item : order.getItems()) {
            String name = item.getProductName()
                + (item.getVariantName() != null ? " (" + item.getVariantName() + ")" : "");
            Phrase description = new Phrase(name, normalFont);
            for (OrderItemSupplement supplement : item.getSupplements()) {
                description.add(new Phrase("\n  + " + supplement.getName() + " (" + money(supplement.getPrice()) + ")", smallFont));
            }
            PdfPCell descCell = new PdfPCell(description);
            descCell.setPadding(5);
            lines.addCell(descCell);
            lines.addCell(cell(money(item.getUnitPrice()), Element.ALIGN_RIGHT));
            lines.addCell(cell(String.valueOf(item.getQuantity()), Element.ALIGN_CENTER));
            lines.addCell(cell(money(item.getLineTotal()), Element.ALIGN_RIGHT));
        }
        document.add(lines);
        document.add(new Paragraph(" "));

        // --- totals ---
        BigDecimal total = order.getTotalAmount();
        BigDecimal divisor = BigDecimal.ONE.add(vatRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        BigDecimal excludingVat = total.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal vat = total.subtract(excludingVat);

        PdfPTable totals = new PdfPTable(new float[] { 7f, 3f });
        totals.setWidthPercentage(45);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.addCell(cell("Total excl. VAT", Element.ALIGN_LEFT));
        totals.addCell(cell(money(excludingVat), Element.ALIGN_RIGHT));
        totals.addCell(cell("VAT (" + vatRate.stripTrailingZeros().toPlainString() + "%)", Element.ALIGN_LEFT));
        totals.addCell(cell(money(vat), Element.ALIGN_RIGHT));
        totals.addCell(boldCell("TOTAL", Element.ALIGN_LEFT));
        totals.addCell(boldCell(money(total), Element.ALIGN_RIGHT));
        document.add(totals);
        document.add(new Paragraph(" "));

        // --- payment ---
        String paymentLine = "Paid by " + payment.getMethod()
            + (payment.getReference() != null ? " (ref: " + payment.getReference() + ")" : "");
        document.add(new Paragraph(paymentLine, normalFont));
        document.add(new Paragraph("Thank you for your visit!", smallFont));

        document.close();
        return out.toByteArray();
    }

    private String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " " + currency;
    }

    private PdfPCell headerCell(String text, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, boldFont));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(align);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell cell(String text, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, normalFont));
        cell.setHorizontalAlignment(align);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell boldCell(String text, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, boldFont));
        cell.setHorizontalAlignment(align);
        cell.setPadding(5);
        return cell;
    }
}