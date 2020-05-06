#!/bin/bash
POMLIST=`find . -name "pom.xml"`
PROFILELIST="dev test PRO_M6 PRO_GH PRO_ZW PRO_GZ PRO_XJ PRO_AC"
ERRORCODE=0
for i in $POMLIST
do
        PROFILEIDLIST=`cat $i | grep   "<profiles>" -A1000 | grep "</profiles>" -B 1000 | grep "<id>"`
        for ID in $PROFILEIDLIST
        do
                ID=`echo $ID | awk -F">" '{ print $2 }' | awk -F"<" '{ print $1 }'`


                for P in $PROFILELIST
                do
                        #echo $P
                        pipei=""
                        if [[ $ID == $P ]];then
                                pipei=true
                                break
                        fi


                done
                if [[ $pipei != "true" ]];then
                        echo "$i ${ID}  error"
                        ERRORCODE=1
                fi

        done

done
if [[ $ERRORCODE == "1" ]];then
        echo "profileID错误,请根据标准修改ID名称,请参考http://wiki.com/"
        exit $ERRORCODE
else
        echo "PROFILEID CHECK SUCCESS"
fi

