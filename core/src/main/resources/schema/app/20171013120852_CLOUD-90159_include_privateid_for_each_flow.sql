-- // CLOUD-90159 include privateid for each flow
-- Migration SQL that makes the change goes here.

ALTER TABLE flowlog ADD COLUMN privateid bigint NOT NULL DEFAULT 1;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE flowlog DROP COLUMN privateid;


