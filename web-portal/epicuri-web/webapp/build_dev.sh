#!/bin/bash

set -x

rm -rf node_modules
rm -f dist/build.js
rm -f dist/build.js.map

npm install
npm run build

echo "back up old files"
ssh -p 2020 epicuriadm@api.dev.epicuri.co.uk 'cd apps/webapp; rm -rf dist.old; [ -f dist ] && mv dist dist.old; mv index.html index.html.old'

echo "copy new files over"
scp -r -P 2020 dist index.html epicuriadm@api.dev.epicuri.co.uk:/home/epicuriadm/apps/webapp
