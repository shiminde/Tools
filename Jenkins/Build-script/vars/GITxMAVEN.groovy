def call(giturl,Branch,Dir,PRESTEPS,SonarOpen,SONARCLASS,UNITTEST,urlfile,profile,command,UNITTESTCOMMOND,POSTSTEPS,recipients){
    try {
    stage('Checkout') {
        checkout([$class: 'GitSCM', branches: [[name: "${Branch}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CloneOption', depth: 0, noTags: true, reference: '', shallow: false]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '22ea7f37-b7ac-40ad-ae65-4566e55586f3', url: "${giturl}"]]])
         
        
    }
    
    
    
    stage('Pre-steps'){
        echo "1"
        
        sh """
            set +x
            Dir="${Dir}"
            if [[ -z \$Dir ]];then
                    
                        echo "当前目录执行"
                    
                    else
                        cd \$Dir
                        echo "进入目录执行\$D"
            fi
            
            echo "start run Presteps"
            
            echo "${PRESTEPS}"
            ${PRESTEPS}
            
            echo "Presteps Down"
            
            rm -f Check-ProfileID.sh
            wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/Check-ProfileID.sh
            sh Check-ProfileID.sh
            
            rm -f Checkout-Pom-Version.sh
            wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/Checkout-Pom-Version.sh  
            sh Checkout-Pom-Version.sh
            echo "Checkout-Pom-Version Done"

            
            
            if [[ "${UNITTEST}" == "true" ]];then 
                echo "star Unit Test"
                echo "${UNITTESTCOMMOND}"
                ${UNITTESTCOMMOND}
                echo "Unit Test Done"
            fi

            
            
        """
    }
    
    parallel (
        Analysis: {
            stage('Sonar') {
                   sh """
                   
            
            set +x
            JOBNAME="${JOB_BASE_NAME}"
            systemname=`echo \$JOBNAME | awk -F'_' '{ print \$3 }'`
            
            Dir="${Dir}"
            if [[ -z \$Dir ]];then
                    
                    echo "当前目录执行"
                    
            else
                    cd \$Dir
                    echo "进入目录执行\$D"
            fi
            
            echo "${SonarOpen}"
            if [[ "${SonarOpen}" == "true" ]];then
                
                rm -rf sonar-project.properties
                wget -q http://172.17.33.199/build/common-tools/sonar/sonar-project.properties
                
                projectKey=`cat sonar-project.properties | grep sonar.projectKey`
                sed -i "s/\$projectKey/sonar.projectKey=\$systemname/g" sonar-project.properties


                projectName=`cat sonar-project.properties | grep sonar.projectName`
                sed -i "s/\$projectName/sonar.projectKey=\$systemname/g" sonar-project.properties

                
            
                panduan=`ls | grep HASH.txt` || echo "1"
                echo "111"
                if [[ \$panduan == "" ]];then
                    touch HASH.txt
                fi
        
                PREHASH=`cat HASH.txt`
                gitHASH=`git rev-list HEAD -n 1`
                HASH=`echo \${gitHASH:0:8}`
        
                echo "${SONARCLASS}"
                
                if [[ "${SONARCLASS}" == "FULL" ]];then
            
                    Version=`cat sonar-project.properties | grep sonar.projectVersion`
                    sed -i "s/\$Version/sonar.projectVersion=\$HASH/g" sonar-project.properties
                    sonar-scanner
                    echo \$HASH > HASH.txt
        
                else
        
        
                    rm -rf sonarbase &&  mkdir sonarbase
                    
                    echo "1111111111111111111111111111"
                    echo "\$PREHASH"
                        
                    if [[ \$PREHASH == "" ]];then
                        echo "第一次增量扫描不执行，下次生效"
                        echo \$HASH > HASH.txt
                        sonar-scanner
            
                    else
            
                        FILELIST=`git diff \$HASH \$PREHASH --name-only`
                        for i in \$FILELIST
                        do
                            mv \$i sonarbase/ || echo "1"
        
                        done
        
                        Version=`cat sonar-project.properties | grep sonar.projectVersion`
                        sed -i "s/\$Version/sonar.projectVersion=\$HASH/g" sonar-project.properties
                        /bin/cp -f sonar-project.properties sonarbase/
                        cd sonarbase&&sonar-scanner
                        cd -
                        echo \$HASH > HASH.txt
        
                    fi
                fi        
            else
                
                echo "Skip Sonar"
            
            fi
        
        """


            }
        },
        Build: {
            // 执行构建
            stage('Build') {
                    sh """
            
                    JOBNAME="${JOB_BASE_NAME}"
                    date
                    set +x
                    jobfullname="${JOB_NAME}"
  
   

                    cat /dev/null > ${urlfile}/${JOB_BASE_NAME}.txt && echo '1'
                    systemname=`echo \$JOBNAME | awk -F'_' '{ print \$3 }'`
                    MORD=`echo \$JOBNAME | awk -F'_' '{ print \$1 }'`
      
					
						
					if [[ \$MORD == "M" ]];then
						deployDstHost="${M_REPO_IP}"
						deployDstStartPath="${M_REPO_PATH}"
						scpdir="${M_SCP_DIR}"
				    else
						deployDstHost="${D_REPO_IP}"
						deployDstStartPath="${D_REPO_PATH}"
						scpdir="${D_SCP_DIR}"
				    fi
    
					echo \$deployDstHost
					echo \$scpdir
					pwd
                    
                    echo `date`

                    gitHASH=`git rev-list HEAD -n 1`
                    HASH=`echo \${gitHASH:0:8}`
                
                    echo \$HASH
                    
                    		
                    Dir="${Dir}"
                
                    if [[ -z \$Dir ]];then
                    
                        echo "当前目录执行"
                    
                    else
                        cd \$Dir
                        echo "进入目录执行\$D"
                    fi
                    

                    pwd
                    
                    
                    find | xargs chmod -R 755 || echo ""
                   
                    #打包前修改权限
                    
                    if [[ "${profile}" == ""  ]];then
                       
                           
                            chmod -R 755 .
                            java -version
                            echo "${command}"
                            ${command}
                            time=`date +"%Y-%m-%d_%H-%M-%S"`
                            
                            set +x
                            
                            dirname=`echo ${JOB_NAME} | awk -F'/' '{ print \$1 }'`
                            des_url="http://\$deployDstHost/build/\$dirname/\$systemname/${JOB_BASE_NAME}/\$HASH-${BUILD_ID}-\$time"
                            
                            scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$HASH-${BUILD_ID}-\$time"
                            echo \$des_url
                            echo \$scp_url
                            
                            list=`find . -name "target"`
                            ssh \$deployDstHost mkdir -p \$scp_url
                            
                            echo `date`
                            for i in \$list
                            do
                                #echo \$i
                                pack_dir=`dirname \${i:2}`
                                cd \$i
                                package=`ls | egrep '*.war\$|*.jar\$|*.tar.gz\$|*.zip\$' || echo `
                                
                                echo ``
                                for x in \$package
                                do
                                    if [ -n \$x  ];then
                                        echo \$x
                                        url="\${i}/\${x}"
                                        echo \$url
                                        md5=`md5sum \$x | awk '{print \$1}'`
                                        #echo \$md5
                                        scp -p \$x  \$deployDstHost:\$scp_url
                                    
                                        echo "\$des_url/\${x}&\$md5" >> ${urlfile}/${JOB_BASE_NAME}.txt
                                    fi
                                done
                                
                                
                                pwd
                                cd -
                                pwd
                                
                                
                                
                            done
                            ssh \$deployDstHost chmod -R 755 \$scpdir
                             
                            echo `date`
                    else
                            set +x
                            for p in "${profile}"
                            do
                                chmod -R 755 .
                                java -version
                             
                                time=`date +"%Y-%m-%d_%H-%M-%S"`
                                
                                systemname=`echo \${JOBNAME} | awk -F'_' '{ print \$3 }'`
                                dirname=`echo ${JOB_NAME} | awk -F'/' '{ print \$1 }'`
                                
                                
                                
                                for i in \$p
                                do
                                    des_url="http://\$deployDstHost/build/\$dirname/\$systemname/${JOB_BASE_NAME}/\$i/\$HASH-${BUILD_ID}-\$time"
                                    scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$i/\$HASH-${BUILD_ID}-\$time"
                                    
                                    echo "-------------------------------------------------------------------------------------------"
                                    echo "start \$i"
                                    echo "${command} -P\$i"
                                    ${command} -P\$i
                                    list=`find . -name "target"`
                                    ssh \$deployDstHost mkdir -p \$scp_url
                                    echo `date`
                                    for l in \$list
                                    do
                                        #echo \$l
                                        pack_dir=`dirname \${l:2}`
                                        cd \$l > /dev/null
                                        package=`ls | egrep '*.war\$|*.jar\$|*.tar.gz\$|*.zip\$' || echo `
                                        
                                            for x in \$package
                                            do
                                                if [ -n \$x  ];then
                                                    #echo \$package
                                                    echo \$x
                                                    url="\${l}/\${x}"
                                                    echo \$url
                                                    md5=`md5sum \$x | awk '{print \$1}'`
                                                    #echo \$md5
                                                    scp -p \$x  \$deployDstHost:\$scp_url
                                    
                                                    echo "\$des_url/\${x}&\$md5" >> ${urlfile}/${JOB_BASE_NAME}.txt
                                                fi
                                            done
                                            pwd
                                        
                                    cd - > /dev/null
                                    
                                    echo `date`
                                    done
                                    
                                done
                                
                        
                            done
                            ssh \$deployDstHost chmod -R 755 \$scpdir
                    fi
                        

                    

                    """

               
                }
                
            stage('Post-steps'){
                    
                    sh """
                    
                    set +x
                    
                    Dir="${Dir}"
                    if [[ -z \$Dir ]];then
                    
                        echo "当前目录执行"
                    
                    else
                        cd \$Dir
                        echo "进入目录执行\$D"
                    fi
                    
                    echo "start run Poststeps"
                    
                    echo "${POSTSTEPS}"
                    ${POSTSTEPS}
            
                    echo "Poststeps Down"
     
                    """
     
                }    
            stage('packagelist'){
                    sh """
                    set +x
                    jobfullname="${JOB_NAME}"
                    
 
                    
                    echo "${JENKINS_NODE}"
                    
                    if [ "${JENKINS_NODE}" != "master" ];then
                        scp ${urlfile}/${JOB_BASE_NAME}.txt ${JENKINS_MASTER_IP}:${urlfile}/${JOB_BASE_NAME}.txt
                    
                    else
                        echo "This is Master"
                    fi

                    allnum=`cat ${urlfile}/${JOB_BASE_NAME}.txt | wc -l`
                    echo "一共有\$allnum个包"
                    LIST=`cat ${urlfile}/${JOB_BASE_NAME}.txt`
                                for b in \$LIST
                                do
                                    url=`echo \$b    | awk -F"&" '{print \$1}'`
                                    md5=`echo \$b    | awk -F"&" '{print \$2}'`
                                    code=`curl -I -m 10 -o /dev/null -s -w %{http_code} \$url`
                                    if [ \$code != 200 ];then
                                        echo "--------------------------faield----------------------------"
                                    fi
                                    
                                    echo "URL:\$url"
                                    echo "状态码: \$code "
                                    echo "MD5: \$md5" 
                                done
                    
         """

                    
                    
                }
                
            
            }
        
    )
    }
    catch (err) {
        echo 'I failed'
        sh "exit 1"
    }
    finally {
    emailext body: '''(本邮件是程序自动下发的，请勿回复！)<br/><hr/>

                项目名称：$PROJECT_NAME<br/><hr/>

                构建编号：$BUILD_NUMBER<br/><hr/>

                构建状态：done<br/><hr/>

                触发原因：${CAUSE}<br/><hr/>

                构建日志地址：<a href="${BUILD_URL}console">${BUILD_URL}console</a><br/><hr/>

                构建地址：<a href="$BUILD_URL">$BUILD_URL</a><br/><hr/>

                变更集:${JELLY_SCRIPT,template="html"}<br/><hr/>''', mimeType: 'text/html',subject: '$PROJECT_NAME', to: "${recipients}"

    }   
}

