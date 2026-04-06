package com.volunteer.exam.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel高校数据导入工具
 * 支持从教育部官方Excel文件导入全国高校名单
 */
@Component
public class ExcelUniversityImporter {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 从Excel文件导入高校数据
     * @param filePath Excel文件路径
     * @return 导入的高校数量
     */
    public int importFromExcel(String filePath) {
        int count = 0;
        
        try (InputStream inputStream = new FileInputStream(filePath)) {
            Workbook workbook = null;
            
            // 根据文件扩展名选择合适的Workbook
            if (filePath.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else if (filePath.endsWith(".xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                throw new IllegalArgumentException("不支持的文件格式，仅支持.xls和.xlsx");
            }
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // 准备SQL - 只保存Excel的6个原始字段
            String sql = "INSERT IGNORE INTO university (school_name, school_id_code, supervisor, " +
                        "location, school_level, remarks) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            
            List<Object[]> batchData = new ArrayList<>();
            
            // 从第2行开始读取（跳过表头）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    // 读取第一列（序号列）
                    String serialNumber = getCellValue(row.getCell(0));
                    
                    // 只处理第一列是数字序号的行，跳过表头和分类行（如"北京市（92所）"）
                    if (serialNumber == null || serialNumber.trim().isEmpty()) {
                        continue;
                    }
                    
                    // 检查是否为数字序号
                    try {
                        Integer.parseInt(serialNumber.trim());
                    } catch (NumberFormatException e) {
                        // 不是数字，跳过此行（表头或分类行）
                        System.out.println("跳过非数据行: " + serialNumber);
                        continue;
                    }
                    
                    // 读取Excel列数据（Excel列索引从0开始）
                    // Excel结构：序号 | 学校名称 | 学校标识码 | 主管部门 | 所在地 | 办学层次 | 备注
                    String schoolName = getCellValue(row.getCell(1));      // 学校名称
                    String schoolIdCode = getCellValue(row.getCell(2));    // 学校标识码
                    String supervisor = getCellValue(row.getCell(3));      // 主管部门
                    String location = getCellValue(row.getCell(4));        // 所在地
                    String schoolLevel = getCellValue(row.getCell(5));     // 办学层次
                    String remarks = getCellValue(row.getCell(6));         // 备注
                    
                    // 跳过空行
                    if (schoolName == null || schoolName.trim().isEmpty()) {
                        continue;
                    }
                    
                    // 添加到批处理列表 - 只保存Excel原始数据
                    batchData.add(new Object[]{
                        schoolName,      // 学校名称
                        schoolIdCode,    // 学校标识码
                        supervisor,      // 主管部门
                        location,        // 所在地
                        schoolLevel,     // 办学层次
                        remarks          // 备注
                    });
                    
                    // 每100条批量插入一次
                    if (batchData.size() >= 100) {
                        jdbcTemplate.batchUpdate(sql, batchData);
                        count += batchData.size();
                        batchData.clear();
                        System.out.println("已导入 " + count + " 所高校...");
                    }
                    
                } catch (Exception e) {
                    System.err.println("导入第 " + (i + 1) + " 行数据失败: " + e.getMessage());
                }
            }
            
            // 插入剩余数据
            if (!batchData.isEmpty()) {
                jdbcTemplate.batchUpdate(sql, batchData);
                count += batchData.size();
            }
            
            workbook.close();
            
        } catch (Exception e) {
            System.err.println("导入Excel失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return count;
    }
    
    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * 根据学校名称和主管部门判断学校类型
     */
    private String determineUniversityType(String name, String supervisor) {
        if (name.contains("师范") || name.contains("教育")) {
            return "师范";
        } else if (name.contains("医") || name.contains("药")) {
            return "医药";
        } else if (name.contains("农") || name.contains("林")) {
            return "农林";
        } else if (name.contains("财经") || name.contains("经济") || name.contains("金融")) {
            return "财经";
        } else if (name.contains("政法") || name.contains("警")) {
            return "政法";
        } else if (name.contains("体育")) {
            return "体育";
        } else if (name.contains("艺术") || name.contains("音乐") || name.contains("美术") || name.contains("戏剧")) {
            return "艺术";
        } else if (name.contains("外国语") || name.contains("语言")) {
            return "语言";
        } else if (name.contains("民族")) {
            return "民族";
        } else if (name.contains("理工") || name.contains("科技") || name.contains("工业") || 
                   name.contains("工程") || name.contains("技术")) {
            return "理工";
        } else {
            return "综合";
        }
    }
}
