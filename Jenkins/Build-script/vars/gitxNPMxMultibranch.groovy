def call(Dir,SonarOpen,urlfile,profile,UploadPs,command,recipients){
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


                    
                    cat /dev/null > ${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt
                    
                    echo "${profile}"
                    
					systemname=`echo \$PROJECTNAME | awk -F'_' '{ print \$3 }'`
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
                    pwd
                    
                    Dir="${Dir}"
                
                    if [[ -z \$Dir ]];then
                    
                        echo "当前目录执行"
                        Dir='.'
                    fi
                    
                    IFS=","
                    OLD_IFS="\$IFS"
                    Dir=(\$Dir)
                    IFS="\$OLD_IFS"
                    
                    for D in \${Dir[@]}
                    do

                    
                        cd \$D
                        #如果是CD到目录里面执行构建,则包名为目录名
                        if [[ \$D != "." ]];then
                            systemname=\$D
                        fi
                    
                        echo "当前的目录为\$D"
                
                        echo "npm install --unsafe-perm=true --allow-root"
                        npm install --unsafe-perm=true --allow-root
                        #npm update
                        profile="${profile}"
                        IFS=","
                        OLD_IFS="\$IFS"
                        arr=(\$profile)
                        IFS="\$OLD_IFS"
                
                        for p in \${arr[@]}

                        do
                            p=`echo \$p | sed s/\\\'//g`
                            echo "start \$p"
                    
                            if [ \$systemname != "" ];then
                                echo "删除上次的包 ,\$systenname"
                                rm -f \$systemname.tar.gz
                                rm -rf \$systemname
                                echo "删除的包和目录为"
                                echo \$systemname.tar.gz
                                echo \$systemname
                                echo "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                                
                                
                            fi
                            echo "npm run \$p"
                            eval npm run \$p
                            
                            if [[ "${UploadPs}" == "True" ]];then
                                echo "-----------------------------开始上传私服--------------------------------"
                                ${NPM_PUBLISH_COMMAND}
                            fi
                            

                            if [[ \$D == "." ]];then
                                echo "将dist目录修改为服务名"
                                D=\$systemname
                            fi
                            
                            echo "变量D为"
                            echo "\$D"
                            echo "..............."

                            
                            cp -R dist \$D
                            
                            
                            find | xargs chmod 755 || echo ""
                           
                            #打包前修改权限

                            tar -zcf \$systemname.tar.gz  \$D
                            time=`date +"%Y-%m-%d_%H-%M-%S"`
                            
                            set +x
                            
                
                            dirname=`echo ${JOB_NAME} | awk -F'/' '{ print \$1 }'`
           
                            if [ \$p == "build" ];then
                                des_url="http://\$deployDstHost/build/\$dirname/\$systemname/${JOB_BASE_NAME}/\$HASH-${BUILD_ID}-\$time"
                                scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$HASH-${BUILD_ID}-\$time"
                    
                            elif [[ \$p =~ build: ]];then
                    
                                p=`echo \$p | awk -F":" '{print \$2}'` 
                                des_url="http://\$deployDstHost/build/\$dirname/\$systemname/${JOB_BASE_NAME}/\$p/\$HASH-${BUILD_ID}-\$time"
                                scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$p/\$HASH-${BUILD_ID}-\$time"
                        
                            elif [[ \$p =~ "build --" ]];then
                                p=`echo \$p | awk -F"--" '{print \$2}'`
                                des_url="http://\$deployDstHost/build/\$dirname/\$systemname/${JOB_BASE_NAME}/\$p/\$HASH-${BUILD_ID}-\$time"
                                scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$p/\$HASH-${BUILD_ID}-\$time"
                        
                            else
                                des_url="http://\$deployDstHost/build/\$dirname/\$systemname/${JOB_BASE_NAME}/\$p/\$HASH-${BUILD_ID}-\$time"
                                scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$p/\$HASH-${BUILD_ID}-\$time"
                    
                            fi

                            echo \$des_url
                            echo \$scp_url
                
                            ssh \$deployDstHost mkdir -p \$scp_url
                            package=`ls | egrep '*.tar.gz'`
                            md5=`md5sum \$package | awk '{print \$1}'`
                       
                            pwd
                            scp -p \$package  \$deployDstHost:\$scp_url
                            echo "\$des_url/\$package&\$md5" >> ${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt
                            ssh \$deployDstHost chmod -R 755 \$scpdir
                        done
                        cd -
                    done


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
