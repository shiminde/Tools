def call(SonarOpen,urlfile,profile,command,recipients){
    try {
    stage('Checkout') {
        checkout scm
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
            
                    JOBNAME="${JOB_BASE_NAME}"
                    date
                    set +x
                    jobfullname="${JOB_NAME}"
                    
                    DIRNAME=`echo \$jobfullname |awk -F"/" '{ print \$1}'`
                    PROJECTNAME=`echo \$jobfullname |awk -F"/" '{ print \$2}'`
                    BRANCHNAME=`echo \$jobfullname |awk -F"/" '{ print \$3}'`

                    SYSTEMNAME=`echo \$PROJECTNAME | awk -F'_' '{ print \$3 }'`
                    
                    cat /dev/null > ${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt && echo '1'
                    
                    echo "${profile}"
			
					MORD=`echo \${JOBNAME} | awk -F'_' '{ print \$1 }'`
					
					echo "Branch is \$BRANCHNAME"
					
					if [[ \$BRANCHNAME == "master" ]];then
					        deployDstHost="${M_REPO_IP}"
					        scpdir="${M_SCP_DIR}"
					else
					        deployDstHost="${D_REPO_IP}"
					        scpdir="${D_SCP_DIR}"
				    fi
					
					echo \$deployDstHost
					echo \$scpdir
					
                    
                    echo `date`

                    gitHASH=`git rev-list HEAD -n 1`
                    HASH=`echo \${gitHASH:0:8}`
                
                    echo \$HASH
                    
                     if [[ "${profile}" == ""  ]];then
                       
                           
                            chmod -R 755 .
                            java -version
                            echo "${command}"
                            ${command}
                            time=`date +"%Y-%m-%d_%H-%M-%S"`
                            
                            set +x
                            
                            des_url="http://\$deployDstHost/build/\$DIRNAME/\$SYSTEMNAME/\$BRANCHNAME/\$HASH-${BUILD_ID}-\$time"
                            
                            scp_url="\$scpdir/\$DIRNAME/\$SYSTEMNAME/\$BRANCHNAME/\$HASH-${BUILD_ID}-\$time"
                            
                            echo "start list X"
                            echo "\$DIRNAME"
                            echo "\$SYSTEMNAME"
                            echo "\$BRANCHNAME"
                            echo "done"
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
                                    
                                        echo "\$des_url/\${x}&\$md5" >> ${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt
                                    fi
                                done
                            
                                pwd
                                cd -
                                ssh \$deployDstHost chmod -R 755 \$scpdir
                                
                            done
                            echo `date`
                    else
                            set +x
                            for p in "${profile}"
                            do
                                chmod -R 755 .
                                java -version
                             
                                time=`date +"%Y-%m-%d_%H-%M-%S"`
                                
                                
                                
                                for i in \$p
                                do
                                    des_url="http://\$deployDstHost/build/\$DIRNAME/\$SYSTEMNAME/\$BRANCHNAME/\$i/\$HASH-${BUILD_ID}-\$time"
                                    scp_url="\$scpdir/\$DIRNAME/\$SYSTEMNAME/\$BRANCHNAME/\$i/\$HASH-${BUILD_ID}-\$time"
                                    echo "start list X"
                                    echo "\$DIRNAME"
                                    echo "\$SYSTEMNAME"
                                    echo "\$BRANCHNAME"
                                    echo "done"
                                    
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
                                    
                                                    echo "\$des_url/\${x}&\$md5" >> ${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt
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
                        scp ${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt ${JENKINS_MASTER_IP}:${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt
                    
                    else
                        echo "This is Master"
                    fi

                    jobfullname="${JOB_NAME}"
                    
                    
                    DIRNAME=`echo \$jobfullname |awk -F"/" '{ print \$1}'`
                    PROJECTNAME=`echo \$jobfullname |awk -F"/" '{ print \$2}'`
                    BRANCHNAME=`echo \$jobfullname |awk -F"/" '{ print \$3}'`
                    
                    allnum=`cat ${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt | wc -l`
                    echo "一共有\$allnum个包"
                    LIST=`cat ${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt`
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
