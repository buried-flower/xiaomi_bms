package org.example.mi_bms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.mi_bms.entity.Battery;

public interface BatteryMapper {
    int deleteByPrimaryKey(Integer batteryType);

    int insert(Battery record);

    Battery selectByPrimaryKey(Integer batteryType);

    List<Battery> selectAll();

    int updateByPrimaryKey(Battery record);
    
    /**
     * 根据电池类型查询电池信息
     */
    @Select("SELECT * FROM battery WHERE battery_type = #{batteryType} AND delete = 0")
    Battery selectByType(@Param("batteryType") Integer batteryType);
}