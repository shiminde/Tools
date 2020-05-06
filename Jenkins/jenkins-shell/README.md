# Jenkins-Shell

## 简介
存放在Jenkins构建中需要执行的脚本,包括构建前执行的脚本、SONAR增量构建脚本等

## 脚本作用详细说明

### 构建前
* Check-ProfileID.sh 根据和持续交付平台2约定好的PROFILEID名称,检查POM里面PROFILEID是否对应
* Checkout-Pom-Version.sh 获取除\<parent\>、\<dependencies\>、\<plugin\>标签之外的<version>标签的值.判断格式是否为标准格式

### SONAR增量扫描

#### 环境说明
* 全量扫描在服务器上执行,详情参考：http://gitlab.sinoiov.com/QA/sonar/blob/master/README.md
* 集成在JENKINS构建中,通过多个构建脚本来实现此功能.
* SONARQUBE服务器地址: http://172.17.33.190:9000 
* 这些脚本存在HTTP服务器(172.17.33.199,/opt/REPOS/build/common-tools/Jenkins-shell)中,每次构建前会删除本地旧脚本,重新下载最新的脚本

#### 脚本说明
* StartTime.py 用于记录构建开始时间
* Check-Sonar-Inc.sh 增量扫描的具体处理过程,对比两次构建的HASH值获取修改的文件，上一次的构建HASH存在HASH.txt,本次的直接通过GIT命令获取，SONAR扫描完后会,如果SONARQUBE分析完毕，才会结束该脚本
* SonarApi-reson.py 增量扫描完成后，获取SONARQUBE的分析结果,如果质量阀为OK,则将本次的HASH写入HASH.txt,如果为ERROR则不更新,并将执行 exit 1 将JENKINS构建置为失败
* TransPackage.py 当MAVEN构建完毕后,执行此脚本,判断返回的结果,如果质量阀为ERROR,则直接将JENKINS置为失败
* EndTime.py 用于记录构建结束的时间

#### 使用说明
* 需要在JENKINS服务器(192.168.104.187,/jenkinsdata/data/.jenkins/common-extended/EMAIL.txt)中,按行写入构建邮件发送地址,例如 WLHYZYB:aa@126.com
* GIT多分支JENKINSFILE: http://gitlab.sinoiov.com/QA/multibranch-pipeline/blob/master/Jenkinsfile-gather/GITxMAVENxSONARxMultibranch-Jenkinsfile
* GIT普通构建JENKINSFILE: http://gitlab.sinoiov.com/QA/multibranch-pipeline/blob/master/Jenkinsfile-gather/GIT-MAVEN-SONAR-JENKINSFILE
* 需要将SonarOpen = "true"即可打开增量扫描

    
