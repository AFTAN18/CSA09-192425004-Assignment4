CREATE TABLE IF NOT EXISTS fees (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  reg_no VARCHAR(20) NOT NULL,
  amount DECIMAL(10,2) NOT NULL CHECK (amount > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS registrations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  reg_no VARCHAR(20) NOT NULL,
  student_name VARCHAR(100) NOT NULL,
  event_name VARCHAR(16) NOT NULL,
  CONSTRAINT uq_registration_event UNIQUE (reg_no,event_name),
  INDEX ix_event_id (event_name,id)
) ENGINE=InnoDB;
