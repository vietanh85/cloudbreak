#!/usr/bin/env bash
source mount-disks-common.sh

format_disks() {
  lazy_format_disks $@
  cd /hadoopfs/fs1 && mkdir logs logs/ambari-server logs/ambari-agent logs/consul-watch logs/kerberos
}

lazy_format_disks() {
  FS_TYPE=ext4
  local linux_flavor=$1
  mkdir /hadoopfs
  for (( i=1; i<=24; i++ )); do
    local label=$(get_next_disk_label $linux_flavor $i)
    local device=$(get_disk_name $linux_flavor $label)
    if [ -e $device ]; then
      MOUNTPOINT=$(grep $device /etc/fstab | tr -s ' \t' ' ' | cut -d' ' -f 2)
      if [ -n "$MOUNTPOINT" ]; then
        umount "$MOUNTPOINT"
        sed -i "\|^$device|d" /etc/fstab
      fi
      if [ -z "$(blkid $device)" ]; then
          echo "formatting: $device"
          mkfs -E lazy_itable_init=1 -O uninit_bg -F -t $FS_TYPE $device
      fi
      # TODO make mount idempotent
      mkdir /hadoopfs/fs${i}
      echo UUID=$(blkid -o value $device | head -1) /hadoopfs/fs${i} $FS_TYPE  defaults,noatime,nofail 0 2 >> /etc/fstab
      mount /hadoopfs/fs${i}
      chmod 777 /hadoopfs/fs${i}
    fi
  done
}

main() {
    local script_name="mount-disk"
    if [ ! -f "$SEMAPHORE_FILE" ]; then
        echo "semaphore file $SEMAPHORE_FILE missing, cannot proceed. Exiting"
        exit
    fi

    local linux_flavor=$(get_linux_flavor)

    echo updated: $linux_flavor
    script_executed=$(grep $SCRIPT_NAME "$SEMAPHORE_FILE")
    if [ -z "$script_executed" ]; then
        [[ $CLOUD_PLATFORM == "AWS" ]] && format_disks $linux_flavor
        echo "$(date +%Y-%m-%d:%H:%M:%S) - $script_name executed" >> $SEMAPHORE_FILE
    fi
}

[[ "$0" == "$BASH_SOURCE" ]] && main "$@"