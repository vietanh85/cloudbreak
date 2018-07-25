-- // RMP-11104 Support for Multiple AZs on AWS Azure
-- Migration SQL that makes the change goes here.

CREATE TABLE availabilityconfig(
   id               BIGINT NOT NULL,
   subnetcidr       CHARACTER VARYING (255),
   availabilityzone TEXT,
   configurations   TEXT DEFAULT '{}',
   instancegroup_id bigint,
   PRIMARY KEY (id)
);

CREATE SEQUENCE availabilityconfig_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

INSERT INTO availabilityconfig (id, instancegroup_id, configurations, subnetcidr, availabilityzone)
   SELECT nextval('availabilityconfig_id_seq') AS "id", instancegroup.id, CONCAT('', SUBSTRING(RTRIM(network.attributes, '}') from 0 for (BIT_LENGTH(RTRIM(network.attributes, '}')) - 1)), ',"type":"AWS"}'), network.subnetcidr, stack.availabilityzone FROM stack
       INNER JOIN network ON stack.network_id=network.id
       INNER JOIN instancegroup ON stack_id=stack.id
       WHERE stack.network_id IS NOT NULL AND stack.cloudplatform='AWS';

INSERT INTO availabilityconfig (id, instancegroup_id, configurations, subnetcidr, availabilityzone)
   SELECT nextval('availabilityconfig_id_seq') AS "id", instancegroup.id, CONCAT('', SUBSTRING(RTRIM(network.attributes, '}') from 0 for (BIT_LENGTH(RTRIM(network.attributes, '}')) - 1)), ',"type":"AZURE"}'), network.subnetcidr, stack.availabilityzone FROM stack
       INNER JOIN network ON stack.network_id=network.id
       INNER JOIN instancegroup ON stack_id=stack.id
       WHERE stack.network_id IS NOT NULL AND stack.cloudplatform='AZURE';

INSERT INTO availabilityconfig (id, instancegroup_id, configurations, subnetcidr, availabilityzone)
   SELECT nextval('availabilityconfig_id_seq') AS "id", instancegroup.id, CONCAT('', SUBSTRING(RTRIM(network.attributes, '}') from 0 for (BIT_LENGTH(RTRIM(network.attributes, '}')) - 1)), ',"type":"GCP"}'), network.subnetcidr, stack.availabilityzone FROM stack
       INNER JOIN network ON stack.network_id=network.id
       INNER JOIN instancegroup ON stack_id=stack.id
       WHERE stack.network_id IS NOT NULL AND stack.cloudplatform='GCP';

INSERT INTO availabilityconfig (id, instancegroup_id, configurations, subnetcidr, availabilityzone)
   SELECT nextval('availabilityconfig_id_seq') AS "id", instancegroup.id, CONCAT('', SUBSTRING(RTRIM(network.attributes, '}') from 0 for (BIT_LENGTH(RTRIM(network.attributes, '}')) - 1)), ',"type":"OPENSTACK"}'), network.subnetcidr, stack.availabilityzone FROM stack
       INNER JOIN network ON stack.network_id=network.id
       INNER JOIN instancegroup ON stack_id=stack.id
       WHERE stack.network_id IS NOT NULL AND stack.cloudplatform='OPENSTACK';

INSERT INTO availabilityconfig (id, instancegroup_id, configurations, subnetcidr, availabilityzone)
   SELECT nextval('availabilityconfig_id_seq') AS "id", instancegroup.id, CONCAT('', SUBSTRING(RTRIM(network.attributes, '}') from 0 for (BIT_LENGTH(RTRIM(network.attributes, '}')) - 1)), ',"type":"YARN"}'), network.subnetcidr, stack.availabilityzone FROM stack
       INNER JOIN network ON stack.network_id=network.id
       INNER JOIN instancegroup ON stack_id=stack.id
       WHERE stack.network_id IS NOT NULL AND stack.cloudplatform='YARN';

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE IF EXISTS availabilityconfig;

DROP SEQUENCE IF EXISTS availabilityconfig_id_seq;