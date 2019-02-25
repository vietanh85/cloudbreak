-- // CLOUD-81698 cleanup LDAP domain
-- Migration SQL that makes the change goes here.


ALTER TABLE ldapConfigurationId DROP COLUMN userSearchFilter;
ALTER TABLE ldapConfigurationId DROP COLUMN groupSearchFilter;
ALTER TABLE ldapConfigurationId DROP COLUMN principalRegex;


ALTER TABLE ldapConfigurationId RENAME COLUMN userSearchAttribute TO userNameAttribute;
ALTER TABLE ldapConfigurationId RENAME COLUMN groupIdAttribute TO groupNameAttribute;


-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE ldapConfigurationId ADD COLUMN userSearchFilter text;
ALTER TABLE ldapConfigurationId ADD COLUMN groupSearchFilter text;
ALTER TABLE ldapConfigurationId ADD COLUMN principalRegex text;

ALTER TABLE ldapConfigurationId RENAME COLUMN userNameAttribute TO userSearchAttribute;
ALTER TABLE ldapConfigurationId RENAME COLUMN groupNameAttribute TO groupIdAttribute;