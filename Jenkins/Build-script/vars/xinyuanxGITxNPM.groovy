def call(giturl,Branch,Dir,SonarOpen,urlfile,profile,command,recipients){
    try {
    stage('Checkout') {
            checkout([$class: 'GitSCM', branches: [[name: "${Branch}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CloneOption', depth: 0, noTags: true, reference: '', shallow: false]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '22ea7f37-b7ac-40ad-ae65-4566e55586f3', url: "${giturl}"]]])
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
                
                cat /dev/null > ${urlfile}/${JOB_BASE_NAME}.txt && echo '1'
                systemname=`echo \$JOBNAME | awk -F'_' '{ print \$3 }'`
                MORD=`echo \$JOBNAME | awk -F'_' '{ print \$1 }'`
				
				gitHASH=`git rev-list HEAD -n 1`
                HASH=`echo \${gitHASH:0:8}`
                
                pwd
                if [[ -n "$Dir" ]];then
                        cd "${Dir}"
                        pwd
                
                fi


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
				
				
                echo "npm install --unsafe-perm=true --allow-root"
                npm install --unsafe-perm=true --allow-root
                
                profile="${profile}"
                
                IFS=","
                OLD_IFS="\$IFS"
                arr=(\$profile)
                IFS="\$OLD_IFS"
                
                echo "aaaaaa"
                
                for p in \${arr[@]}
                do
                    echo "vvvvvvvvvvvvvvvvvvvvvv"
                    p=`echo \$p | sed s/\\\'//g`
                    echo "start \$p"
                    
                    if [ \$systemname != "" ];then
                        echo "删除上次的包 ,\$systenname"
                        rm -f \$systemname.tar.gz
                    fi
                    
                    echo "npm run \$p"
                    #npm run \$p
                    eval npm run \$p
                    
                    find | xargs chmod -R 755 || echo ""
                    
                    tar -zcf \$systemname.tar.gz -C dist/ .
                    time=`date +"%Y-%m-%d_%H-%M-%S"`
                            
                    set +x
                            
                
                    dirname=`echo ${JOB_NAME} | awk -F'/' '{ print \$1 }'`
                    
                    if [ \$p == "build" ];then
                        
                        des_url="http://\$deployDstHost/build/\$dirbranch/\$dirname/\$systemname/${JOB_BASE_NAME}/\$HASH-${BUILD_ID}-\$time"
                        scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$HASH-${BUILD_ID}-\$time"
                    
                    elif [[ \$p =~ build: ]];then
                    
                        p=`echo \$p | awk -F":" '{print \$2}'` 
                        des_url="http://\$deployDstHost/build/\$dirbranch/\$dirname/\$systemname/${JOB_BASE_NAME}/\$p/\$HASH-${BUILD_ID}-\$time"
                        scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$p/\$HASH-${BUILD_ID}-\$time"
                    
                    elif [[ \$p =~ "build --" ]];then
                        p=`echo \$p | awk -F"--" '{print \$2}'`
                        des_url="http://\$deployDstHost/build/\$dirbranch/\$dirname/\$systemname/${JOB_BASE_NAME}/\$p/\$HASH-${BUILD_ID}-\$time"
                        scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$p/\$HASH-${BUILD_ID}-\$time"

                    
                    else
                        des_url="http://\$deployDstHost/build/\$dirbranch/\$dirname/\$systemname/${JOB_BASE_NAME}/\$p/\$HASH-${BUILD_ID}-\$time"
                        scp_url="\$scpdir/\$dirname/\$systemname/${JOB_BASE_NAME}/\$p/\$HASH-${BUILD_ID}-\$time"
                    
                    fi
                    
                    
                    echo \$des_url
                    echo \$scp_url
                
                    mkdir -p \$scp_url
                    package=`ls | egrep '*.tar.gz'`
                    md5=`md5sum \$package | awk '{print \$1}'`
                   
                    pwd
                    cp \$package \$scp_url
                    echo "\$des_url/\${package}&\$md5" >> ${urlfile}/${JOB_BASE_NAME}.txt
                    chmod -R 755 \$scpdir
                done           
                """
                        }
                        
    stage('packagelist'){
                    sh """
                    set +x
                    jobfullname="${JOB_NAME}"
                    
                    rsync -avz ${urlfile}/${JOB_BASE_NAME}.txt 1.203.80.13:${urlfile}/${JOB_BASE_NAME}.txt
                    
                    DIRNAME=`echo \$jobfullname |awk -F"/" '{ print \$1}'`
                    PROJECTNAME=`echo \$jobfullname |awk -F"/" '{ print \$2}'`
                    BRANCHNAME=`echo \$jobfullname |awk -F"/" '{ print \$3}'`
                    
                    
                    
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
