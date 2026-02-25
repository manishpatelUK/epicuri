if [ ! "$#" == 2 ]; then
	echo "Usage: ./server-release.sh <v1> <v2>"
	echo "e.g. ./server-release.sh 1.1 1.2"
	exit 1;
fi

PWD=`dirname $(readlink -f $0)`
cd $PWD/server-api

echo checkout new branch releases/server/$1
git checkout -b releases/server/$1
echo update version for release
mvn versions:set -DnewVersion=$1
echo commit and push
git commit -a -m "new release: $1"
git push --set-upstream origin releases/server/$1
echo update version for server-develop to ${2}-SNAPSHOT
git checkout server-develop
mvn versions:set -DnewVersion=${2}-SNAPSHOT
git commit -a -m "update version number to ${2}-SNAPSHOT in server-develop"
git push

#rm */pom.xml.versionsBackup
#rm pom.xml.versionsBackup
