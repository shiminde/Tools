#!/bin/bash
#cd /jenkinsdata/data/SonarFullScanner
REPOURL=`cat /opt/QA/sonar/ScannerRepoUrl.txt`
TIME=`date +%Y%m%d`
SONARQUBEURL="http://172.17.1.1:9000/dashboard?id="

date
pwd

rm -rf CheckDir && mkdir CheckDir && cd CheckDir

echo "当前目录是"
pwd
for i in $REPOURL
do
	echo "------------------------------------------"
	echo $i
	echo "------------------------------------------"
	PROJECTGROUP=`echo $i | awk -F"/" '{ print $4}'`
	echo $PROJECTGROUP
	
	DINGWEBHOOK=`eval echo '$'"${PROJECTGROUP}"`	
	echo $DINGWEBHOOK
	

	DIRNAME=`basename $i | awk -F'.' '{ print $1 }'`
	echo $DIRNAME
	echo "Start $DIRNAME"
	URL=`echo $i | awk -F'#' '{ print $1 }'`
	BRANCH=`echo $i | awk -F'#' '{ print $2 }'`
	echo $URL","$BRANCH
	pwd
	/usr/bin/git clone $URL -b $BRANCH
	commandaway=`echo $?`
	if [[ $commandaway == 0 ]];then
		pwd
		cd $DIRNAME
		pwd
		rm -rf sonar-project.properties
		wget http://172.17.1.1/build/common-tools/sonar/sonar-project.properties
	
        
       		gitHASH=`/usr/bin/git rev-list HEAD -n 1`
        	HASH=`echo ${gitHASH:0:8}`


        	projectKey=`cat sonar-project.properties | grep sonar.projectKey`
        	sed -i "s/$projectKey/sonar.projectKey=$DIRNAME-FULL/g" sonar-project.properties


        	projectName=`cat sonar-project.properties | grep sonar.projectName`
        	sed -i "s/$projectName/sonar.projectKey=$DIRNAME-FULL/g" sonar-project.properties
        
		Version=`cat sonar-project.properties | grep sonar.projectVersion`
        	sed -i "s/$Version/sonar.projectVersion=$TIME-$HASH/g" sonar-project.properties
               
		date
		echo "start scanner"
		/usr/sbin/sonar-scanner > /dev/null
		date
		echo "sonar-scanner Done"
        	curl -s -X POST  -u 'admin:123' http://172.17.1.1:9000/api/users/create -d "login=$PROJECTGROUP&name=$PROJECTGROUP&password=$PROJECTGROUP"
		curl -s -X POST  -u 'admin:123' http://172.17.1.1:9000/api/permissions/add_user -d "login=$PROJECTGROUP&permission=user&projectKey=$DIRNAME-FULL"
		curl -s -X POST  -u 'admin:123' http://172.17.1.1:9000/api/permissions/add_user -d "login=$PROJECTGROUP&permission=codeviewer&projectKey=$DIRNAME-FULL"
#	curl "$DINGWEBHOOK" \
#   -H 'Content-Type: application/json' \
#   -d '{
#         "msgtype": "link", 
#    "link": {
#        "text": "SONARQUBE地址点击此处", 
#        "title": "'${DIRNAME}'服务扫描完成", 
#        "picUrl": "", 
#        "messageUrl": "'${SONARQUBEURL}${DIRNAME}-FULL'"
#    }
#      }'




		cd ..
		pwd
	fi

done
