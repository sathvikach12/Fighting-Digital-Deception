USE truthchain;

-- Demo passwords are fictional. Replace these accounts before any public deployment.
-- scripts/seed-demo.ps1 generates the hashes and applies this seed file.
INSERT INTO users(name,email,password_hash,role,status) VALUES
('TruthChain Admin','admin@truthchain.local','{bcrypt-hash-admin}','ADMIN','ACTIVE'),
('Aarav Reader','reader@truthchain.local','{bcrypt-hash-reader}','USER','ACTIVE'),
('Mira Pending','pending@truthchain.local','{bcrypt-hash-reader}','USER','PENDING')
ON DUPLICATE KEY UPDATE email=VALUES(email);

INSERT INTO categories(name,description) VALUES
('Science','Research and discoveries'),('Technology','Computing and digital life'),('Civic life','Public institutions and communities'),('Environment','Climate and conservation')
ON DUPLICATE KEY UPDATE description=VALUES(description);

INSERT INTO suspicious_keywords(keyword,reason) VALUES
('miracle cure','Unverified health claim wording'),('secret plan','Potentially sensational framing'),('share before deleted','Urgency tactic often used in viral misinformation')
ON DUPLICATE KEY UPDATE reason=VALUES(reason);

INSERT INTO app_settings(setting_key,setting_value) VALUES('quantization_date',DATE_FORMAT(DATE_SUB(CURDATE(),INTERVAL 7 DAY),'%Y-%m-%d'))
ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value);
