package org.example.mi_bms.mapper;

import org.apache.ibatis.annotations.*;
import org.example.mi_bms.entity.Rule;

import java.util.List;

@Mapper
public interface RuleMapper {
    
    /**
     * 保存预警规则
     */
    @Insert("INSERT INTO rule(rule_id, rule_number, name, battery_type, warning_condition, warning_level, expression, create_time, update_time) " +
            "VALUES(#{ruleId}, #{ruleNumber}, #{name}, #{batteryType}, #{warningCondition}, #{warningLevel}, #{expression}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Rule rule);
    
    /**
     * 根据规则ID查询预警规则
     */
    @Select("SELECT * FROM rule WHERE rule_id = #{ruleId}")
    List<Rule> findByRuleId(@Param("ruleId") Integer ruleId);
    
    /**
     * 根据规则编号查询预警规则
     */
    @Select("SELECT * FROM rule WHERE rule_number = #{ruleNumber}")
    List<Rule> findByRuleNumber(@Param("ruleNumber") Integer ruleNumber);
    
    /**
     * 根据电池类型查询预警规则
     */
    @Select("SELECT * FROM rule WHERE battery_type = #{batteryType}")
    List<Rule> findByBatteryType(@Param("batteryType") String batteryType);
    
    /**
     * 根据电池类型和规则编号查询预警规则
     */
    @Select("SELECT * FROM rule WHERE battery_type = #{batteryType} AND rule_number = #{ruleNumber}")
    List<Rule> findByBatteryTypeAndRuleNumber(@Param("batteryType") String batteryType, @Param("ruleNumber") Integer ruleNumber);
    
    /**
     * 根据ID查询预警规则
     */
    @Select("SELECT * FROM rule WHERE id = #{id}")
    Rule findById(@Param("id") Integer id);
    
    /**
     * 查询所有预警规则
     */
    @Select("SELECT * FROM rule ORDER BY rule_id, rule_number, warning_level")
    List<Rule> findAll();
    
    /**
     * 查询所有预警规则（兼容方法）
     */
    @Select("SELECT * FROM rule ORDER BY rule_id, rule_number, warning_level")
    List<Rule> selectAll();
    
    /**
     * 根据电池类型和告警ID查询预警规则
     */
    @Select("SELECT * FROM rule WHERE battery_type = #{batteryType} AND warn_id = #{warnId}")
    List<Rule> selectByBatteryTypeAndWarnId(@Param("batteryType") String batteryType, @Param("warnId") Integer warnId);
    
    /**
     * 更新预警规则
     */
    @Update("UPDATE rule SET rule_number=#{ruleNumber}, name=#{name}, battery_type=#{batteryType}, " +
            "warning_condition=#{warningCondition}, warning_level=#{warningLevel}, expression=#{expression}, " +
            "update_time=#{updateTime} WHERE id=#{id}")
    int update(Rule rule);
    
    /**
     * 删除预警规则
     */
    @Delete("DELETE FROM rule WHERE id=#{id}")
    int delete(@Param("id") Integer id);
}