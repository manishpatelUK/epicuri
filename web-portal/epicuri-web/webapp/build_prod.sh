#!/bin/bash

rm -rf node_modules
rm -f dist/build.js
rm -f dist/build.js.map

npm install
npm run build

sed -i 's/http:\/\/api-dev.epicuri.co.uk/https:\/\/api-prod.epicuri.co.uk/g' dist/build.js
sed -i 's/http:\/\/portal.dev.epicuri.co.uk/https:\/\/portal.epicuri.co.uk/g' redirect.html

scp -r -P 2020 dist favicon.ico index.html redirect.html epicuriadm@epicuri_prod2.epicuri.co.uk:/home/epicuriadm/apps/webapp
