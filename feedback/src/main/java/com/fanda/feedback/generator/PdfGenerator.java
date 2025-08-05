package com.fanda.feedback.generator;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PdfGenerator {

    public static void saveTextAsPdf(String text, String filePath) throws IOException{
        try (PDDocument document = new PDDocument()){
            float margin = 50;
            float leading = 14.5f;
            float fontSize = 12f;

            PDPage page = new PDPage(PDRectangle.A4);
            PDRectangle mediaBox = page.getMediaBox();
            float width = mediaBox.getWidth()-2 * margin;

            document.addPage(page);

            InputStream fontStream = PdfGenerator.class.getClassLoader().getResourceAsStream("fonts/NotoSansKR-Medium.ttf");
            if (fontStream == null) {
                throw new RuntimeException("폰트 파일을 찾을 수 없습니다. 리소스 경로를 확인하세요.");
            }
            PDType0Font font = PDType0Font.load(document, fontStream);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(font, fontSize);
            contentStream.setLeading(leading);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, mediaBox.getHeight()-margin);

            float yPosition = mediaBox.getHeight()-margin;

            for(String paragraph : text.split("\n")){
                for(String line : wrapText(paragraph, font, 12, width)){
                    if(yPosition <= margin + leading){
                        contentStream.endText();
                        contentStream.close();

                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.setFont(font, fontSize);
                        contentStream.setLeading(leading);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, mediaBox.getHeight() - margin);
                        yPosition = mediaBox.getHeight() - margin;
                    }

                    contentStream.showText(line);
                    contentStream.newLine();
                    yPosition -= leading;
                }
            }

            contentStream.endText();
            contentStream.close();
            document.save(new File(filePath));
        }
        catch (IOException e){
            throw new RuntimeException("PDF 생성 오류");
        }
    }

    // 텍스트 자동 줄바꿈
    private static List<String> wrapText(String text, PDType0Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();

        for (String word : text.split(" ")) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            float size = font.getStringWidth(testLine) / 1000 * fontSize;

            if (size > maxWidth) {
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            } else {
                line = new StringBuilder(testLine);
            }
        }

        if (line.length() > 0) {
            lines.add(line.toString());
        }

        return lines;
    }
}
