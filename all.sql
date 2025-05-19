-- all.sql 由多个SQL文件整合而成

-- ========== 数据库创建 ==========
-- 来自 src/main/resources/schema_update.sql
CREATE DATABASE IF NOT EXISTS mi_bms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE mi_bms;

-- ========== battery 表 ==========
-- 来自 create_tables.sql 和 sample_data.sql
CREATE TABLE IF NOT EXISTS battery (
    battery_type INT NOT NULL AUTO_INCREMENT COMMENT '电池类型ID',
    name VARCHAR(50) NOT NULL COMMENT '电池型号名称',
    capacity DOUBLE COMMENT '电池容量(kWh)',
    create_time DATE COMMENT '创建时间',
    update_time DATE COMMENT '更新时间',
    `delete` INT DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (battery_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电池类型表';

-- battery 测试数据
INSERT INTO battery (battery_type, name, capacity, create_time, update_time, `delete`) VALUES
(1, '磷酸铁锂电池', 60.0, CURDATE(), CURDATE(), 0),
(2, '三元锂电池', 80.0, CURDATE(), CURDATE(), 0),
(3, '固态电池', 100.0, CURDATE(), CURDATE(), 0)
ON DUPLICATE KEY UPDATE name=VALUES(name), capacity=VALUES(capacity);

-- ========== vehicle 表 ==========
-- 来自 schema_update.sql 和 sample_data.sql
DROP PROCEDURE IF EXISTS modify_vehicle_table;
DELIMITER //
CREATE PROCEDURE modify_vehicle_table()
BEGIN
    IF EXISTS (SELECT * FROM information_schema.tables WHERE table_schema = 'mi_bms' AND table_name = 'vehicle') THEN
        IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = 'mi_bms' AND table_name = 'vehicle' AND column_name = 'vin') THEN
            ALTER TABLE vehicle CHANGE COLUMN vin vid CHAR(20) NOT NULL COMMENT '车辆唯一标识';
        END IF;
        IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = 'mi_bms' AND table_name = 'vehicle' AND column_name = 'total_distance') THEN
            ALTER TABLE vehicle ADD COLUMN total_distance DOUBLE DEFAULT 0 COMMENT '总里程(公里)';
        END IF;
        IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = 'mi_bms' AND table_name = 'vehicle' AND column_name = 'battery_health') THEN
            ALTER TABLE vehicle ADD COLUMN battery_health INT DEFAULT 100 COMMENT '电池健康状况(百分比)';
        END IF;
    ELSE
        CREATE TABLE vehicle (
            vid CHAR(20) NOT NULL COMMENT '车辆唯一标识',
            carId INT NOT NULL COMMENT '车辆ID',
            battery_type INT NOT NULL COMMENT '电池类型',
            total_distance DOUBLE DEFAULT 0 COMMENT '总里程(公里)',
            battery_health INT DEFAULT 100 COMMENT '电池健康状况(百分比)',
            create_time DATE COMMENT '创建时间',
            update_time DATE COMMENT '更新时间',
            `delete` INT DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
            PRIMARY KEY (vid)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆信息表';
    END IF;
END //
DELIMITER ;
CALL modify_vehicle_table();
DROP PROCEDURE modify_vehicle_table;

-- vehicle 测试数据
INSERT INTO vehicle (vid, carId, battery_type, total_distance, battery_health, create_time, update_time, `delete`) VALUES
('VID001', 1001, 1, 15000.5, 95, CURDATE(), CURDATE(), 0),
('VID002', 1002, 2, 25678.2, 88, CURDATE(), CURDATE(), 0),
('VID003', 1003, 1, 5432.7, 98, CURDATE(), CURDATE(), 0),
('VID004', 1004, 3, 45987.3, 78, CURDATE(), CURDATE(), 0),
('VID005', 1005, 2, 12345.6, 92, CURDATE(), CURDATE(), 0);

-- ========== rule 表 ==========
-- 来自 sample_data.sql、update_rule_table.sql、create_tables.sql
CREATE TABLE IF NOT EXISTS rule (
    id INT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    name VARCHAR(100) NOT NULL COMMENT '规则名称',
    battery_type INT COMMENT '适用电池类型',
    warn_id INT COMMENT '告警类型ID',
    detail TEXT COMMENT '规则详情',
    create_time DATE COMMENT '创建时间',
    update_time DATE COMMENT '更新时间',
    `delete` INT DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则表';

-- rule 字段补充（兼容旧表结构）
ALTER TABLE rule ADD COLUMN IF NOT EXISTS description VARCHAR(255);
ALTER TABLE rule ADD COLUMN IF NOT EXISTS expression TEXT;

-- rule 测试数据
INSERT INTO rule (name, battery_type, warn_id, detail, create_time, update_time, `delete`) VALUES
('温度过高告警', 1, 1, '[{"expression":"temp>60","level":"高"}]', CURDATE(), CURDATE(), 0),
('电压过低告警', 1, 2, '[{"expression":"voltage<3.2","level":"中"}]', CURDATE(), CURDATE(), 0),
('SOC过低告警', 2, 3, '[{"expression":"soc<10","level":"高"}]', CURDATE(), CURDATE(), 0);

-- rule 旧数据更新
UPDATE rule SET warn_id = 1, battery_type = 1, detail = '温度超过40度时发出警告' WHERE id = 1;

-- ========== battery_warning 表 ==========
-- 来自 schema_rename_rule.sql、drop_battery_signals.sql
CREATE TABLE IF NOT EXISTS battery_warning (
    id INT NOT NULL AUTO_INCREMENT COMMENT '预警记录ID',
    car_id INT NOT NULL COMMENT '车辆ID',
    rule_id INT COMMENT '触发的规则ID',
    battery_type VARCHAR(50) NOT NULL COMMENT '电池类型',
    warn_name VARCHAR(100) NOT NULL COMMENT '预警名称',
    warn_level INT NOT NULL COMMENT '预警等级',
    signal_data TEXT COMMENT '信号数据',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    process_status INT DEFAULT 0 COMMENT '处理状态: 0-未处理, 1-已处理, 2-已忽略',
    process_time DATETIME COMMENT '处理时间',
    process_user VARCHAR(50) COMMENT '处理人',
    remark TEXT COMMENT '备注信息',
    processed INT DEFAULT 0 COMMENT '是否已处理: 0-未处理, 1-已处理',
    warn_id INT COMMENT '警告类型ID',
    raw_signal_data TEXT COMMENT '原始信号数据',
    PRIMARY KEY (id),
    INDEX idx_car_id (car_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_create_time (create_time),
    INDEX idx_process_status (process_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电池预警记录表';

-- battery_warning 字段补充
ALTER TABLE battery_warning MODIFY COLUMN warn_level INT NOT NULL COMMENT '预警等级' AFTER warn_name;
ALTER TABLE battery_warning MODIFY COLUMN car_id INT NOT NULL COMMENT '车辆ID';
ALTER TABLE battery_warning MODIFY COLUMN rule_id INT COMMENT '触发的规则ID';
ALTER TABLE battery_warning MODIFY COLUMN battery_type VARCHAR(50) NOT NULL COMMENT '电池类型';
ALTER TABLE battery_warning MODIFY COLUMN warn_name VARCHAR(100) NOT NULL COMMENT '预警名称';
ALTER TABLE battery_warning MODIFY COLUMN signal_data TEXT COMMENT '信号数据';

-- battery_warning 触发器
DROP TRIGGER IF EXISTS update_battery_warning_processed;
DELIMITER //
CREATE TRIGGER update_battery_warning_processed BEFORE UPDATE ON battery_warning
FOR EACH ROW 
BEGIN
    IF NEW.process_status <> OLD.process_status AND NEW.process_status IN (1, 2) THEN
        SET NEW.processed = 1;
        SET NEW.process_time = NOW();
    END IF;
END//
DELIMITER ;

-- battery_warning 测试数据
INSERT INTO battery_warning (car_id, battery_type, warn_name, warn_level, signal_data, create_time) VALUES
(1, '三元电池', '电压差报警', 0, '{"Mx":"12.5","Mi":"8.2"}', NOW()),
(2, '铁锂电池', '电流差报警', 1, '{"Ix":"12.8","Ii":"11.7"}', NOW());

-- ========== battery_signals 表 ==========
-- 来自 create_battery_signals_table.sql
CREATE TABLE IF NOT EXISTS battery_signals (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  car_id INT NOT NULL COMMENT '车辆ID',
  warn_id INT NOT NULL COMMENT '警告类型：1-电压, 2-电流',
  signal_data TEXT NOT NULL COMMENT '信号数据，JSON格式',
  processed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已处理：0-未处理，1-已处理',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  process_time DATETIME NULL COMMENT '处理时间',
  INDEX idx_car_id (car_id),
  INDEX idx_processed (processed),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电池信号数据表';

-- battery_signals 测试数据
INSERT INTO battery_signals (car_id, warn_id, signal_data, create_time) VALUES
(1, 1, '{"Mx":"12.5","Mi":"8.2"}', NOW()),
(2, 2, '{"Ix":"12.8","Ii":"11.7"}', NOW()),
(3, 1, '{"Mx":"11.8","Mi":"9.6"}', NOW()),
(1, 2, '{"Ix":"10.5","Ii":"9.8"}', NOW()),
(2, 1, '{"Mx":"13.2","Mi":"10.1"}', NOW());

-- ========== 其它操作 ==========
-- battery_signals 表删除（如需重建）
DROP TABLE IF EXISTS battery_signals;
-- battery_warning 表重命名（如需迁移）
-- RENAME TABLE battery_warning TO rule; 