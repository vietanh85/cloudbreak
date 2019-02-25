-- // CLOUD-81698 extend LDAP Domain object
-- Migration SQL that makes the change goes here.


ALTER TABLE ldapConfigurationId ADD COLUMN directoryType VARCHAR(63);
ALTER TABLE ldapConfigurationId ADD COLUMN userObjectClass text;
ALTER TABLE ldapConfigurationId ADD COLUMN groupObjectClass text;
ALTER TABLE ldapConfigurationId ADD COLUMN groupIdAttribute text;
ALTER TABLE ldapConfigurationId ADD COLUMN groupMemberAttribute text;

UPDATE ldapConfigurationId SET userObjectClass = 'person' WHERE userObjectClass IS NULL;
UPDATE ldapConfigurationId SET groupObjectClass = 'groupOfNames' WHERE groupObjectClass IS NULL;
UPDATE ldapConfigurationId SET groupIdAttribute = 'cn' WHERE groupIdAttribute IS NULL;
UPDATE ldapConfigurationId SET groupMemberAttribute = 'member' WHERE groupMemberAttribute IS NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE ldapConfigurationId DROP COLUMN directoryType;
ALTER TABLE ldapConfigurationId DROP COLUMN userObjectClass;
ALTER TABLE ldapConfigurationId DROP COLUMN groupObjectClass;
ALTER TABLE ldapConfigurationId DROP COLUMN groupIdAttribute;
ALTER TABLE ldapConfigurationId DROP COLUMN groupMemberAttribute;
