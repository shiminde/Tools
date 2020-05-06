def call(Dir,PRESTEPS,SonarOpen,SONARCLASS,UNITTEST,UNITTESTCOMMOND,urlfile,profile,command,POSTSTEPS,BuildStatus,recipients){
    try {
    stage('Checkout') {
        BuildStatus="OK"
        sh """
            set +x
            jobfullname="${JOB_NAME}"
            BRANCHNAME=`echo \$jobfullname |awk -F"/" '{ print \$3 }'`
            PROJECTNAME=`echo \$jobfullname |awk -F"/" '{ print \$2}'`
            
            echo \$BRANCHNAME
            rm -f StartTime.py
            wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/StartTime.py
            python StartTime.py > ${urlfile}/\$PROJECTNAME-\$BRANCHNAME-SONAR.txt
            echo "完成"
            rm -f StartTime.py
        
        """

        checkout scm
      }
     
    stage('Pre-steps'){
        echo "1"
        
        sh """
            set +x
           
                             rm -f Check-ProfileID.sh
                             wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/Check-ProfileID.sh
                             sh Check-ProfileID.sh
                             rm -f Check-ProfileID.sh
                                
                             rm -f Checkout-Pom-Version.sh
                             wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/Checkout-Pom-Version.sh  
                             sh Checkout-Pom-Version.sh
                             rm -f Checkout-Pom-Version.sh
                                
                             echo "Checkout-Pom-Version Done"

                             #echo "start run Presteps"
            
                             #echo "${PRESTEPS}"
                             #${PRESTEPS}
            
                             echo "Presteps Down"
                     
            
            
                             #if [[ "${UNITTEST}" == "true" ]];then 
                             #   echo "star Unit Test"
                             #   echo "${UNITTESTCOMMOND}"
                             #   #${UNITTESTCOMMOND}
                             #   echo "Unit Test Done"
                             #fi
                        
                        
                        
            
            
           

            
            
        """
    }

    
    parallel (
        Analysis: {
            stage('Sonar') {
        sh """
                   
            
            
            
            jobfullname="${JOB_NAME}"
            BRANCHNAME=`echo \$jobfullname |awk -F"/" '{ print \$3 }'`
            systemname=`echo \$jobfullname |awk -F"/" '{ print \$2}'`
            systemname=`echo \$systemname |awk -F"_" '{ print \$3}'`
          
            PROJECTNAME=`echo \$jobfullname |awk -F"/" '{ print \$2}'`
            
            
            
            if [[ "${SonarOpen}" == "true" ]];then
                echo "开始扫描"
                rm -f Check-Sonar-Inc.sh
                wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/Check-Sonar-Inc.sh
                sh Check-Sonar-Inc.sh ${JOB_NAME}
                rm -f Check-Sonar-Inc.sh
                rm -f SonarApi-reson.py
                wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/SonarApi-reson.py
                
                python SonarApi-reson.py \${systemname}_\${BRANCHNAME} >> ${urlfile}/\$PROJECTNAME-\$BRANCHNAME-SONAR.txt
                rm -f SonarApi-reson.py
            fi

        
        """


            }
        },
        Build: {
            // 执行构建
            stage('Build') {
                    sh """
            
                    
                    find | chmod -R 755 || echo ""
                    #打包前修改权限

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
                    
                    Dir="${Dir}"
                
                    if [[ -z \$Dir ]];then
                    
                        echo "当前目录执行"
                        Dir='.'
                    
                    fi
                    
                    
                    for D in  \$Dir
                    do
                        echo "进入目录执行\$D"
                        cd \$D
                        #如果是CD到目录里面执行构建,则包名为目录名
                        if [[ \$D != "." ]];then
                            systemname=\$D
                        fi
                    
                        echo "当前的目录为\$D"


                    
                        if [[ "${profile}" == ""  ]];then
                           
                               
                                
                                java -version
                                echo "${command}"
                                ${command}
                                
                                rm -f TransPackage.py
                                wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/TransPackage.py
                                python TransPackage.py \${SYSTEMNAME}_\${BRANCHNAME}
                                rm -f TransPackage.py
                                
                                
                                
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
                                liblist=`find . -name "lib" | grep 'target/lib' || echo`
                                echo "lib----------------------"
                                des_url="http://\$deployDstHost/build/\$DIRNAME/\$SYSTEMNAME/${JOB_BASE_NAME}/lib/\$HASH-${BUILD_ID}-\$time"
                                scp_url="\$scpdir/\$DIRNAME/\$SYSTEMNAME/${JOB_BASE_NAME}/lib/\$HASH-${BUILD_ID}-\$time"
                                
                                echo "开始输出libURL"
                                echo \$des_url
                                echo \$scp_url
                                
                                ssh \$deployDstHost mkdir -p \$scp_url
                                echo "scp 完毕"
                                
                                for i in \$liblist
                                do
                                    if [ -n \$i  ];then
                                    
                                        echo \$i
                                        name=`echo \$i | awk -F"/" '{ print \$2 }'`
                                        echo "名字"
                                        #pwd
                                        targetdir=`dirname \$i`
                                        echo "目标文件夹"
                                        echo \$targetdir
                                        LIBNAME=`echo \$targetdir | awk -F"/" '{ print \$2 }'`
                                        echo "目标文件夹"
                                        cd \$targetdir
                                        
                                        packagename1=`ls | egrep '*.jar\$' | awk -F'.' '{ print \$1 }' || echo `
                    
                                        pwd
                                        
                                        tar cazf \$LIBNAME.tar.gz lib
                                        md5=`md5sum  \$LIBNAME.tar.gz | awk '{print \$1}'`
                                        scp -p  \$LIBNAME.tar.gz \$deployDstHost:\$scp_url
                                    
                                        cd -
                                    
                                        echo "\$des_url/\$LIBNAME.tar.gz&\$md5" >> ${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt
                                        pwd
                                        ssh \$deployDstHost chmod -R 755 \$scpdir
                                    fi
                                done

                        else
                                set +x
                                for p in "${profile}"
                                do
                                    
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
                                        echo \$des_url
                                        echo \$scp_url
                                        
                                        echo "done"
                                        
                                        echo "-------------------------------------------------------------------------------------------"
                                        echo "start \$i"
                                        echo "${command} -P\$i"
                                        ${command} -P\$i
                                        
                                        rm -f TransPackage.py 
                                        wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/TransPackage.py
                                        python TransPackage.py \${SYSTEMNAME}_\${BRANCHNAME}
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
                                        
                                                        echo "\$des_url/\${x}&\$md5" >> ${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt
                                                    fi
                                                done
                                                pwd
                                            
                                        cd - > /dev/null
                                        
                                        echo `date`
                                        liblist=`find . -name "lib" | grep 'target/lib' || echo`
                                        echo "lib----------------------"
                                        des_url="http://\$deployDstHost/build/\$DIRNAME/\$SYSTEMNAME/${JOB_BASE_NAME}/lib/\$HASH-${BUILD_ID}-\$time"
                                        scp_url="\$scpdir/\$DIRNAME/\$SYSTEMNAME/${JOB_BASE_NAME}/lib/\$HASH-${BUILD_ID}-\$time"
                                        
                                        echo "开始输出libURL"
                                        echo \$des_url
                                        echo \$scp_url
                                        
                                        ssh \$deployDstHost mkdir -p \$scp_url
                                        echo "scp 完毕"
                                        
                                        for i in \$liblist
                                        do
                                            if [ -n \$i  ];then
                                            
                                                echo \$i
                                                name=`echo \$i | awk -F"/" '{ print \$2 }'`
                                                echo "名字"
                                                #pwd
                                                targetdir=`dirname \$i`
                                                echo "目标文件夹"
                                                echo \$targetdir
                                                LIBNAME=`echo \$targetdir | awk -F"/" '{ print \$2 }'`
                                                echo "目标文件夹"
                                                cd \$targetdir
                                                
                                                packagename1=`ls | egrep '*.jar\$' | awk -F'.' '{ print \$1 }' || echo `
                            
                                                pwd
                                                
                                                tar cazf \$LIBNAME.tar.gz lib
                                                md5=`md5sum  \$LIBNAME.tar.gz | awk '{print \$1}'`
                                                scp -p  \$LIBNAME.tar.gz \$deployDstHost:\$scp_url
                                            
                                                cd -
                                            
                                                echo "\$des_url/\$LIBNAME.tar.gz&\$md5" >> ${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt
                                                pwd
                                                ssh \$deployDstHost chmod -R 755 \$scpdir
                                            fi
                                        done

                                        done
                                        
                                    done
                                    
                            
                                done
                                ssh \$deployDstHost chmod -R 755 \$scpdir
                        fi
                        pwd
                        echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
                        cd ${WORKSPACE}
                    done
                    

                    """
            
               
        }
                
                

            }
        
   
    
    )
    stage('Post-steps'){
                    
                    sh """
                    
                    set +x
                    
                    
                    
                    echo "start run Poststeps"
                    
                    echo "${POSTSTEPS}"
                    #${POSTSTEPS}
            
                    echo "Poststeps Down"
     
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
    catch (err) {
        BuildStatus="ERROR"
        echo 'I failed'
        sh "exit 1"
    }
    finally {
    sh """
         set +x
         jobfullname="${JOB_NAME}"
         BRANCHNAME=`echo \$jobfullname |awk -F"/" '{ print \$3 }'`
         PROJECTNAME=`echo \$jobfullname |awk -F"/" '{ print \$2}'`
         SYSTEMNAME=`echo \$PROJECTNAME | awk -F'_' '{ print \$3 }'`
         PROJECTGROUP=`echo \$PROJECTNAME | awk -F'_' '{ print \$2 }'`
         echo "XXXXXX"
         echo \$BRANCHNAME
         echo \$PROJECTNAME
         echo \$SYSTEMNAME
         echo "XXXXXX"
    
         if [[ "${SonarOpen}" == "true" ]];then
            #set +x
            rm -f EndTime.py
            wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/EndTime.py
            python EndTime.py >> ${urlfile}/\$PROJECTNAME-\$BRANCHNAME-SONAR.txt
            rm -f EndTime.py
         
         
            RESON=`echo ${urlfile}/\$PROJECTNAME-\$BRANCHNAME-SONAR.txt`
            rm -f Email.py
            wget -q http://172.17.33.199/build/common-tools/Jenkins-shell/Email.py  
            
            emailtouser=`cat ${urlfile}/EMAIL.txt | grep \$PROJECTGROUP | awk -F":" '{ print \$2 }'`
            echo \$emailtouser
            #echo ${recipients}
            echo "aaaaaaaaaaaaaa"
            python Email.py \$PROJECTNAME \$RESON "${BUILD_URL}"console ${BuildStatus} \$BRANCHNAME  \$emailtouser
            rm -f Email.py
         
         fi 
         
         """


    }   

    


}
