#/bin/bash

if [ -z "$1" ]; then
    echo "Usage: $0 <version>"
    exit -1
fi

if [ ! -f access_token ]; then
    echo "Place your access token in a file called access_token"
    exit -2
fi

VERSION=$1
ACCESS_TOKEN=$(<access_token)

API_JSON=$(printf '{"tag_name": "v%s","target_commitish": "master","name": "v%s","body": "Release of version %s","draft": true,"prerelease": false}' $VERSION $VERSION $VERSION)
curl --data "$API_JSON" https://api.github.com/repos/vidstige/jadb/releases?access_token=$ACCESS_TOKEN
