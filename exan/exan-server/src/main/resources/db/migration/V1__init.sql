CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `password_hash` VARCHAR(255) NULL,
  `display_name` VARCHAR(64) NULL,
  `role` VARCHAR(32) NOT NULL DEFAULT 'STUDENT',
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `edu_stage` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(32) NOT NULL,
  `name` VARCHAR(32) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `sort` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stage_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `subject` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `stage_id` BIGINT NOT NULL,
  `code` VARCHAR(32) NOT NULL,
  `name` VARCHAR(32) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `sort` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_stage_code` (`stage_id`, `code`),
  KEY `idx_subject_stage_status` (`stage_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `knowledge_point` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `subject_id` BIGINT NOT NULL,
  `parent_id` BIGINT NOT NULL DEFAULT 0,
  `name` VARCHAR(128) NOT NULL,
  `path` VARCHAR(512) NOT NULL,
  `level` INT NOT NULL DEFAULT 1,
  `sort` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_kp_subject_parent` (`subject_id`, `parent_id`),
  KEY `idx_kp_subject_status` (`subject_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `question_source` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(32) NOT NULL,
  `region_code` VARCHAR(32) NULL,
  `year` INT NULL,
  `paper_name` VARCHAR(255) NULL,
  `copyright_flag` TINYINT NOT NULL DEFAULT 0,
  `raw_file_url` VARCHAR(512) NULL,
  PRIMARY KEY (`id`),
  KEY `idx_qs_type_region_year` (`type`, `region_code`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `stage_id` BIGINT NOT NULL,
  `subject_id` BIGINT NOT NULL,
  `type` VARCHAR(16) NOT NULL,
  `stem` MEDIUMTEXT NOT NULL,
  `difficulty` TINYINT NOT NULL DEFAULT 3,
  `analysis` MEDIUMTEXT NULL,
  `answer` MEDIUMTEXT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  `source_id` BIGINT NULL,
  `question_hash` CHAR(32) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_question_stage_subject_status` (`stage_id`, `subject_id`, `status`),
  KEY `idx_question_subject_type_diff` (`subject_id`, `type`, `difficulty`),
  UNIQUE KEY `uk_question_subject_hash` (`subject_id`, `question_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `question_option` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL,
  `opt_key` VARCHAR(8) NOT NULL,
  `content` MEDIUMTEXT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_qo_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `question_kp_rel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL,
  `kp_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qk_question_kp` (`question_id`, `kp_id`),
  KEY `idx_qk_kp` (`kp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `paper` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `stage_id` BIGINT NOT NULL,
  `subject_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `paper_type` VARCHAR(16) NOT NULL DEFAULT 'FIXED',
  `total_score` INT NOT NULL DEFAULT 100,
  `time_limit_sec` INT NOT NULL DEFAULT 3600,
  `version` INT NOT NULL DEFAULT 1,
  `status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  `pricing_type` VARCHAR(16) NOT NULL DEFAULT 'FREE',
  `price_cent` INT NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_paper_stage_subject_status` (`stage_id`, `subject_id`, `status`),
  KEY `idx_paper_status_pricing` (`status`, `pricing_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `paper_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `paper_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `score` INT NOT NULL DEFAULT 1,
  `sort` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pq_paper_question` (`paper_id`, `question_id`),
  KEY `idx_pq_paper_sort` (`paper_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `exam_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `mode` VARCHAR(16) NOT NULL,
  `stage_id` BIGINT NOT NULL,
  `subject_id` BIGINT NOT NULL,
  `paper_id` BIGINT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'CREATED',
  `started_at` TIMESTAMP NULL,
  `submitted_at` TIMESTAMP NULL,
  `time_limit_sec` INT NULL,
  `score_total` INT NULL,
  `score_got` INT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_user_created` (`user_id`, `created_at`),
  KEY `idx_session_paper_status` (`paper_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `exam_answer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `answer_json` MEDIUMTEXT NULL,
  `is_correct` TINYINT NULL,
  `score_got` INT NULL,
  `answered_at` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_answer_session_question` (`session_id`, `question_id`),
  KEY `idx_answer_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `wrong_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `wrong_count` INT NOT NULL DEFAULT 1,
  `last_wrong_at` TIMESTAMP NULL,
  `last_session_id` BIGINT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wrong_user_question` (`user_id`, `question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_favorite_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fav_user_question` (`user_id`, `question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_type` VARCHAR(32) NOT NULL,
  `ref_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `price_cent` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(16) NOT NULL DEFAULT 'ONLINE',
  PRIMARY KEY (`id`),
  KEY `idx_product_type_ref` (`product_type`, `ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `order_no` VARCHAR(64) NOT NULL,
  `amount_cent` INT NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'CREATED',
  `pay_channel` VARCHAR(16) NOT NULL DEFAULT 'MOCK',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `paid_at` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `entitlement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `ent_type` VARCHAR(32) NOT NULL,
  `ref_id` BIGINT NOT NULL,
  `order_id` BIGINT NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ent_user_type_ref` (`user_id`, `ent_type`, `ref_id`),
  KEY `idx_ent_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `leaderboard_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `lb_type` VARCHAR(16) NOT NULL,
  `stage_id` BIGINT NOT NULL,
  `subject_id` BIGINT NOT NULL,
  `stat_date` VARCHAR(16) NOT NULL,
  `rank_json` MEDIUMTEXT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_lb_type_subject_date` (`lb_type`, `subject_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
