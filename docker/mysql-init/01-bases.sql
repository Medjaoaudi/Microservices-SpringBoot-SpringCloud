-- Une base indépendante par microservice (les services utilisent aussi createDatabaseIfNotExist=true)
CREATE DATABASE IF NOT EXISTS etudiant_db    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS cours_db       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS inscription_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
