-- // BUG-107734 Migrate owner and account information to new organization model part 2
-- Migration SQL that makes the change goes here.

-- cluster resource
ALTER TABLE cluster
    ADD organization_id int8,
    ADD CONSTRAINT clustername_in_org_unique UNIQUE (name, organization_id),
    ADD CONSTRAINT fk_cluster_organization FOREIGN KEY (organization_id) REFERENCES organization(id);

UPDATE cluster
SET organization_id = subquery.org_id
FROM (SELECT userprofile.owner AS up_owner, users.userid AS u_email, user_org_permissions.organization_id AS org_id
      FROM userprofile
      INNER JOIN users ON userprofile.username=users.userid
      INNER JOIN user_org_permissions ON users.id=user_org_permissions.user_id) AS subquery
WHERE owner=up_owner;

-- template resource
ALTER TABLE template
    ADD organization_id int8,
    ADD CONSTRAINT templatename_in_org_unique UNIQUE (name, organization_id),
    ADD CONSTRAINT fk_template_organization FOREIGN KEY (organization_id) REFERENCES organization(id);

UPDATE template
SET organization_id = subquery.org_id
FROM (SELECT userprofile.owner AS up_owner, users.userid AS u_email, user_org_permissions.organization_id AS org_id
      FROM userprofile
      INNER JOIN users ON userprofile.username=users.userid
      INNER JOIN user_org_permissions ON users.id=user_org_permissions.user_id) AS subquery
WHERE owner=up_owner;

-- clustertemplate resource
ALTER TABLE clustertemplate
    ADD organization_id int8,
    ADD CONSTRAINT clustertemplatename_in_org_unique UNIQUE (name, organization_id),
    ADD CONSTRAINT fk_clustertemplate_organization FOREIGN KEY (organization_id) REFERENCES organization(id);

UPDATE clustertemplate
SET organization_id = subquery.org_id
FROM (SELECT userprofile.owner AS up_owner, users.userid AS u_email, user_org_permissions.organization_id AS org_id
      FROM userprofile
      INNER JOIN users ON userprofile.username=users.userid
      INNER JOIN user_org_permissions ON users.id=user_org_permissions.user_id) AS subquery
WHERE owner=up_owner;

-- constrainttemplate resource
ALTER TABLE constrainttemplate
    ADD organization_id int8,
    ADD CONSTRAINT constrainttemplatename_in_org_unique UNIQUE (name, organization_id),
    ADD CONSTRAINT fk_constrainttemplate_organization FOREIGN KEY (organization_id) REFERENCES organization(id);

UPDATE constrainttemplate
SET organization_id = subquery.org_id
FROM (SELECT userprofile.owner AS up_owner, users.userid AS u_email, user_org_permissions.organization_id AS org_id
      FROM userprofile
      INNER JOIN users ON userprofile.username=users.userid
      INNER JOIN user_org_permissions ON users.id=user_org_permissions.user_id) AS subquery
WHERE owner=up_owner;

-- network resource
ALTER TABLE network
    ADD organization_id int8,
    ADD CONSTRAINT networkname_in_org_unique UNIQUE (name, organization_id),
    ADD CONSTRAINT fk_network_organization FOREIGN KEY (organization_id) REFERENCES organization(id);

UPDATE network
SET organization_id = subquery.org_id
FROM (SELECT userprofile.owner AS up_owner, users.userid AS u_email, user_org_permissions.organization_id AS org_id
      FROM userprofile
      INNER JOIN users ON userprofile.username=users.userid
      INNER JOIN user_org_permissions ON users.id=user_org_permissions.user_id) AS subquery
WHERE owner=up_owner;

-- securitygroup resource
ALTER TABLE securitygroup
    ADD organization_id int8,
    ADD CONSTRAINT securitygroupname_in_org_unique UNIQUE (name, organization_id),
    ADD CONSTRAINT fk_securitygroup_organization FOREIGN KEY (organization_id) REFERENCES organization(id);

UPDATE securitygroup
SET organization_id = subquery.org_id
FROM (SELECT userprofile.owner AS up_owner, users.userid AS u_email, user_org_permissions.organization_id AS org_id
      FROM userprofile
      INNER JOIN users ON userprofile.username=users.userid
      INNER JOIN user_org_permissions ON users.id=user_org_permissions.user_id) AS subquery
WHERE owner=up_owner;

-- topology resource
ALTER TABLE topology
    ADD organization_id int8,
    ADD CONSTRAINT topologyname_in_org_unique UNIQUE (name, organization_id),
    ADD CONSTRAINT fk_topology_organization FOREIGN KEY (organization_id) REFERENCES organization(id);

UPDATE topology
SET organization_id = subquery.org_id
FROM (SELECT userprofile.owner AS up_owner, users.userid AS u_email, user_org_permissions.organization_id AS org_id
      FROM userprofile
      INNER JOIN users ON userprofile.username=users.userid
      INNER JOIN user_org_permissions ON users.id=user_org_permissions.user_id) AS subquery
WHERE owner=up_owner;

-- flexsubscription resource
ALTER TABLE flexsubscription
    ADD organization_id int8,
    ADD CONSTRAINT flexsubscriptionname_in_org_unique UNIQUE (name, organization_id),
    ADD CONSTRAINT fk_flexsubscription_organization FOREIGN KEY (organization_id) REFERENCES organization(id);

UPDATE flexsubscription
SET organization_id = subquery.org_id
FROM (SELECT userprofile.owner AS up_owner, users.userid AS u_email, user_org_permissions.organization_id AS org_id
      FROM userprofile
      INNER JOIN users ON userprofile.username=users.userid
      INNER JOIN user_org_permissions ON users.id=user_org_permissions.user_id) AS subquery
WHERE owner=up_owner;

-- smartsense resource
ALTER TABLE smartsensesubscription
    ADD organization_id int8,
    ADD CONSTRAINT smartsensesubscriptionsubscriptionid_in_org_unique UNIQUE (name, organization_id),
    ADD CONSTRAINT fk_smartsensesubscription_organization FOREIGN KEY (organization_id) REFERENCES organization(id);

UPDATE smartsensesubscription
SET organization_id = subquery.org_id
FROM (SELECT userprofile.owner AS up_owner, users.userid AS u_email, user_org_permissions.organization_id AS org_id
      FROM userprofile
      INNER JOIN users ON userprofile.username=users.userid
      INNER JOIN user_org_permissions ON users.id=user_org_permissions.user_id) AS subquery
WHERE owner=up_owner;

-- filesystem resource
ALTER TABLE filesystem
    ADD organization_id int8,
    ADD CONSTRAINT filesystemname_in_org_unique UNIQUE (name, organization_id),
    ADD CONSTRAINT fk_filesystem_organization FOREIGN KEY (organization_id) REFERENCES organization(id);

UPDATE filesystem
SET organization_id = subquery.org_id
FROM (SELECT userprofile.owner AS up_owner, users.userid AS u_email, user_org_permissions.organization_id AS org_id
      FROM userprofile
      INNER JOIN users ON userprofile.username=users.userid
      INNER JOIN user_org_permissions ON users.id=user_org_permissions.user_id) AS subquery
WHERE owner=up_owner;

-- userprofile resource
ALTER TABLE userprofile
    ADD organization_id int8,
    ADD CONSTRAINT userprofileusername_in_org_unique UNIQUE (username, organization_id),
    ADD CONSTRAINT fk_userprofile_organization FOREIGN KEY (organization_id) REFERENCES organization(id);

UPDATE userprofile
SET organization_id = subquery.org_id
FROM (SELECT userprofile.owner AS up_owner, users.userid AS u_email, user_org_permissions.organization_id AS org_id
      FROM userprofile
      INNER JOIN users ON userprofile.username=users.userid
      INNER JOIN user_org_permissions ON users.id=user_org_permissions.user_id) AS subquery
WHERE owner=up_owner;

-- //@UNDO
-- SQL to undo the change goes here.

-- cluster resource
ALTER TABLE cluster
    DROP CONSTRAINT IF EXISTS clustername_in_org_unique,
    DROP CONSTRAINT IF EXISTS fk_cluster_organization,
    DROP COLUMN IF EXISTS organization_id;

-- template resource
ALTER TABLE template
    DROP CONSTRAINT IF EXISTS templatename_in_org_unique,
    DROP CONSTRAINT IF EXISTS fk_template_organization,
    DROP COLUMN IF EXISTS organization_id;

-- clustertemplate resource
ALTER TABLE clustertemplate
    DROP CONSTRAINT IF EXISTS clustertemplatename_in_org_unique,
    DROP CONSTRAINT IF EXISTS fk_clustertemplate_organization,
    DROP COLUMN IF EXISTS organization_id;

-- constrainttemplate resource
ALTER TABLE constrainttemplate
    DROP CONSTRAINT IF EXISTS constrainttemplatename_in_org_unique,
    DROP CONSTRAINT IF EXISTS fk_constrainttemplate_organization,
    DROP COLUMN IF EXISTS organization_id;

-- network resource
ALTER TABLE network
    DROP CONSTRAINT IF EXISTS networkname_in_org_unique,
    DROP CONSTRAINT IF EXISTS fk_network_organization,
    DROP COLUMN IF EXISTS organization_id;

-- securitygroup resource
ALTER TABLE securitygroup
    DROP CONSTRAINT IF EXISTS securitygroupname_in_org_unique,
    DROP CONSTRAINT IF EXISTS fk_securitygroup_organization,
    DROP COLUMN IF EXISTS organization_id;

-- topology resource
ALTER TABLE topology
    DROP CONSTRAINT IF EXISTS topologyname_in_org_unique,
    DROP CONSTRAINT IF EXISTS fk_topology_organization,
    DROP COLUMN IF EXISTS organization_id;

-- flexsubscription resource
ALTER TABLE flexsubscription
    DROP CONSTRAINT IF EXISTS flexsubscriptionname_in_org_unique,
    DROP CONSTRAINT IF EXISTS fk_flexsubscription_organization,
    DROP COLUMN IF EXISTS organization_id;

-- smartsense resource
ALTER TABLE smartsensesubscription
    DROP CONSTRAINT IF EXISTS smartsensesubscriptionsubscriptionid_in_org_unique,
    DROP CONSTRAINT IF EXISTS fk_smartsensesubscription_organization,
    DROP COLUMN IF EXISTS organization_id;

-- filesystem resource
ALTER TABLE filesystem
    DROP CONSTRAINT IF EXISTS filesystemname_in_org_unique,
    DROP CONSTRAINT IF EXISTS fk_filesystem_organization,
    DROP COLUMN IF EXISTS organization_id;

-- userprofile resource
ALTER TABLE userprofile
    DROP CONSTRAINT IF EXISTS userprofileusername_in_org_unique,
    DROP CONSTRAINT IF EXISTS fk_userprofile_organization,
    DROP COLUMN IF EXISTS organization_id;