def call(giturl,SonarOpen,urlfile,profile,command,recipients){
    stage('Checkout') {
            checkout([$class: 'GitSCM', branches: [[name: '*/dev']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CloneOption', depth: 0, noTags: true, reference: '', shallow: false]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '22ea7f37-b7ac-40ad-ae65-4566e55586f3', url: giturl]]])
      }
    
    parallel (
        Analysis: {
            stage('Sonar') {
                    sh """
            set +x
            echo "${SonarOpen}"
            if [[ "${SonarOpen}" == "true" ]];then
                sonar-runner
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
                    set +x
                    JOBNAME="${JOB_BASE_NAME}"
                    cat /dev/null > ${urlfile}/${JOB_BASE_NAME}.txt
                    date
                    
					systemname=`echo \$JOBNAME | awk -F'_' '{ print \$3 }'`
					MORD=`echo \$JOBNAME | awk -F'_' '{ print \$1 }'`
					
					if [[ \$MORD == "M" ]];then
						
						deployDstHost="47.99.99.88"
						scpdir="${M_SCP_DIR}"
						dirbranch="master"
					else
						deployDstHost="47.99.99.88"
						scpdir="${D_SCP_DIR}"
						dirbranch="dev"
					fi
					
					echo \$deployDstHost
					echo \$scpdir
					
                    profile="${profile}"
                    echo `date`
                    gitHASH=`git rev-list HEAD -n 1`
                    HASH=`echo \${gitHASH:0:8}`
                    
                    if [[ \$profile == ""  ]];then
                       
                            #find . -type d|xargs chmod 755
                            #find . -type f|xargs chmod 644
                            chmod -R 755 .
                            java -version
                            ${command}
                            time=`date +"%Y-%m-%d_%H-%M-%S"`
                            
                            set +x
                            
                            
                            dirname=`echo ${JOB_NAME} | awk -F'/' '{ print \$1 }'`
                            des_url="http://\$deployDstHost/build/\$dirbranch/\$dirname/\$systemname/${JOB_BASE_NAME}/\$HASH-${BUILD_ID}-\$time"
                            scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$HASH-${BUILD_ID}-\$time"
                            echo \$des_url
                            echo "-----------------XXXXXXXXXXXXXX-------------"
                            echo \$scp_url
                            
                            
                            list=`find . -name "target"`
                            mkdir -p /\$scp_url
                            echo `date`
                            for i in \$list
                            do
                                #echo \$i
                                pack_dir=`dirname \${i:2}`
                                cd \$i
                                package=`ls | egrep '*.war\$|*.jar\$|*.tar.gz\$|*.zip\$' || echo `
                                
                                echo \$package
                                
                                for x in \$package
                                do
                                    if [ -n \$x  ];then
                                        echo \$x
                                        url="\${i}/\${x}"
                                        echo \$url
                                        md5=`md5sum \$x | awk '{print \$1}'`
                                        #echo \$md5
                                        cp  \$x  \$scp_url
                                    
                                        echo "\$des_url/\${x}&\$md5" >> ${urlfile}/${JOB_BASE_NAME}.txt
                                    fi
                                done
                            
                                pwd
                                cd -
                                chmod -R 755 \$scpdir
                                
                            done
                            echo `date`
                    
                    else
                            for p in \$profile
                            do
                                chmod -R 755 .
                                java -version
                             
                                time=`date +"%Y-%m-%d_%H-%M-%S"`
                                set +x
                                
                                dirname=`echo ${JOB_NAME} | awk -F'/' '{ print \$1 }'`
                                
                                
                                
                                for i in \$p
                                do
                                    des_url="http://\$deployDstHost/build/\$dirbranch/\$dirname/\$systemname/${JOB_BASE_NAME}/\$i/\$HASH-${BUILD_ID}-\$time"
                                    scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$i/\$HASH-${BUILD_ID}-\$time"
                                    
                                    echo "-------------------------------------------------------------------------------------------"
                                    echo "start \$i"
                                    echo "${command} -P\$i"
                                    ${command} -P\$i
                                    list=`find . -name "target"`
                                    mkdir -p \$scp_url
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
                                                    cp \$x  \$scp_url
                                    
                                                    echo "\$des_url/\${x}&\$md5" >> ${urlfile}/${JOB_BASE_NAME}.txt
                                                fi
                                            done
                                            pwd
                                        
                                    cd - > /dev/null
                                    chmod -R 755 \$scpdir
                                    echo `date`
                                    done
                                    
                                done
                                
                        
                            done            
                    fi
                """
                }
            
                stage('packagelist'){
                    sh """
                    set +x 
                    allnum=`cat ${urlfile}/${JOB_BASE_NAME}.txt | wc -l`
                    rsync -avz ${urlfile}/${JOB_BASE_NAME}.txt 1.203.80.13:${urlfile}/${JOB_BASE_NAME}.txt
                    echo "一共有\$allnum个包"
                    LIST=`cat ${urlfile}/${JOB_BASE_NAME}.txt`
                                for b in \$LIST
                                do
                                    url=`echo \$b    | awk -F"&" '{print \$1}'`
                                    md5=`echo \$b    | awk -F"&" '{print \$2}'`
                                   
                                    
                                    echo "URL:\$url"
                                    
                                    echo "MD5: \$md5" 
                                done
                    
                    """
                    
                    
                }
                

            
        }
        
    )
    stage('EMAIL'){
         emailext body: '''(本邮件是程序自动下发的，请勿回复！)<br/><hr/>

                项目名称：$PROJECT_NAME<br/><hr/>

                构建编号：$BUILD_NUMBER<br/><hr/>

                构建状态：SUCCESS<br/><hr/>

                触发原因：${CAUSE}<br/><hr/>

                构建日志地址：<a href="${BUILD_URL}console">${BUILD_URL}console</a><br/><hr/>

                构建地址：<a href="$BUILD_URL">$BUILD_URL</a><br/><hr/>

                变更集:${JELLY_SCRIPT,template="html"}<br/><hr/>''', mimeType: 'text/html',subject: '$PROJECT_NAME', to: recipients
      
               
        }
    
    
   
    
    


}