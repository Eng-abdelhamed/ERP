package services;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import models.Attendance;
import models.Payroll;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportService {

    public void generatePdfReport(File destFile, String reportTitle, WritableImage chartImage,
                                  List<Attendance> attendance, List<Payroll> payrolls) throws IOException {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("ERP System Report");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
                contentStream.newLineAtOffset(50, 725);
                contentStream.showText(reportTitle);
                contentStream.endText();

                String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 12);
                contentStream.newLineAtOffset(50, 705);
                contentStream.showText("Generated on: " + dateStr);
                contentStream.endText();

                contentStream.setLineWidth(1f);
                contentStream.moveTo(50, 690);
                contentStream.lineTo(545, 690);
                contentStream.stroke();

                float currentY = 670;

               
                if (chartImage != null) {
                    BufferedImage bImage = SwingFXUtils.fromFXImage(chartImage, null);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bImage, "png", baos);
                    byte[] imageBytes = baos.toByteArray();

                    PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageBytes, "chart");
                    
                
                    float maxWidth = 495;
                    float scale = maxWidth / pdImage.getWidth();
                    float newWidth = pdImage.getWidth() * scale;
                    float newHeight = pdImage.getHeight() * scale;

                    if (newHeight > 300) {
                        scale = 300f / pdImage.getHeight();
                        newWidth = pdImage.getWidth() * scale;
                        newHeight = pdImage.getHeight() * scale;
                    }
                    float startX = (PDRectangle.A4.getWidth() - newWidth) / 2;
                    currentY -= newHeight;
                    
                    contentStream.drawImage(pdImage, startX, currentY, newWidth, newHeight);
                    currentY -= 30; 
                }
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(50, currentY);
                contentStream.showText("Summary Statistics");
                contentStream.endText();
                currentY -= 20;
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, currentY);

                if (attendance != null && !attendance.isEmpty()) {
                    long present = attendance.stream().filter(a -> "Present".equals(a.getStatus())).count();
                    long absent = attendance.stream().filter(a -> "Absent".equals(a.getStatus())).count();
                    contentStream.showText("Total Attendance Logs: " + attendance.size());
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Total Present: " + present);
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Total Absent: " + absent);
                } else if (payrolls != null && !payrolls.isEmpty()) {
                    double totalGross = payrolls.stream().mapToDouble(p -> p.getBaseSalary() + p.getBonus()).sum();
                    double totalNet = payrolls.stream().mapToDouble(Payroll::getNetSalary).sum();
                    contentStream.showText(String.format("Total Payroll Records: %d", payrolls.size()));
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText(String.format("Total Gross Salary: $%,.2f", totalGross));
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText(String.format("Total Net Salary: $%,.2f", totalNet));
                } else {
                    contentStream.showText("No detailed records found for this period.");
                }
                
                contentStream.endText();
            }

            document.save(destFile);
        }
    }
}
