#!/usr/bin/env bash

./compare-models.sh dynatrace_s1.json dynatrace_s2.json 0.1 > diff_s1_s2 
./compare-models.sh dynatrace_s2.json dynatrace_s3.json 0.1 > diff_s2_s3 
./compare-models.sh dynatrace_s3.json dynatrace_s4.json 0.1 > diff_s3_s4 
./compare-models.sh dynatrace_s1.json dynatrace_s2_only.json 0.1 > diff_s1_s2_only 
./compare-models.sh dynatrace_s2_only.json dynatrace_s3_only.json 0.1 > diff_s2_s3_only 
./compare-models.sh dynatrace_s3_only.json dynatrace_s4_only.json 0.1 > diff_s3_s4_only 
./compare-models.sh dynatrace_s1.json dynatrace_s3_only.json 0.1 > diff_s1_s3_only 
./compare-models.sh dynatrace_s1.json dynatrace_s4_only.json 0.1 > diff_s1_s4_only 
./compare-models.sh dynatrace_s2_only.json dynatrace_s4_only.json 0.1 > diff_s2_s4_only 
./compare-models.sh dynatrace_s1.json dynatrace_s2_only.json 0.1 > diff_s1_s2_only 
./compare-models.sh dynatrace_s1.json dynatrace_s3_only.json 0.1 > diff_s1_s3_only 
./compare-models.sh dynatrace_s1.json dynatrace_s4_only.json 0.1 > diff_s1_s4_only 
./compare-models.sh dynatrace_s1.json dynatrace_s4_only.json 0.1 > diff_s1_s4_only 
./compare-models.sh dynatrace_s2_only.json dynatrace_s3_only.json 0.1 > diff_s2_s3_only 
./compare-models.sh dynatrace_s2_only.json dynatrace_s4_only.json 0.1 > diff_s2_s4_only 
./compare-models.sh dynatrace_s3_only.json dynatrace_s4_only.json 0.1 > diff_s3_s4_only 
