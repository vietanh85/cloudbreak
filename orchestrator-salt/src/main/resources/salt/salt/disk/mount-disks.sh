#!/usr/bin/env bash
source format-and-mount-common.sh

LOG_FILE=/var/log/mount-disks.log

# expected lists
# ATTACHED_VOLUME_UUID_LIST - contains a list of uuids of volumes attached to the instance
# PREVIOUS_FSTAB - contains the fstab if any from a previous instance

mount_all_from_fstab() {
      local return_value=0
      log $LOG_FILE mounting via fstab, value: "$PREVIOUS_FSTAB"
      for uuid in $ATTACHED_VOLUME_UUID_LIST; do
          local fstab_line=$(echo "$PREVIOUS_FSTAB" | grep $uuid)
          mount_one "$fstab_line"
          return_value=$(($? || return_value ))
      done
      return $((return_value))
}

mount_all_sequential() {
      local return_value=0
      log $LOG_FILE mounting for first time, no previous fstab information present
      local counter=1
      for uuid in $ATTACHED_VOLUME_UUID_LIST; do
          mount_one "UUID=$uuid /hadoopfs/fs${counter} $FS_TYPE defaults,noatime,nofail 0 2"
          return_value=$(($? || return_value ))
          ((counter++))
      done

      return $return_value
}

mount_one() {
      local return_value=0
      local success=0
      local fstab_line=$1
      local path=$(echo $fstab_line | cut -d' ' -f2)

      log $LOG_FILE mounting to path $path, line in fstab: $fstab_line
      mkdir $path >> $LOG_FILE 2>&1
      echo $fstab_line >> /etc/fstab
      log $LOG_FILE result of editing fstab: $?
      mount $path >> $LOG_FILE 2>&1
      if [ ! $? -eq 0 ]; then
        log $LOG_FILE error mounting device on $path
        return_value=1
      fi
      log $LOG_FILE result of mounting $path: $?
      chmod 777 $path >> $LOG_FILE 2>&1
      return $((return_value))
}

mount_common() {
    local return_value
    mkdir /hadoopfs
    if [[ -z $PREVIOUS_FSTAB  ]]; then
        mount_all_sequential
        return_value=$?
    else
        mount_all_from_fstab
        return_value=$?
    fi
    cd /hadoopfs/fs1 && mkdir logs logs/ambari-server logs/ambari-agent logs/consul-watch logs/kerberos >> $LOG_FILE 2>&1
    return_value=$(($? || return_value ))
    return $((return_value))
}

save_env_vars_to_log_file() {
    log $LOG_FILE environment variables:
    log $LOG_FILE ATTACHED_VOLUME_UUID_LIST=$ATTACHED_VOLUME_UUID_LIST
    log $LOG_FILE CLOUD_PLATFORM=$CLOUD_PLATFORM
    log $LOG_FILE PREVIOUS_FSTAB=$PREVIOUS_FSTAB
}

main() {
    local script_name="mount-disk"
    save_env_vars_to_log_file
    can_start $script_name $LOG_FILE
    mount_common
    return_code=$?
    exit_with_code $LOG_FILE $return_code "Script $script_name ended"
}

[[ "$0" == "$BASH_SOURCE" ]] && main "$@"
