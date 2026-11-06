CREATE DATABASE IF NOT EXISTS truthchain CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE truthchain;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(60) NOT NULL,
  role ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER',
  status ENUM('PENDING','ACTIVE','INACTIVE') NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_status_role(status,role)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE,
  description VARCHAR(500) NOT NULL DEFAULT '',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS news (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  category_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(240) NOT NULL,
  description TEXT NOT NULL,
  publisher VARCHAR(180) NOT NULL,
  published_at DATE NOT NULL,
  view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  version INT UNSIGNED NOT NULL DEFAULT 1,
  content_hash CHAR(64) NOT NULL,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_by BIGINT UNSIGNED NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_news_category FOREIGN KEY(category_id) REFERENCES categories(id),
  CONSTRAINT fk_news_creator FOREIGN KEY(created_by) REFERENCES users(id),
  INDEX idx_news_category_date(category_id,published_at),
  INDEX idx_news_deleted_date(is_deleted,published_at),
  FULLTEXT INDEX ft_news_text(title,description)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS news_versions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  article_id BIGINT UNSIGNED NOT NULL,
  version INT UNSIGNED NOT NULL,
  category_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(240) NOT NULL,
  description TEXT NOT NULL,
  publisher VARCHAR(180) NOT NULL,
  published_at DATE NOT NULL,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  content_hash CHAR(64) NOT NULL,
  edited_by BIGINT UNSIGNED NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_news_version(article_id,version),
  CONSTRAINT fk_version_article FOREIGN KEY(article_id) REFERENCES news(id),
  CONSTRAINT fk_version_category FOREIGN KEY(category_id) REFERENCES categories(id),
  CONSTRAINT fk_version_editor FOREIGN KEY(edited_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS suspicious_keywords (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  keyword VARCHAR(100) NOT NULL UNIQUE,
  reason VARCHAR(255) NOT NULL DEFAULT '',
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS moderation_records (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  article_id BIGINT UNSIGNED NOT NULL UNIQUE,
  status ENUM('CLEAR','NEEDS_REVIEW','REVIEWED') NOT NULL DEFAULT 'CLEAR',
  reviewed_by BIGINT UNSIGNED NULL,
  reviewed_at TIMESTAMP NULL,
  notes VARCHAR(1000) NOT NULL DEFAULT '',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_moderation_article FOREIGN KEY(article_id) REFERENCES news(id),
  CONSTRAINT fk_moderation_reviewer FOREIGN KEY(reviewed_by) REFERENCES users(id),
  INDEX idx_moderation_status(status,created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS moderation_matches (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  article_id BIGINT UNSIGNED NOT NULL,
  keyword VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_match(article_id,keyword),
  CONSTRAINT fk_match_article FOREIGN KEY(article_id) REFERENCES news(id) ON DELETE CASCADE,
  INDEX idx_matches_keyword(keyword)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ledger_blocks (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  block_index BIGINT UNSIGNED NOT NULL UNIQUE,
  article_id BIGINT UNSIGNED NOT NULL,
  version INT UNSIGNED NOT NULL,
  block_timestamp VARCHAR(40) NOT NULL,
  content_hash CHAR(64) NOT NULL,
  previous_hash VARCHAR(64) NOT NULL,
  block_hash CHAR(64) NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_article_ledger_version(article_id,version),
  CONSTRAINT fk_ledger_article FOREIGN KEY(article_id) REFERENCES news(id),
  INDEX idx_ledger_article(article_id,version)
) ENGINE=InnoDB;

-- Reserve article_id=0 for the genesis record; real articles begin at id 1.
INSERT INTO news(id,category_id,title,description,publisher,published_at,version,content_hash,is_deleted,created_by)
SELECT 0,c.id,'Ledger genesis','Reserved system row for the initial ledger block','TruthChain',CURRENT_DATE,0,'GENESIS',TRUE,u.id
FROM categories c JOIN users u ON u.role='ADMIN' ORDER BY c.id,u.id LIMIT 1
ON DUPLICATE KEY UPDATE id=id;
INSERT INTO ledger_blocks(block_index,article_id,version,block_timestamp,content_hash,previous_hash,block_hash)
SELECT 0,0,0,'GENESIS','GENESIS','GENESIS',SHA2('0|0|0|GENESIS|GENESIS|GENESIS',256)
WHERE NOT EXISTS(SELECT 1 FROM ledger_blocks);

DELIMITER $$
DROP TRIGGER IF EXISTS ledger_blocks_no_update$$
CREATE TRIGGER ledger_blocks_no_update BEFORE UPDATE ON ledger_blocks FOR EACH ROW
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Ledger blocks are append-only'; END$$
DROP TRIGGER IF EXISTS ledger_blocks_no_delete$$
CREATE TRIGGER ledger_blocks_no_delete BEFORE DELETE ON ledger_blocks FOR EACH ROW
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Ledger blocks are append-only'; END$$
DELIMITER ;

CREATE TABLE IF NOT EXISTS search_history (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  query_text VARCHAR(255) NOT NULL DEFAULT '',
  category_id BIGINT UNSIGNED NULL,
  result_count INT UNSIGNED NOT NULL DEFAULT 0,
  searched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_search_user FOREIGN KEY(user_id) REFERENCES users(id),
  CONSTRAINT fk_search_category FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE SET NULL,
  INDEX idx_search_user_date(user_id,searched_at),
  INDEX idx_search_date(searched_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS app_settings (
  setting_key VARCHAR(100) NOT NULL PRIMARY KEY,
  setting_value VARCHAR(500) NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

