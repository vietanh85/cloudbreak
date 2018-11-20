/opt/salt/mount-disks-initialize.sh:
  file.managed:
    - makedirs: True
    - source: salt://disk/mount-disks-initialize.sh
    - mode: 744

/opt/salt/mount-disks.sh:
  file.managed:
    - makedirs: True
    - source: salt://disk/mount-disks.sh
    - mode: 744

/opt/salt/find-device-and-format.sh:
  file.managed:
    - makedirs: True
    - source: salt://disk/find-device-and-format.sh
    - mode: 744

/opt/salt/format-and-mount-common.sh:
  file.managed:
    - makedirs: True
    - source: salt://disk/format-and-mount-common.sh
    - mode: 744
