package org.example.mi_bms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.mi_bms.entity.Vehicle;

public interface VehicleMapper {
    int deleteByPrimaryKey(String vid);

    int insert(Vehicle record);

    Vehicle selectByPrimaryKey(String vid);

    List<Vehicle> selectAll();

    int updateByPrimaryKey(Vehicle record);

    Vehicle selectByCarId(Integer carid);
    
    /**
     * 根据电池类型查询车辆列表
     */
    @Select("SELECT * FROM vehicle WHERE battery_type = #{batteryType} AND delete = 0")
    List<Vehicle> selectByBatteryType(@Param("batteryType") Integer batteryType);
}