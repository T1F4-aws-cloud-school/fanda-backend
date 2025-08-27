package com.fanda.banner.generator;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class PdfGenerator {

    // 전각/탭 정규화 (ASCII 표 어긋남 방지)
    private static String normalizeAscii(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s, Normalizer.Form.NFKC);
        t = t.replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\t", "    ")
                .replace("\u3000", " ");
        return t;
    }

    public static void saveTextAsPdf(String text, String filePath) throws IOException {
        String normalized = normalizeAscii(text);

        try (PDDocument document = new PDDocument()) {
            float margin   = 50f;
            float leading  = 14.5f;
            float fontSize = 12f;

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDRectangle mediaBox = page.getMediaBox();
            float printableWidth = mediaBox.getWidth() - 2 * margin;

            // 본문(가변폭 한글) + 표(모노스페이스)
            InputStream bodyStream = PdfGenerator.class.getClassLoader()
                    .getResourceAsStream("fonts/NotoSansKR-Medium.ttf");
            if (bodyStream == null) throw new RuntimeException("본문 폰트(NotoSansKR-Medium.ttf) 누락");
            PDType0Font bodyFont = PDType0Font.load(document, bodyStream, true);

            InputStream monoStream = PdfGenerator.class.getClassLoader()
                    .getResourceAsStream("fonts/D2Coding.ttf");
            if (monoStream == null) throw new RuntimeException("모노스페이스 폰트(D2Coding.ttf) 누락");
            PDType0Font monoFont = PDType0Font.load(document, monoStream, true);

            PDPageContentStream cs = new PDPageContentStream(document, page);
            cs.setFont(bodyFont, fontSize);
            cs.setLeading(leading);
            cs.beginText();
            cs.newLineAtOffset(margin, mediaBox.getHeight() - margin);

            float y = mediaBox.getHeight() - margin;

            for (String rawLine : normalized.split("\n", -1)) {
                String line = rawLine;          // 원본 유지
                String trim = line.trim();

                // 1) 빈 줄 → 한 줄 여백
                if (trim.isEmpty()) {
                    if (needsPageBreak(y, margin, leading)) {
                        cs = newPage(document, cs, bodyFont, fontSize, leading, margin);
                        page = document.getPage(document.getNumberOfPages() - 1);
                        mediaBox = page.getMediaBox();
                        y = mediaBox.getHeight() - margin;
                    }
                    cs.newLine();
                    y -= leading;
                    continue;
                }

                // 2) ASCII 표/구분선 감지 → 모노스페이스로 그대로 출력
                boolean isTableLine =
                        trim.startsWith("|") ||
                                trim.matches("^[+\\-|\\s]+$") ||
                                (trim.contains("|") && trim.contains("-"));

                if (isTableLine) {
                    if (needsPageBreak(y, margin, leading)) {
                        cs = newPage(document, cs, bodyFont, fontSize, leading, margin);
                        page = document.getPage(document.getNumberOfPages() - 1);
                        mediaBox = page.getMediaBox();
                        y = mediaBox.getHeight() - margin;
                    }
                    cs.setFont(monoFont, 11f);          // 표만 고정폭
                    cs.showText(line);
                    cs.newLine();
                    y -= leading;
                    cs.setFont(bodyFont, fontSize);     // 본문 폰트 복원
                    continue;
                }

                // 3) 일반 문장 → 자동 줄바꿈(가변폭)
                for (String wrapped : wrapText(line, bodyFont, fontSize, printableWidth)) {
                    if (needsPageBreak(y, margin, leading)) {
                        cs = newPage(document, cs, bodyFont, fontSize, leading, margin);
                        page = document.getPage(document.getNumberOfPages() - 1);
                        mediaBox = page.getMediaBox();
                        y = mediaBox.getHeight() - margin;
                    }
                    cs.showText(wrapped);
                    cs.newLine();
                    y -= leading;
                }
            }

            cs.endText();
            cs.close();
            document.save(new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException("PDF 생성 오류", e);
        }
    }

    private static boolean needsPageBreak(float y, float margin, float leading) {
        return y <= margin + leading;
    }

    private static PDPageContentStream newPage(
            PDDocument doc, PDPageContentStream prev,
            PDType0Font font, float fontSize, float leading, float margin
    ) throws IOException {
        if (prev != null) {
            prev.endText();
            prev.close();
        }
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        cs.setFont(font, fontSize);
        cs.setLeading(leading);
        cs.beginText();
        cs.newLineAtOffset(margin, page.getMediaBox().getHeight() - margin);
        return cs;
    }

    // 단어 단위 래핑 + 너무 긴 단어는 문자 단위 세분화
    private static List<String> wrapText(String text, PDType0Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();

        String[] words = text.split(" ");
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (stringWidth(font, fontSize, test) <= maxWidth) {
                line.setLength(0);
                line.append(test);
            } else {
                // 현재 줄 확정
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                // 단어 자체가 너무 길면 문자 단위로 쪼갬
                if (stringWidth(font, fontSize, word) > maxWidth) {
                    StringBuilder chunk = new StringBuilder();
                    for (int i = 0; i < word.length(); i++) {
                        char c = word.charAt(i);
                        String candidate = chunk.toString() + c;
                        if (stringWidth(font, fontSize, candidate) > maxWidth) {
                            if (chunk.length() > 0) {
                                lines.add(chunk.toString());
                                chunk.setLength(0);
                            }
                        }
                        chunk.append(c);
                    }
                    if (chunk.length() > 0) lines.add(chunk.toString());
                } else {
                    line.append(word);
                }
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    private static float stringWidth(PDType0Font font, float fontSize, String s) throws IOException {
        return font.getStringWidth(s) / 1000f * fontSize;
    }

//    public static void saveTextAsPdf(String text, String filePath) throws IOException{
//        try (PDDocument document = new PDDocument()){
//            float margin = 50;
//            float leading = 14.5f;
//            float fontSize = 12f;
//
//            PDPage page = new PDPage(PDRectangle.A4);
//            PDRectangle mediaBox = page.getMediaBox();
//            float width = mediaBox.getWidth()-2 * margin;
//
//            document.addPage(page);
//
//            InputStream fontStream = PdfGenerator.class.getClassLoader().getResourceAsStream("fonts/NotoSansKR-Medium.ttf");
//            if (fontStream == null) {
//                throw new RuntimeException("폰트 파일을 찾을 수 없습니다. 리소스 경로를 확인하세요.");
//            }
//            PDType0Font font = PDType0Font.load(document, fontStream);
//
//            PDPageContentStream contentStream = new PDPageContentStream(document, page);
//            contentStream.setFont(font, fontSize);
//            contentStream.setLeading(leading);
//            contentStream.beginText();
//            contentStream.newLineAtOffset(margin, mediaBox.getHeight()-margin);
//
//            float yPosition = mediaBox.getHeight()-margin;
//
//            for(String paragraph : text.split("\n")){
//                for(String line : wrapText(paragraph, font, 12, width)){
//                    if(yPosition <= margin + leading){
//                        contentStream.endText();
//                        contentStream.close();
//
//                        page = new PDPage(PDRectangle.A4);
//                        document.addPage(page);
//                        contentStream = new PDPageContentStream(document, page);
//                        contentStream.setFont(font, fontSize);
//                        contentStream.setLeading(leading);
//                        contentStream.beginText();
//                        contentStream.newLineAtOffset(margin, mediaBox.getHeight() - margin);
//                        yPosition = mediaBox.getHeight() - margin;
//                    }
//
//                    contentStream.showText(line);
//                    contentStream.newLine();
//                    yPosition -= leading;
//                }
//            }
//
//            contentStream.endText();
//            contentStream.close();
//            document.save(new File(filePath));
//        }
//        catch (IOException e){
//            throw new RuntimeException("PDF 생성 오류");
//        }
//    }
//
//    // 텍스트 자동 줄바꿈
//    private static List<String> wrapText(String text, PDType0Font font, float fontSize, float maxWidth) throws IOException {
//        List<String> lines = new ArrayList<>();
//        StringBuilder line = new StringBuilder();
//
//        for (String word : text.split(" ")) {
//            String testLine = line.length() == 0 ? word : line + " " + word;
//            float size = font.getStringWidth(testLine) / 1000 * fontSize;
//
//            if (size > maxWidth) {
//                if (line.length() > 0) {
//                    lines.add(line.toString());
//                    line = new StringBuilder(word);
//                } else {
//                    lines.add(word);
//                }
//            } else {
//                line = new StringBuilder(testLine);
//            }
//        }
//
//        if (line.length() > 0) {
//            lines.add(line.toString());
//        }
//
//        return lines;
//    }
}
