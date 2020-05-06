# SONAR全量扫描

## 使用说明
* source Sonar-Full-all.sh 扫描结果上传到sonarqube开始分析
* 等待sonarqube分析完成后, 执行 python SonarEmailSend.py 从API获取结果然后通知用户

### 文件说明
* Check-GitUrl-lastupdate.py 该脚本获取最新一天更新过的GIT库,将库URL写入TXT中
* ScannerFull.sh 运行脚本,将TXT中的GIT库下载下来,执行扫描分析
* SonarEmailSend.py 从SONARQUBE的API获取结果,发送邮件和和钉钉通知用户
