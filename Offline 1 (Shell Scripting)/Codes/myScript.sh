#!/bin/bash

#unzip and overwrite the same named files in the destination folder (if there exist any). Overwrite is indicated by '-o'.

rm -rf submissionAll/
rm -rf Output/

unzip -o SubmissionsAll.zip -d submissionAll/
#rm -f SubmissionsAll.zip

cd submissionAll/

#temporarilry create Output/ and Temp/ folders in submissionAll/ so that we can move them back to main folder. 

touch marks.txt
touch absenteeList.txt

mkdir Output/
mkdir Output/Extra
mkdir Temp/

#Getting all the rolls from the CSV files in absenteeList.txt

sort -t',' ../CSE_322.csv | grep -o "[0-9]\{7\}" >> absenteeList.txt

#getting all the file names in submissionAll/ folder. In this process, we can get the file name even if they has whitespace in them.

############## This part will work like a loop ###############

find . -name "*.zip" |while read fname; do
  studentNameFromZipFile=`echo $(cut -d'_' -f1 <<< $fname | cut -d'/' -f2)`


#${variable%pattern} is like $variable, minus shortest matching pattern from the back-end;
#${variable##pattern} is like $variable, minus the longest matching pattern from front-end.

  delim="assignsubmission_file_"

  fileNameFromZip=${fname##*$delim}
  fileNameFromZip=${fileNameFromZip%.*}

  temRollNoFromZip=`echo "$fileNameFromZip" | grep -o "[0-9]\{7\}"`

  if [[ $temRollNoFromZip =~ ^[0-9]{7}$ ]]; then 

        grep -vwE "$temRollNoFromZip" absenteeList.txt > abs2.txt
        mv abs2.txt absenteeList.txt

  elif [[ $temRollNoFromZip =~ ^$ ]]; then 
        echo "$temRollNoFromZip ---> 0" >> marks.txt
  fi 

done

#code absenteeList.txt

find . -name "*.zip" |while read fname; do
  studentNameFromZipFile=`echo $(cut -d'_' -f1 <<< $fname | cut -d'/' -f2)`

  # echo $fname

  delim="assignsubmission_file_"

  fileNameFromZip=${fname##*$delim}
  fileNameFromZip=${fileNameFromZip%.*}

  unzip -o "$fname" -d Temp/
  rm "$fname"


  cd Temp/

  noOfSubDirectory=`ls | wc -l`

  if [ $noOfSubDirectory == 1 ]; then 

       finalFolderName=`ls`

       if [[ $finalFolderName =~ ^[0-9]{7}$ ]]; then 

            alreadyMarkedOrNot=`cat ../marks.txt | grep -ciF "$finalFolderName"`

            if [ $alreadyMarkedOrNot == 0 ]; then 
                echo "$finalFolderName --->  10" >> ../marks.txt 
            fi 

            mv "$finalFolderName" ../Output/

            grep -vwE "$finalFolderName" ../absenteeList.txt > ../abs2.txt
            mv ../abs2.txt ../absenteeList.txt

       elif [[ $finalFolderName =~ ^.*[0-9]{7}.*$ ]]; then

            tem=$finalFolderName
            finalFolderName=`echo "$finalFolderName" | grep -o "[0-9]\{7\}"`

            alreadyMarkedOrNot=`cat ../marks.txt | grep -ciF "$finalFolderName"`

            if [ $alreadyMarkedOrNot == 0 ]; then 
                echo "$finalFolderName --->  5" >> ../marks.txt 
            fi 

            mv "$tem" "$finalFolderName"
            mv "$finalFolderName" ../Output/

            grep -vwE "$finalFolderName" ../absenteeList.txt > ../abs2.txt
            mv ../abs2.txt ../absenteeList.txt

       else 

            rollFromZip=`echo "$fileNameFromZip" | grep -o "[0-9]\{7\}"`

            #roll not found in zip

            if [[ $rollFromZip =~ ^$ ]]; then 

                rollFromCSVFile=`cat ../../CSE_322.csv | grep -iF "$studentNameFromZipFile" | grep -o "[0-9]\{7\}"`

                numberOfInstance=`cat ../../CSE_322.csv | grep -ciF "$studentNameFromZipFile"`

                if [ $numberOfInstance == 1 ]; then 
                    
                    alreadyMarkedOrNot=`cat ../marks.txt | grep -ciF "$rollFromCSVFile"`

                    if [ $alreadyMarkedOrNot == 0 ]; then 
                        echo "$rollFromCSVFile --->  0" >> ../marks.txt 
                    fi

                    mv "$finalFolderName" "$rollFromCSVFile"
                    mv "$rollFromCSVFile" ../Output/
                    

                    grep -vwE "$rollFromCSVFile" ../absenteeList.txt > ../abs2.txt
                    mv ../abs2.txt ../absenteeList.txt

                elif [ $numberOfInstance == 2 ]; then 

                    firstRoll=`echo -e "$rollFromCSVFile" | cut -d$'\n' -f1`

                    secondRoll=`echo -e "$rollFromCSVFile" | cut -d$'\n' -f2`

                    isStd1InAbsenteeListOrNot=`cat ../absenteeList.txt | grep -c "$firstRoll"`

                    isStd2InAbsenteeListOrNot=`cat ../absenteeList.txt | grep -c "$secondRoll"`

                    
                    if [ $isStd1InAbsenteeListOrNot == 1 ] && [ $isStd2InAbsenteeListOrNot == 0 ]; then 

                        alreadyMarkedOrNot=`cat ../marks.txt | grep -ciF "$firstRoll"`

                        if [ $alreadyMarkedOrNot == 0 ]; then 
                            echo "$firstRoll --->  0" >> ../marks.txt 
                        fi
                      
                        mv "$finalFolderName" "$firstRoll"
                        mv "$firstRoll" ../Output/
  
                        grep -vwE "$firstRoll" ../absenteeList.txt > ../abs2.txt
                        mv ../abs2.txt ../absenteeList.txt

                    elif [ $isStd2InAbsenteeListOrNot == 1 ] &&  [ $isStd1InAbsenteeListOrNot == 0 ]; then 
                        
                        alreadyMarkedOrNot=`cat ../marks.txt | grep -ciF "$secondRoll"`

                        if [ $alreadyMarkedOrNot == 0 ]; then 
                            echo "$secondRoll --->  0" >> ../marks.txt 
                        fi

                        mv "$finalFolderName" "$secondRoll"
                        mv "$secondRoll" ../Output/

                        grep -vwE "$secondRoll" ../absenteeList.txt > ../abs2.txt
                        mv ../abs2.txt ../absenteeList.txt

                    elif [ $isStd2InAbsenteeListOrNot == 1 ]  && [ $isStd1InAbsenteeListOrNot == 1 ]; then 
                        
                        mv "$finalFolderName" "$studentNameFromZipFile"
                        mv "$studentNameFromZipFile" ../Output/Extra/

                    fi 
                fi 
            else 

                alreadyMarkedOrNot=`cat ../marks.txt | grep -ciF "$rollFromZip"`

                if [ $alreadyMarkedOrNot == 0 ]; then 
                    echo "$rollFromZip --->  0" >> ../marks.txt 
                fi

                mv "$finalFolderName" "$rollFromZip"
                mv "$rollFromZip" ../Output/

                grep -vwE "$rollFromZip" ../absenteeList.txt > ../abs2.txt
                mv ../abs2.txt ../absenteeList.txt

            fi
       fi 
  else 
        rollFromZip=`echo "$fileNameFromZip" | grep -o "[0-9]\{7\}"`

            if [[ $rollFromZip =~ ^$ ]]; then 

                rollFromCSVFile=`cat ../../CSE_322.csv | grep -iF "$studentNameFromZipFile" | grep -o "[0-9]\{7\}"`

                numberOfInstance=`cat ../../CSE_322.csv | grep -ciF "$studentNameFromZipFile"`

                if [ $numberOfInstance == 1 ]; then 
                    
                    alreadyMarkedOrNot=`cat ../marks.txt | grep -ciF "$rollFromCSVFile"`

                    if [ $alreadyMarkedOrNot == 0 ]; then 
                        echo "$rollFromCSVFile --->  0" >> ../marks.txt 
                    fi
                    
                    cd ../
                    mv -T Temp "$rollFromCSVFile"
                    mv "$rollFromCSVFile" Output/
                    mkdir Temp
                    cd Tem/

                    grep -vwE "$rollFromCSVFile" ../absenteeList.txt > ../abs2.txt
                    mv ../abs2.txt ../absenteeList.txt


                elif [ $numberOfInstance == 2 ]; then 

                    firstRoll=`echo -e "$rollFromCSVFile" | cut -d$'\n' -f1`

                    secondRoll=`echo -e "$rollFromCSVFile" | cut -d$'\n' -f2`

                    isStd1InAbsenteeListOrNot=`cat ../absenteeList.txt | grep -c "$firstRoll"`

                    isStd2InAbsenteeListOrNot=`cat ../absenteeList.txt | grep -c "$secondRoll"`

                    
                    if [ $isStd1InAbsenteeListOrNot == 1 ] && [ $isStd2InAbsenteeListOrNot == 0 ]; then 

                        alreadyMarkedOrNot=`cat ../marks.txt | grep -ciF "$firstRoll"`

                        if [ $alreadyMarkedOrNot == 0 ]; then 
                            echo "$firstRoll --->  0" >> ../marks.txt 
                        fi
                        
                        grep -vwE "$firstRoll" ../absenteeList.txt > ../abs2.txt
                        mv ../abs2.txt ../absenteeList.txt

                        cd ../
                        mv -T Temp "$firstRoll"
                        mv "$firstRoll" Output/
                        mkdir Temp
                        cd Tem/


                    elif [ $isStd2InAbsenteeListOrNot == 1 ] &&  [ $isStd1InAbsenteeListOrNot == 0 ]; then 
                        
                        alreadyMarkedOrNot=`cat ../marks.txt | grep -ciF "$secondRoll"`

                        if [ $alreadyMarkedOrNot == 0 ]; then 
                            echo "$secondRoll --->  0" >> ../marks.txt 
                        fi

                        grep -vwE "$secondRoll" ../absenteeList.txt > ../abs2.txt
                        mv ../abs2.txt ../absenteeList.txt

                        cd ../
                        mv -T Temp "$secondRoll"
                        mv "$secondRoll" Output/
                        mkdir Temp
                        cd Tem/


                    elif [ $isStd2InAbsenteeListOrNot == 1 ]  && [ $isStd1InAbsenteeListOrNot == 1 ]; then 
                        
                        cd ../
                        mv -T Temp "$studentNameFromZipFile"
                        mv "$studentNameFromZipFile" Output/Extra/
                        mkdir Temp
                        cd Tem/


                    fi 
                fi 
            else 
                alreadyMarkedOrNot=`cat ../marks.txt | grep -ciF "$rollFromZip"`

                if [ $alreadyMarkedOrNot == 0 ]; then 
                    echo "$rollFromZip --->  0" >> ../marks.txt 
                fi

                
                grep -vwE "$rollFromZip" ../absenteeList.txt > ../abs2.txt
                mv ../abs2.txt ../absenteeList.txt

                cd ../
                mv -T Temp "$rollFromZip"
                mv "$rollFromZip" Output/
                mkdir Temp
                cd Temp/

            fi
       fi 
  #fi 

  cd ../
  rm -rf Temp/

done



IFS=$'\n'       # make newlines the only separator
set -f          # disable globbing
for i in $(cat < "absenteeList.txt"); do
  echo "$i --->  0" >> marks.txt
done

sort marks.txt | uniq | tee marks.txt

mv marks.txt Output/
mv absenteeList.txt Output/
mv Output/ ../

rm SubmissionsAll.zip


