CREATE TABLE IF NOT EXISTS `import_job` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `job_type` VARCHAR(32) NOT NULL DEFAULT 'QUESTION_JSON',
  `stage_id` BIGINT NULL,
  `subject_id` BIGINT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'FINISHED',
  `total_count` INT NOT NULL DEFAULT 0,
  `inserted_count` INT NOT NULL DEFAULT 0,
  `duplicate_count` INT NOT NULL DEFAULT 0,
  `failed_count` INT NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_job_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `import_job_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `job_id` BIGINT NOT NULL,
  `question_id` BIGINT NULL,
  `subject_id` BIGINT NOT NULL,
  `question_hash` CHAR(32) NULL,
  `result` VARCHAR(16) NOT NULL,
  `message` VARCHAR(255) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_job_item_job` (`job_id`),
  KEY `idx_job_item_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
