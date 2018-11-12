#!/usr/bin/env bash
source mount-disks-common.sh

main() {
    echo "$(date +%Y-%m-%d:%H:%M:%S) - semaphore file created" >> $SEMAPHORE_FILE
}

[[ "$0" == "$BASH_SOURCE" ]] && main "$@"