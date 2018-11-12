#!/usr/bin/env bash

SEMAPHORE_FILE=/var/cb-mount-executed

FL_REDHAT="redhat"
FL_ANY="any"
REDHAT_RELEASE_FILE="/etc/redhat-release"

get_linux_flavor() {
    local linux_flavor=$FL_ANY
    if [ -e  $REDHAT_RELEASE_FILE ]; then
        linux_flavor=$FL_REDHAT
    fi
    echo $linux_flavor
}

get_next_disk_label() {
    local linux_flavor=$1
    local index=$2
    local label=$(printf "\x$(printf %x $((START_LABEL+i)))")
    if [ $linux_flavor == $FL_REDHAT ]; then
        label=$index
    fi
    echo $label
}

get_platform_prefix_for_linux_flavor () {
    local linux_flavor=$1
    local prefix=$PLATFORM_PREFIX
    if [ $linux_flavor == $FL_REDHAT ]; then
        prefix="nvme"
    fi
    echo $prefix
}

get_platform_postfix_for_linux_flavor () {
    local linux_flavor=$1
    local postfix=""
    if [ $linux_flavor == $FL_REDHAT ]; then
        postfix="n1"
    fi
    echo $postfix
}

get_disk_name() {
    local linux_flavor=$1
    local label=$2
    local updated_platform_disk_prefix=$(get_platform_prefix_for_linux_flavor $linux_flavor)
    local updated_platform_disk_postfix=$(get_platform_postfix_for_linux_flavor $linux_flavor)
    local device=/dev/${updated_platform_disk_prefix}${label}${updated_platform_disk_postfix}
    echo $device
}

