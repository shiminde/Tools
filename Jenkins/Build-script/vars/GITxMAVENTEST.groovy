def call(giturl,Branch,Dir,PRESTEPS,SonarOpen,SONARCLASS,UNITTEST,urlfile,profile,command,UNITTESTCOMMOND,POSTSTEPS,BuildStatus,recipients){
    try {
    stage('Checkout') {
        BuildStatus="OK"
        sh """
            set +x
            rm -f StartTime.py
            wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/StartTime.py
            python StartTime.py > ${urlfile}/${JOB_BASE_NAME}-SONAR.txt
            rm -f StartTime.py
        
        """
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
            
            #rm -f Check-ProfileID.sh
            #wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/Check-ProfileID.sh
            #sh Check-ProfileID.sh
            #rm -f Check-ProfileID.sh
            
            #rm -f Checkout-Pom-Version.sh
            #wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/Checkout-Pom-Version.sh  
            #sh Checkout-Pom-Version.sh
            #rm -f Checkout-Pom-Version.sh
            
            #echo "Checkout-Pom-Version Done"

            
            
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
            
            Dir="${Dir}"
            if [[ -z \$Dir ]];then
                    
                        echo "当前目录执行"
                    
                    else
                        cd \$Dir
                        echo "进入目录执行\$D"
            fi
            
            JOBNAME="${JOB_BASE_NAME}"
            systemname=`echo \$JOBNAME | awk -F'_' '{ print \$3 }'`
            if [[ "${SonarOpen}" == "true" ]];then
                echo "开始扫描"
                rm -f Check-Sonar-Inc.sh
                wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/Check-Sonar-Inc.sh
                sh Check-Sonar-Inc.sh
                rm -f Check-Sonar-Inc.sh
                rm -f SonarApi-reson.py
                wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/SonarApi-reson.py
                python SonarApi-reson.py \$systemname >> ${urlfile}/${JOB_BASE_NAME}-SONAR.txt
                rm -f SonarApi-reson.py
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
                            
                            if [[ "${SonarOpen}" == "true" ]];then
                                rm -f TransPackage.py
                                wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/TransPackage.py
                                python TransPackage.py \$systemname
                                rm -f TransPackage.py
                            fi

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
                                    
                                    if [[ "${SonarOpen}" == "true" ]];then
                                        rm -f TransPackage.py
                                        wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/TransPackage.py
                                        python TransPackage.py \$systemname
                                        rm -f TransPackage.py
                                    fi
                                    
                                    rm -f TransPackage.py
                                    wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/TransPackage.py
                                    python TransPackage.py \$systemname
                                    rm -f TransPackage.py
                                    
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
             
        }
        )
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
    catch (err) {
        BuildStatus="ERROR"
        echo 'I failed'
        sh "exit 1"
        
    }
    finally {
         
        sh """
         JOBNAME="${JOB_BASE_NAME}"
         jobfullname="${JOB_NAME}"
         projectgroup=`echo \$JOBNAME | awk -F'_' '{ print \$2 }'`
       
        
        
         if [[ "${SonarOpen}" == "true" ]];then
            set +x
            rm -f EndTime.py
            wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/EndTime.py
            python EndTime.py >> ${urlfile}/${JOB_BASE_NAME}-SONAR.txt
            rm -f EndTime.py
          
         
            RESON=`echo ${urlfile}/${JOB_BASE_NAME}-SONAR.txt`
            rm -f Email.py
            wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/Email.py  
            
            
            emailtouser=`cat ${urlfile}/EMAIL.txt | grep \$projectgroup | awk -F":" '{ print \$2 }'`
            echo \$emailtouser

            
            python Email.py ${JOB_BASE_NAME} \$RESON "${BUILD_URL}"console ${BuildStatus} ${Branch} \$emailtouser
            rm -f Email.py
         
         fi 
         
         """
    

    }
}

