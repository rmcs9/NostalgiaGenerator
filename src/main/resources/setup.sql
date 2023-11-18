CREATE DATABASE memry;
USE memry;

CREATE TABLE msgTerms
(
    termID      int,
    termLiteral varchar(50),
    PRIMARY KEY (termID)
);
CREATE TABLE msgMessages
(
    termID  int,
    termMSG varchar(255),
    FOREIGN KEY (termID) REFERENCES msgTerms (termID)
);

CREATE TABLE onCooldown
(
    discord_id      varchar(32),
    cooldown_expire TIMESTAMP
);