package com.greengrocer.utils;

import com.greengrocer.models.Order;
import com.greengrocer.models.OrderItem;
import com.greengrocer.models.User;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for generating PDF invoices.
 */
public class InvoiceGenerator {

    // Primary Green Color (#77B43F is a nice fresh green, similar to the example)
    private static final DeviceRgb PRIMARY_GREEN = new DeviceRgb(119, 180, 63);
    private static final DeviceRgb LIGHT_GRAY_BG = new DeviceRgb(240, 240, 240);

    public static byte[] generateInvoice(Order order, User customer) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            // --- Header Section ---
            // Logo (Placeholder text for now) and Service Provider Info
            Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }));
            headerTable.setWidth(UnitValue.createPercentValue(100));

            // Left: Logo
            Cell logoCell = new Cell().add(new Paragraph("GreenGrocer")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(PRIMARY_GREEN))
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            headerTable.addCell(logoCell);

            // Right: Service Provider Info (Empty cell for spacing if needed, or actual
            // info)
            // For this design, the provider info is below the logo.
            headerTable.addCell(new Cell().setBorder(Border.NO_BORDER));

            document.add(headerTable);

            // Provider Details below Logo
            document.add(new Paragraph("Service Provider")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginTop(5));
            document.add(new Paragraph(
                    "123 Market Street, Istanbul, Turkey\n(555) 123-4567 | hello@greengrocer.com")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(20));

            // --- Recipient and Invoice Details Section ---
            Table detailsTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }));
            detailsTable.setWidth(UnitValue.createPercentValue(100));
            detailsTable.setMarginBottom(20);

            // Left: Recipient Info
            Cell recipientCell = new Cell().setBorder(Border.NO_BORDER);
            recipientCell.add(new Paragraph("RECIPIENT:")
                    .setFontSize(10)
                    .setBold()
                    .setFontColor(ColorConstants.BLACK));
            recipientCell.add(new Paragraph(customer.getFullName())
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY));
            recipientCell.add(new Paragraph(customer.getAddress() + "\n" + customer.getPhone())
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY));
            detailsTable.addCell(recipientCell);

            // Right: Invoice Details Box
            Cell invoiceBoxCell = new Cell().setBorder(Border.NO_BORDER);

            // Inner table for the green box look
            Table invoiceInfoTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }));
            invoiceInfoTable.setWidth(UnitValue.createPercentValue(100));

            // Header Row (Green)
            Cell invoiceHeader = new Cell(1, 2)
                    .add(new Paragraph("Invoice #" + order.getId())
                            .setFontSize(14)
                            .setBold()
                            .setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(PRIMARY_GREEN)
                    .setPadding(10)
                    .setBorder(Border.NO_BORDER);
            invoiceInfoTable.addCell(invoiceHeader);

            // Date Rows (Gray)
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            invoiceInfoTable.addCell(new Cell().add(new Paragraph("Issued"))
                    .setBackgroundColor(LIGHT_GRAY_BG).setPadding(5).setBorder(Border.NO_BORDER)
                    .setFontSize(10));
            invoiceInfoTable.addCell(new Cell().add(new Paragraph(order.getOrderTime().format(fmt)))
                    .setBackgroundColor(LIGHT_GRAY_BG).setPadding(5).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT).setFontSize(10));

            invoiceInfoTable.addCell(new Cell().add(new Paragraph("Due"))
                    .setBackgroundColor(LIGHT_GRAY_BG).setPadding(5).setBorder(Border.NO_BORDER)
                    .setFontSize(10));
            invoiceInfoTable.addCell(new Cell()
                    .add(new Paragraph(order.getRequestedDeliveryTime().format(fmt)))
                    .setBackgroundColor(LIGHT_GRAY_BG).setPadding(5).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT).setFontSize(10));

            // Total Row (Green)
            invoiceInfoTable.addCell(new Cell().add(new Paragraph("Total"))
                    .setBackgroundColor(PRIMARY_GREEN).setPadding(5).setBorder(Border.NO_BORDER)
                    .setFontColor(ColorConstants.WHITE).setBold());
            invoiceInfoTable.addCell(new Cell()
                    .add(new Paragraph(String.format("₺%.2f", order.getTotalCost())))
                    .setBackgroundColor(PRIMARY_GREEN).setPadding(5).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT).setFontColor(ColorConstants.WHITE)
                    .setBold());

            invoiceBoxCell.add(invoiceInfoTable);
            detailsTable.addCell(invoiceBoxCell);

            document.add(detailsTable);

            // --- Line Items Section ---
            document.add(new Paragraph("For Services Rendered")
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginBottom(5));

            Table itemsTable = new Table(UnitValue.createPercentArray(new float[] { 4, 1, 1, 1 }));
            itemsTable.setWidth(UnitValue.createPercentValue(100));

            // Header Row
            itemsTable.addHeaderCell(new Cell()
                    .add(new Paragraph("PRODUCT / SERVICE").setBold()
                            .setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(PRIMARY_GREEN).setBorder(Border.NO_BORDER).setPadding(8));
            itemsTable.addHeaderCell(new Cell()
                    .add(new Paragraph("QTY.").setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(PRIMARY_GREEN).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER).setPadding(8));
            itemsTable.addHeaderCell(new Cell()
                    .add(new Paragraph("UNIT PRICE").setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(PRIMARY_GREEN).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT).setPadding(8));
            itemsTable.addHeaderCell(new Cell()
                    .add(new Paragraph("TOTAL").setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(PRIMARY_GREEN).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT).setPadding(8));

            // Items
            for (OrderItem item : order.getItems()) {
                itemsTable.addCell(new Cell().add(new Paragraph(item.getProductName()))
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                        .setPadding(8));
                itemsTable.addCell(new Cell()
                        .add(new Paragraph(String.format("%.2f", item.getAmount())))
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                        .setTextAlignment(TextAlignment.CENTER).setPadding(8));
                itemsTable.addCell(new Cell()
                        .add(new Paragraph(String.format("%.2f", item.getUnitPrice())))
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                        .setTextAlignment(TextAlignment.RIGHT).setPadding(8));
                itemsTable.addCell(new Cell()
                        .add(new Paragraph(String.format("₺%.2f", item.getTotalPrice())))
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                        .setTextAlignment(TextAlignment.RIGHT).setPadding(8));
            }

            document.add(itemsTable);

            // --- Summary Section ---
            document.add(new Paragraph("\n"));
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }));
            summaryTable.setWidth(UnitValue.createPercentValue(40)); // Smaller width, aligned right
            summaryTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

            // Subtotal
            summaryTable.addCell(new Cell().add(new Paragraph("Subtotal")).setBorder(Border.NO_BORDER)
                    .setFontColor(ColorConstants.DARK_GRAY));
            summaryTable.addCell(new Cell().add(new Paragraph(String.format("₺%.2f", order.getSubtotal())))
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(ColorConstants.DARK_GRAY));

            // Discount (if any)
            if (order.getDiscountAmount() > 0) {
                summaryTable.addCell(new Cell().add(new Paragraph("Discount"))
                        .setBorder(Border.NO_BORDER).setFontColor(ColorConstants.RED));
                summaryTable.addCell(new Cell()
                        .add(new Paragraph(String.format("-₺%.2f", order.getDiscountAmount())))
                        .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                        .setFontColor(ColorConstants.RED));
            }

            // VAT
            summaryTable.addCell(new Cell().add(new Paragraph("Tax Rate (18%)")).setBorder(Border.NO_BORDER)
                    .setFontColor(ColorConstants.DARK_GRAY));
            summaryTable.addCell(new Cell().add(new Paragraph(String.format("₺%.2f", order.getVatAmount())))
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(ColorConstants.DARK_GRAY));

            // Divider
            summaryTable.addCell(new Cell(1, 2).setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1))
                    .setHeight(1).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
                    .setBorderTop(Border.NO_BORDER));

            // Total
            summaryTable.addCell(new Cell().add(new Paragraph("Total").setBold())
                    .setBorder(Border.NO_BORDER).setPaddingTop(5));
            summaryTable.addCell(new Cell()
                    .add(new Paragraph(String.format("₺%.2f", order.getTotalCost())).setBold())
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                    .setPaddingTop(5));

            document.add(summaryTable);

            // --- Footer ---
            document.add(new Paragraph("\n\nThanks for your business!")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.LEFT));

            // Powered by (Optional branding)
            document.add(new Paragraph("POWERED BY GREENGROCER")
                    .setFontSize(8)
                    .setBold()
                    .setFontColor(ColorConstants.LIGHT_GRAY)
                    .setMarginTop(20));

            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
