#!/bin/bash
JOBNAME=`basename $(pwd)`
IPADDRESS="172.17.1.1"
rm -rf sonar-project.properties
wget -q http://172.17.1.1/build/common-tools/sonar/sonar-project.properties

echo $1

if [[ $1  =~ "MULTI" ]];then
	echo "多分支情况"
	#systemname=`echo $JOBNAME | awk -F'_' '{ print $4 }'`
	systemname=`echo $1 | awk -F "_" '{ print $3 }' | awk -F "/" '{ print $1 }'`
	echo $systemname
	
	PROJECTNAME=`echo $1 | awk -F"/" '{ print $1 }'`
	BRANCHNAME=`echo $1 | awk -F"/" '{ print $3 }'`
	#PROJECTNAME=`echo $JOBNAME | awk -F'_' '{ print $3 }'`
	#BRANCHNAME=`echo $JOBNAME | awk -F "_" '{ print $5 }' | awk -F "-" '{ print $1 }'`
	echo $PROJECTNAME
	echo $BRANCHNAME
	systemname=${systemname}_${BRANCHNAME}
	projectKey=`cat sonar-project.properties | grep sonar.projectKey`
	sed -i "s/$projectKey/sonar.projectKey=${systemname}/g" sonar-project.properties
	projectName=`cat sonar-project.properties | grep sonar.projectName`
	sed -i "s/$projectName/sonar.projectKey=${systemname}/g" sonar-project.properties

else	
	echo "普通情况"
	systemname=`echo $JOBNAME | awk -F'_' '{ print $3 }'`
	echo $systemname

	PROJECTNAME=`echo $JOBNAME | awk -F'_' '{ print $2 }'`
	echo $PROJECTNAME
	projectKey=`cat sonar-project.properties | grep sonar.projectKey`
	sed -i "s/$projectKey/sonar.projectKey=$systemname/g" sonar-project.properties
	projectName=`cat sonar-project.properties | grep sonar.projectName`
	sed -i "s/$projectName/sonar.projectKey=$systemname/g" sonar-project.properties
fi







panduan=`ls | grep HASH.txt` || echo "1"
if [[ $panduan == "" ]];then
    touch /jenkinsdata/data/.jenkins/common-extended/$systemname-HASH.txt
fi

PREHASH=`cat /jenkinsdata/data/.jenkins/common-extended/$systemname-HASH.txt`
gitHASH=`git rev-list HEAD -n 1`
HASH=`echo ${gitHASH:0:8}`

echo "${SONARCLASS}"

if [[ "${SONARCLASS}" == "FULL" ]];then

    Version=`cat sonar-project.properties | grep sonar.projectVersion`
    sed -i "s/$Version/sonar.projectVersion=$HASH/g" sonar-project.properties
    sonar-scanner
    echo $HASH > /jenkinsdata/data/.jenkins/common-extended/$systemname-HASH.txt

else
    if [[ $HASH != $PREHASH ]];then
		
    	rm -rf sonarbase &&  mkdir sonarbase
    fi 
    echo "$PREHASH"
        
    if [[ $PREHASH == "" ]];then
        echo "第一次增量扫描不执行，下次生效"
	Version=`cat sonar-project.properties | grep sonar.projectVersion`
        sed -i "s/$Version/sonar.projectVersion=$HASH/g" sonar-project.properties
        /bin/cp -f sonar-project.properties sonarbase/
        cd sonarbase&&sonar-scanner
	cd -
        echo $HASH > /jenkinsdata/data/.jenkins/common-extended/$systemname-HASH.txt
	curl -s -X POST  -u 'admin:123' http://$IPADDRESS:9000/api/users/create -d "login=$PROJECTNAME&name=$PROJECTNAME&password=$PROJECTNAME"
        curl -s -X POST  -u 'admin:123' http://$IPADDRESS:9000/api/permissions/add_user -d "login=$PROJECTNAME&permission=user&projectKey=$systemname"
        curl -s -X POST  -u 'admin:123' http://$IPADDRESS:9000/api/permissions/add_user -d "login=$PROJECTNAME&permission=codeviewer&projectKey=$systemname"

    else
	
	echo "变量对比"
	echo $HASH
	echo $PREHASH
	echo "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
        FILELIST=`git diff $HASH $PREHASH --name-only`
        for i in $FILELIST
        do
            /bin/cp -r $i sonarbase/ || echo "1"

        done

        Version=`cat sonar-project.properties | grep sonar.projectVersion`
        sed -i "s/$Version/sonar.projectVersion=$HASH/g" sonar-project.properties
        /bin/cp -f sonar-project.properties sonarbase/
        cd sonarbase&&sonar-scanner

	curl -s -X POST  -u 'admin:123' http://$IPADDRESS:9000/api/users/create -d "login=$PROJECTNAME&name=$PROJECTNAME&password=$PROJECTNAME"
        curl -s -X POST  -u 'admin:123' http://$IPADDRESS:9000/api/permissions/add_user -d "login=$PROJECTNAME&permission=user&projectKey=$systemname"
        curl -s -X POST  -u 'admin:123' http://$IPADDRESS:9000/api/permissions/add_user -d "login=$PROJECTNAME&permission=codeviewer&projectKey=$systemname"

        cd -                         
        #echo $HASH > HASH.txt
        #HASH为本次构建最新的HASH,PREHASH为上次构建的HASH,如果上次构建的质量阀为ERROR,则不更新此文件

    fi
fi        
        
echo "本地扫描完成,等待返回结果"

RESON="1"
while [[ $RESON != "" ]]
do
	echo "开始循环"
	date
	sleep 10
	RESON=`curl -s -X POST  -u 'admin:123' http://$IPADDRESS:9000/api/ce/activity -d "component=$systemname&status=IN_PROGRESS,PENDING" | grep $systemname`
	#echo $RESON
done
echo "SONARQUBE扫描分析完成"
