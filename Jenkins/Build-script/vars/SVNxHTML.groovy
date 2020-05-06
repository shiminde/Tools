def call(repourl,SonarOpen,urlfile,profile,command,recipients){
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
                    set +x
                    find | xargs chmod -R 755
                    
                    JOBNAME="${JOB_BASE_NAME}"
                    cat /dev/null > ${urlfile}/${JOB_BASE_NAME}.txt
                    date
                    
                    
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
					
                    
                    echo `date`
                    
                    
                    dirname=`echo ${JOB_NAME} | awk -F'/' '{ print \$1 }'`
                    svnnum=`svn info | grep "Last Changed Rev" | awk -F" " '{ print \$4 }'`
                    
                    
                    if [ \$systemname != "" ];
                        then
                        echo "删除上次的包 ,\$systenname"
                        rm -f \$systemname.tar.gz
                    fi
                    
                   
                    
                    ${command} \$systemname.tar.gz *
                    
                    find | xargs chmod -R 755
                    
                    time=`date +"%Y-%m-%d_%H-%M-%S"`
                            
                    set +x
         
                    des_url="http://\$deployDstHost/build/\$dirname/\$systemname/${JOB_BASE_NAME}/\$svnnum-${BUILD_ID}-\$time"
                    scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$svnnum-${BUILD_ID}-\$time"
                    echo \$des_url
                    echo \$scp_url
                
                    ssh \$deployDstHost mkdir -p \$scp_url
                    package=`ls | egrep '*.tar.gz'`
                    md5=`md5sum \$package | awk '{print \$1}'`
                    #echo \$md5
                    pwd
                    scp -p \$package  \$deployDstHost:\$scp_url
                    echo "\$des_url/\${package}&\$md5" >> ${urlfile}/${JOB_BASE_NAME}.txt
                    ssh \$deployDstHost chmod -R 755 \$scpdir
                    
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
    stage('EMAIL'){
          emailext body: '''(本邮件是程序自动下发的，请勿回复！)<br/><hr/>

                项目名称：$PROJECT_NAME<br/><hr/>

                构建编号：$BUILD_NUMBER<br/><hr/>

                构建状态：SUCCESS<br/><hr/>

                触发原因：${CAUSE}<br/><hr/>

                构建日志地址：<a href="${BUILD_URL}console">${BUILD_URL}console</a><br/><hr/>

                构建地址：<a href="$BUILD_URL">$BUILD_URL</a><br/><hr/>

                变更集:${JELLY_SCRIPT,template="html"}<br/><hr/>''', mimeType: 'text/html',subject: '$PROJECT_NAME', to: "${recipients}"

               
        }





}
