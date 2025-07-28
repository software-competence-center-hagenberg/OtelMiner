#!/usr/bin/env bash

compare() {
cd "$1" || exit
echo "diff_s1_s2"
../compare-models.sh S1.json S2.json 0.1 > diff_s1_s2
echo "diff_s1_s3"
../compare-models.sh S1.json S3.json 0.1 > diff_s1_s3
echo "diff_s1_s4"
../compare-models.sh S1.json S4.json 0.1 > diff_s1_s4


echo "diff_s2_s3"
../compare-models.sh S2.json S3.json 0.1 > diff_s2_s3
echo "diff_s2_s4"
../compare-models.sh S2.json S4.json 0.1 > diff_s2_s4

echo "diff_s3_s4"
../compare-models.sh S3.json S4.json 0.1 > diff_s3_s4

echo "diff_s1_s2_only"
../compare-models.sh S1.json S2_only.json 0.1 > diff_s1_s2_only
echo "diff_s1_s3_only"
../compare-models.sh S1.json S3_only.json 0.1 > diff_s1_s3_only
echo "diff_s1_s4_only"
../compare-models.sh S1.json S4_only.json 0.1 > diff_s1_s4_only

echo "diff_s2_s3_only"
../compare-models.sh S2_only.json S3_only.json 0.1 > diff_s2_s3_only
echo "diff_s2_s4_only"
../compare-models.sh S2_only.json S4_only.json 0.1 > diff_s2_s4_only

echo "diff_s3_s4_only"
../compare-models.sh S3_only.json S4_only.json 0.1 > diff_s3_s4_only
echo "...done"
cd ..
}

echo "comparing astroshop models..."
compare astro-shop

echo "comparing train-ticket models..."
compare train-ticket
