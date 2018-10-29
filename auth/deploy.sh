#!/bin/bash

set -eo pipefail

if [[ "$TRACE" ]]; then
    : ${START_TIME:=$(date +%s)}
    export START_TIME
    export PS4='+ [TRACE $BASH_SOURCE:$LINENO][ellapsed: $(( $(date +%s) -  $START_TIME ))] '
    set -x
fi

debug() {
  [[ "$DEBUG" ]] && echo "-----> $*" 1>&2 || :
}

new_version() {
  if [[ ! $VERSION == 2* ]] ; then
    git checkout $VERSION
    debug "building docker image for version: $VERSION"

    # Build docker and push to hortonworks repo
    docker build -t ${IMAGE}:${VERSION} .
    docker push ${IMAGE}:${VERSION}
    docker rmi ${IMAGE}:${VERSION}
  fi
  
}

main() {
  : ${VERSION:?"required!"}
  : ${DOCKERHUB_USERNAME:?"required!"}
  : ${DOCKERHUB_PASSWORD:?"required!"}
  : ${DEBUG:=1}

  new_version "$@"
}

[[ "$0" ==  "$BASH_SOURCE" ]] && main "$@"