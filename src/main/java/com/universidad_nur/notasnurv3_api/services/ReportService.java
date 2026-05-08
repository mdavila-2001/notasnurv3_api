package com.universidad_nur.notasnurv3_api.services;

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
import com.universidad_nur.notasnurv3_api.entities.AttendanceStatus;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.AttendanceRepository;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    public byte[] generateActaNotasPdf(Integer subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada."));

        List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subjectId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Acta Oficial de Notas", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Materia: " + subject.getCode() + " - " + subject.getName()));
            document.add(new Paragraph("Docente: " + (subject.getTeacher() != null ? subject.getTeacher().getFullName() : "N/A")));
            document.add(new Paragraph("Gestión: " + subject.getSemester().getManagement().getYear() + " - Semestre " + subject.getSemester().getNumber()));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 4f, 2f, 2.5f});

            addTableHeader(table, "CI", "Estudiante", "Nota Final", "Estado");

            for (Enrollment e : enrollments) {
                table.addCell(e.getAcademicRecord().getUser().getCi());
                table.addCell(e.getAcademicRecord().getUser().getFullName());
                table.addCell(e.getFinalScore() != null ? String.valueOf(e.getFinalScore()) : "N/A");
                table.addCell(e.getStatus().name());
            }

            document.add(table);

            document.add(new Paragraph("\n\n"));
            Paragraph signature = new Paragraph("___________________________\nFirma del Docente");
            signature.setAlignment(Element.ALIGN_RIGHT);
            document.add(signature);

            document.close();
            return baos.toByteArray();
        } catch (Exception _) {
            throw new InvalidOperationException("Error al generar PDF de Acta de Notas");
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateAsistenciaExcel(Integer subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Materia no encontrada.");
        }

        List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subjectId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Asistencias");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"CI", "Estudiante", "Faltas", "Estado"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle style = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowNum = 1;
            for (Enrollment e : enrollments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(e.getAcademicRecord().getUser().getCi());
                row.createCell(1).setCellValue(e.getAcademicRecord().getUser().getFullName());

                long absences = attendanceRepository.countByEnrollmentIdAndStatus(e.getId(), AttendanceStatus.ABSENT);
                row.createCell(2).setCellValue(absences);
                row.createCell(3).setCellValue(e.getStatus().name());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception _) {
            throw new InvalidOperationException("Error al generar Excel de Asistencias");
        }
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setPhrase(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }
}
