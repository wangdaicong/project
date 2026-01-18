CREATE TABLE IF NOT EXISTS `exam_session_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `score` INT NOT NULL DEFAULT 1,
  `sort` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_esq_session_question` (`session_id`, `question_id`),
  KEY `idx_esq_session_sort` (`session_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
