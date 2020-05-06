#!/bin/bash
pomlist=`find . -name 'pom.xml'`
list=""

for i in $pomlist
do
        CHECKPARENT=`cat $i | grep "</parent>"`
        CHECKDEP=`cat $i | grep "<dependencies>"`
        CHECKPLU=`cat $i | grep "<plugin>"`

        if [[ $CHECKPARENT != "" ]];then

                if [[ $CHECKDEP != "" ]] && [[ $CHECKPLU == "" ]];then
                        CHECK=`cat  $i | grep "<dependencies>" -m 1 -B1000 | grep "</parent>" -m 1 -A1000 | grep "<version>" | grep -v "{"`  #check dependencies before string  
                        DEP=2

                elif [[ $CHECKDEP != "" ]] && [[ $CHECKPLU != "" ]];then
                        CHECK=`cat  $i | grep "<dependencies>" -m 1 -B1000 | grep "</parent>" -m 1 -A1000 | grep "<version>" | grep -v "{"`  #check dependencies before string  
                        DEP=2

                elif [[ $CHECKDEP == "" ]] && [[ $CHECKPLU == "" ]];then
                        CHECK=`cat $i |  grep "<version>" | grep "</parent>" -m 1 -A1000 | grep -v "{"`

                elif [[ $CHECKDEP == "" ]] && [[ $CHECKPLU != "" ]];then
                        CHECK=`cat $i | grep "<plugin>" -m 1 -B1000 | grep "</parent>" -m 1 -A1000 | grep "<version>" | grep -v "{"`
                        DEP=1
                fi
                CHECKPREPARENT=`cat  $i |  grep "<parent>" -m 1 -B1000 | grep "<version>" | grep -v "{"`
                for p in $CHECKPREPARENT
                do

                        val=`echo $p | grep -Ev "[0-9]+.[0-9]+.[0-9]+(-SNAPSHOT){0,1}"`
                        for n in $val
                        do
                                PRELINE=`cat -n $i |  grep "<parent>" -m 1 -B1000 | grep "<version>" | grep "$n"`
                                echo "file $i $PRELINE line"

                        done
                done


                for d in $CHECK
                do
                        val=`echo $d | grep -Ev "[0-9]+.[0-9]+.[0-9]+(-SNAPSHOT){0,1}"`
                        for n in $val
                        do
                                if [[ $DEP == "1" ]];then
                                        line=`cat -n  $i | grep "<plugin>" -m 1 -B1000 | grep "</parent>" -m 1 -A1000 | grep "$n"`
                                elif [[ $DEP == "2" ]];then

                                        line=`cat -n  $i | grep "$n"`
                                else
                                        line=`cat -n  $i | grep "<dependencies>" -m 1 -B1000 | grep "</parent>" -m 1 -A1000 | grep "$n"`
                                fi
                                echo "file $i $line line"
                        done

                done
        else

                CHECK=`cat  $i | grep "<dependencies>" -m 1 -B1000 |  grep "<version>" | grep -v "{"`  #check dependencies before string  


                if [[ $CHECK == "" ]];then
                        CHECK=`cat $i | grep "<plugin>" -m 1 -B1000 |  grep "<version>" | grep -v "{"` # if not dependencies string check plugin  before string
                        DEP="1"

                        if [[ $CHECK == "" ]];then                       # if not dependencies and plugin string,check version   
                                CHECK=`cat $i |  grep "<version>" |  grep -v "{"`
                                DEP="2"

                        fi
                fi
                for d in $CHECK
                do
                        val=`echo $d | grep -Ev "[0-9]+.[0-9]+.[0-9]+(-SNAPSHOT){0,1}"`
                        for n in $val
                        do
                                if [[ $DEP == "1" ]];then
                                        line=`cat -n  $i | grep "<plugin>" -m 1 -B1000 |  grep "$n"`

                                elif [[ $DEP == "2" ]];then
                                        line=`cat -n  $i | grep "$n"`
                                else
                                        line=`cat -n  $i | grep "<dependencies>" -m 1 -B1000 |  grep "$n"`
                                fi
                                echo "file $i $line line"

                        done

                done


        fi

done

if [[ $line != "" ]] || [[ $PRELINE != "" ]];then
        exit 1
fi
echo "aaa"

