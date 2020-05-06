def call(urlfile,profile,command) {
sh """
            
                    JOBNAME="${JOB_BASE_NAME}"
                    date
                    set +x
                    jobfullname="${JOB_NAME}"
                    
                    DIRNAME=`echo \$jobfullname |awk -F"/" '{ print \$1}'`
                    PROJECTNAME=`echo \$jobfullname |awk -F"/" '{ print \$2}'`
                    BRANCHNAME=`echo \$jobfullname |awk -F"/" '{ print \$3}'`


                    
                    cat /dev/null > ${urlfile}/\$PROJECTNAME-\$BRANCHNAME.txt
                    echo "bbbbbbb"
                    echo "${profile}"
                    
                    pomlist=`find . -name "pom.xml"`
                    profileID=""
					systemname=`echo \$PROJECTNAME | awk -F'_' '{ print \$3 }'`
					MORD=`echo \${JOBNAME} | awk -F'_' '{ print \$1 }'`
					
					
						
					deployDstHost="${D_REPO_IP}"
					deployDstStartPath="${D_REPO_PATH}"
					scpdir="${D_SCP_DIR}"
				
					
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