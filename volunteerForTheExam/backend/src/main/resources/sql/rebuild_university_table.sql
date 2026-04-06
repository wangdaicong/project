DROP TABLE IF EXISTS university;

CREATE TABLE university (
  id BIGINT NOT NULL AUTO_INCREMENT,
  school_name VARCHAR(100) NOT NULL,
  school_id_code VARCHAR(50) DEFAULT NULL,
  supervisor VARCHAR(100) DEFAULT NULL,
  location VARCHAR(100) DEFAULT NULL,
  school_level VARCHAR(20) DEFAULT NULL,
  remarks VARCHAR(500) DEFAULT NULL,
  is_985 TINYINT(1) DEFAULT 0,
  is_211 TINYINT(1) DEFAULT 0,
  is_double_first_class TINYINT(1) DEFAULT 0,
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_school_name (school_name),
  KEY idx_school_id_code (school_id_code),
  KEY idx_supervisor (supervisor),
  KEY idx_location (location),
  KEY idx_school_level (school_level),
  KEY idx_985_211_double (is_985, is_211, is_double_first_class)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DESC university;
