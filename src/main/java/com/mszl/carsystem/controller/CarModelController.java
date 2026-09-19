package com.mszl.carsystem.controller;

import com.mszl.carsystem.dao.mapper.CarModelMapper;
import com.mszl.carsystem.dao.pojo.CarModel;
import com.mszl.carsystem.service.CarModelService;
import com.mszl.carsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@RestController
@RequestMapping("/api")
public class CarModelController {

    @Autowired
    private CarModelService carModelService;

    @Autowired
    private CarModelMapper carModelMapper;
    @GetMapping("/car-models")
    public Result listModels() {
        return carModelService.list();
    }

    @GetMapping("/car-models/{id}")
    public Result modelDetail(@PathVariable Integer id) {
        return carModelService.getById(id);
    }

    @PostMapping("/car-models")
    public Result create(@RequestBody Object body) {
        return carModelService.create(body);
    }

    @PutMapping("/car-models/{id}")
    public Result update(@PathVariable Integer id, @RequestBody Object body) {
        return carModelService.update(id, body);
    }

    @DeleteMapping("/car-models/{id}")
    public Result delete(@PathVariable Integer id) {
        return carModelService.delete(id);
    }

    /** 批量导入车型：上传 CSV/Excel（第一张 Sheet 或 CSV 表格） */
    @PostMapping("/car-models/import")
    public Result importExcel(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail(400, "请选择要导入的文件（.csv/.xlsx/.xls）");
        }

        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".csv")) {
            return importCsv(file);
        }

        int success = 0;
        int failed = 0;
        java.util.List<java.util.Map<String, Object>> errors = new java.util.ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) {
                return Result.fail(400, "Excel 文件无有效 Sheet");
            }

            DataFormatter fmt = new DataFormatter();
            int last = sheet.getLastRowNum();
            for (int r = 0; r <= last; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                // 跳过表头：如果第一列是“车型名称/ModelName”等
                if (r == 0) {
                    String c0 = fmt.formatCellValue(row.getCell(0)).trim();
                    if (c0.contains("车型") || c0.toLowerCase().contains("model")) {
                        continue;
                    }
                }

                String modelName = fmt.formatCellValue(row.getCell(0)).trim();   // A 车型名称
                String brand = fmt.formatCellValue(row.getCell(1)).trim();       // B 品牌
                String guidePriceRaw = fmt.formatCellValue(row.getCell(2)).trim(); // C 指导价
                String yearRaw = fmt.formatCellValue(row.getCell(3)).trim();     // D 生产年份
                String powerType = fmt.formatCellValue(row.getCell(4)).trim();   // E 动力类型
                String bodyType = fmt.formatCellValue(row.getCell(5)).trim();    // F 车身类型
                String carImage = fmt.formatCellValue(row.getCell(6)).trim();    // G 图片路径

                // 空行跳过
                if (modelName.isEmpty() && brand.isEmpty() && guidePriceRaw.isEmpty()
                        && yearRaw.isEmpty() && powerType.isEmpty() && bodyType.isEmpty() && carImage.isEmpty()) {
                    continue;
                }
                if (modelName.isEmpty() || brand.isEmpty()) {
                    failed++;
                    errors.add(java.util.Map.of("row", r + 1, "message", "车型名称/品牌不能为空"));
                    continue;
                }

                java.math.BigDecimal guidePrice = null;
                if (!guidePriceRaw.isEmpty()) {
                    try {
                        guidePrice = new java.math.BigDecimal(guidePriceRaw.replace(",", ""));
                    } catch (Exception e) {
                        // ignore, keep null
                    }
                }
                Integer year = null;
                if (!yearRaw.isEmpty()) {
                    try {
                        year = Integer.parseInt(yearRaw.replaceAll("\\.0$", ""));
                    } catch (Exception e) {
                        // ignore
                    }
                }

                try {
                    CarModel m = new CarModel();
                    m.setModelName(modelName);
                    m.setBrand(brand);
                    m.setGuidePrice(guidePrice);
                    m.setProductionYear(year);
                    m.setPowerType(powerType.isEmpty() ? null : powerType);
                    m.setBodyType(bodyType.isEmpty() ? null : bodyType);
                    m.setCarImage(carImage.isEmpty() ? null : carImage);
                    carModelMapper.insert(m);
                    success++;
                } catch (Exception e) {
                    failed++;
                    errors.add(java.util.Map.of("row", r + 1, "message", e.getMessage() == null ? "写入失败" : e.getMessage()));
                }
            }
        } catch (Exception e) {
            return Result.fail(500, "导入失败：" + (e.getMessage() == null ? "解析 Excel 出错" : e.getMessage()));
        }

        return Result.success(java.util.Map.of(
                "successCount", success,
                "failedCount", failed,
                "errors", errors
        ));
    }

    private Result importCsv(MultipartFile file) {
        int success = 0;
        int failed = 0;
        java.util.List<java.util.Map<String, Object>> errors = new java.util.ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setSkipHeaderRecord(false)
                     .setTrim(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {

            int rowNum = 0;
            for (CSVRecord record : parser) {
                rowNum++;
                if (record == null) continue;

                // 跳过表头
                if (rowNum == 1) {
                    String c0 = record.size() > 0 ? record.get(0).trim() : "";
                    if (c0.contains("车型") || c0.toLowerCase(Locale.ROOT).contains("model")) {
                        continue;
                    }
                }

                String modelName = getCsvCell(record, 0);
                String brand = getCsvCell(record, 1);
                String guidePriceRaw = getCsvCell(record, 2);
                String yearRaw = getCsvCell(record, 3);
                String powerType = getCsvCell(record, 4);
                String bodyType = getCsvCell(record, 5);
                String carImage = getCsvCell(record, 6);

                boolean allEmpty = modelName.isEmpty() && brand.isEmpty() && guidePriceRaw.isEmpty()
                        && yearRaw.isEmpty() && powerType.isEmpty() && bodyType.isEmpty() && carImage.isEmpty();
                if (allEmpty) continue;

                if (modelName.isEmpty() || brand.isEmpty()) {
                    failed++;
                    errors.add(java.util.Map.of("row", rowNum, "message", "车型名称/品牌不能为空"));
                    continue;
                }

                java.math.BigDecimal guidePrice = null;
                if (!guidePriceRaw.isEmpty()) {
                    try {
                        guidePrice = new java.math.BigDecimal(guidePriceRaw.replace(",", ""));
                    } catch (Exception ignored) {
                    }
                }
                Integer year = null;
                if (!yearRaw.isEmpty()) {
                    try {
                        year = Integer.parseInt(yearRaw.replaceAll("\\.0$", ""));
                    } catch (Exception ignored) {
                    }
                }

                try {
                    CarModel m = new CarModel();
                    m.setModelName(modelName);
                    m.setBrand(brand);
                    m.setGuidePrice(guidePrice);
                    m.setProductionYear(year);
                    m.setPowerType(powerType.isEmpty() ? null : powerType);
                    m.setBodyType(bodyType.isEmpty() ? null : bodyType);
                    m.setCarImage(carImage.isEmpty() ? null : carImage);
                    carModelMapper.insert(m);
                    success++;
                } catch (Exception e) {
                    failed++;
                    errors.add(java.util.Map.of("row", rowNum, "message", e.getMessage() == null ? "写入失败" : e.getMessage()));
                }
            }
        } catch (Exception e) {
            return Result.fail(500, "导入失败：" + (e.getMessage() == null ? "解析 CSV 出错" : e.getMessage()));
        }

        return Result.success(java.util.Map.of(
                "successCount", success,
                "failedCount", failed,
                "errors", errors
        ));
    }

    private static String getCsvCell(CSVRecord record, int idx) {
        if (record == null || idx < 0 || idx >= record.size()) return "";
        String s = record.get(idx);
        if (s == null) return "";
        return s.trim();
    }
}