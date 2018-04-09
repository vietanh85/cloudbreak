-- // BUG-99322 [API] blueprint name throws 'duplicate key value violates unique constraint' error
-- Migration SQL that makes the change goes here.

ALTER TABLE blueprint RENAME COLUMN name TO displayname;
ALTER TABLE blueprint ADD COLUMN name varchar(255);
UPDATE blueprint b set name=b.ambariname where status='DEFAULT';
UPDATE blueprint b set name=concat_ws('-', b.ambariname, b.id::text) where status != 'DEFAULT';
ALTER TABLE blueprint ALTER name SET NOT NULL;
ALTER TABLE blueprint DROP CONSTRAINT IF EXISTS uk_blueprint_account_name;
ALTER TABLE ONLY blueprint DROP CONSTRAINT IF EXISTS uk_blueprint_account_displayname;
ALTER TABLE ONLY blueprint ADD CONSTRAINT uk_blueprint_account_name UNIQUE (account, name);
ALTER TABLE ONLY blueprint ADD CONSTRAINT uk_blueprint_account_displayname UNIQUE (account, displayname);

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE blueprint DROP CONSTRAINT IF EXISTS uk_blueprint_account_name;
ALTER TABLE blueprint DROP CONSTRAINT IF EXISTS uk_blueprint_account_displayname;
ALTER TABLE blueprint DROP COLUMN IF EXISTS name;
ALTER TABLE blueprint RENAME COLUMN displayname TO name;
ALTER TABLE ONLY blueprint ADD CONSTRAINT uk_blueprint_account_name UNIQUE (account, name);

