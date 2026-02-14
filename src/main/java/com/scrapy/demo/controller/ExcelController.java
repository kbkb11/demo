package com.scrapy.demo.controller;

import com.scrapy.demo.service.ExcelService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Excel导入导出控制器
 * 提供学生信息和成绩数据的批量导入导出功能
 */
@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    /**
     * 导出学生信息
     * GET /api/excel/export/students
     */
    @GetMapping("/export/students")
    public ResponseEntity<byte[]> exportStudents() throws Exception {
        byte[] data = excelService.exportStudents();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("students.xlsx", StandardCharsets.UTF_8)
                        .build()
        );
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * 导出成绩信息
     * GET /api/excel/export/scores
     */
    @GetMapping("/export/scores")
    public ResponseEntity<byte[]> exportScores() throws Exception {
        byte[] data = excelService.exportScores();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("scores.xlsx", StandardCharsets.UTF_8)
                        .build()
        );
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * 导入学生信息
     * POST /api/excel/import/students
     * 请求体：multipart/form-data，包含 file 字段
     */
    @PostMapping("/import/students")
    public ResponseEntity<Map<String, Object>> importStudents(
            @RequestParam("file") MultipartFile file) throws Exception {
        
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || 
            (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "文件格式错误，请上传Excel文件（.xlsx 或 .xls）"
            ));
        }

        Map<String, Object> result = excelService.importStudents(file);
        return ResponseEntity.ok(result);
    }

    /**
     * 导入成绩信息
     * POST /api/excel/import/scores
     * 请求体：multipart/form-data，包含 file 字段
     */
    @PostMapping("/import/scores")
    public ResponseEntity<Map<String, Object>> importScores(
            @RequestParam("file") MultipartFile file) throws Exception {
        
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || 
            (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "文件格式错误，请上传Excel文件（.xlsx 或 .xls）"
            ));
        }

        Map<String, Object> result = excelService.importScores(file);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取学生导入模板
     * GET /api/excel/template/students
     */
    @GetMapping("/template/students")
    public ResponseEntity<byte[]> getStudentTemplate() throws Exception {
        // 返回一个空的学生导入模板
        byte[] data = excelService.exportStudents();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("student_template.xlsx", StandardCharsets.UTF_8)
                        .build()
        );
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * 获取成绩导入模板
     * GET /api/excel/template/scores
     */
    @GetMapping("/template/scores")
    public ResponseEntity<byte[]> getScoreTemplate() throws Exception {
        // 返回一个空的成绩导入模板
        byte[] data = excelService.exportScores();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("score_template.xlsx", StandardCharsets.UTF_8)
                        .build()
        );
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}
