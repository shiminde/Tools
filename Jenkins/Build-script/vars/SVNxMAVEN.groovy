def call(repourl,SonarOpen,urlfile,profile,command,recipients){
    try {
    stage('Checkout') {
        checkout([$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: false, ignoreDirPropChanges: false, includedRegions: '', locations: [[credentialsId: 'd5506714-a3d2-4c71-8d8f-66f5a44a94d1', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: repourl]], workspaceUpdater: [$class: 'UpdateUpdater']])
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
            stage('Build') {
                sh """
                
                   
                    
                    JOBNAME="${JOB_BASE_NAME}"
                    cat /dev/null > ${urlfile}/${JOB_BASE_NAME}.txt
                    date
                    set +x
                    pomlist=`find . -name "pom.xml"`
                    profileID=""
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
					
                    profile="${profile}"
                    echo `date`
                    
                    
                    dirname=`echo ${JOB_NAME} | awk -F'/' '{ print \$1 }'`
                    svnnum=`svn info | grep "Last Changed Rev" | awk -F" " '{ print \$4 }'`
                    
                    
                    if [[ \$profile == ""  ]];then
                       
                            java -version
                            echo "${command}"
                            ${command}
                            time=`date +"%Y-%m-%d_%H-%M-%S"`
                            
                            find | xargs chmod -R 755 || echo ""
                            

                            set +x
                            
                            
                        
                            des_url="http://\$deployDstHost/build/\$dirname/\$systemname/${JOB_BASE_NAME}/\$svnnum-${BUILD_ID}-\$time"
                            scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$svnnum-${BUILD_ID}-\$time"
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
                                
                                
                            done
                            ssh \$deployDstHost chmod -R 755 \$scpdir
                            echo `date`
                    
                    else
                            for p in \$profile
                            do
                            
                                java -version
                                time=`date +"%Y-%m-%d_%H-%M-%S"`
                                #set +x
                               
                                
                                
                                
                                for i in \$p
                                do
                                    des_url="http://\$deployDstHost/build/\$dirname/\$systemname/${JOB_BASE_NAME}/\$i/\$svnnum-${BUILD_ID}-\$time"
                                    scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$i/\$svnnum-${BUILD_ID}-\$time"
                                    
                                    echo "-------------------------------------------------------------------------------------------"
                                    echo "start \$i"
                                    echo "${command} -P\$i"
                                    ${command} -P\$i
                                    find | xargs chmod -R 755 || echo ""
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
                                    ssh \$deployDstHost chmod -R 755 \$scpdir
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
