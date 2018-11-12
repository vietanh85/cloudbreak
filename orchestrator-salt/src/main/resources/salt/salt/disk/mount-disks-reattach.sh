#!/usr/bin/env bash
source mount-disks-common.sh

get_expected_mount_point_for_uuid() {
    local uuid=$1
    local mount_point=$(grep $uuid $FSTAB_ORIGINAL | cut -f2 -d' ')
    echo $mount_point
}

remount_disks() {
  FS_TYPE=ext4
  local linux_flavor=$1
  mkdir /hadoopfs
  for (( i=1; i<=24; i++ )); do
    local label=$(get_next_disk_label $linux_flavor $i)
    local device=$(get_disk_name $linux_flavor $label)
    if [ -e $device ]; then
      local current_mount_point=$(grep $device /etc/fstab | tr -s ' \t' ' ' | cut -d' ' -f 2)
      echo current mount point: $current_mount_point
      if [ -n "$current_mount_point" ]; then
        umount "$current_mount_point"
        sed -i "\|^$device|d" /etc/fstab
      fi
      local uuid=$(blkid -o value $device | head -1)
      expected_mount_point=$(get_expected_mount_point_for_uuid $uuid)
      echo uuid: $uuid, expected mount point: $expected_mount_point
      # TODO make mount idempotent
      mkdir /hadoopfs/fs${i}
      echo UUID=$uuid $expected_mount_point $FS_TYPE  defaults,noatime,nofail 0 2 >> /etc/fstab
      mount $expected_mount_point
      chmod 777 /hadoopfs/fs${i}
    fi
  done
}


main() {
    echo fstab old: $FSTAB_ORIGINAL

    local script_name="mount-disk-reattach"
    if [ ! -f "$SEMAPHORE_FILE" ]; then
        echo "semaphore file missing, cannot proceed. Exiting"
        exit
    fi

    local linux_flavor=$FL_ANY
    if [ -e  $REDHAT_RELEASE_FILE ]; then
        linux_flavor=$FL_REDHAT
    fi

#    echo updated: $updated_platform_disk_prefix, $updated_platform_disk_postfix, $linux_flavor
#    script_executed=$(grep $script_name "$SEMAPHORE_FILE")
#    if [ -z "$script_executed" ]; then
        [[ $CLOUD_PLATFORM == "AWS" ]] && remount_disks $linux_flavor
#        echo "$(date +%Y-%m-%d:%H:%M:%S) - $script_name executed" >> $SEMAPHORE_FILE
#    fi
}

[[ "$0" == "$BASH_SOURCE" ]] && main "$@"
