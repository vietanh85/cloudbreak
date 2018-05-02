#!/bin/bash

## logging
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

set -x

export CLOUD_PLATFORM="${cloudPlatform}"
export START_LABEL=${platformDiskStartLabel}
export PLATFORM_DISK_PREFIX=${platformDiskPrefix}
export LAZY_FORMAT_DISK_LIMIT=12
export IS_GATEWAY=${gateway?c}
export TMP_SSH_KEY="${tmpSshKey}"
export SSH_USER=${sshUser}
export SALT_BOOT_PASSWORD=${saltBootPassword}
export SALT_BOOT_SIGN_KEY=${signaturePublicKey}
export CB_CERT=${cbCert}

${customUserData}

curl -Lo /opt/salt-bootstrap https://www.dropbox.com/s/xl3b1pitebryep2/salt-bootstrap?dl=0
chmod +x /opt/salt-bootstrap
systemctl stop salt-bootstrap
mv /opt/salt-bootstrap $(which salt-bootstrap)
systemctl start salt-bootstrap

/usr/bin/user-data-helper.sh "$@" &> /var/log/user-data.log