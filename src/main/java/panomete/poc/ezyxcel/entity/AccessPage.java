package panomete.poc.ezyxcel.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class AccessPage{
        @ExcelProperty("name")
        String name;
        @ExcelProperty("description")
        String description;
}
