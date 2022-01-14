-- Drop old indexes
DROP INDEX IF EXISTS recipient_id_index ON affected;
DROP INDEX IF EXISTS recipient_id_index ON feedback;

-- Rename columns
ALTER TABLE affected CHANGE COLUMN IF EXISTS recipient_id party_id varchar(255);
ALTER TABLE disturbance_feedback CHANGE COLUMN IF EXISTS recipient_id party_id varchar(255);
ALTER TABLE disturbance_feedback_history CHANGE COLUMN IF EXISTS recipient_id party_id varchar(255);
ALTER TABLE feedback CHANGE COLUMN IF EXISTS recipient_id party_id varchar(255);

-- Create new indexes
CREATE INDEX IF NOT EXISTS party_id_index on affected (party_id);
CREATE INDEX IF NOT EXISTS party_id_index on feedback (party_id);

-- Necessary line in order to document the change. 
INSERT INTO disturbance.schema_history (schema_version,comment,applied) VALUES ('002','Rename attribute recipient_id to party_id', NOW());