#!/bin/bash
WORKDIR="/opt/QA/sonar"
python $WORKDIR/Check-GitUrl-lastupdate.py
cd $WORKDIR && pwd && source $WORKDIR/ScannerFull.sh
