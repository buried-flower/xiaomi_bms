-- 修改rule表，添加缺失的字段
ALTER TABLE `rule`
ADD COLUMN `warn_id` INT,
ADD COLUMN `battery_type` INT,
ADD COLUMN `detail` VARCHAR(255);

-- 更新现有规则数据
UPDATE `rule` SET 
  `warn_id` = 1, 
  `battery_type` = 1, 
  `detail` = '温度超过40度时发出警告'
WHERE `id` = 1; 