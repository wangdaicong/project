ALTER TABLE `import_job`
  ADD COLUMN `original_filename` VARCHAR(255) NULL,
  ADD COLUMN `stored_file_path` VARCHAR(512) NULL;
